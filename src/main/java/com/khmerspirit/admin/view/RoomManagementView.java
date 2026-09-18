package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.RewardModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.RewardFileService;
import com.khmerspirit.admin.service.RoomFileService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

/**
 * Room & Story Guide Management View featuring TableView, CRUD operations,
 * and direct editing of in-game Story Guide Panel information (Title, Lore, Next-Steps).
 */
public class RoomManagementView extends VBox {

    private final RoomFileService roomFileService = new RoomFileService();
    private final RewardFileService rewardFileService = new RewardFileService();

    private final TableView<RoomModel> tableView = new TableView<>();
    private final ObservableList<RoomModel> roomList = FXCollections.observableArrayList();

    public RoomManagementView() {
        setSpacing(16);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildToolbar();
        buildTableView();
        loadData();
    }

    private void buildHeader() {
        VBox header = new VBox(2);
        Label title = new Label("🏠  Rooms & Story Guides");
        title.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Configure haunted chambers, question quotas, key locks, and in-game guide narratives");
        subtitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildToolbar() {
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(12, 14, 12, 14));
        actionRow.getStyleClass().add("admin-card-container");

        Button addBtn = new Button("➕  Add Chamber");
        addBtn.getStyleClass().add("btn-modern-primary");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("✏️  Edit Chamber & Guide");
        editBtn.getStyleClass().add("btn-modern-secondary");
        editBtn.setOnAction(e -> showEditDialog());

        Button deleteBtn = new Button("🗑️  Delete Chamber");
        deleteBtn.getStyleClass().add("btn-modern-secondary");
        deleteBtn.setStyle("-fx-text-fill: #f87171;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button refreshBtn = new Button("🔄  Refresh Repository");
        refreshBtn.getStyleClass().add("btn-modern-secondary");
        refreshBtn.setOnAction(e -> loadData());

        actionRow.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);
        getChildren().add(actionRow);
    }

    @SuppressWarnings("unchecked")
    private void buildTableView() {
        tableView.setStyle("-fx-background-color: #0c1322; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<RoomModel, String> idCol = new TableColumn<>("Room ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(110);

        TableColumn<RoomModel, String> nameCol = new TableColumn<>("Room Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);

        TableColumn<RoomModel, Integer> reqCol = new TableColumn<>("Required Qs");
        reqCol.setCellValueFactory(new PropertyValueFactory<>("requiredQs"));
        reqCol.setPrefWidth(95);

        TableColumn<RoomModel, String> guideTitleCol = new TableColumn<>("Story Guide Panel Title");
        guideTitleCol.setCellValueFactory(new PropertyValueFactory<>("guideTitle"));
        guideTitleCol.setPrefWidth(260);

        TableColumn<RoomModel, String> keyCol = new TableColumn<>("Key Reward");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("keyReward"));
        keyCol.setPrefWidth(110);

        TableColumn<RoomModel, String> nextCol = new TableColumn<>("Next Room");
        nextCol.setCellValueFactory(new PropertyValueFactory<>("nextRoomId"));
        nextCol.setPrefWidth(110);

        TableColumn<RoomModel, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(70);

        tableView.getColumns().addAll(idCol, nameCol, reqCol, guideTitleCol, keyCol, nextCol, activeCol);
        getChildren().add(tableView);
    }

    public void loadData() {
        List<RoomModel> rooms = roomFileService.loadRooms();
        roomList.setAll(rooms);
        tableView.setItems(roomList);
    }

    private void showAddDialog() {
        RoomFormDialog dialog = new RoomFormDialog(null, roomList, rewardFileService.loadRewards());
        Optional<RoomModel> result = dialog.showAndWait();
        result.ifPresent(r -> {
            roomList.add(r);
            roomFileService.saveRooms(roomList);
            loadData();
            showInfo("Success", "Room added successfully!");
        });
    }

    private void showEditDialog() {
        RoomModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a room from the table to edit.");
            return;
        }
        RoomFormDialog dialog = new RoomFormDialog(selected, roomList, rewardFileService.loadRewards());
        Optional<RoomModel> result = dialog.showAndWait();
        result.ifPresent(r -> {
            int idx = roomList.indexOf(selected);
            if (idx >= 0) {
                roomList.set(idx, r);
            }
            roomFileService.saveRooms(roomList);
            loadData();
            showInfo("Success", "Room and Story Guide info updated successfully!");
        });
    }

