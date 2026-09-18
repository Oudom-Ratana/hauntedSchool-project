package com.khmerspirit.scene;

import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import com.khmerspirit.player.CharacterType;
import com.khmerspirit.save.SaveManager;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * Pixel-art character selection screen matching the authentic Khmer Spirit visual design.
 * Features 3 interactive playable characters, high-fidelity artwork, sound effects,
 * and responsive scaling for any screen resolution.
 */
public class CharacterScene {

    public static final double DESIGN_WIDTH = 2528.0;
    public static final double DESIGN_HEIGHT = 1684.0;

    private final CharacterType[] characters = CharacterType.getSelectableCharacters();
    private final CharacterCard[] cards = new CharacterCard[characters.length];
    private int selectedIndex = 0;

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #030507;");
        root.setFocusTraversable(true);

        // Ambient blurred edge backdrop to blend seamlessly with any screen aspect ratio
        ImageView ambientBg = createImageView("/images/Charactors/character_scene_backdrop.jpg");
        if (ambientBg != null) {
            ambientBg.fitWidthProperty().bind(root.widthProperty());
            ambientBg.fitHeightProperty().bind(root.heightProperty());
            ambientBg.setPreserveRatio(false);
            ambientBg.setEffect(new BoxBlur(18, 18, 2));
            ambientBg.setOpacity(0.35);
            root.getChildren().add(ambientBg);
        }

        // Virtual 2528x1684 canvas layer
        Pane canvasPane = new Pane();
        canvasPane.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        canvasPane.setMinSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        canvasPane.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

        // Main sharp backdrop image
        ImageView backdrop = createImageView("/images/Charactors/character_scene_backdrop.jpg");
        if (backdrop != null) {
            backdrop.setFitWidth(DESIGN_WIDTH);
            backdrop.setFitHeight(DESIGN_HEIGHT);
            backdrop.setPreserveRatio(false);
            canvasPane.getChildren().add(backdrop);
        }

        // Add 3 interactive character cards
        double[] cardX = { 564.0, 1060.0, 1556.0 };
        double cardY = 492.0;

        for (int i = 0; i < characters.length; i++) {
            CharacterCard card = new CharacterCard(characters[i]);
            card.setLayoutX(cardX[i]);
            card.setLayoutY(cardY);

            final int cardIndex = i;
            card.setOnMouseClicked(event -> {
                if (cardIndex == selectedIndex && event.getClickCount() == 2) {
                    startGame();
                } else {
                    select(cardIndex, true);
                }
            });

            cards[i] = card;
            canvasPane.getChildren().add(card);
        }

