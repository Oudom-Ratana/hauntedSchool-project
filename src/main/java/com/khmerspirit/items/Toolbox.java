package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Toolbox extends Item {

    public Toolbox() {
        super("toolbox", "Toolbox", true, Color.web("#a85e3a"));
    }

    @Override
    public String getUseMessage() {
        return "Flashlight overhauled! Restored +50% battery and halved power drain rate for 60 seconds!";
    }

    @Override
    public String getAbilityDescription() {
        return "Engineer's maintenance toolkit that restores 50% flashlight battery and optimizes circuits to halve battery drain rate for 60s.";
    }
}
