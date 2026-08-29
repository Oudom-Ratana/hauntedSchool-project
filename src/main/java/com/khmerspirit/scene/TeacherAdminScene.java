package com.khmerspirit.scene;

import com.khmerspirit.admin.view.AdminLoginView;
import com.khmerspirit.admin.view.AdminMainView;
import com.khmerspirit.config.Constants;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

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
            AdminLoginView loginView = new AdminLoginView(() -> {
                loggedIn = true;
                showAppropriateView();
            });
            rootContainer.getChildren().add(loginView);
        } else {
            AdminMainView mainView = new AdminMainView(() -> {
                loggedIn = false;
                showAppropriateView();
            });
            rootContainer.getChildren().add(mainView);
        }
    }
}
