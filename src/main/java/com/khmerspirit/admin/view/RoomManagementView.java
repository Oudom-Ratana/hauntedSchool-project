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
 * Room Management View featuring TableView, CRUD operations, and configuration for required questions and rewards.
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
        VBox header = new VBox(4);
        Label title = new Label("ROOM MANAGEMENT SYSTEM");
        title.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 10, 0.3, 0, 0);");
        Label subtitle = new Label("Configure haunted map locations, required quiz counts, room progression, and unlocked rewards");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildToolbar() {
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(14));
        actionRow.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-background-radius: 8px;");

        Button addBtn = new Button("+ ADD ROOM");
        addBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #35572F, #1E351C); -fx-text-fill: #E6D3A7; -fx-font-weight: bold; -fx-border-color: #76A14D; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("✏ EDIT ROOM");
        editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #7A6135, #4A3A1F); -fx-text-fill: #F0DFB7; -fx-font-weight: bold; -fx-border-color: #D4AF37; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditDialog());

        Button deleteBtn = new Button("🗑 DELETE ROOM");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #7F2020, #4A1212); -fx-text-fill: #FFB3B3; -fx-font-weight: bold; -fx-border-color: #FF4D4D; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button refreshBtn = new Button("🔄 REFRESH");
        refreshBtn.setStyle("-fx-background-color: #2E3E50; -fx-text-fill: #C9D6DF; -fx-font-weight: bold; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadData());

        actionRow.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);
        getChildren().add(actionRow);
    }

    @SuppressWarnings("unchecked")
    private void buildTableView() {
        tableView.setStyle("-fx-background-color: rgba(12, 18, 26, 0.95); -fx-border-color: #2E3E50; -fx-border-width: 1px;");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<RoomModel, String> idCol = new TableColumn<>("Room ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);

        TableColumn<RoomModel, String> nameCol = new TableColumn<>("Room Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<RoomModel, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(260);

        TableColumn<RoomModel, Integer> reqCol = new TableColumn<>("Required Qs");
        reqCol.setCellValueFactory(new PropertyValueFactory<>("requiredQs"));
        reqCol.setPrefWidth(110);

        TableColumn<RoomModel, String> rewardCol = new TableColumn<>("Reward");
        rewardCol.setCellValueFactory(new PropertyValueFactory<>("reward"));
        rewardCol.setPrefWidth(150);

        TableColumn<RoomModel, String> nextCol = new TableColumn<>("Next Room");
        nextCol.setCellValueFactory(new PropertyValueFactory<>("nextRoomId"));
        nextCol.setPrefWidth(120);

        TableColumn<RoomModel, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(80);

        tableView.getColumns().addAll(idCol, nameCol, descCol, reqCol, rewardCol, nextCol, activeCol);
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
            showInfo("Success", "Room updated successfully!");
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

    // Dialog for Room Creation & Editing
    private static class RoomFormDialog extends Dialog<RoomModel> {

        private final TextField idField = new TextField();
        private final TextField nameField = new TextField();
        private final TextArea descArea = new TextArea();
        private final Spinner<Integer> reqQsSpinner = new Spinner<>(1, 20, 5);
        private final ComboBox<String> rewardBox = new ComboBox<>();
        private final ComboBox<String> nextRoomBox = new ComboBox<>();
        private final CheckBox activeCheckBox = new CheckBox("Active");

        public RoomFormDialog(RoomModel existing, List<RoomModel> allRooms, List<RewardModel> rewards) {
            setTitle(existing == null ? "Add New Room" : "Edit Room " + existing.getId());
            setHeaderText(existing == null ? "Configure room properties and requirements." : "Modify room properties.");

            DialogPane pane = getDialogPane();
            pane.setStyle("-fx-background-color: #0E1622; -fx-text-fill: #E8D2A0;");
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(16));

            // Populate rewards
            for (RewardModel r : rewards) {
                rewardBox.getItems().add(r.getName());
            }
            if (rewardBox.getItems().isEmpty()) {
                rewardBox.getItems().addAll("High-Power Flashlight", "Extra Batteries", "Ancient Holy Charm", "Master Key");
            }
            rewardBox.getSelectionModel().selectFirst();

            // Populate next room candidates
            nextRoomBox.getItems().add("None (Final Room)");
            for (RoomModel r : allRooms) {
                if (existing == null || !r.getId().equalsIgnoreCase(existing.getId())) {
                    nextRoomBox.getItems().add(r.getId());
                }
            }
            nextRoomBox.getSelectionModel().selectFirst();

            descArea.setPrefRowCount(3);

            grid.add(new Label("Room ID:"), 0, 0); grid.add(idField, 1, 0);
            grid.add(new Label("Room Name:"), 0, 1); grid.add(nameField, 1, 1);
            grid.add(new Label("Description:"), 0, 2); grid.add(descArea, 1, 2);
            grid.add(new Label("Required Qs (Default: 5):"), 0, 3); grid.add(reqQsSpinner, 1, 3);
            grid.add(new Label("Reward:"), 0, 4); grid.add(rewardBox, 1, 4);
            grid.add(new Label("Next Room ID:"), 0, 5); grid.add(nextRoomBox, 1, 5);
            grid.add(new Label("Active Status:"), 0, 6); grid.add(activeCheckBox, 1, 6);

            if (existing != null) {
                idField.setText(existing.getId());
                idField.setDisable(true);
                nameField.setText(existing.getName());
                descArea.setText(existing.getDescription());
                reqQsSpinner.getValueFactory().setValue(existing.getRequiredQs());
                if (existing.getReward() != null) rewardBox.setValue(existing.getReward());
                if (existing.getNextRoomId() != null) nextRoomBox.setValue(existing.getNextRoomId());
                activeCheckBox.setSelected(existing.isActive());
            } else {
                idField.setText("room_" + (allRooms.size() + 1));
                activeCheckBox.setSelected(true);
            }

            pane.setContent(grid);

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    String desc = descArea.getText().trim();
                    int req = reqQsSpinner.getValue();
                    String reward = rewardBox.getValue();
                    String next = nextRoomBox.getValue();

                    if (id.isEmpty() || name.isEmpty() || desc.isEmpty()) {
                        showFormError("Validation Error", "Room ID, Name, and Description cannot be empty.");
                        return null;
                    }

                    if (existing == null && allRooms.stream().anyMatch(r -> r.getId().equalsIgnoreCase(id))) {
                        showFormError("Duplicate ID", "A room with ID '" + id + "' already exists!");
                        return null;
                    }

                    return new RoomModel(id, name, desc, req, reward, "None (Final Room)".equalsIgnoreCase(next) ? "" : next, activeCheckBox.isSelected());
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