        // Add bottom navigation buttons
        // Back Button (ថយ)
        DropShadow backGlow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(220, 180, 80, 0.8), 24, 0.45, 0, 0);
        StackPane btnBack = createImageButton("/images/Charactors/btn_back.png",
                358.0, 152.0, backGlow, () -> SceneManager.showMainMenu());
        btnBack.setLayoutX(550.0);
        btnBack.setLayoutY(1412.0);

        // Start Button (ចាប់ផ្ដើមហ្គេម)
        DropShadow startGlow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(255, 225, 95, 0.95), 35, 0.55, 0, 0);
        StackPane btnStart = createImageButton("/images/Charactors/btn_start.png",
                630.0, 192.0, startGlow, this::startGame);
        btnStart.setLayoutX(950.0);
        btnStart.setLayoutY(1390.0);

        // Continue Button (បន្តទៀត)
        DropShadow nextGlow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(72, 195, 120, 0.8), 24, 0.45, 0, 0);
        StackPane btnNext = createImageButton("/images/Charactors/btn_next.png",
                356.0, 152.0, nextGlow, this::continueGame);
        btnNext.setLayoutX(1624.0);
        btnNext.setLayoutY(1412.0);

        // Settings Button (⚙)
        DropShadow gearGlow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(255, 210, 80, 0.85), 24, 0.45, 0, 0);
        StackPane btnSettings = createImageButton("/images/Charactors/btn_settings.png",
                160.0, 160.0, gearGlow, () -> showAudioSettings(root));
        btnSettings.setLayoutX(2310.0);
        btnSettings.setLayoutY(60.0);

        canvasPane.getChildren().addAll(btnBack, btnStart, btnNext, btnSettings);

        // Scale transform to preserve exact pixel proportions at any window size
        Group scalableGroup = new Group(canvasPane);
        DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
            double sx = root.getWidth() / DESIGN_WIDTH;
            double sy = root.getHeight() / DESIGN_HEIGHT;
            return Math.min(sx, sy);
        }, root.widthProperty(), root.heightProperty());

        scalableGroup.scaleXProperty().bind(scaleBinding);
        scalableGroup.scaleYProperty().bind(scaleBinding);

        StackPane centerWrapper = new StackPane(scalableGroup);
        centerWrapper.setAlignment(Pos.CENTER);
        root.getChildren().add(centerWrapper);

        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.setOnKeyPressed(event -> handleKey(event.getCode()));

        Platform.runLater(root::requestFocus);
        select(0, false);

        return scene;
    }

    private StackPane createImageButton(String imagePath, double width, double height,
                                        DropShadow hoverGlow, Runnable action) {
        StackPane button = new StackPane();
        button.setPrefSize(width, height);
        button.setMinSize(width, height);
        button.setMaxSize(width, height);
        button.setCursor(Cursor.HAND);
        button.setFocusTraversable(true);

        ImageView iv = createImageView(imagePath);
        if (iv != null) {
            iv.setFitWidth(width);
            iv.setFitHeight(height);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            button.getChildren().add(iv);
        }

        button.setOnMouseEntered(event -> {
            animateScale(button, 1.055);
            button.setEffect(hoverGlow);
            AudioManager.getInstance().playOneShot("footsteps");
        });

        button.setOnMouseExited(event -> {
            animateScale(button, 1.0);
            button.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.65), 12, 0.25, 0, 3));
        });

        button.setOnMousePressed(event -> animateScale(button, 0.95));
        button.setOnMouseReleased(event -> animateScale(button, 1.055));

        button.setOnMouseClicked(event -> {
            if (action != null) action.run();
        });

        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                if (action != null) action.run();
            }
        });

        button.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.65), 12, 0.25, 0, 3));
        return button;
    }

    private void handleKey(KeyCode key) {
        switch (key) {
            case LEFT, A -> select((selectedIndex + characters.length - 1) % characters.length, true);
            case RIGHT, D -> select((selectedIndex + 1) % characters.length, true);
            case ENTER, SPACE -> startGame();
            case ESCAPE, BACK_SPACE -> SceneManager.showMainMenu();
            default -> { }
        }
    }

    private void select(int index, boolean playSound) {
        selectedIndex = index;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                cards[i].setSelected(i == selectedIndex);
            }
        }
        if (playSound) {
            AudioManager.getInstance().playOneShot("door");
        }
    }

    private void startGame() {
        AudioManager.getInstance().playStartGame();
        AudioManager.getInstance().stopHomeMusic();
        SceneManager.showGame(characters[selectedIndex].getGameCharacterName());
    }

    private void continueGame() {
        SaveManager saves = new SaveManager();
        if (saves.hasSave()) {
            SceneManager.showGameWithSave(saves.load());
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "No saved game is available yet.");
        alert.setHeaderText("CONTINUE");
        alert.showAndWait();
    }

    private void showAudioSettings(StackPane root) {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlay.setAlignment(Pos.CENTER);

        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(380);
        panel.getStyleClass().add("menu-panel");

        Label title = new Label("AUDIO SETTINGS");
        title.getStyleClass().add("hud-title");

        panel.getChildren().addAll(
                title,
                createSliderRow("Master", AudioManager.getInstance().getMasterVolume(),
                        value -> AudioManager.getInstance().setMasterVolume(value)),
                createSliderRow("Ambience", AudioManager.getInstance().getAmbienceVolume(),
                        value -> AudioManager.getInstance().setAmbienceVolume(value)),
                createSliderRow("SFX", AudioManager.getInstance().getSfxVolume(),
                        value -> AudioManager.getInstance().setSfxVolume(value)),
                createSliderRow("Music", AudioManager.getInstance().getMusicVolume(),
                        value -> AudioManager.getInstance().setMusicVolume(value))
        );

        Button closeButton = new Button("CLOSE");
        closeButton.getStyleClass().add("menu-button");
        closeButton.setOnAction(event -> root.getChildren().remove(overlay));
        panel.getChildren().add(closeButton);

        overlay.getChildren().add(panel);
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                root.getChildren().remove(overlay);
            }
        });

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

    private static void animateScale(javafx.scene.Node node, double targetScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), node);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.play();
    }

    private static ImageView createImageView(String resourcePath) {
        InputStream stream = CharacterScene.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return null;
        }
        return new ImageView(new Image(stream));
    }
}
