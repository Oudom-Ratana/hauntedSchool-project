package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.QuestionModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.QuestionFileService;
import com.khmerspirit.admin.service.RoomFileService;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Modern Question Management View matching draftDesign.png,
 * featuring a 3-column split view:
 * 1. Rooms & Tasks selector card list (Left Column)
 * 2. Room & Task question data table with action buttons (Center Column)
 * 3. Add/Edit question inspector form with live validation (Right Column)
 */
public class QuestionManagementView extends HBox {

    private final QuestionFileService questionFileService = new QuestionFileService();
    private final RoomFileService roomFileService = new RoomFileService();

    private final ObservableList<QuestionModel> masterData = FXCollections.observableArrayList();
    private FilteredList<QuestionModel> filteredData;

    // Room Card Column components
    private final VBox roomCardsContainer = new VBox(8);
    private String selectedRoomId = "classroomA";
    private String selectedRoomDisplayName = "Classroom";

    // Center Column components
    private Label tableHeaderTitle;
    private Label tableHeaderSubtitle;
    private TextField searchField;
    private ComboBox<String> taskFilterBox;
    private final TableView<QuestionModel> tableView = new TableView<>();

    // Right Column Form components
    private Label formHeaderTitle;
    private QuestionModel editingQuestion = null;
    private ComboBox<String> formRoomBox;
    private ComboBox<Integer> formTaskBox;
    private TextArea questionTextArea;
    private Label charCounterLabel;
    private TextField optAField;
    private TextField optBField;
    private TextField optCField;
    private TextField optDField;
    private ComboBox<String> formAnsBox;
    private RadioButton rbActive;
    private RadioButton rbInactive;

    public QuestionManagementView() {
        setSpacing(14);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #070b14;");

        // Build 3-column layout
        VBox leftCol = buildRoomsColumn();
        VBox centerCol = buildQuestionsColumn();
        VBox rightCol = buildFormColumn();

        HBox.setHgrow(centerCol, Priority.ALWAYS);

        getChildren().addAll(leftCol, centerCol, rightCol);

        loadData();
    }

