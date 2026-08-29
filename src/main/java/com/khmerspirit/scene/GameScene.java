package com.khmerspirit.scene;

import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.config.Constants;
import com.khmerspirit.core.Game;
import com.khmerspirit.core.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameScene {

    private final String selectedCharacter;
    private final com.khmerspirit.save.SaveData saveData;
    private Game game;
    private Label heartsLabel;
    private Label roomLabel;
    private Label inventoryLabel;
    private AnimationTimer hudTimer;

    public GameScene(String selectedCharacter) {
        this(selectedCharacter, null);
    }

    public GameScene(String selectedCharacter, com.khmerspirit.save.SaveData saveData) {
        this.selectedCharacter = selectedCharacter;
        this.saveData = saveData;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("game-root");

        HBox hud = createHud();
        Canvas canvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - 96);
        StackPane playArea = new StackPane(canvas);
        if (saveData == null) {
            game = new Game(canvas, selectedCharacter);
        } else {
            game = new Game(canvas, selectedCharacter, saveData);
        }
        game.setGameOverHandler(() -> showGameOverOverlay(playArea));

        Button backButton = new Button("MENU");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(event -> {
            if (hudTimer != null) hudTimer.stop();
            game.stop();
            AudioManager.getInstance().stopAll();
            SceneManager.showMainMenu();
        });

        HBox footer = new HBox(backButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(6, 28, 8, 28));

        root.setTop(hud);
        root.setCenter(playArea);
        root.setBottom(footer);

        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.setOnKeyPressed(event -> game.getPlayerController().press(event.getCode()));
        scene.setOnKeyReleased(event -> game.getPlayerController().release(event.getCode()));
        AudioManager audio = AudioManager.getInstance();
        audio.stopAll();
        audio.playLoop("rain");
        audio.playLoop("wind");
        audio.playLoop("ambience");
        game.start();
        startHudUpdates();
        return scene;
    }

    private HBox createHud() {
        HBox hud = new HBox(16);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(10, 28, 8, 28));
        hud.getStyleClass().add("hud-bar");

        VBox titleBox = new VBox(2);
        Label title = new Label("HAUNTED SCHOOL");
        title.getStyleClass().add("hud-title");
        Label character = new Label("Character: " + selectedCharacter);
        character.getStyleClass().add("hud-text");
        titleBox.getChildren().addAll(title, character);

        heartsLabel = new Label("♥ 5");
        heartsLabel.getStyleClass().addAll("hud-card", "stat-pill");

        roomLabel = new Label("Room: Entrance");
        roomLabel.getStyleClass().addAll("hud-card", "stat-pill");

        inventoryLabel = new Label("Inv: none");
        inventoryLabel.getStyleClass().addAll("hud-card", "inventory-chip");

        hud.getChildren().addAll(titleBox, heartsLabel, roomLabel, inventoryLabel);
        return hud;
    }

    private void startHudUpdates() {
        hudTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (game == null) return;
                heartsLabel.setText("♥ " + game.getPlayer().getHearts());
                roomLabel.setText("Room: " + game.getCurrentRoomName());
                StringBuilder inventoryText = new StringBuilder("Inv:");
                game.getInventory().getItemCounts().forEach((id, count) -> {
                    if (inventoryText.length() > 4) inventoryText.append(", ");
                    inventoryText.append(id).append("x").append(count);
                });
                inventoryLabel.setText(inventoryText.length() > 4 ? inventoryText.toString() : "Inv: none");
            }
        };
        hudTimer.start();
    }

    private void showGameOverOverlay(StackPane playArea) {
        if (hudTimer != null) {
            hudTimer.stop();
        }

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("game-over-overlay");

        VBox panel = new VBox(14);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("game-over-panel");

        Label title = new Label("GAME OVER");
        title.getStyleClass().add("game-over-title");

        Label message = new Label("The school has taken your last heart.");
        message.getStyleClass().add("game-over-message");

        Button retryButton = new Button("PLAY AGAIN");
        retryButton.getStyleClass().add("primary-button");
        retryButton.setOnAction(event -> {
            game.stop();
            new com.khmerspirit.save.SaveManager().deleteSave();
            SceneManager.showGame(selectedCharacter);
        });

        Button menuButton = new Button("MAIN MENU");
        menuButton.getStyleClass().add("secondary-button");
        menuButton.setOnAction(event -> {
            game.stop();
            new com.khmerspirit.save.SaveManager().deleteSave();
            SceneManager.showMainMenu();
        });

        HBox actions = new HBox(12, retryButton, menuButton);
        actions.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, message, actions);
        overlay.getChildren().add(panel);
        playArea.getChildren().add(overlay);
    }
}
