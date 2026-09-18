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
        String name = selectedCharacter == null ? "" : selectedCharacter.toLowerCase();
        String path;
        Color shirtColor;
        Color lowerColor;

        if (name.contains("ghost") || name.contains("spirit") || name.contains("ខ្មោច")) {
            path = Constants.GHOST_SPRITE;
            shirtColor = Color.web("#1c0a1f");
            lowerColor = Color.web("#0c030f");
        } else if (name.contains("songk") || name.contains("monk")) {
            path = Constants.PREAH_SONGK_PLAYER_SPRITE;
            shirtColor = Color.web("#d96b14");
            lowerColor = Color.web("#b85207");
        } else if (name.contains("chanda") || name.contains("female") || name.contains("girl")) {
            path = Constants.CHANDA_PLAYER_SPRITE;
            shirtColor = Color.web("#e8e4dc");
            lowerColor = Color.web("#222638");
        } else if (name.contains("kakada")) {
            path = Constants.KAKADA_PLAYER_SPRITE;
            shirtColor = Color.web("#4a8505");
            lowerColor = Color.web("#242c3d");
        } else if (name.contains("sophea")) {
            path = Constants.SOPHEA_PLAYER_SPRITE;
            shirtColor = Color.web("#f7b7d2");
            lowerColor = Color.web("#1e2340");
        } else if (name.contains("dara")) {
            path = Constants.DARA_PLAYER_SPRITE;
            shirtColor = Color.web("#2575fc");
            lowerColor = Color.web("#141720");
        } else if (name.contains("bora")) {
            path = Constants.BORA_PLAYER_SPRITE;
            shirtColor = Color.web("#e52d27");
            lowerColor = Color.web("#12141c");
        } else if (name.contains("kesor")) {
            path = Constants.KESOR_PLAYER_SPRITE;
            shirtColor = Color.web("#27ae60");
            lowerColor = Color.web("#3e3223");
        } else if (name.contains("kru") || name.contains("healer")) {
            path = Constants.KRU_KHMER_PLAYER_SPRITE;
            shirtColor = Color.web("#f5f2eb");
            lowerColor = Color.web("#2c241b");
        } else if (name.contains("rithy") || name.contains("bokator")) {
            path = Constants.RITHY_PLAYER_SPRITE;
            shirtColor = Color.web("#1c1e24");
            lowerColor = Color.web("#1d2d44");
        } else if (name.contains("kalyan") || name.contains("reporter")) {
            path = Constants.KALYAN_PLAYER_SPRITE;
            shirtColor = Color.web("#d4901a");
            lowerColor = Color.web("#212534");
        } else if (name.contains("visal") || name.contains("archivist")) {
            path = Constants.VISAL_PLAYER_SPRITE;
            shirtColor = Color.web("#1b2845");
            lowerColor = Color.web("#b59e7a");
        } else if (name.contains("neary") || name.contains("medic")) {
            path = Constants.NEARY_PLAYER_SPRITE;
            shirtColor = Color.web("#f4f6fa");
            lowerColor = Color.web("#114b5f");
        } else if (name.contains("samnang") || name.contains("rebel")) {
            path = Constants.SAMNANG_PLAYER_SPRITE;
            shirtColor = Color.web("#6a0dad");
            lowerColor = Color.web("#18181c");
        } else if (name.contains("tevy") || name.contains("flutist")) {
            path = Constants.TEVY_PLAYER_SPRITE;
            shirtColor = Color.web("#196f3d");
            lowerColor = Color.web("#512e5f");
        } else if (name.contains("sovann") || name.contains("explorer")) {
            path = Constants.SOVANN_PLAYER_SPRITE;
            shirtColor = Color.web("#556b2f");
            lowerColor = Color.web("#705838");
        } else {
            path = Constants.VICHEKA_PLAYER_SPRITE;
            shirtColor = Color.web("#f0eee4");
            lowerColor = Color.web("#242c3d");
        }
        return loadImage(path, () -> createFallbackPlayerSheet(shirtColor, lowerColor));
    }

    public Image loadItemSprite(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        String path = "/images/items/" + itemId.toLowerCase() + ".png";
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        InputStream stream = AssetManager.class.getResourceAsStream(path);
        if (stream != null) {
            Image img = new Image(stream);
            imageCache.put(path, img);
            return img;
        }
        return null;
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

    public Image loadImage(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        return imageCache.computeIfAbsent(resourcePath, path -> {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        });
    }

    @FunctionalInterface
    private interface ImageFactory {
        Image create();
    }
}
