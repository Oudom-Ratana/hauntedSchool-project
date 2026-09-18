package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.MapItemModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.MapItemFileService;
import com.khmerspirit.admin.service.RoomFileService;
import com.khmerspirit.items.Item;
import com.khmerspirit.items.ItemRegistry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import java.io.InputStream;
import java.util.*;

/**
 * Interactive Real Map Item Spawner & Placement Management View.
 * Allows admins to view the actual in-game room map, click anywhere on the floor/furniture
 * to visually set exact item coordinates, and spawn, inspect, move, or delete items live.
 */
public class MapItemManagementView extends VBox {

    private static final double CANVAS_WIDTH = 720.0;
    private static final double CANVAS_HEIGHT = 405.0;

    private final MapItemFileService mapItemFileService = new MapItemFileService();
    private final RoomFileService roomFileService = new RoomFileService();

    private final ObservableList<MapItemModel> masterData = FXCollections.observableArrayList();
    private FilteredList<MapItemModel> filteredData;

    // View Switching
    private final StackPane contentStack = new StackPane();
    private final HBox visualMapContainer = new HBox(16);
    private final VBox tableContainer = new VBox(12);
    private Button btnViewVisual;
    private Button btnViewTable;

    // Interactive Map Controls (Visual Mode)
    private ComboBox<RoomOption> roomSelector;
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private Label coordHoverLabel;
    private Label roomItemCountLabel;
    private final Map<String, Image> cachedRoomImages = new HashMap<>();

    // Placement / Inspector Card Controls
    private Label inspectorModeTitle;
    private Label selectedItemBadge;
    private TextField targetTileXField;
    private TextField targetTileYField;
    private ComboBox<ItemOption> itemTypeSelector;
    private Label itemAbilityDescLabel;
    private TextField descField;
    private CheckBox activeCheckBox;
    private Button btnSpawnOrUpdate;
    private Button btnDeleteSelected;
    private Button btnDeselect;

    // Visual selection state
    private String currentRoomId = "classroomA";
    private double targetTileX = 24.0;
    private double targetTileY = 15.0;
    private MapItemModel selectedMapItem = null;

    // Table Mode Controls
    private TableView<MapItemModel> tableView;
    private ComboBox<String> tableRoomFilterBox;
    private ComboBox<String> tableItemFilterBox;

    public MapItemManagementView() {
        setSpacing(14);
        setPadding(new Insets(18));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildViewModeSwitcher();
        buildVisualMapView();
        buildTableView();

        contentStack.getChildren().addAll(tableContainer, visualMapContainer);
        VBox.setVgrow(contentStack, Priority.ALWAYS);
        getChildren().add(contentStack);

        switchViewMode(true); // Default to visual interactive map mode
        loadData();
    }

    private void buildHeader() {
        VBox header = new VBox(2);
        Label title = new Label("🗺️  Map Item Spawner & Abilities");
        title.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Click directly anywhere on the real room map to place, inspect, or move item pickups with live coordinate precision");
        subtitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildViewModeSwitcher() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.getStyleClass().add("admin-card-container");

        btnViewVisual = new Button("🗺️  Interactive Map View");
        btnViewVisual.getStyleClass().add("btn-modern-primary");
        btnViewVisual.setOnAction(e -> switchViewMode(true));

        btnViewTable = new Button("📋  Item Data Table");
        btnViewTable.getStyleClass().add("btn-modern-secondary");
        btnViewTable.setOnAction(e -> switchViewMode(false));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄  Refresh Map Data");
        btnRefresh.getStyleClass().add("btn-modern-secondary");
        btnRefresh.setOnAction(e -> loadData());

        bar.getChildren().addAll(btnViewVisual, btnViewTable, spacer, btnRefresh);
        getChildren().add(bar);
    }

    private void switchViewMode(boolean visual) {
        visualMapContainer.setVisible(visual);
        visualMapContainer.setManaged(visual);
        tableContainer.setVisible(!visual);
        tableContainer.setManaged(!visual);

        if (visual) {
            btnViewVisual.getStyleClass().setAll("button", "btn-modern-primary");
            btnViewTable.getStyleClass().setAll("button", "btn-modern-secondary");
            redrawMapCanvas();
        } else {
            btnViewTable.getStyleClass().setAll("button", "btn-modern-primary");
            btnViewVisual.getStyleClass().setAll("button", "btn-modern-secondary");
        }
    }

