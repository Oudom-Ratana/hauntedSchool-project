package com.khmerspirit.core;

import com.khmerspirit.config.Constants;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private final Map<String, Image> imageCache = new HashMap<>();

    public Image loadPlayerSprite(String selectedCharacter) {
        String path = selectedCharacter.toLowerCase().contains("female")
                ? Constants.FEMALE_PLAYER_SPRITE
                : Constants.MALE_PLAYER_SPRITE;
        Color shirtColor = selectedCharacter.toLowerCase().contains("female") ? Color.web("#e8e4dc") : Color.web("#f0eee4");
        Color lowerColor = selectedCharacter.toLowerCase().contains("female") ? Color.web("#2e2533") : Color.web("#26313a");
        return loadImage(path, () -> createFallbackPlayerSheet(shirtColor, lowerColor));
    }

    private Image loadImage(String path, ImageFactory fallbackFactory) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        InputStream stream = AssetManager.class.getResourceAsStream(path);
        Image image = stream == null ? fallbackFactory.create() : new Image(stream);
        imageCache.put(path, image);
        return image;
    }

    private Image createFallbackPlayerSheet(Color shirtColor, Color lowerColor) {
        int frameWidth = Constants.PLAYER_WIDTH;
        int frameHeight = Constants.PLAYER_HEIGHT;
        WritableImage sheet = new WritableImage(frameWidth * 4, frameHeight * 4);

        for (int row = 0; row < 4; row++) {
            for (int frame = 0; frame < 4; frame++) {
                drawFallbackFrame(sheet, row, frame, shirtColor, lowerColor);
            }
        }
        return sheet;
    }

    private void drawFallbackFrame(WritableImage sheet, int row, int frame, Color shirtColor, Color lowerColor) {
        int frameWidth = Constants.PLAYER_WIDTH;
        int frameHeight = Constants.PLAYER_HEIGHT;
        int offsetX = frame * frameWidth;
        int offsetY = row * frameHeight;
        int legOffset = frame == 1 ? 2 : frame == 3 ? -2 : 0;

        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {
                sheet.getPixelWriter().setColor(offsetX + x, offsetY + y, Color.TRANSPARENT);
            }
        }

        fillRect(sheet, offsetX + 10, offsetY + 4, 12, 12, Color.web("#151515"));
        fillRect(sheet, offsetX + 11, offsetY + 10, 10, 8, Color.web("#d0aa82"));
        fillRect(sheet, offsetX + 8, offsetY + 19, 16, 14, shirtColor);
        fillRect(sheet, offsetX + 6, offsetY + 22, 4, 12, Color.web("#d0aa82"));
        fillRect(sheet, offsetX + 22, offsetY + 22, 4, 12, Color.web("#d0aa82"));
        fillRect(sheet, offsetX + 10 + legOffset, offsetY + 33, 5, 10, lowerColor);
        fillRect(sheet, offsetX + 17 - legOffset, offsetY + 33, 5, 10, lowerColor);

        if (row == 0) {
            fillRect(sheet, offsetX + 12, offsetY + 14, 2, 2, Color.web("#191919"));
            fillRect(sheet, offsetX + 18, offsetY + 14, 2, 2, Color.web("#191919"));
        }
    }

    private void fillRect(WritableImage image, int startX, int startY, int width, int height, Color color) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                image.getPixelWriter().setColor(x, y, color);
            }
        }
    }

    @FunctionalInterface
    private interface ImageFactory {
        Image create();
    }
}
