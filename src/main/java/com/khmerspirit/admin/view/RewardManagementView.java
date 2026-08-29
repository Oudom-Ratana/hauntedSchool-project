package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.RewardModel;
import com.khmerspirit.admin.service.RewardFileService;

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
 * Reward Management View featuring TableView, CRUD operations, and item quantity configuration.
 */
public class RewardManagementView extends VBox {

    private final RewardFileService rewardFileService = new RewardFileService();

    private final TableView<RewardModel> tableView = new TableView<>();
    private final ObservableList<RewardModel> rewardList = FXCollections.observableArrayList();

    public RewardManagementView() {
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
        Label title = new Label("REWARD MANAGEMENT SYSTEM");
        title.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 10, 0.3, 0, 0);");
        Label subtitle = new Label("Configure items, keys, holy charms, batteries, and health items awarded to players upon room completion");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildToolbar() {
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(14));
        actionRow.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-background-radius: 8px;");

        Button addBtn = new Button("+ ADD REWARD");
        addBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #35572F, #1E351C); -fx-text-fill: #E6D3A7; -fx-font-weight: bold; -fx-border-color: #76A14D; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("✏ EDIT REWARD");
        editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #7A6135, #4A3A1F); -fx-text-fill: #F0DFB7; -fx-font-weight: bold; -fx-border-color: #D4AF37; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditDialog());

        Button deleteBtn = new Button("🗑 DELETE REWARD");
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

        TableColumn<RewardModel, String> idCol = new TableColumn<>("Reward ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);

        TableColumn<RewardModel, String> nameCol = new TableColumn<>("Reward Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<RewardModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(140);

        TableColumn<RewardModel, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(350);

        TableColumn<RewardModel, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nameCol, typeCol, descCol, qtyCol);
        getChildren().add(tableView);
    }

    public void loadData() {
        List<RewardModel> rewards = rewardFileService.loadRewards();
        rewardList.setAll(rewards);
        tableView.setItems(rewardList);
    }

    private void showAddDialog() {
        RewardFormDialog dialog = new RewardFormDialog(null, rewardList);
        Optional<RewardModel> result = dialog.showAndWait();
        result.ifPresent(r -> {
            rewardList.add(r);
            rewardFileService.saveRewards(rewardList);
            loadData();
            showInfo("Success", "Reward added successfully!");
        });
    }

    private void showEditDialog() {
        RewardModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a reward from the table to edit.");
            return;
        }
        RewardFormDialog dialog = new RewardFormDialog(selected, rewardList);
        Optional<RewardModel> result = dialog.showAndWait();
        result.ifPresent(r -> {
            int idx = rewardList.indexOf(selected);
            if (idx >= 0) {
                rewardList.set(idx, r);
            }
            rewardFileService.saveRewards(rewardList);
            loadData();
            showInfo("Success", "Reward updated successfully!");
        });
    }

    private void handleDelete() {
        RewardModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a reward from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Reward " + selected.getName() + " (" + selected.getId() + ")?");
        confirm.setContentText("Are you sure you want to remove this reward?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            rewardList.remove(selected);
            rewardFileService.saveRewards(rewardList);
            loadData();
            showInfo("Deleted", "Reward has been removed successfully.");
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

    // Dialog for Reward Creation & Editing
    private static class RewardFormDialog extends Dialog<RewardModel> {

        private final TextField idField = new TextField();
        private final TextField nameField = new TextField();
        private final ComboBox<String> typeBox = new ComboBox<>();
        private final TextArea descArea = new TextArea();
        private final Spinner<Integer> qtySpinner = new Spinner<>(1, 999, 1);

        public RewardFormDialog(RewardModel existing, List<RewardModel> allRewards) {
            setTitle(existing == null ? "Add New Reward" : "Edit Reward " + existing.getId());
            setHeaderText(existing == null ? "Fill in reward details and quantity." : "Modify reward details.");

            DialogPane pane = getDialogPane();
            pane.setStyle("-fx-background-color: #0E1622; -fx-text-fill: #E8D2A0;");
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(16));

            typeBox.getItems().addAll("Item", "Key", "Battery", "Health", "Buff", "Currency", "Tool");
            typeBox.getSelectionModel().selectFirst();

            descArea.setPrefRowCount(3);

            grid.add(new Label("Reward ID:"), 0, 0); grid.add(idField, 1, 0);
            grid.add(new Label("Reward Name:"), 0, 1); grid.add(nameField, 1, 1);
            grid.add(new Label("Type:"), 0, 2); grid.add(typeBox, 1, 2);
            grid.add(new Label("Description:"), 0, 3); grid.add(descArea, 1, 3);
            grid.add(new Label("Quantity:"), 0, 4); grid.add(qtySpinner, 1, 4);

            if (existing != null) {
                idField.setText(existing.getId());
                idField.setDisable(true);
                nameField.setText(existing.getName());
                if (existing.getType() != null) typeBox.setValue(existing.getType());
                descArea.setText(existing.getDescription());
                qtySpinner.getValueFactory().setValue(existing.getQuantity());
            } else {
                idField.setText("RWD_" + String.format("%03d", allRewards.size() + 1));
            }

            pane.setContent(grid);

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    String type = typeBox.getValue();
                    String desc = descArea.getText().trim();
                    int qty = qtySpinner.getValue();

                    if (id.isEmpty() || name.isEmpty() || type == null || desc.isEmpty()) {
                        showFormError("Validation Error", "Reward ID, Name, Type, and Description cannot be empty.");
                        return null;
                    }

                    if (existing == null && allRewards.stream().anyMatch(r -> r.getId().equalsIgnoreCase(id))) {
                        showFormError("Duplicate ID", "A reward with ID '" + id + "' already exists!");
                        return null;
                    }

                    return new RewardModel(id, name, type, desc, qty);
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
