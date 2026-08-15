package com.khmerspirit.save;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SaveData {

    private final String characterName;
    private final Map<String, Integer> inventoryItems;
    private final String currentRoomId;
    private final List<String> completedRooms;
    private final int hearts;
    private final String currentTaskRoomId;
    private final int currentTaskCorrectCount;
    private final double playTimeSeconds;
    private final double playerX;
    private final double playerY;

    public SaveData() {
        this.characterName = "Male Student";
        this.inventoryItems = new LinkedHashMap<>();
        this.currentRoomId = "entrance";
        this.completedRooms = new ArrayList<>();
        this.hearts = 5;
        this.currentTaskRoomId = "";
        this.currentTaskCorrectCount = 0;
        this.playTimeSeconds = 0.0;
        this.playerX = 40 * com.khmerspirit.config.Constants.TILE_SIZE;
        this.playerY = 50 * com.khmerspirit.config.Constants.TILE_SIZE;
    }

    public SaveData(String characterName,
                    Map<String, Integer> inventoryItems,
                    String currentRoomId,
                    List<String> completedRooms,
                    int hearts,
                    String currentTaskRoomId,
                    int currentTaskCorrectCount,
                    double playTimeSeconds,
                    double playerX,
                    double playerY) {
        this.characterName = Objects.requireNonNull(characterName);
        this.inventoryItems = new LinkedHashMap<>(inventoryItems == null ? Map.of() : inventoryItems);
        this.currentRoomId = currentRoomId == null ? "" : currentRoomId;
        this.completedRooms = new ArrayList<>(completedRooms == null ? List.of() : completedRooms);
        this.hearts = hearts;
        this.currentTaskRoomId = currentTaskRoomId == null ? "" : currentTaskRoomId;
        this.currentTaskCorrectCount = currentTaskCorrectCount;
        this.playTimeSeconds = playTimeSeconds;
        this.playerX = playerX;
        this.playerY = playerY;
    }

    public String getCharacterName() {
        return characterName;
    }

    public Map<String, Integer> getInventoryItems() {
        return new LinkedHashMap<>(inventoryItems);
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public List<String> getCompletedRooms() {
        return new ArrayList<>(completedRooms);
    }

    public int getHearts() {
        return hearts;
    }

    public String getCurrentTaskRoomId() {
        return currentTaskRoomId;
    }

    public int getCurrentTaskCorrectCount() {
        return currentTaskCorrectCount;
    }

    public double getPlayTimeSeconds() {
        return playTimeSeconds;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }
}
