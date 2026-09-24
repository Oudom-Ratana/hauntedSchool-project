package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.RoomBarrierModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.RoomBarrierFileService;
import com.khmerspirit.admin.service.RoomFileService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.*;

/**
 * Visual Room Barrier & Collision Customization View.
 * Allows visual inspection of chamber background artwork and click-and-drag drawing
 * of collision boxes on props, tables, walls, and obstacles with instant game persistence.
 */
public class RoomBarrierManagementView extends VBox {

    private static final double NATIVE_WIDTH = 1376.0;
    private static final double NATIVE_HEIGHT = 768.0;

    // Canvas display dimensions (2/3 scale = 0.6667)
    private static final double CANVAS_WIDTH = 917.0;
    private static final double CANVAS_HEIGHT = 512.0;

    private final RoomBarrierFileService barrierFileService = new RoomBarrierFileService();
    private final RoomFileService roomFileService = new RoomFileService();

    private final Map<String, Image> cachedRoomImages = new HashMap<>();

    private ComboBox<RoomOption> roomSelector;
    private Label barrierCountLabel;
    private Label statusMsgLabel;

    private ToggleButton btnModeDraw;
    private ToggleButton btnModeSelect;

    private Canvas canvas;
    private GraphicsContext gc;

    private final ObservableList<RoomBarrierModel> currentBarriers = FXCollections.observableArrayList();
    private final ListView<RoomBarrierModel> barrierListView = new ListView<>();

    private String currentRoomId = "classroomA";
    private RoomBarrierModel selectedBarrier = null;

    // Drag / Draw state
    private boolean isDragging = false;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private double currentMouseX = 0;
    private double currentMouseY = 0;

    // Moving existing barrier state
    private double moveOffsetX = 0;
    private double moveOffsetY = 0;

    // Inspector fields
    private TextField txtLabel;
    private Spinner<Integer> spX;
    private Spinner<Integer> spY;
    private Spinner<Integer> spWidth;
    private Spinner<Integer> spHeight;
    private Button btnDeleteSelected;

    public RoomBarrierManagementView() {
        setSpacing(14);
        setPadding(new Insets(18));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildControlBar();
        buildMainLayout();
        loadData();
    }

    private void buildHeader() {
        VBox header = new VBox(2);
        Label title = new Label("Room Barriers & Collision Customizer");
        title.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Visually draw, move, and customize collision barrier boxes directly on tables, props, and walls for all chambers");
        subtitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildControlBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.getStyleClass().add("admin-card-container");

        Label lblRoom = new Label("Select Room:");
        lblRoom.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-font-size: 12.5px;");

        roomSelector = new ComboBox<>();
        populateRoomSelector();
        roomSelector.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f8fafc; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-radius: 6px; -fx-background-radius: 6px;");
        roomSelector.setOnAction(e -> {
            RoomOption opt = roomSelector.getValue();
            if (opt != null && !opt.id.equalsIgnoreCase(currentRoomId)) {
                switchRoom(opt.id);
            }
        });

        // Mode toggles
        ToggleGroup modeGroup = new ToggleGroup();
        btnModeDraw = new ToggleButton("Draw Box Mode");
        btnModeDraw.setToggleGroup(modeGroup);
        btnModeDraw.setSelected(true);
        btnModeDraw.getStyleClass().add("btn-modern-primary");

        btnModeSelect = new ToggleButton("Select / Move Mode");
        btnModeSelect.setToggleGroup(modeGroup);
        btnModeSelect.getStyleClass().add("btn-modern-secondary");

        btnModeDraw.setOnAction(e -> {
            btnModeDraw.getStyleClass().setAll("button", "toggle-button", "btn-modern-primary");
            btnModeSelect.getStyleClass().setAll("button", "toggle-button", "btn-modern-secondary");
            statusMsgLabel.setText("Mode: Draw - Click and drag anywhere on map to add a barrier box.");
        });

        btnModeSelect.setOnAction(e -> {
            btnModeSelect.getStyleClass().setAll("button", "toggle-button", "btn-modern-primary");
            btnModeDraw.getStyleClass().setAll("button", "toggle-button", "btn-modern-secondary");
            statusMsgLabel.setText("Mode: Select - Click a barrier box to select or drag to move it.");
        });

        Button btnResetDefault = new Button("Reset to Default");
        btnResetDefault.getStyleClass().add("btn-modern-secondary");
        btnResetDefault.setOnAction(e -> handleResetToDefault());

        Button btnClearAll = new Button("Clear All");
        btnClearAll.getStyleClass().add("btn-modern-secondary");
        btnClearAll.setStyle("-fx-text-fill: #f87171;");
        btnClearAll.setOnAction(e -> handleClearAll());

        Button btnSave = new Button("Save Barriers");
        btnSave.getStyleClass().add("btn-modern-primary");
        btnSave.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> handleSave());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        barrierCountLabel = new Label("Barriers: 0");
        barrierCountLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 13px;");