    /**
     * Column 1: Rooms & Tasks Card List (Left)
     */
    private VBox buildRoomsColumn() {
        VBox col = new VBox(12);
        col.setPrefWidth(240);
        col.setMinWidth(220);
        col.setMaxWidth(260);
        col.getStyleClass().add("admin-card-container");

        // Header Title
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("🏠");
        iconLbl.setStyle("-fx-font-size: 14px;");
        Label titleLbl = new Label("Rooms & Tasks");
        titleLbl.getStyleClass().add("admin-card-header-title");
        headerBox.getChildren().addAll(iconLbl, titleLbl);

        // Scrollable room cards
        ScrollPane scrollPane = new ScrollPane(roomCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        col.getChildren().addAll(headerBox, scrollPane);
        return col;
    }

    /**
     * Column 2: Question Table for selected room & task (Center)
     */
    private VBox buildQuestionsColumn() {
        VBox col = new VBox(12);
        col.getStyleClass().add("admin-card-container");

        // 1. Header with Task Title & Controls
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label screenIcon = new Label("📺");
        screenIcon.setStyle("-fx-font-size: 18px;");

        VBox titleBox = new VBox(2);
        tableHeaderTitle = new Label("Classroom - Task 1");
        tableHeaderTitle.getStyleClass().add("admin-card-header-title");
        tableHeaderTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        tableHeaderSubtitle = new Label("Manage questions for this task");
        tableHeaderSubtitle.getStyleClass().add("admin-card-header-sub");
        titleBox.getChildren().addAll(tableHeaderTitle, tableHeaderSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter: Task selection
        taskFilterBox = new ComboBox<>();
        taskFilterBox.getItems().addAll("All Tasks", "Task 1", "Task 2", "Task 3", "Task 4", "Task 5");
        taskFilterBox.getSelectionModel().selectFirst();
        taskFilterBox.getStyleClass().add("modern-form-input");
        taskFilterBox.setPrefWidth(120);
        taskFilterBox.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Filter: Search field
        searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.getStyleClass().add("modern-form-input");
        searchField.setPrefWidth(180);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Preview In-Game Quiz Button
        Button btnPreview = new Button("👁️ Preview");
        btnPreview.getStyleClass().add("btn-modern-secondary");
        btnPreview.setOnAction(e -> handlePreview());

        headerRow.getChildren().addAll(screenIcon, titleBox, spacer, taskFilterBox, searchField, btnPreview);

        // 2. TableView matching draftDesign.png
        buildTableView();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        col.getChildren().addAll(headerRow, tableView);
        return col;
    }

    @SuppressWarnings("unchecked")
    private void buildTableView() {
        tableView.setStyle("-fx-background-color: #0c1322; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        // Column 1: # (Index)
        TableColumn<QuestionModel, Integer> indexCol = new TableColumn<>("#");
        indexCol.setPrefWidth(42);
        indexCol.setStyle("-fx-alignment: center;");
        indexCol.setCellValueFactory(cellData -> {
            int idx = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });

        // Column 2: Question (Text)
        TableColumn<QuestionModel, String> textCol = new TableColumn<>("Question");
        textCol.setPrefWidth(290);
        textCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getText()));

        // Column 3: Type (Pill Badge)
        TableColumn<QuestionModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(85);
        typeCol.setStyle("-fx-alignment: center;");
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuestionType()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge-pill", "badge-pill-mcq");
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Column 4: Status (Pill Badge)
        TableColumn<QuestionModel, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(85);
        statusCol.setStyle("-fx-alignment: center;");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isActive()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(active ? "Active" : "Inactive");
                    badge.getStyleClass().add("badge-pill");
                    badge.getStyleClass().add(active ? "badge-pill-active" : "badge-pill-inactive");
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Column 5: Actions (Edit & Delete)
        TableColumn<QuestionModel, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(95);
        actionCol.setStyle("-fx-alignment: center;");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(6, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-icon-action");
                btnDelete.getStyleClass().add("btn-icon-danger");

                btnEdit.setOnAction(e -> {
                    QuestionModel q = getTableView().getItems().get(getIndex());
                    populateFormForEditing(q);
                });

                btnDelete.setOnAction(e -> {
                    QuestionModel q = getTableView().getItems().get(getIndex());
                    handleDelete(q);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableView.getColumns().addAll(indexCol, textCol, typeCol, statusCol, actionCol);

        // Click row to edit
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                populateFormForEditing(newV);
            }
        });
    }

    /**
     * Column 3: Add New Question / Edit Form (Right)
     */
    private VBox buildFormColumn() {
        VBox col = new VBox(10);
        col.setPrefWidth(350);
        col.setMinWidth(320);
        col.setMaxWidth(380);
        col.getStyleClass().add("admin-card-container");

        // Header
        HBox headerRow = new HBox(6);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        formHeaderTitle = new Label("➕ Add New Question");
        formHeaderTitle.getStyleClass().add("admin-card-header-title");
        headerRow.getChildren().add(formHeaderTitle);

        // Scrollable Form Fields
        VBox formFields = new VBox(10);

        // 1. Room Field
        Label lblRoom = new Label("Room");
        lblRoom.getStyleClass().add("modern-form-label");
        formRoomBox = new ComboBox<>();
        formRoomBox.setMaxWidth(Double.MAX_VALUE);
        formRoomBox.getStyleClass().add("modern-form-input");

        // 2. Task Number Field
        Label lblTask = new Label("Task Number");
        lblTask.getStyleClass().add("modern-form-label");
        formTaskBox = new ComboBox<>();
        formTaskBox.getItems().addAll(1, 2, 3, 4, 5);
        formTaskBox.getSelectionModel().selectFirst();
        formTaskBox.setMaxWidth(Double.MAX_VALUE);
        formTaskBox.getStyleClass().add("modern-form-input");

        // 3. Question Text Area + Char counter
        Label lblPrompt = new Label("Question Text");
        lblPrompt.getStyleClass().add("modern-form-label");

        questionTextArea = new TextArea();
        questionTextArea.setPromptText("Enter the question here...");
        questionTextArea.setPrefRowCount(3);
        questionTextArea.setWrapText(true);
        questionTextArea.getStyleClass().add("modern-form-input");

        charCounterLabel = new Label("0/500");
        charCounterLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-alignment: center-right;");
        questionTextArea.textProperty().addListener((obs, oldV, newV) -> {
            int len = newV != null ? newV.length() : 0;
            charCounterLabel.setText(len + "/500");
        });

        HBox charCountBox = new HBox(charCounterLabel);
        charCountBox.setAlignment(Pos.CENTER_RIGHT);

        // 4. Question Type (MCQ Only)
        Label lblType = new Label("Question Type");
        lblType.getStyleClass().add("modern-form-label");

        HBox typeBadgeBox = new HBox();
        Label mcqBadge = new Label("Multiple Choice (MCQ)");
        mcqBadge.getStyleClass().addAll("badge-pill", "badge-pill-mcq");
        mcqBadge.setStyle("-fx-padding: 4px 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
        typeBadgeBox.getChildren().add(mcqBadge);

        // 5. Options (for MCQ)
        Label lblOptions = new Label("Options (for MCQ)");
        lblOptions.getStyleClass().add("modern-form-label");

        optAField = new TextField();
        optAField.setPromptText("Option A");
        optAField.getStyleClass().add("modern-form-input");
        HBox rowA = new HBox(8, new Label("A."), optAField);
        rowA.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(optAField, Priority.ALWAYS);

        optBField = new TextField();
        optBField.setPromptText("Option B");
        optBField.getStyleClass().add("modern-form-input");
        HBox rowB = new HBox(8, new Label("B."), optBField);
        rowB.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(optBField, Priority.ALWAYS);

        optCField = new TextField();
        optCField.setPromptText("Option C");
        optCField.getStyleClass().add("modern-form-input");
        HBox rowC = new HBox(8, new Label("C."), optCField);
        rowC.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(optCField, Priority.ALWAYS);

        optDField = new TextField();
        optDField.setPromptText("Option D");
        optDField.getStyleClass().add("modern-form-input");
        HBox rowD = new HBox(8, new Label("D."), optDField);
        rowD.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(optDField, Priority.ALWAYS);

        VBox optionsBox = new VBox(6, rowA, rowB, rowC, rowD);

        // 6. Correct Answer
        Label lblAns = new Label("Correct Answer");
        lblAns.getStyleClass().add("modern-form-label");
        formAnsBox = new ComboBox<>();
        formAnsBox.getItems().addAll("A", "B", "C", "D");
        formAnsBox.getSelectionModel().selectFirst();
        formAnsBox.setMaxWidth(Double.MAX_VALUE);
        formAnsBox.getStyleClass().add("modern-form-input");

        // 7. Status (Active / Inactive)
        Label lblStatus = new Label("Status");
        lblStatus.getStyleClass().add("modern-form-label");
        ToggleGroup statusGroup = new ToggleGroup();
        rbActive = new RadioButton("Active");
        rbActive.setToggleGroup(statusGroup);
        rbActive.setSelected(true);
        rbActive.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11.5px;");

        rbInactive = new RadioButton("Inactive");
        rbInactive.setToggleGroup(statusGroup);
        rbInactive.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11.5px;");

        HBox statusBox = new HBox(16, rbActive, rbInactive);

        // 8. Action Buttons (Save & Clear)
        Button btnSave = new Button("💾 Save Question");
        btnSave.getStyleClass().add("btn-modern-primary");
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> handleSaveForm());

        Button btnClear = new Button("🔄 Clear");
        btnClear.getStyleClass().add("btn-modern-secondary");
        btnClear.setOnAction(e -> resetForm());

        HBox buttonBox = new HBox(10, btnSave, btnClear);
        buttonBox.setPadding(new Insets(6, 0, 0, 0));

        formFields.getChildren().addAll(
                lblRoom, formRoomBox,
                lblTask, formTaskBox,
                lblPrompt, questionTextArea, charCountBox,
                lblType, typeBadgeBox,
                lblOptions, optionsBox,
                lblAns, formAnsBox,
                lblStatus, statusBox,
                buttonBox
        );

        ScrollPane scrollForm = new ScrollPane(formFields);
        scrollForm.setFitToWidth(true);
        scrollForm.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(scrollForm, Priority.ALWAYS);

        col.getChildren().addAll(headerRow, scrollForm);
        return col;
    }

    /**
     * Load data from QuestionFileService & RoomFileService and construct room cards
     */
    public void loadData() {
        List<QuestionModel> questions = questionFileService.loadQuestions();
        masterData.setAll(questions);
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        // Populate room options in right form
        List<RoomModel> rooms = roomFileService.loadRooms();
        ObservableList<String> roomOptions = FXCollections.observableArrayList();
        for (RoomModel r : rooms) {
            roomOptions.add(r.getName() + " [" + r.getId() + "]");
        }
        formRoomBox.setItems(roomOptions);
        if (!roomOptions.isEmpty()) {
            formRoomBox.getSelectionModel().selectFirst();
        }

        // Build left column room cards
        renderRoomCards(rooms);

        // Apply filters
        applyFilters();
    }

    private void renderRoomCards(List<RoomModel> rooms) {
        roomCardsContainer.getChildren().clear();

        for (RoomModel r : rooms) {
            HBox card = new HBox(10);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("admin-room-card");

            if (r.getId().equalsIgnoreCase(selectedRoomId)) {
                card.getStyleClass().add("admin-room-card-selected");
            }

            // Thumbnail Image
            ImageView thumbView = new ImageView();
            Image img = loadRoomImage(r.getId());
            if (img != null) {
                thumbView.setImage(img);
                thumbView.setFitWidth(54);
                thumbView.setFitHeight(42);
                thumbView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(54, 42);
                clip.setArcWidth(6);
                clip.setArcHeight(6);
                thumbView.setClip(clip);
            }

            // Title & Task count
            VBox infoBox = new VBox(2);
            Label nameLbl = new Label(r.getName());
            nameLbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

            long taskCount = masterData.stream().filter(q -> isRoomMatch(q.getRoom(), r.getId())).count();
            Label taskLbl = new Label(taskCount + " tasks");
            taskLbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-text-fill: #38bdf8;");

            infoBox.getChildren().addAll(nameLbl, taskLbl);
            card.getChildren().addAll(thumbView, infoBox);

            card.setOnMouseClicked(e -> {
                selectedRoomId = r.getId();
                selectedRoomDisplayName = r.getName();
                tableHeaderTitle.setText(r.getName() + " - Task 1");

                // Pre-select this room in form
                for (String opt : formRoomBox.getItems()) {
                    if (opt.contains("[" + r.getId() + "]")) {
                        formRoomBox.setValue(opt);
                        break;
                    }
                }

                renderRoomCards(rooms);
                applyFilters();
            });

            roomCardsContainer.getChildren().add(card);
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String taskSel = taskFilterBox.getValue();

        filteredData.setPredicate(q -> {
            // Room match
            if (!isRoomMatch(q.getRoom(), selectedRoomId)) {
                return false;
            }

            // Task match
            if (taskSel != null && !taskSel.equals("All Tasks")) {
                int targetTask = 1;
                try {
                    targetTask = Integer.parseInt(taskSel.replace("Task ", "").trim());
                } catch (Exception ignored) {}
                if (q.getTaskNumber() != targetTask) {
                    return false;
                }
            }

            // Query match
            if (!query.isEmpty()) {
                boolean matchText = q.getText() != null && q.getText().toLowerCase().contains(query);
                boolean matchId = q.getId() != null && q.getId().toLowerCase().contains(query);
                return matchText || matchId;
            }

            return true;
        });
    }

    private void populateFormForEditing(QuestionModel q) {
        this.editingQuestion = q;
        formHeaderTitle.setText("✏️ Edit Question: " + q.getId());

        // Room
        String curRoom = q.getRoom();
        for (String item : formRoomBox.getItems()) {
            if (item.contains("[" + curRoom + "]") || item.equalsIgnoreCase(curRoom)) {
                formRoomBox.setValue(item);
                break;
            }
        }

        formTaskBox.setValue(q.getTaskNumber() > 0 ? q.getTaskNumber() : 1);
        questionTextArea.setText(q.getText() != null ? q.getText() : "");

        optAField.setText(q.getOptionA() != null ? q.getOptionA() : "");
        optBField.setText(q.getOptionB() != null ? q.getOptionB() : "");
        optCField.setText(q.getOptionC() != null ? q.getOptionC() : "");
        optDField.setText(q.getOptionD() != null ? q.getOptionD() : "");

        formAnsBox.setValue(q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "A");

        if (q.isActive()) {
            rbActive.setSelected(true);
        } else {
            rbInactive.setSelected(true);
        }
    }

    private void handleSaveForm() {
        String roomRaw = formRoomBox.getValue();
        Integer taskNum = formTaskBox.getValue();
        String text = questionTextArea.getText().trim();
        String a = optAField.getText().trim();
        String b = optBField.getText().trim();
        String c = optCField.getText().trim();
        String d = optDField.getText().trim();
        String ans = formAnsBox.getValue();
        boolean active = rbActive.isSelected();
        String qType = "MCQ";

        if (roomRaw == null || text.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a room and enter the question prompt text.");
            return;
        }

        String roomId = roomRaw;
        if (roomRaw.contains("[") && roomRaw.contains("]")) {
            roomId = roomRaw.substring(roomRaw.indexOf("[") + 1, roomRaw.indexOf("]")).trim();
        }

        if (editingQuestion != null) {
            // Update existing question
            editingQuestion.setText(text);
            editingQuestion.setRoom(roomId);
            editingQuestion.setTaskNumber(taskNum != null ? taskNum : 1);
            editingQuestion.setQuestionType(qType);
            editingQuestion.setOptionA(a);
            editingQuestion.setOptionB(b);
            editingQuestion.setOptionC(c);
            editingQuestion.setOptionD(d);
            editingQuestion.setCorrectAnswer(ans);
            editingQuestion.setActive(active);

            questionFileService.saveQuestions(masterData);
            loadData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Question updated successfully!");
        } else {
            // Create new question
            String newId = "Q_" + (masterData.size() + 1);
            QuestionModel newQ = new QuestionModel(
                    newId, text, a, b, c, d, ans, "General", roomId, "Medium",
                    "Item", "Flashlight", "", active, qType, taskNum != null ? taskNum : 1
            );
            masterData.add(newQ);
            questionFileService.saveQuestions(masterData);
            loadData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "New question added successfully!");
        }

        resetForm();
    }

    private void resetForm() {
        editingQuestion = null;
        formHeaderTitle.setText("➕ Add New Question");
        questionTextArea.clear();
        optAField.clear();
        optBField.clear();
        optCField.clear();
        optDField.clear();
        rbActive.setSelected(true);
        formAnsBox.getSelectionModel().selectFirst();
        formTaskBox.getSelectionModel().selectFirst();
        tableView.getSelectionModel().clearSelection();
    }

    private void handleDelete(QuestionModel q) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Question " + q.getId() + "?");
        confirm.setContentText("Are you sure you want to remove this question?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            masterData.remove(q);
            questionFileService.saveQuestions(masterData);
            loadData();
            resetForm();
        }
    }

