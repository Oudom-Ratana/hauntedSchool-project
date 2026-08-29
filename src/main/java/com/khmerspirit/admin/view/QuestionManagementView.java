package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.QuestionModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.QuestionFileService;
import com.khmerspirit.admin.service.RoomFileService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Question Management View featuring TableView, Search, Multi-Filter, CRUD, and Player Preview.
 */
public class QuestionManagementView extends VBox {

    private final QuestionFileService questionFileService = new QuestionFileService();
    private final RoomFileService roomFileService = new RoomFileService();

    private final TableView<QuestionModel> tableView = new TableView<>();
    private final ObservableList<QuestionModel> masterData = FXCollections.observableArrayList();
    private FilteredList<QuestionModel> filteredData;

    private TextField searchField;
    private ComboBox<String> roomFilterBox;
    private ComboBox<String> categoryFilterBox;
    private ComboBox<String> difficultyFilterBox;

    public QuestionManagementView() {
        setSpacing(16);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildToolbar();
        buildTableView();
        loadData();
    }

    private void buildHeader() {
        VBox header = new VBox(4);
        Label title = new Label("QUESTION MANAGEMENT SYSTEM");
        title.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 10, 0.3, 0, 0);");
        Label subtitle = new Label("Create, modify, filter, and preview quiz challenges for all haunted school rooms");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildToolbar() {
        VBox toolbarContainer = new VBox(12);
        toolbarContainer.setPadding(new Insets(14));
        toolbarContainer.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-background-radius: 8px;");

        // Row 1: Search & Filters
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search question text, ID, or explanation...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        roomFilterBox = new ComboBox<>();
        roomFilterBox.setPromptText("Filter Room");
        roomFilterBox.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        categoryFilterBox = new ComboBox<>();
        categoryFilterBox.getItems().addAll("All Categories", "Programming", "Networking", "CyberSecurity", "Hardware", "General");
        categoryFilterBox.getSelectionModel().selectFirst();
        categoryFilterBox.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        difficultyFilterBox = new ComboBox<>();
        difficultyFilterBox.getItems().addAll("All Difficulties", "Easy", "Medium", "Hard");
        difficultyFilterBox.getSelectionModel().selectFirst();
        difficultyFilterBox.setStyle("-fx-background-color: rgba(10, 14, 22, 0.9); -fx-text-fill: #f1f5f9; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Button clearFilterBtn = new Button("RESET FILTERS");
        clearFilterBtn.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); -fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6px; -fx-background-radius: 6px;");
        clearFilterBtn.setOnAction(e -> resetFilters());

        filterRow.getChildren().addAll(new Label("Search:"), searchField, roomFilterBox, categoryFilterBox, difficultyFilterBox, clearFilterBtn);

        // Row 2: Action Buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("+ ADD QUESTION");
        addBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #35572F, #1E351C); -fx-text-fill: #E6D3A7; -fx-font-weight: bold; -fx-border-color: #76A14D; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("✏ EDIT");
        editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #7A6135, #4A3A1F); -fx-text-fill: #F0DFB7; -fx-font-weight: bold; -fx-border-color: #D4AF37; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditDialog());

        Button deleteBtn = new Button("🗑 DELETE");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #7F2020, #4A1212); -fx-text-fill: #FFB3B3; -fx-font-weight: bold; -fx-border-color: #FF4D4D; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button previewBtn = new Button("👁 PLAYER PREVIEW");
        previewBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #1D5370, #0F2E3F); -fx-text-fill: #7BB7D8; -fx-font-weight: bold; -fx-border-color: #3B97D4; -fx-cursor: hand;");
        previewBtn.setOnAction(e -> handlePreview());

        actionRow.getChildren().addAll(addBtn, editBtn, deleteBtn, previewBtn);

        toolbarContainer.getChildren().addAll(filterRow, actionRow);
        getChildren().add(toolbarContainer);

        // Filter triggers
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        roomFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        difficultyFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @SuppressWarnings("unchecked")
    private void buildTableView() {
        tableView.setStyle("-fx-background-color: rgba(12, 18, 26, 0.95); -fx-border-color: #2E3E50; -fx-border-width: 1px;");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<QuestionModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(90);

        TableColumn<QuestionModel, String> textCol = new TableColumn<>("Question Text");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        textCol.setPrefWidth(280);

        TableColumn<QuestionModel, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("room"));
        roomCol.setPrefWidth(110);

        TableColumn<QuestionModel, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(110);

        TableColumn<QuestionModel, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        diffCol.setPrefWidth(90);

        TableColumn<QuestionModel, String> ansCol = new TableColumn<>("Answer");
        ansCol.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        ansCol.setPrefWidth(70);

        TableColumn<QuestionModel, String> rewardCol = new TableColumn<>("Reward Value");
        rewardCol.setCellValueFactory(new PropertyValueFactory<>("rewardValue"));
        rewardCol.setPrefWidth(120);

        TableColumn<QuestionModel, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(70);

        tableView.getColumns().addAll(idCol, textCol, roomCol, catCol, diffCol, ansCol, rewardCol, activeCol);
        getChildren().add(tableView);
    }

