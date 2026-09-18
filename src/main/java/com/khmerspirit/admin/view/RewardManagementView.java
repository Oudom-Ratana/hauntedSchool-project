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
        VBox header = new VBox(2);
        Label title = new Label("💾  Save Data & Relic Vault");
        title.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Configure mystical artifacts, chamber keys, charms, batteries, and health items awarded to players");
        subtitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);
    }

    private void buildToolbar() {
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(12, 14, 12, 14));
        actionRow.getStyleClass().add("admin-card-container");

        Button addBtn = new Button("➕  Add Reward");
        addBtn.getStyleClass().add("btn-modern-primary");
        addBtn.setOnAction(e -> showAddDialog());

        Button editBtn = new Button("✏️  Edit Reward");
        editBtn.getStyleClass().add("btn-modern-secondary");
        editBtn.setOnAction(e -> showEditDialog());

        Button deleteBtn = new Button("🗑️  Delete Reward");
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
            setTitle(existing == null ? "Add New Reward" : "Edit Reward: " + existing.getId());
            setHeaderText(existing == null ? "Configure reward relic details, item type, and drop quantity." : "Modify reward details and quantity.");

            DialogPane pane = getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setPrefWidth(480);

            // Modern input styling
            idField.getStyleClass().add("modern-form-input");
            nameField.getStyleClass().add("modern-form-input");
            typeBox.getStyleClass().add("modern-form-input");
            descArea.getStyleClass().add("modern-form-input");
            qtySpinner.getStyleClass().add("modern-form-input");

            GridPane grid = new GridPane();
            grid.setHgap(14);
            grid.setVgap(12);
            grid.setPadding(new Insets(18));

            typeBox.getItems().addAll("Item", "Key", "Battery", "Health", "Buff", "Currency", "Tool");
            typeBox.getSelectionModel().selectFirst();
            typeBox.setMaxWidth(Double.MAX_VALUE);

            descArea.setPrefRowCount(3);

            grid.add(createFormLabel("Reward ID:"), 0, 0); grid.add(idField, 1, 0);
            grid.add(createFormLabel("Reward Name:"), 0, 1); grid.add(nameField, 1, 1);
            grid.add(createFormLabel("Item Type:"), 0, 2); grid.add(typeBox, 1, 2);
            grid.add(createFormLabel("Description:"), 0, 3); grid.add(descArea, 1, 3);
            grid.add(createFormLabel("Quantity:"), 0, 4); grid.add(qtySpinner, 1, 4);

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

        private static Label createFormLabel(String text) {
            Label lbl = new Label(text);
            lbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
            return lbl;
        }
    }
}