        bar.getChildren().addAll(lblRoom, roomSelector, btnModeDraw, btnModeSelect, btnResetDefault, btnClearAll, spacer, barrierCountLabel, btnSave);
        getChildren().add(bar);
    }

    private void buildMainLayout() {
        HBox layout = new HBox(16);
        layout.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left Pane: Canvas View
        VBox canvasContainer = new VBox(6);
        canvasContainer.setAlignment(Pos.TOP_LEFT);

        HBox canvasHeader = new HBox(10);
        canvasHeader.setAlignment(Pos.CENTER_LEFT);
        statusMsgLabel = new Label("Mode: Draw - Click and drag anywhere on map to add a barrier box.");
        statusMsgLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11.5px; -fx-font-style: italic;");
        canvasHeader.getChildren().add(statusMsgLabel);

        StackPane canvasBox = new StackPane();
        canvasBox.setStyle("-fx-background-color: #0c1322; -fx-border-color: #1e293b; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        canvasBox.setPrefSize(CANVAS_WIDTH + 4, CANVAS_HEIGHT + 4);
        canvasBox.setMaxSize(CANVAS_WIDTH + 4, CANVAS_HEIGHT + 4);

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        setupCanvasInteractions();

        canvasBox.getChildren().add(canvas);
        canvasContainer.getChildren().addAll(canvasHeader, canvasBox);

        // Right Pane: Inspector & Barrier List
        VBox rightPane = new VBox(14);
        rightPane.setPrefWidth(320);
        rightPane.setMinWidth(300);

        // Inspector Card
        VBox inspectorCard = new VBox(10);
        inspectorCard.setPadding(new Insets(14));
        inspectorCard.getStyleClass().add("admin-card-container");

        Label inspTitle = new Label("Barrier Box Inspector");
        inspTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        txtLabel = new TextField("Obstacle");
        txtLabel.getStyleClass().add("modern-form-input");

        spX = new Spinner<>(0, (int) NATIVE_WIDTH, 0, 5);
        spX.setEditable(true);
        spX.getStyleClass().add("modern-form-input");

        spY = new Spinner<>(0, (int) NATIVE_HEIGHT, 0, 5);
        spY.setEditable(true);
        spY.getStyleClass().add("modern-form-input");

        spWidth = new Spinner<>(10, (int) NATIVE_WIDTH, 100, 5);
        spWidth.setEditable(true);
        spWidth.getStyleClass().add("modern-form-input");

        spHeight = new Spinner<>(10, (int) NATIVE_HEIGHT, 100, 5);
        spHeight.setEditable(true);
        spHeight.getStyleClass().add("modern-form-input");

        grid.add(createLabel("Label:"), 0, 0); grid.add(txtLabel, 1, 0);
        grid.add(createLabel("X Pos:"), 0, 1); grid.add(spX, 1, 1);
        grid.add(createLabel("Y Pos:"), 0, 2); grid.add(spY, 1, 2);
        grid.add(createLabel("Width:"), 0, 3); grid.add(spWidth, 1, 3);
        grid.add(createLabel("Height:"), 0, 4); grid.add(spHeight, 1, 4);

        HBox inspButtons = new HBox(8);
        Button btnUpdateBox = new Button("Apply Edit");
        btnUpdateBox.getStyleClass().add("btn-modern-primary");
        btnUpdateBox.setOnAction(e -> applyInspectorChanges());

        btnDeleteSelected = new Button("Delete Box");
        btnDeleteSelected.getStyleClass().add("btn-modern-secondary");
        btnDeleteSelected.setStyle("-fx-text-fill: #f87171;");
        btnDeleteSelected.setOnAction(e -> deleteSelectedBarrier());

        inspButtons.getChildren().addAll(btnUpdateBox, btnDeleteSelected);

        inspectorCard.getChildren().addAll(inspTitle, grid, inspButtons);

        // List of all barriers in room
        VBox listCard = new VBox(8);
        listCard.setPadding(new Insets(14));
        listCard.getStyleClass().add("admin-card-container");
        VBox.setVgrow(listCard, Priority.ALWAYS);

        Label listTitle = new Label("Room Barriers List");
        listTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 13.5px;");

        barrierListView.setStyle("-fx-background-color: #070b14; -fx-control-inner-background: #070b14; -fx-border-color: #1e293b; -fx-border-radius: 6px;");
        barrierListView.getStyleClass().add("barrier-list-view");
        barrierListView.setItems(currentBarriers);
        barrierListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(RoomBarrierModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #070b14;");
                } else {
                    setText(item.getLabel() + "  [" + (int) item.getX() + ", " + (int) item.getY() + "]  " + (int) item.getWidth() + "x" + (int) item.getHeight());
                    if (isSelected()) {
                        setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-padding: 7 10;");
                    } else {
                        setStyle("-fx-background-color: #0c1322; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-padding: 7 10;");
                    }
                }
            }
        });

        barrierListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedBarrier = newVal;
                loadSelectedIntoInspector();
                redrawCanvas();
            }
        });

        listCard.getChildren().addAll(listTitle, barrierListView);
        rightPane.getChildren().addAll(inspectorCard, listCard);

        layout.getChildren().addAll(canvasContainer, rightPane);
        getChildren().add(layout);
    }

    private void setupCanvasInteractions() {
        canvas.setOnMousePressed(this::handleCanvasPressed);
        canvas.setOnMouseDragged(this::handleCanvasDragged);
        canvas.setOnMouseReleased(this::handleCanvasReleased);
        canvas.setOnMouseMoved(e -> {
            double nx = e.getX() * (NATIVE_WIDTH / CANVAS_WIDTH);
            double ny = e.getY() * (NATIVE_HEIGHT / CANVAS_HEIGHT);
            statusMsgLabel.setText(String.format("Coordinates: (%.0f, %.0f) | Mode: %s",
                    nx, ny, btnModeDraw.isSelected() ? "Draw Box" : "Select / Move"));
        });
    }

    private void handleCanvasPressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        double cx = e.getX();
        double cy = e.getY();
        double nx = cx * (NATIVE_WIDTH / CANVAS_WIDTH);
        double ny = cy * (NATIVE_HEIGHT / CANVAS_HEIGHT);

        dragStartX = cx;
        dragStartY = cy;
        currentMouseX = cx;
        currentMouseY = cy;
        isDragging = true;

        if (btnModeSelect.isSelected()) {
            // Find clicked barrier (search in reverse so top-drawn boxes are checked first)
            RoomBarrierModel clicked = null;
            for (int i = currentBarriers.size() - 1; i >= 0; i--) {
                RoomBarrierModel b = currentBarriers.get(i);
                if (nx >= b.getX() && nx <= (b.getX() + b.getWidth()) &&
                    ny >= b.getY() && ny <= (b.getY() + b.getHeight())) {
                    clicked = b;
                    break;
                }
            }

            selectedBarrier = clicked;
            barrierListView.getSelectionModel().select(clicked);
            loadSelectedIntoInspector();

            if (selectedBarrier != null) {
                moveOffsetX = nx - selectedBarrier.getX();
                moveOffsetY = ny - selectedBarrier.getY();
            }
            redrawCanvas();
        }
    }

    private void handleCanvasDragged(MouseEvent e) {
        if (!isDragging) return;

        currentMouseX = Math.max(0, Math.min(CANVAS_WIDTH, e.getX()));
        currentMouseY = Math.max(0, Math.min(CANVAS_HEIGHT, e.getY()));

        double nx = currentMouseX * (NATIVE_WIDTH / CANVAS_WIDTH);
        double ny = currentMouseY * (NATIVE_HEIGHT / CANVAS_HEIGHT);

        if (btnModeSelect.isSelected() && selectedBarrier != null) {
            // Move selected barrier
            double newX = Math.max(0, Math.min(NATIVE_WIDTH - selectedBarrier.getWidth(), nx - moveOffsetX));
            double newY = Math.max(0, Math.min(NATIVE_HEIGHT - selectedBarrier.getHeight(), ny - moveOffsetY));
            selectedBarrier.setX(newX);
            selectedBarrier.setY(newY);
            loadSelectedIntoInspector();
        }

        redrawCanvas();
    }

    private void handleCanvasReleased(MouseEvent e) {
        if (!isDragging) return;
        isDragging = false;

        if (btnModeDraw.isSelected()) {
            double startNX = Math.min(dragStartX, currentMouseX) * (NATIVE_WIDTH / CANVAS_WIDTH);
            double startNY = Math.min(dragStartY, currentMouseY) * (NATIVE_HEIGHT / CANVAS_HEIGHT);
            double w = Math.abs(currentMouseX - dragStartX) * (NATIVE_WIDTH / CANVAS_WIDTH);
            double h = Math.abs(currentMouseY - dragStartY) * (NATIVE_HEIGHT / CANVAS_HEIGHT);

            // Minimum box size of 15x15 native pixels
            if (w >= 15 && h >= 15) {
                RoomBarrierModel newBox = new RoomBarrierModel(startNX, startNY, w, h, "Obstacle #" + (currentBarriers.size() + 1));
                currentBarriers.add(newBox);
                selectedBarrier = newBox;
                barrierListView.getSelectionModel().select(newBox);
                loadSelectedIntoInspector();
                updateCountLabel();
            }
        }

        redrawCanvas();
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // 1. Draw room background artwork
        Image bg = getRoomMapImage(currentRoomId);
        if (bg != null) {
            gc.drawImage(bg, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            gc.setFill(Color.web("#070b14"));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        double scaleX = CANVAS_WIDTH / NATIVE_WIDTH;
        double scaleY = CANVAS_HEIGHT / NATIVE_HEIGHT;

        // 2. Draw existing barriers
        for (int i = 0; i < currentBarriers.size(); i++) {
            RoomBarrierModel b = currentBarriers.get(i);
            boolean isSel = (b == selectedBarrier);

            double bx = b.getX() * scaleX;
            double by = b.getY() * scaleY;
            double bw = b.getWidth() * scaleX;
            double bh = b.getHeight() * scaleY;

            if (isSel) {
                // Highlighted Selected Box (Gold / Cyan)
                gc.setFill(Color.rgb(250, 204, 21, 0.45));
                gc.fillRect(bx, by, bw, bh);
                gc.setStroke(Color.rgb(250, 204, 21, 1.0));
                gc.setLineWidth(2.5);
                gc.strokeRect(bx, by, bw, bh);
            } else {
                // Standard Solid Barrier Box (Red / Rose)
                gc.setFill(Color.rgb(239, 68, 68, 0.38));
                gc.fillRect(bx, by, bw, bh);
                gc.setStroke(Color.rgb(239, 68, 68, 0.88));
                gc.setLineWidth(1.8);
                gc.strokeRect(bx, by, bw, bh);
            }

            // Draw barrier label pill
            String text = "#" + (i + 1) + " " + b.getLabel();
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.setFill(Color.rgb(15, 23, 42, 0.85));
            double tagWidth = text.length() * 6.5 + 8;
            gc.fillRect(bx + 2, by + 2, tagWidth, 14);
            gc.setFill(isSel ? Color.web("#fef08a") : Color.web("#fecdd3"));
            gc.fillText(text, bx + 6, by + 12);
        }

        // 3. Draw Rubberband while dragging in Draw Mode
        if (isDragging && btnModeDraw.isSelected()) {
            double rx = Math.min(dragStartX, currentMouseX);
            double ry = Math.min(dragStartY, currentMouseY);
            double rw = Math.abs(currentMouseX - dragStartX);
            double rh = Math.abs(currentMouseY - dragStartY);

            gc.setFill(Color.rgb(34, 197, 94, 0.30));
            gc.fillRect(rx, ry, rw, rh);
            gc.setStroke(Color.rgb(34, 197, 94, 0.95));
            gc.setLineWidth(2.0);
            gc.strokeRect(rx, ry, rw, rh);

            // Size badge
            double nw = rw * (NATIVE_WIDTH / CANVAS_WIDTH);
            double nh = rh * (NATIVE_HEIGHT / CANVAS_HEIGHT);
            String dim = String.format("%.0fx%.0f", nw, nh);
            gc.setFill(Color.rgb(0, 0, 0, 0.75));
            gc.fillRect(rx + 4, ry + 4, dim.length() * 7 + 8, 14);
            gc.setFill(Color.web("#86efac"));
            gc.fillText(dim, rx + 8, ry + 15);
        }
    }

    private void applyInspectorChanges() {
        if (selectedBarrier == null) return;
        selectedBarrier.setLabel(txtLabel.getText().trim());
        selectedBarrier.setX(spX.getValue());
        selectedBarrier.setY(spY.getValue());
        selectedBarrier.setWidth(spWidth.getValue());
        selectedBarrier.setHeight(spHeight.getValue());
        barrierListView.refresh();
        redrawCanvas();
    }

    private void deleteSelectedBarrier() {
        if (selectedBarrier != null) {
            currentBarriers.remove(selectedBarrier);
            selectedBarrier = null;
            updateCountLabel();
            redrawCanvas();
        }
    }

    private void handleClearAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Barriers");
        confirm.setHeaderText("Clear all collision barriers for " + currentRoomId + "?");
        confirm.setContentText("This will remove all barrier obstacles for this room.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                currentBarriers.clear();
                selectedBarrier = null;
                updateCountLabel();
                redrawCanvas();
            }
        });
    }

    private void handleResetToDefault() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Barriers");
        confirm.setHeaderText("Reset barriers for " + currentRoomId + " to defaults?");
        confirm.setContentText("This will overwrite current edits with the default walls and obstacles template.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                List<RoomBarrierModel> def = barrierFileService.getDefaultBarriersForRoom(currentRoomId);
                currentBarriers.setAll(def);
                selectedBarrier = null;
                updateCountLabel();
                redrawCanvas();
            }
        });
    }

    private void handleSave() {
        boolean ok = barrierFileService.saveBarriersForRoom(currentRoomId, currentBarriers);
        if (ok) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Barriers Saved");
            alert.setHeaderText(null);
            alert.setContentText("Successfully saved " + currentBarriers.size() + " barrier collision boxes for " + currentRoomId + "!\nThese will apply immediately in-game.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save room barriers. Please check permissions.");
            alert.showAndWait();
        }
    }

    public void loadData() {
        List<RoomBarrierModel> loaded = barrierFileService.loadBarriersForRoom(currentRoomId);
        currentBarriers.setAll(loaded);
        selectedBarrier = null;
        updateCountLabel();
        redrawCanvas();
    }

    private void switchRoom(String newRoomId) {
        currentRoomId = newRoomId;
        loadData();
    }

    private void loadSelectedIntoInspector() {
        if (selectedBarrier != null) {
            txtLabel.setText(selectedBarrier.getLabel());
            spX.getValueFactory().setValue((int) selectedBarrier.getX());
            spY.getValueFactory().setValue((int) selectedBarrier.getY());
            spWidth.getValueFactory().setValue((int) selectedBarrier.getWidth());
            spHeight.getValueFactory().setValue((int) selectedBarrier.getHeight());
            btnDeleteSelected.setDisable(false);
        } else {
            btnDeleteSelected.setDisable(true);
        }
    }

    private void updateCountLabel() {
        barrierCountLabel.setText("Barriers: " + currentBarriers.size());
    }

    private void populateRoomSelector() {
        roomSelector.getItems().clear();
        List<RoomModel> rooms = roomFileService.loadRooms();
        if (rooms != null && !rooms.isEmpty()) {
            for (RoomModel r : rooms) {
                roomSelector.getItems().add(new RoomOption(r.getId(), r.getName()));
            }
        } else {
            roomSelector.getItems().addAll(
                    new RoomOption("classroomA", "Classroom A"),
                    new RoomOption("classroomB", "Teachers' Lounge"),
                    new RoomOption("computer", "Music & Art Room"),
                    new RoomOption("entrance", "Restroom / Washroom"),
                    new RoomOption("dormitory", "School Infirmary"),
                    new RoomOption("basement", "Storage Vault"),
                    new RoomOption("laboratory", "Science Lab"),
                    new RoomOption("library", "Lore Library"),
                    new RoomOption("teacher", "Principal's Office"),
                    new RoomOption("hall", "Central Main Hall")
            );
        }
        roomSelector.getSelectionModel().selectFirst();
    }

    private Image getRoomMapImage(String roomId) {
        if (cachedRoomImages.containsKey(roomId.toLowerCase())) {
            return cachedRoomImages.get(roomId.toLowerCase());
        }

        String path = switch (roomId.toLowerCase()) {
            case "classrooma", "classroom" -> "/images/maps/classroom/classroom_map.png";
            case "classroomb", "teachers_lounge" -> "/images/maps/teachers_lounge/teachers_lounge_map.png";
            case "computer", "music_art_room" -> "/images/maps/music_art_room/music_art_room_map.png";
            case "library" -> "/images/maps/library/library_map.png";
            case "laboratory", "science_lab" -> "/images/maps/science_lab/science_lab_map.png";
            case "teacher", "principal_office" -> "/images/maps/principal_office/principal_office_map.png";
            case "dormitory", "infirmary" -> "/images/maps/infirmary/infirmary_map.png";
            case "basement", "storage_room" -> "/images/maps/storage_room/storage_room_map.png";
            case "entrance", "restroom" -> "/images/maps/restroom/restroom_map.png";
            case "hall", "main_hall", "hallway" -> "/images/maps/main_hall/main_hall_map.png";
            default -> "/images/maps/main_hall/main_hall_map.png";
        };

        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                Image img = new Image(stream);
                cachedRoomImages.put(roomId.toLowerCase(), img);
                return img;
            }
        } catch (Exception ignored) {
        }

        try {
            InputStream stream = getClass().getResourceAsStream("/images/maps/classroom_room.png");
            if (stream != null) {
                Image img = new Image(stream);
                cachedRoomImages.put(roomId.toLowerCase(), img);
                return img;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11.5px;");
        return lbl;
    }

    public static class RoomOption {
        public final String id;
        public final String displayName;

        public RoomOption(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName + " (" + id + ")";
        }
    }
}