    public void loadData() {
        List<QuestionModel> questions = questionFileService.loadQuestions();
        masterData.setAll(questions);
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        // Update Room filter options dynamically from RoomFileService
        List<RoomModel> rooms = roomFileService.loadRooms();
        ObservableList<String> roomOptions = FXCollections.observableArrayList("All Rooms");
        for (RoomModel r : rooms) {
            roomOptions.add(r.getId());
        }
        roomFilterBox.setItems(roomOptions);
        roomFilterBox.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String selRoom = roomFilterBox.getValue();
        String selCat = categoryFilterBox.getValue();
        String selDiff = difficultyFilterBox.getValue();

        filteredData.setPredicate(q -> {
            // Search filter
            boolean matchesSearch = query.isEmpty() ||
                    (q.getId() != null && q.getId().toLowerCase().contains(query)) ||
                    (q.getText() != null && q.getText().toLowerCase().contains(query)) ||
                    (q.getExplanation() != null && q.getExplanation().toLowerCase().contains(query));

            // Room filter
            boolean matchesRoom = selRoom == null || "All Rooms".equalsIgnoreCase(selRoom) ||
                    (q.getRoom() != null && q.getRoom().equalsIgnoreCase(selRoom));

            // Category filter
            boolean matchesCat = selCat == null || "All Categories".equalsIgnoreCase(selCat) ||
                    (q.getCategory() != null && q.getCategory().equalsIgnoreCase(selCat));

            // Difficulty filter
            boolean matchesDiff = selDiff == null || "All Difficulties".equalsIgnoreCase(selDiff) ||
                    (q.getDifficulty() != null && q.getDifficulty().equalsIgnoreCase(selDiff));

            return matchesSearch && matchesRoom && matchesCat && matchesDiff;
        });
    }

    private void resetFilters() {
        searchField.clear();
        roomFilterBox.getSelectionModel().selectFirst();
        categoryFilterBox.getSelectionModel().selectFirst();
        difficultyFilterBox.getSelectionModel().selectFirst();
    }

    private void showAddDialog() {
        QuestionFormDialog dialog = new QuestionFormDialog(null, masterData, roomFileService.loadRooms());
        Optional<QuestionModel> result = dialog.showAndWait();
        result.ifPresent(q -> {
            masterData.add(q);
            questionFileService.saveQuestions(masterData);
            loadData();
            showInfo("Success", "Question added successfully!");
        });
    }