    private void handlePreview() {
        QuestionModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null && !tableView.getItems().isEmpty()) {
            selected = tableView.getItems().get(0);
        }
        if (selected != null) {
            QuestionPreviewDialog.showPreview(selected);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "No Question", "Please select a question from the table to preview.");
        }
    }

    private boolean isRoomMatch(String qRoom, String targetId) {
        if (qRoom == null || targetId == null) return false;
        String qr = qRoom.toLowerCase().trim();
        String tr = targetId.toLowerCase().trim();
        if (qr.equals(tr)) return true;
        if ((qr.equals("classrooma") || qr.equals("classroom")) && (tr.equals("classrooma") || tr.equals("classroom"))) return true;
        if ((qr.equals("classroomb") || qr.equals("teachers_lounge")) && (tr.equals("classroomb") || tr.equals("teachers_lounge"))) return true;
        if ((qr.equals("computer") || qr.equals("music_art_room")) && (tr.equals("computer") || tr.equals("music_art_room"))) return true;
        if ((qr.equals("laboratory") || qr.equals("science_lab")) && (tr.equals("laboratory") || tr.equals("science_lab"))) return true;
        if ((qr.equals("teacher") || qr.equals("principal_office")) && (tr.equals("teacher") || tr.equals("principal_office"))) return true;
        if ((qr.equals("dormitory") || qr.equals("infirmary")) && (tr.equals("dormitory") || tr.equals("infirmary"))) return true;
        if ((qr.equals("basement") || qr.equals("storage_room")) && (tr.equals("basement") || tr.equals("storage_room"))) return true;
        if ((qr.equals("entrance") || qr.equals("restroom")) && (tr.equals("entrance") || tr.equals("restroom"))) return true;
        if ((qr.equals("hall") || qr.equals("main_hall") || qr.equals("school")) && (tr.equals("hall") || tr.equals("main_hall") || tr.equals("school"))) return true;
        return false;
    }

    private Image loadRoomImage(String roomId) {
        String filename = "classroom_a.png";
        String rid = roomId != null ? roomId.toLowerCase() : "";
        if (rid.contains("library")) filename = "library.png";
        else if (rid.contains("lab")) filename = "laboratory.png";
        else if (rid.contains("base") || rid.contains("storage")) filename = "basement_stairway.png";
        else if (rid.contains("exit") || rid.contains("courtyard") || rid.contains("gate")) filename = "courtyard_gate.png";
        else if (rid.contains("teacher") || rid.contains("lounge") || rid.contains("principal")) filename = "teacher_room.png";
        else if (rid.contains("dorm") || rid.contains("infirmary")) filename = "dormitory.png";
        else if (rid.contains("hall") || rid.contains("entrance") || rid.contains("restroom")) filename = "gloomy_hallway.png";

        String resPath = "/images/environment_concepts/" + filename;
        String filePath = "images/environment_concepts/" + filename;

        try {
            InputStream is = getClass().getResourceAsStream(resPath);
            if (is != null) return new Image(is);
            File f = new File(filePath);
            if (f.exists()) return new Image(f.toURI().toString());
            File rf = new File("src/main/resources" + resPath);
            if (rf.exists()) return new Image(rf.toURI().toString());
        } catch (Exception ignored) {}
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
