package com.khmerspirit.scene;

import com.khmerspirit.core.AssetManager;
import com.khmerspirit.player.CharacterType;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * Interactive card component displaying the character pixel artwork, title, and bio,
 * featuring smooth scale animations and golden glow selection effects.
 */
public final class CharacterCard extends StackPane {

    public static final double CARD_WIDTH = 410.0;
    public static final double CARD_HEIGHT = 804.0;

    private static final DropShadow SELECTED_GLOW = new DropShadow(
            BlurType.GAUSSIAN, Color.rgb(255, 215, 80, 0.95), 36, 0.55, 0, 0);
    private static final DropShadow HOVER_GLOW = new DropShadow(
            BlurType.GAUSSIAN, Color.rgb(255, 210, 70, 0.65), 22, 0.38, 0, 0);
    private static final DropShadow UNSELECTED_SHADOW = new DropShadow(
            BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.75), 16, 0.3, 0, 6);

    private final CharacterType character;
    private final ImageView cardImageView;
    private final StackPane highlightBorder;
    private boolean selected = false;

    public CharacterCard(CharacterType character) {
        this(character, null);
    }

    public CharacterCard(CharacterType character, AssetManager assets) {
        this.character = character;

        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        setAlignment(Pos.CENTER);
        setCursor(Cursor.HAND);
        setFocusTraversable(true);
        getStyleClass().add("character-card-container");

        cardImageView = new ImageView();
        cardImageView.setFitWidth(CARD_WIDTH);
        cardImageView.setFitHeight(CARD_HEIGHT);
        cardImageView.setPreserveRatio(true);
        cardImageView.setSmooth(true);

        InputStream cardStream = getClass().getResourceAsStream(character.getCardImagePath());
        if (cardStream != null) {
            cardImageView.setImage(new Image(cardStream));
        }

        highlightBorder = new StackPane();
        highlightBorder.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        highlightBorder.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        highlightBorder.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        highlightBorder.setMouseTransparent(true);
        highlightBorder.getStyleClass().add("character-card-highlight");

        getChildren().addAll(cardImageView, highlightBorder);

        setOnMouseEntered(event -> {
            if (!selected) {
                animateScale(1.025);
                setOpacity(0.96);
                setEffect(HOVER_GLOW);
                highlightBorder.setStyle("-fx-border-color: rgba(255, 215, 80, 0.65); -fx-border-width: 3px; -fx-border-radius: 10px;");
            }
        });

        setOnMouseExited(event -> {
            if (!selected) {
                animateScale(1.0);
                setOpacity(0.82);
                setEffect(UNSELECTED_SHADOW);
                highlightBorder.setStyle("-fx-border-color: transparent;");
            }
        });

        applyState(false);
    }

    public CharacterType getCharacter() {
        return character;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        applyState(true);
    }

    private void applyState(boolean animate) {
        if (selected) {
            if (animate) {
                animateScale(1.045);
            } else {
                setScaleX(1.045);
                setScaleY(1.045);
            }
            setOpacity(1.0);
            setEffect(SELECTED_GLOW);
            highlightBorder.setStyle("-fx-border-color: #ffd700; -fx-border-width: 4px; -fx-border-radius: 10px;");
            getStyleClass().add("character-card-active");
        } else {
            if (animate) {
                animateScale(1.0);
            } else {
                setScaleX(1.0);
                setScaleY(1.0);
            }
            setOpacity(0.82);
            setEffect(UNSELECTED_SHADOW);
            highlightBorder.setStyle("-fx-border-color: transparent;");
            getStyleClass().remove("character-card-active");
        }
    }

    private void animateScale(double targetScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), this);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.play();
    }
}