    // =========================================================================
    // VISUAL INTERACTIVE MAP VIEW
    // =========================================================================
    private void buildVisualMapView() {
        visualMapContainer.setAlignment(Pos.TOP_LEFT);

        // Left side: Room Picker, Canvas & Rulers
        VBox leftPane = new VBox(10);
        leftPane.setAlignment(Pos.TOP_LEFT);

        HBox topMapBar = new HBox(12);
        topMapBar.setAlignment(Pos.CENTER_LEFT);

        Label lblRoom = new Label("Room:");
        lblRoom.setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: bold;");

        roomSelector = new ComboBox<>();
        populateRoomSelector();
        roomSelector.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f8fafc; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-radius: 6px; -fx-background-radius: 6px;");
        roomSelector.setOnAction(e -> {
            RoomOption sel = roomSelector.getValue();
            if (sel != null) {
                currentRoomId = sel.id;
                selectedMapItem = null;
                updateInspectorCard();
                redrawMapCanvas();
            }
        });

        roomItemCountLabel = new Label("Items: 0");
        roomItemCountLabel.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        coordHoverLabel = new Label("Cursor: (X: --, Y: --)");
        coordHoverLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-family: 'Consolas', monospace; -fx-font-weight: bold;");

        topMapBar.getChildren().addAll(lblRoom, roomSelector, roomItemCountLabel, spacer, coordHoverLabel);

        // Canvas container with aesthetic glass border
        StackPane canvasWrapper = new StackPane();
        canvasWrapper.setStyle("-fx-background-color: #020617; -fx-border-color: #334155; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 14, 0.4, 0, 4);");

        mapCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = mapCanvas.getGraphicsContext2D();

        mapCanvas.setOnMouseMoved(this::handleCanvasMouseMoved);
        mapCanvas.setOnMouseClicked(this::handleCanvasMouseClicked);

        canvasWrapper.getChildren().add(mapCanvas);

        Label mapHintLabel = new Label("💡 Click anywhere on the map to target tile placement. Click an existing item pin to inspect, move, or delete it.");
        mapHintLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");

        leftPane.getChildren().addAll(topMapBar, canvasWrapper, mapHintLabel);

        // Right side: Placement & Inspector Card
        VBox inspectorCard = buildInspectorCard();

        visualMapContainer.getChildren().addAll(leftPane, inspectorCard);
    }

    private VBox buildInspectorCard() {
        VBox card = new VBox(12);
        card.setPrefWidth(310);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(22, 28, 42, 0.95), rgba(12, 16, 26, 0.95)); -fx-border-color: rgba(212, 175, 55, 0.35); -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        inspectorModeTitle = new Label("🎯  Pinpoint New Item");
        inspectorModeTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #ffd591;");

        selectedItemBadge = new Label("Mode: New Placement Crosshair");
        selectedItemBadge.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        // Coordinates Row
        HBox coordRow = new HBox(8);
        coordRow.setAlignment(Pos.CENTER_LEFT);

        VBox xBox = new VBox(3);
        Label lblX = new Label("Tile X:");
        lblX.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold;");
        targetTileXField = new TextField("24.0");
        targetTileXField.setStyle("-fx-background-color: #0b1120; -fx-text-fill: #38bdf8; -fx-font-family: 'Consolas', monospace; -fx-font-weight: 900; -fx-border-color: #334155; -fx-border-radius: 4px;");
        xBox.getChildren().addAll(lblX, targetTileXField);

        VBox yBox = new VBox(3);
        Label lblY = new Label("Tile Y:");
        lblY.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold;");
        targetTileYField = new TextField("15.0");
        targetTileYField.setStyle("-fx-background-color: #0b1120; -fx-text-fill: #38bdf8; -fx-font-family: 'Consolas', monospace; -fx-font-weight: 900; -fx-border-color: #334155; -fx-border-radius: 4px;");
        yBox.getChildren().addAll(lblY, targetTileYField);

        HBox.setHgrow(xBox, Priority.ALWAYS);
        HBox.setHgrow(yBox, Priority.ALWAYS);
        coordRow.getChildren().addAll(xBox, yBox);

