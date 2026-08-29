package com.khmerspirit.scene;

import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenuScene {

    private static final class RainDrop {
        double x, y, speed;
        double length;
    }

    private MediaPlayer mediaPlayer;

    public Scene createScene() {
        StackPane root = new StackPane();
        root.getStyleClass().add("menu-root");

        MediaView mediaView = createVideoBackground();

        Canvas backgroundCanvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        BorderPane content = new BorderPane();
        content.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getStyleClass().add("title-box");

        Label title = new Label("KHMER SPIRIT");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("THE HAUNTED SCHOOL");
        subtitle.getStyleClass().add("game-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        VBox menu = new VBox(10);
        menu.setAlignment(Pos.CENTER);
        menu.getStyleClass().add("menu-panel");

        Button newGameButton = createMenuButton("NEW GAME");
        com.khmerspirit.save.SaveManager _sm = new com.khmerspirit.save.SaveManager();
        newGameButton.setOnAction(event -> {
            stopVideo();
            _sm.deleteSave();
            SceneManager.showCharacterSelection();
        });

        Button continueButton = createMenuButton("CONTINUE");
        com.khmerspirit.save.SaveManager saveManager = new com.khmerspirit.save.SaveManager();
        if (saveManager.hasSave()) {
            continueButton.setDisable(false);
            continueButton.setOnAction(event -> {
                stopVideo();
                com.khmerspirit.save.SaveData saveData = saveManager.load();
                SceneManager.showGameWithSave(saveData);
            });
        } else {
            continueButton.setDisable(true);
        }

        Button characterButton = createMenuButton("CHARACTER");
        characterButton.setOnAction(event -> {
            stopVideo();
            SceneManager.showCharacterSelection();
        });

        Button teacherButton = createMenuButton("TEACHER ADMIN");
        teacherButton.setOnAction(event -> {
            stopVideo();
            SceneManager.showTeacherAdmin();
        });

        Button settingsButton = createMenuButton("SETTINGS");
        settingsButton.setOnAction(event -> showAudioSettings(root));

        Button exitButton = createMenuButton("EXIT");
        exitButton.setOnAction(event -> {
            stopVideo();
            SceneManager.exitGame();
        });

        menu.getChildren().addAll(newGameButton, continueButton, characterButton, teacherButton, settingsButton, exitButton);

        Label version = new Label("v1.0.0");
        version.getStyleClass().add("version-label");

        StackPane center = new StackPane(menu);
        center.setAlignment(Pos.CENTER_LEFT);
        center.setPadding(new Insets(210, 0, 0, 78));

        content.setTop(titleBox);
        content.setCenter(center);
        content.setBottom(version);
        BorderPane.setMargin(titleBox, new Insets(36, 0, 0, 58));
        BorderPane.setMargin(version, new Insets(0, 0, 14, 18));

        if (mediaView != null) {
            root.getChildren().add(mediaView);
        }
        root.getChildren().addAll(backgroundCanvas, content);
        AudioManager audio = AudioManager.getInstance();
        audio.stopAll();
        if (mediaView == null) {
            audio.playLoop("menu_music");
            audio.playLoop("ambience");
        }
        startRainAnimation(backgroundCanvas, mediaView != null);

        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private MediaView createVideoBackground() {
        String videoUrl = getVideoUrl();
        if (videoUrl == null) {
            System.err.println("Menu video URL could not be resolved.");
            return null;
        }
        try {
            System.out.println("Loading menu video from: " + videoUrl);
            Media media = new Media(videoUrl);
            media.setOnError(() -> System.err.println("Media Error: " + media.getError()));
            
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnError(() -> System.err.println("MediaPlayer Error: " + mediaPlayer.getError()));
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(false);
            mediaPlayer.setAutoPlay(true);
            
            mediaPlayer.setOnReady(() -> {
                System.out.println("MediaPlayer ready. Playing video sound & track.");
                double volume = AudioManager.getInstance().getMasterVolume() * AudioManager.getInstance().getMusicVolume();
                mediaPlayer.setVolume(volume);
                mediaPlayer.play();
            });
            
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(Constants.WINDOW_WIDTH);
            mediaView.setFitHeight(Constants.WINDOW_HEIGHT);
            mediaView.setPreserveRatio(false);
            
            return mediaView;
        } catch (Exception e) {
            System.err.println("Failed to initialize background video: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getVideoUrl() {
        try {
            File relativeFile = new File("images/bg/menu-video2.mp4");
            if (relativeFile.exists()) {
                return relativeFile.toURI().toString();
            }

            File devFile = new File("src/main/resources/images/bg/menu-video.mp4");
            if (devFile.exists()) {
                return devFile.toURI().toString();
            }

            URL resource = getClass().getResource("/images/bg/menu-video2.mp4");
            if (resource == null) {
                resource = getClass().getResource("/images/bg/menu-video.mp4");
            }
            if (resource != null) {
                File tempVideo = File.createTempFile("hanted_school_menu_video_", ".mp4");
                tempVideo.deleteOnExit();
                try (InputStream in = resource.openStream();
                     FileOutputStream out = new FileOutputStream(tempVideo)) {
                    byte[] buffer = new byte[16384];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                    return tempVideo.toURI().toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Error resolving menu video path: " + e.getMessage());
        }
        return null;
    }

    private void stopVideo() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            mediaPlayer = null;
        }
    }

    private void showAudioSettings(StackPane root) {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setAlignment(Pos.CENTER);

        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(360);
        panel.getStyleClass().add("menu-panel");

        Label title = new Label("AUDIO SETTINGS");
        title.getStyleClass().add("hud-title");

        panel.getChildren().addAll(
                title,
                createSliderRow("Master", AudioManager.getInstance().getMasterVolume(), value -> {
                    AudioManager.getInstance().setMasterVolume(value);
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(value * AudioManager.getInstance().getMusicVolume());
                    }
                }),
                createSliderRow("Ambience", AudioManager.getInstance().getAmbienceVolume(), value -> AudioManager.getInstance().setAmbienceVolume(value)),
                createSliderRow("SFX", AudioManager.getInstance().getSfxVolume(), value -> AudioManager.getInstance().setSfxVolume(value)),
                createSliderRow("Music", AudioManager.getInstance().getMusicVolume(), value -> {
                    AudioManager.getInstance().setMusicVolume(value);
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(AudioManager.getInstance().getMasterVolume() * value);
                    }
                })
        );

        Button closeButton = new Button("CLOSE");
        closeButton.getStyleClass().add("menu-button");
        closeButton.setOnAction(event -> root.getChildren().remove(overlay));
        panel.getChildren().add(closeButton);

        overlay.getChildren().add(panel);
        root.getChildren().add(overlay);
    }

    private VBox createSliderRow(String labelText, double value, java.util.function.Consumer<Double> consumer) {
        Label label = new Label(labelText);
        label.getStyleClass().add("hud-text");
        Slider slider = new Slider(0.0, 1.0, value);
        slider.setPrefWidth(280);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue.doubleValue()));
        VBox row = new VBox(4, label, slider);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private void startRainAnimation(Canvas canvas, boolean hasVideo) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        Random random = new Random(11);
        List<RainDrop> drops = new ArrayList<>();
        for (int i = 0; i < 95; i++) {
            RainDrop drop = new RainDrop();
            drop.x = random.nextDouble() * canvas.getWidth();
            drop.y = random.nextDouble() * canvas.getHeight();
            drop.speed = 180 + random.nextDouble() * 180;
            drop.length = 8 + random.nextDouble() * 12;
            drops.add(drop);
        }

        AnimationTimer timer = new AnimationTimer() {
            private long lastNanos = 0;
            @Override
            public void handle(long now) {
                if (lastNanos == 0) {
                    lastNanos = now;
                }
                double delta = (now - lastNanos) / 1_000_000_000.0;
                lastNanos = now;
                g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                if (!hasVideo) {
                    LinearGradient sky = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.rgb(6, 12, 18, 0.95)),
                            new Stop(0.6, Color.rgb(10, 20, 24, 0.92)),
                            new Stop(1, Color.rgb(3, 6, 8, 0.98)));
                    g.setFill(sky);
                    g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }

                g.setStroke(Color.rgb(168, 216, 255, 0.16));
                g.setLineWidth(1.0);
                for (int i = 0; i < 14; i++) {
                    double x = 60 + i * 72 + Math.sin((now / 1_000_000_000.0) * (0.4 + i * 0.02)) * 18;
                    double y = 120 + i * 22;
                    g.strokeLine(x, y, x + 54, y + 28);
                }

                for (RainDrop drop : drops) {
                    drop.y += drop.speed * delta;
                    if (drop.y > canvas.getHeight()) {
                        drop.y = -drop.length;
                        drop.x = random.nextDouble() * canvas.getWidth();
                    }
                    g.setStroke(Color.rgb(200, 226, 247, 0.35));
                    g.setLineWidth(1.0);
                    g.strokeLine(drop.x, drop.y, drop.x + 1.5, drop.y + drop.length);
                }

                g.setFill(Color.rgb(255, 255, 255, 0.08));
                for (int i = 0; i < 16; i++) {
                    double x = (i * 71) % canvas.getWidth();
                    double y = 60 + (i * 37) % 220;
                    g.fillOval(x, y, 2, 2);
                }
            }
        };
        timer.start();
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
}

