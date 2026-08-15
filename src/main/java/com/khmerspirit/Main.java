package com.khmerspirit;

import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.initialize(primaryStage);
        primaryStage.setTitle(Constants.GAME_TITLE);
        primaryStage.setMinWidth(Constants.MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(Constants.MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(false);
        SceneManager.showMainMenu();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
