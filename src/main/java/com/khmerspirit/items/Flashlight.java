package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Flashlight extends Item {

    public Flashlight() {
        super("flashlight", "Flashlight", false, Color.web("#d9d0a4"));
    }

    @Override
    public String getUseMessage() {
        return "Flashlight toggled! Illuminates darkness in front of player [Press F or slot hotkey].";
    }

    @Override
    public String getAbilityDescription() {
        return "High-intensity handheld beam that cuts through dark corridors. Consumes battery power when active.";
    }
}
