package com.khmerspirit.scene;

import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CharacterScene {

    private static final String MALE_CHARACTER = "Male Student";
    private static final String FEMALE_CHARACTER = "Female Student";

    private String selectedCharacter = MALE_CHARACTER;
    private VBox maleCard;
    private VBox femaleCard;

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen-root");

        Label heading = new Label("CHARACTER SELECT");
        heading.getStyleClass().add("section-heading");
        BorderPane.setAlignment(heading, Pos.CENTER);
        BorderPane.setMargin(heading, new Insets(28, 0, 16, 0));

        HBox characterCards = new HBox(22);
        characterCards.setAlignment(Pos.CENTER);
        characterCards.setPadding(new Insets(20, 34, 20, 34));

        maleCard = createCharacterCard("MALE CHARACTER", "Boy student", "White shirt, dark trousers, brave expression", MALE_CHARACTER);
        femaleCard = createCharacterCard("FEMALE CHARACTER", "Girl student", "White shirt, dark skirt, focused expression", FEMALE_CHARACTER);
        HBox.setHgrow(maleCard, Priority.ALWAYS);
        HBox.setHgrow(femaleCard, Priority.ALWAYS);
        characterCards.getChildren().addAll(maleCard, femaleCard);
        updateSelectedCardStyles();

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(0, 0, 36, 0));

        Button backButton = new Button("BACK");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(event -> SceneManager.showMainMenu());

        Button startButton = new Button("START GAME");
        startButton.getStyleClass().add("primary-button");
        startButton.setOnAction(event -> SceneManager.showGame(selectedCharacter));

        actions.getChildren().addAll(backButton, startButton);

        root.setTop(heading);
        root.setCenter(characterCards);
        root.setBottom(actions);

        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private VBox createCharacterCard(String titleText, String characterName, String descriptionText, String characterValue) {
        VBox card = new VBox(18);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("character-card");
        card.setOnMouseClicked(event -> {
            selectedCharacter = characterValue;
            updateSelectedCardStyles();
        });

        Label title = new Label(titleText);
        title.getStyleClass().add("card-title");

        HBox sprites = new HBox(18);
        sprites.setAlignment(Pos.CENTER);
        sprites.getChildren().addAll(createPixelStudent("1"), createPixelStudent("2"), createPixelStudent("3"));

        Label name = new Label(characterName);
        name.getStyleClass().add("character-name");

        Label description = new Label(descriptionText);
        description.getStyleClass().add("character-description");
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);

        Button selectButton = new Button("SELECT");
        selectButton.getStyleClass().add("secondary-button");
        selectButton.setOnAction(event -> {
            selectedCharacter = characterValue;
            updateSelectedCardStyles();
        });

        card.getChildren().addAll(title, sprites, name, description, selectButton);
        return card;
    }

    private StackPane createPixelStudent(String number) {
        StackPane sprite = new StackPane();
        sprite.getStyleClass().add("pixel-student");

        Label face = new Label(number);
        face.getStyleClass().add("pixel-student-label");
        sprite.getChildren().add(face);

        return sprite;
    }

    private void updateSelectedCardStyles() {
        applySelectedStyle(maleCard, MALE_CHARACTER.equals(selectedCharacter));
        applySelectedStyle(femaleCard, FEMALE_CHARACTER.equals(selectedCharacter));
    }

    private void applySelectedStyle(VBox card, boolean selected) {
        if (card == null) {
            return;
        }
        card.getStyleClass().remove("character-card-selected");
        if (selected) {
            card.getStyleClass().add("character-card-selected");
        }
    }
}
