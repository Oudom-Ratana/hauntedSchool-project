package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Battery extends Item {

    public Battery() {
        super("battery", "Battery", true, Color.web("#7f8a93"));
    }

    @Override
    public String getUseMessage() {
        return "Inserted fresh battery! Flashlight recharged to 100% power!";
    }

    @Override
    public String getAbilityDescription() {
        return "High-voltage alkaline battery that instantly recharges flashlight battery to 100% capacity.";
    }
}
