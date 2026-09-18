package com.khmerspirit.scene;

import com.khmerspirit.admin.view.AdminLoginView;
import com.khmerspirit.admin.view.AdminMainView;
import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Scene wrapper integrating the Admin Panel (Login & Dashboard) into SceneManager.
 */
public class TeacherAdminScene {

    private boolean loggedIn = false;
    private StackPane rootContainer;

    public Scene createScene() {
        rootContainer = new StackPane();
        rootContainer.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        rootContainer.getStyleClass().add("screen-root");

        showAppropriateView();

        return new Scene(rootContainer, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private void showAppropriateView() {
        rootContainer.getChildren().clear();

        if (!loggedIn) {
            try {
                AdminLoginView loginView = new AdminLoginView(() -> {
                    loggedIn = true;
                    showAppropriateView();
                });
                rootContainer.getChildren().add(loginView);
            } catch (Throwable t) {
                t.printStackTrace();
                showErrorFallback(t);
            }
        } else {
            try {
                AdminMainView mainView = new AdminMainView(() -> {
                    loggedIn = false;
                    showAppropriateView();
                });
                rootContainer.getChildren().add(mainView);
            } catch (Throwable t) {
                t.printStackTrace();
                showErrorFallback(t);
            }
        }
    }

    private void showErrorFallback(Throwable t) {
        rootContainer.getChildren().clear();

        VBox card = new VBox(14);
        card.setMaxWidth(680);
        card.setMaxHeight(440);
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(15, 23, 42, 0.96); -fx-border-color: #ef4444; -fx-border-width: 2px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 20, 0.5, 0, 0);");

        Label title = new Label("⚠️ Admin Panel Error");
        title.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f87171;");

        Label desc = new Label("An unexpected error occurred while loading the admin interface:");
        desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        TextArea stackArea = new TextArea(sw.toString());
        stackArea.setEditable(false);
        stackArea.setWrapText(true);
        stackArea.setPrefRowCount(8);
        stackArea.setStyle("-fx-control-inner-background: #0b1120; -fx-text-fill: #fca5a5; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button btnBackLogin = new Button("Return to Login");
        btnBackLogin.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnBackLogin.setOnAction(e -> {
            loggedIn = false;
            showAppropriateView();
        });

        Button btnMainMenu = new Button("Main Menu");
        btnMainMenu.setStyle("-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnMainMenu.setOnAction(e -> SceneManager.showMainMenu());

        btnRow.getChildren().addAll(btnBackLogin, btnMainMenu);

        card.getChildren().addAll(title, desc, stackArea, btnRow);
        rootContainer.getChildren().add(card);
    }
}
