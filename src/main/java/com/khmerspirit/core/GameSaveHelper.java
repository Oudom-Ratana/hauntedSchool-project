package com.khmerspirit.core;

import com.khmerspirit.inventory.Inventory;
import com.khmerspirit.save.SaveData;

import java.util.List;
import java.util.Map;

public final class GameSaveHelper {

    private GameSaveHelper() {}

    public static SaveData buildFrom(Game game) {
        String character = game.getPlayer().getCharacterName();
        Map<String, Integer> items = game.getInventory().getItemCounts();
        String currentRoomId = game.getCurrentRoomId();
        List<String> completed = game.getEducationCompletedRooms();
        int hearts = game.getPlayer().getHearts();
        String currentTaskRoom = game.getEducationActiveRoomId() == null ? "" : game.getEducationActiveRoomId();
        int taskCorrect = game.getEducationActiveCorrectCount();
        double playTime = game.getPlayTimeSeconds();
        double px = game.getPlayer().getCenterX();
        double py = game.getPlayer().getCenterY();
        return new SaveData(character, items, currentRoomId == null ? "" : currentRoomId, completed, hearts, currentTaskRoom, taskCorrect, playTime, px, py);
    }
}