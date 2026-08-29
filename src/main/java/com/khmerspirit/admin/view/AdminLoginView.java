package com.khmerspirit.admin.view;

import com.khmerspirit.admin.service.AdminFileService;
import com.khmerspirit.core.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Admin Panel Login View styled with sleek dark gothic horror aesthetics,
 * obsidian glass paneling, subtle crimson glows, and modern typography.
 */
public class AdminLoginView extends StackPane {

    private final AdminFileService adminFileService = new AdminFileService();
    private final Runnable onLoginSuccess;

    public AdminLoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setPrefSize(1280, 720);

        getStyleClass().add("admin-root-bg");

        // Dark fog overlay
        Region fogOverlay = new Region();
        fogOverlay.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(5, 7, 12, 0.88), rgba(12, 16, 26, 0.95));");
        fogOverlay.prefWidthProperty().bind(widthProperty());
        fogOverlay.prefHeightProperty().bind(heightProperty());

        getChildren().addAll(fogOverlay, buildLoginCard());
    }

    private VBox buildLoginCard() {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(42, 36, 42, 36));
        card.setMaxWidth(440);
        card.getStyleClass().add("admin-stat-card");
        card.setStyle("-fx-background-color: rgba(10, 14, 22, 0.94); -fx-border-color: rgba(225, 29, 72, 0.35); -fx-border-width: 1px; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.85), 32, 0.5, 0, 8);");

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);

        Label title = new Label("TEACHER ADMIN");
        title.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 2px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.6), 12, 0.3, 0, 0);");

        Label subtitle = new Label("KHMER SPIRIT • HAUNTED SCHOOL LOGIC ENGINE");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #e11d48; -fx-letter-spacing: 1.5px; -fx-alignment: center;");

        titleBox.getChildren().addAll(title, subtitle);

        VBox userBox = new VBox(6);
        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
        TextField userField = new TextField();
        userField.setPromptText("Enter Username (e.g. admin)");
        userField.setPrefHeight(44);
        userField.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-font-size: 14px; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 0 12 0 12;");
        userBox.getChildren().addAll(userLabel, userField);

        VBox passBox = new VBox(6);
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter Password (e.g. admin123)");
        passField.setPrefHeight(44);
        passField.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-font-size: 14px; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 0 12 0 12;");
        passBox.getChildren().addAll(passLabel, passField);

        Label statusMsg = new Label();
        statusMsg.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center;");
        statusMsg.setVisible(false);

        Button loginBtn = new Button("ENTER ADMIN SYSTEM");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(46);
        loginBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #be123c, #881337); -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-font-size: 14px; -fx-border-color: #f43f5e; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 14, 0.35, 0, 0);");

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setPrefHeight(42);
        backBtn.setStyle("-fx-background-color: rgba(15, 23, 42, 0.6); -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;");
        backBtn.setOnAction(e -> SceneManager.showMainMenu());

        Runnable handleLoginAction = () -> {
            String u = userField.getText() != null ? userField.getText().trim() : "";
            String p = passField.getText() != null ? passField.getText().trim() : "";

            if (adminFileService.authenticate(u, p)) {
                statusMsg.setVisible(false);
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } else {
                statusMsg.setText("Access Denied! Incorrect username or password.");
                statusMsg.setVisible(true);
                showLoginErrorAlert();
            }
        };

        loginBtn.setOnAction(e -> handleLoginAction.run());
        passField.setOnAction(e -> handleLoginAction.run());
        userField.setOnAction(e -> handleLoginAction.run());

        Label hintLbl = new Label("Credentials stored in config/admin.json\nDefault fallback: admin / admin123");
        hintLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-text-alignment: center; -fx-alignment: center;");

        card.getChildren().addAll(
                titleBox,
                userBox, passBox,
                statusMsg,
                loginBtn, backBtn,
                hintLbl
        );

        return card;
    }

    private void showLoginErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Invalid Administrative Credentials");
        alert.setContentText("Check your credentials stored in config/admin.json.\nDefault prototype credentials: admin / admin123");
        alert.showAndWait();
    }
}
