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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class GameScene {

    private final String selectedCharacter;
    private final com.khmerspirit.save.SaveData saveData;
    private Game game;
    private Label heartsLabel;
    private Label roomLabel;
    private Label inventoryLabel;
    private AnimationTimer hudTimer;
    private StackPane pauseOverlay;
    private boolean isGameOver = false;
    private boolean isVictory = false;

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

        Canvas canvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - 96);
        StackPane playArea = new StackPane(canvas);
        if (saveData == null) {
            game = new Game(canvas, selectedCharacter);
        } else {
            game = new Game(canvas, selectedCharacter, saveData);
        }
        game.setGameOverHandler(() -> showGameOverOverlay(playArea));
        game.setVictoryHandler(() -> showVictoryOverlay(playArea));

        HBox hud = createHud(playArea);

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

        root.setTop(hud);
        root.setCenter(playArea);
        // Note: No bottom footer so UI is 100% visible on small / windowed displays

        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        // Event filter captures Arrow keys, WASD, and hotkeys before any control focus traversal
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.ESCAPE || code == KeyCode.P) {
                if (!isGameOver && !isVictory) {
                    togglePause(playArea);
                }
                event.consume();
                return;
            }
            if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                if (game != null && !game.isPaused() && !isGameOver && !isVictory) {
                    game.getPlayerController().press(code);
                }
                event.consume(); // Prevent JavaFX focus traversal
                return;
            }
            if (game != null && !game.isPaused() && !isGameOver && !isVictory) {
                game.getPlayerController().press(code);
            }
        });

        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                if (game != null && !game.isPaused() && !isGameOver && !isVictory) {
                    game.getPlayerController().release(code);
                }
                event.consume();
                return;
            }
            if (game != null && !game.isPaused() && !isGameOver && !isVictory) {
                game.getPlayerController().release(code);
            }
        });

        AudioManager audio = AudioManager.getInstance();
        audio.stopAll();
        audio.playGameMusic();
        audio.playLoop("rain");
        audio.playStartGame();
        game.start();
        startHudUpdates();
        return scene;
    }

    private HBox createHud(StackPane playArea) {
        HBox hud = new HBox(14);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(8, 20, 8, 20));
        hud.getStyleClass().add("hud-bar");

        VBox titleBox = new VBox(2);
        Label title = new Label("HAUNTED SCHOOL");
        title.getStyleClass().add("hud-title");
        Label character = new Label("Character: " + selectedCharacter);
        character.getStyleClass().add("hud-text");
        titleBox.getChildren().addAll(title, character);

        heartsLabel = new Label("♥ 5");
        heartsLabel.getStyleClass().addAll("hud-card", "stat-pill");
        heartsLabel.setStyle("-fx-background-color: rgba(180, 20, 30, 0.3); -fx-border-color: #e74c3c; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4 10; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        roomLabel = new Label("Room: Entrance");
        roomLabel.getStyleClass().addAll("hud-card", "stat-pill");
        roomLabel.setStyle("-fx-background-color: rgba(41, 128, 185, 0.25); -fx-border-color: #3498db; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4 10; -fx-text-fill: #85c1e9; -fx-font-weight: bold;");

        inventoryLabel = new Label("Inv: none");
        inventoryLabel.getStyleClass().addAll("hud-card", "inventory-chip");
        inventoryLabel.setStyle("-fx-background-color: rgba(243, 156, 18, 0.2); -fx-border-color: #f39c12; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4 10; -fx-text-fill: #f9e79f; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button pauseButton = new Button("⏸ PAUSE");
        pauseButton.setFocusTraversable(false);
        pauseButton.getStyleClass().add("secondary-button");
        pauseButton.setStyle("-fx-background-color: rgba(212, 175, 55, 0.18); -fx-border-color: #d4af37; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-cursor: hand;");
        pauseButton.setOnAction(event -> togglePause(playArea));

        Button homeButton = new Button("🏠 MENU");
        homeButton.setFocusTraversable(false);
        homeButton.getStyleClass().add("secondary-button");
        homeButton.setStyle("-fx-background-color: rgba(192, 57, 43, 0.28); -fx-border-color: #c0392b; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-cursor: hand;");
        homeButton.setOnAction(event -> returnToMainMenu());

        hud.getChildren().addAll(titleBox, heartsLabel, roomLabel, inventoryLabel, spacer, pauseButton, homeButton);
        return hud;
    }

    private void returnToMainMenu() {
        if (hudTimer != null) hudTimer.stop();
        if (game != null) {
            game.saveNow();
            game.stop();
        }
        AudioManager.getInstance().stopAll();
        SceneManager.showMainMenu();
    }

    private void togglePause(StackPane playArea) {
        if (isGameOver) return;
        if (pauseOverlay != null) {
            resumeGame(playArea);
        } else {
            pauseGame(playArea);
        }
    }

    private void pauseGame(StackPane playArea) {
        if (isGameOver || game == null || pauseOverlay != null) return;
        game.pause();

        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(5, 5, 10, 0.75);");
        pauseOverlay.setPickOnBounds(true);

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(440, 420);
        card.setPadding(new Insets(26, 32, 26, 32));
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #1d182b, #0f0c18); "
                + "-fx-border-color: #d4af37; -fx-border-width: 2px; -fx-border-radius: 12px; "
                + "-fx-background-radius: 12px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.9), 24, 0.4, 0, 6);");

        Label title = new Label("✦ GAME PAUSED ✦");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #f1c40f; "
                + "-fx-effect: dropshadow(gaussian, rgba(241, 196, 15, 0.5), 10, 0.3, 0, 0);");

        Label subtitle = new Label("Take a breath... the spirits are waiting.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #c9b994;");

        Label statusMsg = new Label("");
        statusMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        Button resumeBtn = new Button("▶  RESUME GAME  [ESC]");
        resumeBtn.setMaxWidth(Double.MAX_VALUE);
        resumeBtn.setStyle("-fx-background-color: #1e824c; -fx-text-fill: #ffffff; -fx-font-size: 14px; "
                + "-fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
        resumeBtn.setOnAction(e -> resumeGame(playArea));

        Button saveBtn = new Button("💾  SAVE PROGRESS");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #3498db; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; "
                + "-fx-font-weight: bold; -fx-padding: 9 20; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            game.saveNow();
            statusMsg.setText("✓ Progress saved successfully!");
            AudioManager.getInstance().playOneShot("puzzle_complete");
        });

        Button homeBtn = new Button("🏠  SAVE & RETURN TO MENU");
        homeBtn.setMaxWidth(Double.MAX_VALUE);
        homeBtn.setStyle("-fx-background-color: #5c2020; -fx-border-color: #c0392b; -fx-text-fill: #ffeaa7; -fx-font-size: 14px; "
                + "-fx-font-weight: bold; -fx-padding: 9 20; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
        homeBtn.setOnAction(e -> returnToMainMenu());

        Button exitBtn = new Button("❌  SAVE & QUIT TO DESKTOP");
        exitBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setStyle("-fx-background-color: #2b1111; -fx-border-color: #7f1d1d; -fx-text-fill: #fca5a5; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> {
            if (game != null) game.saveNow();
            SceneManager.exitGame();
        });

        card.getChildren().addAll(title, subtitle, statusMsg, resumeBtn, saveBtn, homeBtn, exitBtn);
        pauseOverlay.getChildren().add(card);
        playArea.getChildren().add(pauseOverlay);
        resumeBtn.requestFocus();
    }

    private void resumeGame(StackPane playArea) {
        if (pauseOverlay != null) {
            playArea.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
        }
        if (game != null) {
            game.resume();
        }
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
            isGameOver = true;
            if (hudTimer != null) {
                hudTimer.stop();
            }
            if (pauseOverlay != null) {
                playArea.getChildren().remove(pauseOverlay);
                pauseOverlay = null;
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

    private void showVictoryOverlay(StackPane playArea) {
        javafx.application.Platform.runLater(() -> {
            isVictory = true;
            if (hudTimer != null) {
                hudTimer.stop();
            }
            if (pauseOverlay != null) {
                playArea.getChildren().remove(pauseOverlay);
                pauseOverlay = null;
            }

            // Trigger triumphant Khmer victory celebration theme
            AudioManager.getInstance().playVictoryMusic();

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(5, 12, 10, 0.88);");
            overlay.setPickOnBounds(true);

            VBox card = new VBox(15);
            card.setMaxSize(760, 530);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(26, 36, 26, 36));
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #112017 0%, #1c180e 60%, #2a1f0d 100%); " +
                "-fx-background-radius: 18px; " +
                "-fx-border-color: #f1c40f; " +
                "-fx-border-width: 2.5px; " +
                "-fx-border-radius: 18px; " +
                "-fx-effect: dropshadow(gaussian, rgba(241, 196, 15, 0.45), 32, 0.4, 0, 0);"
            );

            // Khmer decorative badge
            Label badge = new Label("✨ ជ័យជម្នះ • FREEDOM AT LAST ✨");
            badge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f1c40f; -fx-background-color: rgba(241, 196, 15, 0.15); -fx-background-radius: 12px; -fx-padding: 4 14; -fx-border-color: rgba(241, 196, 15, 0.5); -fx-border-radius: 12px;");

            // Triumphant Title
            Label title = new Label("VICTORY! YOU ESCAPED!");
            title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #ffd700; -fx-effect: dropshadow(one-pass-box, #d4ac0d, 8, 0.3, 0, 2);");

            Label subtitle = new Label("The morning sun breaks over Sisowath High. The curse is shattered forever.");
            subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0; -fx-font-style: italic;");

            // Story Mode Epilogue Box
            VBox storyBox = new VBox(6);
            storyBox.setPadding(new Insets(12, 16, 12, 16));
            storyBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45); -fx-background-radius: 10px; -fx-border-color: rgba(241, 196, 15, 0.3); -fx-border-radius: 10px;");

            Label storyKhmer = new Label("អ្នកបានយកឈ្នះលើវិញ្ញាណអាក្រក់ និងដោះស្រាយអាថ៌កំបាំងសាលាដោយជោគជ័យ!");
            storyKhmer.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");

            Label storyText = new Label(
                "As the grand wrought-iron gates swung open to the dawn light, the spectral cries of the Ahp, Pret, " +
                "and trapped spirits faded into the calm Phnom Penh morning breeze. With every classroom purified " +
                "and the sacred Khmer relics safely recovered, the fifty-year supernatural seal is broken forever."
            );
            storyText.setWrapText(true);
            storyText.setMaxWidth(680);
            storyText.setStyle("-fx-font-size: 12px; -fx-text-fill: #cfd8dc; -fx-line-spacing: 3px;");
            storyBox.getChildren().addAll(storyKhmer, storyText);

            // Stats Dashboard (4 stat cards)
            HBox statsBox = new HBox(12);
            statsBox.setAlignment(Pos.CENTER);

            double playSeconds = (game != null) ? game.getPlayTimeSeconds() : 0.0;
            int mins = (int) (playSeconds / 60);
            int secs = (int) (playSeconds % 60);
            String timeFormatted = String.format("%02d:%02d", mins, secs);

            int heartsLeft = (game != null && game.getPlayer() != null) ? game.getPlayer().getHearts() : 5;
            int itemsCount = (game != null && game.getInventory() != null) ? game.getInventory().getItemCounts().values().stream().mapToInt(Integer::intValue).sum() : 0;
            int roomsPurified = (game != null) ? game.getEducationCompletedRooms().size() : 10;

            statsBox.getChildren().addAll(
                createStatCard("⏱ TIME SURVIVED", timeFormatted, "#3498db"),
                createStatCard("♥ REMAINING VITALITY", heartsLeft + " / 5 Hearts", "#e74c3c"),
                createStatCard("🏺 RELICS COLLECTED", itemsCount + " Items", "#f39c12"),
                createStatCard("🏫 ROOMS PURIFIED", roomsPurified + " / 10 Rooms", "#2ecc71")
            );

            // Action Buttons
            HBox actions = new HBox(18);
            actions.setAlignment(Pos.CENTER);
            actions.setPadding(new Insets(6, 0, 0, 0));

            Button playAgainBtn = new Button("↻ PLAY AGAIN");
            playAgainBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 24; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(39, 174, 96, 0.4), 10, 0.3, 0, 2);"
            );
            playAgainBtn.setOnMouseEntered(e -> playAgainBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 8px; -fx-cursor: hand;"
            ));
            playAgainBtn.setOnMouseExited(e -> playAgainBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 8px; -fx-cursor: hand;"
            ));
            playAgainBtn.setOnAction(e -> {
                if (hudTimer != null) hudTimer.stop();
                game.stop();
                new com.khmerspirit.save.SaveManager().deleteSave();
                SceneManager.showGame(selectedCharacter);
            });

            Button menuBtn = new Button("⌂ MAIN MENU");
            menuBtn.setStyle(
                "-fx-background-color: #d4ac0d; " +
                "-fx-text-fill: #111111; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 24; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(212, 172, 13, 0.4), 10, 0.3, 0, 2);"
            );
            menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(
                "-fx-background-color: #f1c40f; -fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 8px; -fx-cursor: hand;"
            ));
            menuBtn.setOnMouseExited(e -> menuBtn.setStyle(
                "-fx-background-color: #d4ac0d; -fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 8px; -fx-cursor: hand;"
            ));
            menuBtn.setOnAction(e -> {
                if (hudTimer != null) hudTimer.stop();
                game.stop();
                new com.khmerspirit.save.SaveManager().deleteSave();
                SceneManager.showMainMenu();
            });

            actions.getChildren().addAll(playAgainBtn, menuBtn);

            card.getChildren().addAll(badge, title, subtitle, storyBox, statsBox, actions);
            overlay.getChildren().add(card);
            playArea.getChildren().add(overlay);
            playAgainBtn.requestFocus();
        });
    }

    private VBox createStatCard(String label, String value, String accentColor) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 14, 8, 14));
        box.setStyle(
            "-fx-background-color: rgba(20, 25, 30, 0.75); " +
            "-fx-background-radius: 8px; " +
            "-fx-border-color: " + accentColor + "; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 8px; " +
            "-fx-min-width: 140px;"
        );
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #a0aec0;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        box.getChildren().addAll(lbl, val);
        return box;
    }
}