        // Sync coordinate field edits to canvas
        targetTileXField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                targetTileX = Double.parseDouble(newV.trim());
                redrawMapCanvas();
            } catch (Exception ignored) {}
        });
        targetTileYField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                targetTileY = Double.parseDouble(newV.trim());
                redrawMapCanvas();
            } catch (Exception ignored) {}
        });

        // Ability Preview Box
        VBox abilityBox = new VBox(3);
        Label lblAbility = new Label("Item Ability & Role:");
        lblAbility.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold;");
        itemAbilityDescLabel = new Label("Select an item above to preview its in-game power.");
        itemAbilityDescLabel.setWrapText(true);
        itemAbilityDescLabel.setMinHeight(48);
        itemAbilityDescLabel.setStyle("-fx-background-color: rgba(30, 41, 59, 0.6); -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 8; -fx-background-radius: 6px; -fx-border-color: rgba(255, 255, 255, 0.05); -fx-border-radius: 6px;");
        abilityBox.getChildren().addAll(lblAbility, itemAbilityDescLabel);

        // Item Selector
        VBox itemBox = new VBox(4);
        Label lblItem = new Label("Item to Place:");
        lblItem.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold;");
        itemTypeSelector = new ComboBox<>();
        populateItemTypeSelector();
        itemTypeSelector.setMaxWidth(Double.MAX_VALUE);
        itemTypeSelector.setStyle("-fx-background-color: #0b1120; -fx-text-fill: #f8fafc; -fx-border-color: #334155; -fx-border-radius: 6px;");
        itemBox.getChildren().addAll(lblItem, itemTypeSelector);

        itemTypeSelector.setOnAction(e -> {
            ItemOption sel = itemTypeSelector.getValue();
            if (sel != null && sel.item != null && itemAbilityDescLabel != null) {
                itemAbilityDescLabel.setText(sel.item.getAbilityDescription());
            }
        });

        // Spot Description
        VBox descBox = new VBox(4);
        Label lblDesc = new Label("Placement Note / Clue:");
        lblDesc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold;");
        descField = new TextField();
        descField.setPromptText("e.g. Resting on the wooden desk");
        descField.setStyle("-fx-background-color: #0b1120; -fx-text-fill: #f8fafc; -fx-border-color: #334155; -fx-border-radius: 6px;");
        descBox.getChildren().addAll(lblDesc, descField);

        // Active Checkbox
        activeCheckBox = new CheckBox("Active in Live Game");
        activeCheckBox.setSelected(true);
        activeCheckBox.setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: bold;");

        // Action Buttons
        btnSpawnOrUpdate = new Button("➕  Spawn Item Here");
        btnSpawnOrUpdate.setMaxWidth(Double.MAX_VALUE);
        btnSpawnOrUpdate.getStyleClass().add("btn-khmer-gold");
        btnSpawnOrUpdate.setOnAction(e -> handleSpawnOrUpdate());

        btnDeleteSelected = new Button("🗑️  Remove Item");
        btnDeleteSelected.setMaxWidth(Double.MAX_VALUE);
        btnDeleteSelected.getStyleClass().add("btn-khmer-danger");
        btnDeleteSelected.setVisible(false);
        btnDeleteSelected.setManaged(false);
        btnDeleteSelected.setOnAction(e -> handleDeleteVisualItem());

        btnDeselect = new Button("✕  Clear Selection");
        btnDeselect.setMaxWidth(Double.MAX_VALUE);
        btnDeselect.getStyleClass().add("btn-khmer-neutral");
        btnDeselect.setOnAction(e -> {
            selectedMapItem = null;
            updateInspectorCard();
            redrawMapCanvas();
        });

        card.getChildren().addAll(
                inspectorModeTitle,
                selectedItemBadge,
                coordRow,
                itemBox,
                abilityBox,
                descBox,
                activeCheckBox,
                btnSpawnOrUpdate,
                btnDeleteSelected,
                btnDeselect
        );
        return card;
    }

    private void handleCanvasMouseMoved(MouseEvent e) {
        int cols = getRoomColumns(currentRoomId);
        int rows = getRoomRows(currentRoomId);

        double tileX = Math.round((e.getX() / CANVAS_WIDTH) * cols * 2.0) / 2.0;
        double tileY = Math.round((e.getY() / CANVAS_HEIGHT) * rows * 2.0) / 2.0;

        coordHoverLabel.setText(String.format("Hover: (X: %.1f, Y: %.1f)", tileX, tileY));
    }

    private void handleCanvasMouseClicked(MouseEvent e) {
        int cols = getRoomColumns(currentRoomId);
        int rows = getRoomRows(currentRoomId);

        // Check if an existing item pin was clicked
        List<MapItemModel> roomItems = getItemsForRoom(currentRoomId);
        MapItemModel hitItem = null;

        for (MapItemModel item : roomItems) {
            double ix = (item.getTileX() / cols) * CANVAS_WIDTH;
            double iy = (item.getTileY() / rows) * CANVAS_HEIGHT;
            if (Math.hypot(e.getX() - ix, e.getY() - iy) <= 20.0) {
                hitItem = item;
                break;
            }
        }

        if (hitItem != null) {
            // Selected an existing item pin
            selectedMapItem = hitItem;
            targetTileX = hitItem.getTileX();
            targetTileY = hitItem.getTileY();
            updateInspectorCard();
            redrawMapCanvas();
        } else {
            // Set target placement crosshair
            selectedMapItem = null;
            targetTileX = Math.round((e.getX() / CANVAS_WIDTH) * cols * 2.0) / 2.0;
            targetTileY = Math.round((e.getY() / CANVAS_HEIGHT) * rows * 2.0) / 2.0;
            targetTileX = Math.max(1.0, Math.min(cols - 1.0, targetTileX));
            targetTileY = Math.max(1.0, Math.min(rows - 1.0, targetTileY));

            targetTileXField.setText(String.format("%.1f", targetTileX));
            targetTileYField.setText(String.format("%.1f", targetTileY));
            updateInspectorCard();
            redrawMapCanvas();
        }
    }

    private void updateInspectorCard() {
        if (inspectorModeTitle == null || targetTileXField == null || targetTileYField == null || itemTypeSelector == null) {
            return;
        }
        if (selectedMapItem != null) {
            inspectorModeTitle.setText("✏️  Edit Placed Item");
            inspectorModeTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #ffd591;");
            selectedItemBadge.setText("Selected ID: " + selectedMapItem.getId() + " (" + selectedMapItem.getItemName() + ")");
            selectedItemBadge.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-text-fill: #ffd591; -fx-font-weight: bold;");

            targetTileXField.setText(String.format("%.1f", selectedMapItem.getTileX()));
            targetTileYField.setText(String.format("%.1f", selectedMapItem.getTileY()));

            // Select item in combo
            for (ItemOption opt : itemTypeSelector.getItems()) {
                if (opt.id.equalsIgnoreCase(selectedMapItem.getItemId())) {
                    itemTypeSelector.setValue(opt);
                    break;
                }
            }

            descField.setText(selectedMapItem.getDescription() != null ? selectedMapItem.getDescription() : "");
            activeCheckBox.setSelected(selectedMapItem.isActive());

            btnSpawnOrUpdate.setText("💾  Save Changes");
            btnSpawnOrUpdate.getStyleClass().setAll("btn-khmer-gold");

            btnDeleteSelected.setVisible(true);
            btnDeleteSelected.setManaged(true);
        } else {
            inspectorModeTitle.setText("🎯  Pinpoint New Item");
            inspectorModeTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #ffd591;");
            selectedItemBadge.setText("Target: (X: " + targetTileX + ", Y: " + targetTileY + ") in " + currentRoomId);
            selectedItemBadge.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-text-fill: #34d399; -fx-font-weight: bold;");

            btnSpawnOrUpdate.setText("➕  Spawn Item Here");
            btnSpawnOrUpdate.getStyleClass().setAll("btn-khmer-gold");

            btnDeleteSelected.setVisible(false);
            btnDeleteSelected.setManaged(false);
        }
    }

    private void handleSpawnOrUpdate() {
        ItemOption selItem = itemTypeSelector.getValue();
        if (selItem == null) {
            showError("No Item Selected", "Please choose an item type to spawn.");
            return;
        }

        double tx;
        double ty;
        try {
            tx = Double.parseDouble(targetTileXField.getText().trim());
            ty = Double.parseDouble(targetTileYField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Invalid Coordinates", "Coordinates must be valid numbers.");
            return;
        }

        String desc = descField.getText().trim();
        boolean active = activeCheckBox.isSelected();

        if (selectedMapItem != null) {
            // Update existing
            int idx = masterData.indexOf(selectedMapItem);
            MapItemModel updated = new MapItemModel(
                    selectedMapItem.getId(),
                    selItem.id,
                    selItem.displayName,
                    currentRoomId,
                    tx,
                    ty,
                    desc,
                    active
            );
            if (idx >= 0) {
                masterData.set(idx, updated);
            }
            selectedMapItem = updated;
            mapItemFileService.saveMapItems(masterData);
            redrawMapCanvas();
            showInfo("Updated", "Map item placement updated successfully!");
        } else {
            // Spawn new
            String newId = "ITEM_" + String.format("%03d", masterData.size() + 1);
            MapItemModel newItem = new MapItemModel(
                    newId,
                    selItem.id,
                    selItem.displayName,
                    currentRoomId,
                    tx,
                    ty,
                    desc,
                    active
            );
            masterData.add(newItem);
            mapItemFileService.saveMapItems(masterData);
            selectedMapItem = newItem;
            updateInspectorCard();
            redrawMapCanvas();
            showInfo("Item Placed!", "Item spawned at (" + tx + ", " + ty + ") in " + currentRoomId + ". Players can see it live!");
        }
    }

    private void handleDeleteVisualItem() {
        if (selectedMapItem == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Item");
        confirm.setHeaderText("Remove " + selectedMapItem.getItemName() + " from this map?");
        confirm.setContentText("This item will no longer appear on the live map.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            masterData.remove(selectedMapItem);
            mapItemFileService.saveMapItems(masterData);
            selectedMapItem = null;
            updateInspectorCard();
            redrawMapCanvas();
            showInfo("Removed", "Item has been removed from the map.");
        }
    }

    private void redrawMapCanvas() {
        if (gc == null) return;

        int cols = getRoomColumns(currentRoomId);
        int rows = getRoomRows(currentRoomId);

        // 1. Draw Background Map Artwork
        Image mapImg = getRoomMapImage(currentRoomId);
        if (mapImg != null) {
            gc.drawImage(mapImg, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            // Blueprint fallback grid
            gc.setFill(Color.web("#060c18"));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        // 2. Subtle Coordinate Grid Lines (Every 4 tiles)
        gc.setStroke(Color.rgb(255, 255, 255, 0.08));
        gc.setLineWidth(1.0);
        for (int c = 0; c <= cols; c += 4) {
            double x = (c / (double) cols) * CANVAS_WIDTH;
            gc.strokeLine(x, 0, x, CANVAS_HEIGHT);
        }
        for (int r = 0; r <= rows; r += 4) {
            double y = (r / (double) rows) * CANVAS_HEIGHT;
            gc.strokeLine(0, y, CANVAS_WIDTH, y);
        }

        // 3. Render Placed Item Pins for Current Room
        List<MapItemModel> items = getItemsForRoom(currentRoomId);
        if (roomItemCountLabel != null) {
            roomItemCountLabel.setText("Items in Room: " + items.size());
        }

        for (MapItemModel item : items) {
            double ix = (item.getTileX() / cols) * CANVAS_WIDTH;
            double iy = (item.getTileY() / rows) * CANVAS_HEIGHT;

            boolean isSelected = (selectedMapItem != null && selectedMapItem.getId().equalsIgnoreCase(item.getId()));

            // Ground shadow
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillOval(ix - 14, iy - 3, 28, 10);

            // Item Pin Beacon
            Item regItem = ItemRegistry.findById(item.getItemId()).orElse(null);
            Color itemColor = regItem != null ? regItem.getColor() : Color.web("#eab308");

            if (isSelected) {
                // Golden pulsing selection ring
                gc.setStroke(Color.web("#f59e0b"));
                gc.setLineWidth(3.0);
                gc.strokeOval(ix - 19, iy - 19, 38, 38);
                gc.setFill(Color.rgb(245, 158, 11, 0.25));
                gc.fillOval(ix - 19, iy - 19, 38, 38);
            }

            // Pin body
            gc.setFill(itemColor);
            gc.fillOval(ix - 11, iy - 11, 22, 22);
            gc.setStroke(Color.web("#ffffff"));
            gc.setLineWidth(2.0);
            gc.strokeOval(ix - 11, iy - 11, 22, 22);

            // Item label badge below pin
            String shortName = item.getItemName().length() > 14 ? item.getItemName().substring(0, 13) + "…" : item.getItemName();
            double textW = shortName.length() * 6.5 + 12;

            gc.setFill(Color.rgb(15, 23, 42, 0.85));
            gc.fillRoundRect(ix - textW / 2.0, iy + 13, textW, 16, 5, 5);
            gc.setStroke(isSelected ? Color.web("#f59e0b") : Color.rgb(255, 255, 255, 0.3));
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(ix - textW / 2.0, iy + 13, textW, 16, 5, 5);

            gc.setFill(Color.web("#f8fafc"));
            gc.fillText(shortName, ix - textW / 2.0 + 6, iy + 25);
        }

        // 4. Target Crosshair (when in new placement mode)
        if (selectedMapItem == null) {
            double tx = (targetTileX / cols) * CANVAS_WIDTH;
            double ty = (targetTileY / rows) * CANVAS_HEIGHT;

            // Reticle rings
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(2.0);
            gc.strokeOval(tx - 15, ty - 15, 30, 30);
            gc.strokeOval(tx - 7, ty - 7, 14, 14);

            // Crosshair lines
            gc.strokeLine(tx - 22, ty, tx - 17, ty);
            gc.strokeLine(tx + 17, ty, tx + 22, ty);
            gc.strokeLine(tx, ty - 22, tx, ty - 17);
            gc.strokeLine(tx, ty + 17, tx, ty + 22);

            // Coordinate Tag
            String targetTag = String.format("📍 (%.1f, %.1f)", targetTileX, targetTileY);
            gc.setFill(Color.rgb(15, 23, 42, 0.90));
            gc.fillRoundRect(tx - 40, ty - 32, 80, 18, 5, 5);
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(tx - 40, ty - 32, 80, 18, 5, 5);

            gc.setFill(Color.web("#38bdf8"));
            gc.fillText(targetTag, tx - 34, ty - 19);
        }

        // 5. Room Header Watermark
        gc.setFill(Color.rgb(12, 16, 26, 0.88));
        gc.fillRoundRect(12, 12, 280, 28, 6, 6);
        gc.setStroke(Color.rgb(212, 175, 55, 0.45));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(12, 12, 280, 28, 6, 6);

        gc.setFill(Color.web("#ffd591"));
        gc.fillText("❖ SANCTUM MAP: " + currentRoomId.toUpperCase() + " (" + cols + "x" + rows + " Tiles) ❖", 18, 31);
    }

    private List<MapItemModel> getItemsForRoom(String roomId) {
        List<MapItemModel> list = new ArrayList<>();
        for (MapItemModel item : masterData) {
            if (item.getRoomId().equalsIgnoreCase(roomId)) {
                list.add(item);
            }
        }
        return list;
    }

    private int getRoomColumns(String roomId) {
        return 48;
    }

    private int getRoomRows(String roomId) {
        if (roomId.equalsIgnoreCase("classrooma") || roomId.equalsIgnoreCase("classroom")) {
            return 30;
        }
        return 27;
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
        } catch (Exception ignored) {}

        // Fallback to classroom_room.png if classroom_map.png missing
        try {
            InputStream stream = getClass().getResourceAsStream("/images/maps/classroom_room.png");
            if (stream != null) {
                Image img = new Image(stream);
                cachedRoomImages.put(roomId.toLowerCase(), img);
                return img;
            }
        } catch (Exception ignored) {}

        return null;
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
                    new RoomOption("library", "Lore Library"),
                    new RoomOption("laboratory", "Science Lab"),
                    new RoomOption("teacher", "Principal's Office"),
                    new RoomOption("dormitory", "School Infirmary"),
                    new RoomOption("basement", "Storage Vault"),
                    new RoomOption("entrance", "Restroom & Mirror"),
                    new RoomOption("hall", "Central Main Hall")
            );
        }
        roomSelector.getSelectionModel().selectFirst();
    }

    private void populateItemTypeSelector() {
        if (itemTypeSelector == null) return;
        itemTypeSelector.getItems().clear();
        for (Item it : ItemRegistry.getAllItems()) {
            itemTypeSelector.getItems().add(new ItemOption(it.getId(), it.getDisplayName(), it));
        }
        if (!itemTypeSelector.getItems().isEmpty()) {
            itemTypeSelector.getSelectionModel().selectFirst();
            if (itemAbilityDescLabel != null && itemTypeSelector.getValue() != null && itemTypeSelector.getValue().item != null) {
                itemAbilityDescLabel.setText(itemTypeSelector.getValue().item.getAbilityDescription());
            }
        }
    }

    // =========================================================================
    // DATA TABLE VIEW (BATCH SEARCH & METADATA EDIT)
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void buildTableView() {
        tableContainer.setSpacing(12);

        // Filter & Action Toolbar for Table
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(10, 14, 10, 14));
        filterRow.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-background-radius: 8px;");

        tableRoomFilterBox = new ComboBox<>();
        tableRoomFilterBox.getItems().add("All Rooms");
        for (RoomModel r : roomFileService.loadRooms()) {
            tableRoomFilterBox.getItems().add(r.getName() + " [" + r.getId() + "]");
        }
        tableRoomFilterBox.getSelectionModel().selectFirst();
        tableRoomFilterBox.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        tableItemFilterBox = new ComboBox<>();
        tableItemFilterBox.getItems().add("All Items");
        for (Item it : ItemRegistry.getAllItems()) {
            tableItemFilterBox.getItems().add(it.getDisplayName() + " [" + it.getId() + "]");
        }
        tableItemFilterBox.getSelectionModel().selectFirst();
        tableItemFilterBox.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Button resetBtn = new Button("RESET FILTERS");
        resetBtn.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); -fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");
        resetBtn.setOnAction(e -> {
            tableRoomFilterBox.getSelectionModel().selectFirst();
            tableItemFilterBox.getSelectionModel().selectFirst();
        });

        tableRoomFilterBox.valueProperty().addListener((obs, oldV, newV) -> applyTableFilters());
        tableItemFilterBox.valueProperty().addListener((obs, oldV, newV) -> applyTableFilters());

        filterRow.getChildren().addAll(new Label("Filter Room:"), tableRoomFilterBox, new Label("Item:"), tableItemFilterBox, resetBtn);

        tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: rgba(12, 18, 26, 0.95); -fx-border-color: #2E3E50; -fx-border-width: 1px;");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<MapItemModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(95);

        TableColumn<MapItemModel, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        nameCol.setPrefWidth(180);

        TableColumn<MapItemModel, String> itemKeyCol = new TableColumn<>("Item ID");
        itemKeyCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemKeyCol.setPrefWidth(120);

        TableColumn<MapItemModel, String> roomCol = new TableColumn<>("Room / Map");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        roomCol.setPrefWidth(130);

        TableColumn<MapItemModel, Double> xCol = new TableColumn<>("Tile X");
        xCol.setCellValueFactory(new PropertyValueFactory<>("tileX"));
        xCol.setPrefWidth(75);

        TableColumn<MapItemModel, Double> yCol = new TableColumn<>("Tile Y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("tileY"));
        yCol.setPrefWidth(75);

        TableColumn<MapItemModel, String> descCol = new TableColumn<>("Spot Description / In-Game Note");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(260);

        TableColumn<MapItemModel, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(70);

        tableView.getColumns().addAll(idCol, nameCol, itemKeyCol, roomCol, xCol, yCol, descCol, activeCol);

        tableContainer.getChildren().addAll(filterRow, tableView);
    }

    private void applyTableFilters() {
        if (filteredData == null) return;
        String selRoom = tableRoomFilterBox.getValue();
        String selItem = tableItemFilterBox.getValue();

        filteredData.setPredicate(item -> {
            if (selRoom != null && !selRoom.equals("All Rooms")) {
                String extractId = extractBracketId(selRoom);
                if (!item.getRoomId().equalsIgnoreCase(extractId)) return false;
            }
            if (selItem != null && !selItem.equals("All Items")) {
                String extractId = extractBracketId(selItem);
                if (!item.getItemId().equalsIgnoreCase(extractId)) return false;
            }
            return true;
        });
    }

    private String extractBracketId(String s) {
        if (s == null) return "";
        int start = s.indexOf('[');
        int end = s.indexOf(']');
        if (start >= 0 && end > start) {
            return s.substring(start + 1, end).trim();
        }
        return s.trim();
    }

    public void loadData() {
        List<MapItemModel> items = mapItemFileService.loadMapItems();
        masterData.setAll(items);
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
        redrawMapCanvas();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Helper option classes for combo boxes
    private static class RoomOption {
        final String id;
        final String name;

        RoomOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " [" + id + "]";
        }
    }

    private static class ItemOption {
        final String id;
        final String displayName;
        final Item item;

        ItemOption(String id, String displayName, Item item) {
            this.id = id;
            this.displayName = displayName;
            this.item = item;
        }

        @Override
        public String toString() {
            return displayName + " [" + id + "]";
        }
    }
}
