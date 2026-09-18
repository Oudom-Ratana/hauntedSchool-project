package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Key extends Item {

    public Key() {
        super("key", "Room Key", false, Color.web("#d4a947"));
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