    private void handleDelete() {
        RoomModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a room from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Room " + selected.getName() + " (" + selected.getId() + ")?");
        confirm.setContentText("Are you sure you want to remove this room?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            roomList.remove(selected);
            roomFileService.saveRooms(roomList);
            loadData();
            showInfo("Deleted", "Room has been removed successfully.");
        }
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

    // Dialog for Room & Story Guide Creation & Editing
    private static class RoomFormDialog extends Dialog<RoomModel> {

        private final TextField idField = new TextField();
        private final TextField nameField = new TextField();
        private final TextArea descArea = new TextArea();
        private final Spinner<Integer> reqQsSpinner = new Spinner<>(1, 20, 5);
        private final ComboBox<String> nextRoomBox = new ComboBox<>();
        private final ComboBox<String> keyRewardBox = new ComboBox<>();
        private final CheckBox activeCheckBox = new CheckBox("Active");

        // Story Guide panel inputs
        private final TextField guideTitleField = new TextField();
        private final TextArea guideNarrativeArea = new TextArea();
        private final TextArea guideNextStepArea = new TextArea();

        public RoomFormDialog(RoomModel existing, List<RoomModel> allRooms, List<RewardModel> rewards) {
            setTitle(existing == null ? "Add New Chamber" : "Edit Chamber: " + existing.getName());
            setHeaderText(existing == null ? "Configure chamber properties, questions required, and Story Guide." : "Modify chamber parameters and narrative directions.");

            DialogPane pane = getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setPrefWidth(600);

            // Apply modern input classes
            idField.getStyleClass().add("modern-form-input");
            nameField.getStyleClass().add("modern-form-input");
            descArea.getStyleClass().add("modern-form-input");
            reqQsSpinner.getStyleClass().add("modern-form-input");
            nextRoomBox.getStyleClass().add("modern-form-input");
            keyRewardBox.getStyleClass().add("modern-form-input");
            guideTitleField.getStyleClass().add("modern-form-input");
            guideNarrativeArea.getStyleClass().add("modern-form-input");
            guideNextStepArea.getStyleClass().add("modern-form-input");

            GridPane grid = new GridPane();
            grid.setHgap(14);
            grid.setVgap(10);
            grid.setPadding(new Insets(16));

            // Populate next room candidates
            nextRoomBox.getItems().add("None (Final Room)");
            for (RoomModel r : allRooms) {
                if (existing == null || !r.getId().equalsIgnoreCase(existing.getId())) {
                    nextRoomBox.getItems().add(r.getId());
                }
            }
            nextRoomBox.getSelectionModel().selectFirst();
            nextRoomBox.setMaxWidth(Double.MAX_VALUE);

            // Populate Key Reward options
            keyRewardBox.getItems().addAll("key", "master_key", "none");
            keyRewardBox.getSelectionModel().selectFirst();
            keyRewardBox.setMaxWidth(Double.MAX_VALUE);

            descArea.setPrefRowCount(2);
            guideNarrativeArea.setPrefRowCount(3);
            guideNextStepArea.setPrefRowCount(3);

            int row = 0;
            grid.add(createFormLabel("Chamber ID:"), 0, row); grid.add(idField, 1, row);
            grid.add(createFormLabel("Active Status:"), 2, row); grid.add(activeCheckBox, 3, row);
            row++;

            grid.add(createFormLabel("Chamber Name:"), 0, row); grid.add(nameField, 1, row, 3, 1);
            row++;

            grid.add(createFormLabel("Description:"), 0, row); grid.add(descArea, 1, row, 3, 1);
            row++;

            grid.add(createFormLabel("Required Questions:"), 0, row); grid.add(reqQsSpinner, 1, row);
            grid.add(createFormLabel("Next Chamber:"), 2, row); grid.add(nextRoomBox, 3, row);
            row++;

            grid.add(createFormLabel("Key Given:"), 0, row); grid.add(keyRewardBox, 1, row, 3, 1);
            row++;

            // Story Guide Section Separator
            VBox guideHeaderCard = new VBox(2);
            guideHeaderCard.setPadding(new Insets(10, 12, 10, 12));
            guideHeaderCard.setStyle("-fx-background-color: rgba(37, 99, 235, 0.15); -fx-border-color: #2563eb; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px;");

            Label guideHeader = new Label("❖  IN-GAME STORY GUIDE CONFIGURATION");
            guideHeader.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px; -fx-font-weight: 900; -fx-text-fill: #60a5fa;");
            Label guideHeaderSub = new Label("Controls the lore popup and directions shown to player upon chamber purification");
            guideHeaderSub.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            guideHeaderCard.getChildren().addAll(guideHeader, guideHeaderSub);

            grid.add(guideHeaderCard, 0, row, 4, 1);
            row++;

            grid.add(createFormLabel("Guide Title:"), 0, row); grid.add(guideTitleField, 1, row, 3, 1);
            guideTitleField.setPromptText("e.g. CLASSROOM A PURIFIED: SPIRIT OF SOTHEA");
            row++;

            grid.add(createFormLabel("Lore Narrative:"), 0, row); grid.add(guideNarrativeArea, 1, row, 3, 1);
            guideNarrativeArea.setPromptText("Story and lore explaining what occurred when this chamber was purified...");
            row++;

            grid.add(createFormLabel("Next Objectives:"), 0, row); grid.add(guideNextStepArea, 1, row, 3, 1);
            guideNextStepArea.setPromptText("Clear directives telling the player what to do next and where to proceed...");
            row++;

            if (existing != null) {
                idField.setText(existing.getId());
                idField.setDisable(true);
                nameField.setText(existing.getName());
                descArea.setText(existing.getDescription());
                reqQsSpinner.getValueFactory().setValue(existing.getRequiredQs());
                if (existing.getNextRoomId() != null) nextRoomBox.setValue(existing.getNextRoomId());
                if (existing.getKeyReward() != null) keyRewardBox.setValue(existing.getKeyReward());
                activeCheckBox.setSelected(existing.isActive());

                guideTitleField.setText(existing.getGuideTitle() != null ? existing.getGuideTitle() : "");
                guideNarrativeArea.setText(existing.getGuideNarrative() != null ? existing.getGuideNarrative() : "");
                guideNextStepArea.setText(existing.getGuideNextStep() != null ? existing.getGuideNextStep() : "");
            } else {
                idField.setText("room_" + (allRooms.size() + 1));
                activeCheckBox.setSelected(true);
                keyRewardBox.setValue("key");
                guideTitleField.setText("NEW CHAMBER PURIFIED");
                guideNarrativeArea.setText("The ancient spirits of this chamber rest peacefully at last.");
                guideNextStepArea.setText("WHAT TO DO NEXT & WHERE TO GO:\n• Door Key acquired! Step out and explore the next chamber.");
            }

            pane.setContent(grid);

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    String desc = descArea.getText().trim();
                    int req = reqQsSpinner.getValue();
                    String next = nextRoomBox.getValue();
                    String keyRew = keyRewardBox.getValue();
                    String gTitle = guideTitleField.getText().trim();
                    String gNarr = guideNarrativeArea.getText().trim();
                    String gNext = guideNextStepArea.getText().trim();

                    if (id.isEmpty() || name.isEmpty()) {
                        showFormError("Validation Error", "Room ID and Name cannot be empty.");
                        return null;
                    }

                    if (existing == null && allRooms.stream().anyMatch(r -> r.getId().equalsIgnoreCase(id))) {
                        showFormError("Duplicate ID", "A room with ID '" + id + "' already exists!");
                        return null;
                    }

                    return new RoomModel(
                            id, name, desc, req, keyRew,
                            "None (Final Room)".equalsIgnoreCase(next) ? "" : next,
                            activeCheckBox.isSelected(),
                            gTitle, gNarr, gNext, keyRew
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

        private static Label createFormLabel(String text) {
            Label lbl = new Label(text);
            lbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
            return lbl;
        }
    }
}
