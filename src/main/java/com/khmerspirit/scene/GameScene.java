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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

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

        playArea.widthProperty().addListener((obs, oldV, newV) -> {
            double w = newV.doubleValue();
            if (w > 200 && game != null) {
                canvas.setWidth(w);
                game.onResize(w, canvas.getHeight());
            }
        });
        playArea.heightProperty().addListener((obs, oldV, newV) -> {
            double h = newV.doubleValue();
            if (h > 200 && game != null) {
                canvas.setHeight(h);
                game.onResize(canvas.getWidth(), h);
            }
        });

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
        audio.playGameMusic();
        audio.playLoop("rain");
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
        javafx.application.Platform.runLater(() -> {
            if (hudTimer != null) {
                hudTimer.stop();
            }

            AudioManager.getInstance().playOneShot("ghost");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.42);");
            overlay.setPickOnBounds(true);

            StackPane panelContainer = new StackPane();
            panelContainer.setMaxSize(580, 330);
            panelContainer.setPickOnBounds(false);

            try {
                var stream = getClass().getResourceAsStream("/images/ui/game_over_panel.png");
                if (stream != null) {
                    javafx.scene.image.ImageView bgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(stream));
                    bgView.setFitWidth(580);
                    bgView.setFitHeight(330);
                    bgView.setPreserveRatio(false);
                    bgView.setMouseTransparent(true);
                    bgView.setEffect(new javafx.scene.effect.DropShadow(24, javafx.scene.paint.Color.rgb(180, 10, 10, 0.65)));
                    panelContainer.getChildren().add(bgView);
                }
            } catch (Exception ignored) {}

            Button retryButton = createImageButton(
                    "/images/ui/btn_gameover_retry.png",
                    "/images/ui/btn_gameover_retry_hover.png",
                    "RETRY",
                    () -> {
                        if (hudTimer != null) hudTimer.stop();
                        game.stop();
                        new com.khmerspirit.save.SaveManager().deleteSave();
                        SceneManager.showGame(selectedCharacter);
                    }
            );

            Button menuButton = createImageButton(
                    "/images/ui/btn_gameover_main_menu.png",
                    "/images/ui/btn_gameover_main_menu_hover.png",
                    "MAIN MENU",
                    () -> {
                        if (hudTimer != null) hudTimer.stop();
                        game.stop();
                        new com.khmerspirit.save.SaveManager().deleteSave();
                        SceneManager.showMainMenu();
                    }
            );

            HBox actions = new HBox(28, retryButton, menuButton);
            actions.setAlignment(Pos.CENTER);
            actions.setPickOnBounds(false);
            actions.setTranslateY(24);

            panelContainer.getChildren().add(actions);
            overlay.getChildren().add(panelContainer);
            playArea.getChildren().add(overlay);
            retryButton.requestFocus();
        });
    }

    private Button createImageButton(String normalRes, String hoverRes, String fallbackText, Runnable action) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand; -fx-border-width: 0;");
        button.setPickOnBounds(true);

        Image normalImg = null;
        Image hoverImg = null;
        try {
            var nStream = getClass().getResourceAsStream(normalRes);
            if (nStream != null) normalImg = new Image(nStream);
            var hStream = getClass().getResourceAsStream(hoverRes);
            if (hStream != null) hoverImg = new Image(hStream);
        } catch (Exception ignored) {}

        if (normalImg != null) {
            ImageView iv = new ImageView(normalImg);
            iv.setFitWidth(200);
            iv.setFitHeight(46);
            iv.setPreserveRatio(false);
            button.setGraphic(iv);

            final Image fNormal = normalImg;
            final Image fHover = hoverImg != null ? hoverImg : normalImg;

            button.setOnMouseEntered(e -> {
                iv.setImage(fHover);
                button.setEffect(new DropShadow(18, Color.rgb(255, 60, 60, 0.85)));
            });
            button.setOnMouseExited(e -> {
                iv.setImage(fNormal);
                button.setEffect(null);
            });
        } else {
            button.setText(fallbackText);
            button.setStyle("-fx-background-color: #5c1111; -fx-text-fill: #ffeaa7; -fx-padding: 10px 24px; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        button.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });

        return button;
    }
}
