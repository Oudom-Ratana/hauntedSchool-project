package com.khmerspirit.save;

import com.khmerspirit.inventory.Inventory;
import com.khmerspirit.items.ItemRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SaveManager {

    private static final String INVENTORY_PREFIX = "inventory.";
    private static final Path SAVE_PATH = Path.of("saves", "game.properties");

    private final FileManager fileManager;

    public SaveManager() {
        this.fileManager = new FileManager();
    }

    public SaveData load() {
        Properties properties = fileManager.loadProperties(SAVE_PATH);
        if (properties.isEmpty()) {
            return new SaveData();
        }

        String character = properties.getProperty("character", "Male Student");
        double playerX = parseDouble(properties.getProperty("player.x"), 40 * com.khmerspirit.config.Constants.TILE_SIZE);
        double playerY = parseDouble(properties.getProperty("player.y"), 50 * com.khmerspirit.config.Constants.TILE_SIZE);
        String currentRoom = properties.getProperty("current.room", "entrance");
        int hearts = parseInt(properties.getProperty("hearts"), 5);
        String taskRoom = properties.getProperty("task.room", "");
        int taskCorrect = parseInt(properties.getProperty("task.correct"), 0);
        double playTime = parseDouble(properties.getProperty("play.time"), 0.0);

        Map<String, Integer> items = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith(INVENTORY_PREFIX)) continue;
            String itemId = name.substring(INVENTORY_PREFIX.length());
            if (ItemRegistry.findById(itemId).isEmpty()) continue;
            int count = parseInt(properties.getProperty(name), 0);
            if (count > 0) items.put(itemId, count);
        }

        List<String> completed = new ArrayList<>();
        String comp = properties.getProperty("completed.rooms", "");
        if (!comp.isBlank()) {
            for (String id : comp.split(",")) completed.add(id.trim());
        }

        return new SaveData(character, items, currentRoom, completed, hearts, taskRoom, taskCorrect, playTime, playerX, playerY);
    }

    public void saveGame(SaveData data) {
        Properties properties = new Properties();
        properties.setProperty("character", data.getCharacterName());
        properties.setProperty("player.x", Double.toString(data.getPlayerX()));
        properties.setProperty("player.y", Double.toString(data.getPlayerY()));
        properties.setProperty("current.room", data.getCurrentRoomId());
        properties.setProperty("hearts", Integer.toString(data.getHearts()));
        properties.setProperty("task.room", data.getCurrentTaskRoomId());
        properties.setProperty("task.correct", Integer.toString(data.getCurrentTaskCorrectCount()));
        properties.setProperty("play.time", Double.toString(data.getPlayTimeSeconds()));

        if (!data.getCompletedRooms().isEmpty()) {
            properties.setProperty("completed.rooms", String.join(",", data.getCompletedRooms()));
        }

        data.getInventoryItems().forEach((k, v) -> properties.setProperty(INVENTORY_PREFIX + k, Integer.toString(v)));

        fileManager.saveProperties(SAVE_PATH, properties);
    }

    public boolean hasSave() {
        Properties p = fileManager.loadProperties(SAVE_PATH);
        return !p.isEmpty();
    }

    public void deleteSave() {
        fileManager.deleteIfExists(SAVE_PATH);
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }

    private double parseDouble(String v, double def) {
        try { return Double.parseDouble(v); } catch (Exception e) { return def; }
    }
}