    private void showEditDialog() {
        QuestionModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a question from the table to edit.");
            return;
        }
        QuestionFormDialog dialog = new QuestionFormDialog(selected, masterData, roomFileService.loadRooms());
        Optional<QuestionModel> result = dialog.showAndWait();
        result.ifPresent(q -> {
            int idx = masterData.indexOf(selected);
            if (idx >= 0) {
                masterData.set(idx, q);
            }
            questionFileService.saveQuestions(masterData);
            loadData();
            showInfo("Success", "Question updated successfully!");
        });
    }

    private void handleDelete() {
        QuestionModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a question from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Question " + selected.getId() + "?");
        confirm.setContentText("Are you sure you want to remove this question? Backups will be updated automatically.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            masterData.remove(selected);
            questionFileService.saveQuestions(masterData);
            loadData();
            showInfo("Deleted", "Question " + selected.getId() + " has been removed.");
        }
    }

    private void handlePreview() {
        QuestionModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a question from the table to preview.");
            return;
        }
        QuestionPreviewDialog.showPreview(selected);
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

    // Inner Form Dialog Class for Add/Edit
    private static class QuestionFormDialog extends Dialog<QuestionModel> {

        private final TextField idField = new TextField();
        private final TextArea textArea = new TextArea();
        private final TextField optAField = new TextField();
        private final TextField optBField = new TextField();
        private final TextField optCField = new TextField();
        private final TextField optDField = new TextField();
        private final ComboBox<String> ansBox = new ComboBox<>();
        private final ComboBox<String> catBox = new ComboBox<>();
        private final ComboBox<String> roomBox = new ComboBox<>();
        private final ComboBox<String> diffBox = new ComboBox<>();
        private final TextField rewardTypeField = new TextField();
        private final TextField rewardValField = new TextField();
        private final TextArea explanationArea = new TextArea();
        private final CheckBox activeCheckBox = new CheckBox("Active");

        public QuestionFormDialog(QuestionModel existing, List<QuestionModel> allQuestions, List<RoomModel> availableRooms) {
            setTitle(existing == null ? "Add New Question" : "Edit Question " + existing.getId());
            setHeaderText(existing == null ? "Fill in all fields to add a new question to the pool." : "Modify fields and save changes.");

            DialogPane pane = getDialogPane();
            pane.setStyle("-fx-background-color: #0E1622; -fx-text-fill: #E8D2A0;");
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(16));

            // Populate choices
            ansBox.getItems().addAll("A", "B", "C", "D");
            catBox.getItems().addAll("Programming", "Networking", "CyberSecurity", "Hardware", "General");
            for (RoomModel r : availableRooms) {
                roomBox.getItems().add(r.getId());
            }
            if (roomBox.getItems().isEmpty()) {
                roomBox.getItems().addAll("entrance", "classroomA", "classroomB", "computer", "laboratory", "library", "basement");
            }
            diffBox.getItems().addAll("Easy", "Medium", "Hard");

            textArea.setPrefRowCount(3);
            explanationArea.setPrefRowCount(2);

            grid.add(new Label("Question ID:"), 0, 0); grid.add(idField, 1, 0);
            grid.add(new Label("Room:"), 2, 0); grid.add(roomBox, 3, 0);

            grid.add(new Label("Question Text:"), 0, 1); grid.add(textArea, 1, 1, 3, 1);

            grid.add(new Label("Option A:"), 0, 2); grid.add(optAField, 1, 2);
            grid.add(new Label("Option B:"), 2, 2); grid.add(optBField, 3, 2);
            grid.add(new Label("Option C:"), 0, 3); grid.add(optCField, 1, 3);
            grid.add(new Label("Option D:"), 2, 3); grid.add(optDField, 3, 3);

            grid.add(new Label("Correct Answer:"), 0, 4); grid.add(ansBox, 1, 4);
            grid.add(new Label("Category:"), 2, 4); grid.add(catBox, 3, 4);

            grid.add(new Label("Difficulty:"), 0, 5); grid.add(diffBox, 1, 5);
            grid.add(new Label("Active Status:"), 2, 5); grid.add(activeCheckBox, 3, 5);

            grid.add(new Label("Reward Type:"), 0, 6); grid.add(rewardTypeField, 1, 6);
            grid.add(new Label("Reward Value:"), 2, 6); grid.add(rewardValField, 3, 6);

            grid.add(new Label("Explanation:"), 0, 7); grid.add(explanationArea, 1, 7, 3, 1);

            if (existing != null) {
                idField.setText(existing.getId());
                idField.setDisable(true); // Don't edit ID
                textArea.setText(existing.getText());
                optAField.setText(existing.getOptionA());
                optBField.setText(existing.getOptionB());
                optCField.setText(existing.getOptionC());
                optDField.setText(existing.getOptionD());
                ansBox.setValue(existing.getCorrectAnswer());
                catBox.setValue(existing.getCategory());
                roomBox.setValue(existing.getRoom());
                diffBox.setValue(existing.getDifficulty());
                rewardTypeField.setText(existing.getRewardType());
                rewardValField.setText(existing.getRewardValue());
                explanationArea.setText(existing.getExplanation());
                activeCheckBox.setSelected(existing.isActive());
            } else {
                idField.setText("Q_" + (allQuestions.size() + 1));
                ansBox.getSelectionModel().selectFirst();
                catBox.getSelectionModel().selectFirst();
                roomBox.getSelectionModel().selectFirst();
                diffBox.getSelectionModel().selectFirst();
                rewardTypeField.setText("Item");
                rewardValField.setText("Flashlight");
                activeCheckBox.setSelected(true);
            }

            pane.setContent(grid);

            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    // Input Validation
                    String id = idField.getText().trim();
                    String text = textArea.getText().trim();
                    String a = optAField.getText().trim();
                    String b = optBField.getText().trim();
                    String c = optCField.getText().trim();
                    String d = optDField.getText().trim();
                    String ans = ansBox.getValue();
                    String cat = catBox.getValue();
                    String room = roomBox.getValue();
                    String diff = diffBox.getValue();

                    if (id.isEmpty() || text.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() ||
                        ans == null || cat == null || room == null || diff == null) {
                        showFormError("Validation Error", "All required fields must be filled in (ID, Text, Options A-D, Answer, Category, Room, Difficulty).");
                        return null;
                    }

                    if (existing == null && allQuestions.stream().anyMatch(q -> q.getId().equalsIgnoreCase(id))) {
                        showFormError("Duplicate ID", "A question with ID '" + id + "' already exists! Please use a unique ID.");
                        return null;
                    }

                    return new QuestionModel(
                            id, text, a, b, c, d, ans, cat, room, diff,
                            rewardTypeField.getText().trim(), rewardValField.getText().trim(),
                            explanationArea.getText().trim(), activeCheckBox.isSelected()
                    );
                }
                return null;
            });
        }

        private void showFormError(String title, String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }
}
