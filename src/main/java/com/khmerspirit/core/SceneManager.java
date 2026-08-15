package com.khmerspirit.core;

import com.khmerspirit.config.Constants;
import com.khmerspirit.scene.CharacterScene;
import com.khmerspirit.scene.GameScene;
import com.khmerspirit.scene.MainMenuScene;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public final class SceneManager {

    private static Stage stage;

    private SceneManager() {
    }

    public static void initialize(Stage primaryStage) {
        stage = Objects.requireNonNull(primaryStage, "primaryStage must not be null");
    }

    public static void showMainMenu() {
        setScene(new MainMenuScene().createScene());
    }

    public static void showCharacterSelection() {
        setScene(new CharacterScene().createScene());
    }

    public static void showGame(String selectedCharacter) {
        setScene(new GameScene(selectedCharacter).createScene());
    }

    public static void showGameWithSave(com.khmerspirit.save.SaveData saveData) {
        // When continuing, use the character from save and restore
        setScene(new com.khmerspirit.scene.GameScene(saveData.getCharacterName(), saveData).createScene());
    }

    public static void showTeacherAdmin() {
        setScene(new com.khmerspirit.scene.TeacherAdminScene().createScene());
    }

    public static void exitGame() {
        requireStage().close();
    }

    private static void setScene(Scene scene) {
        Scene preparedScene = Objects.requireNonNull(scene, "scene must not be null");
        preparedScene.getStylesheets().add(Constants.MAIN_STYLESHEET);
        requireStage().setScene(preparedScene);
    }

    private static Stage requireStage() {
        if (stage == null) {
            throw new IllegalStateException("SceneManager has not been initialized.");
        }
        return stage;
    }
}
