package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Key extends Item {

    public Key() {
        this("key", "Room Key");
    }

    public Key(String id, String displayName) {
        super(id, displayName, false, Color.web("#d4a947"));
    }

    @Override
    public String getUseMessage() {
        return "Brass Room Key: Unlocks sealed classroom and department doors in the school.";
    }

    @Override
    public String getAbilityDescription() {
        return "Heavy brass room key engraved with ancient protective motifs that unlocks sealed school rooms and classrooms.";
    }
}
