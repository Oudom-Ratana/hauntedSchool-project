package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Battery extends Item {

    public Battery() {
        super("battery", "Battery", true, Color.web("#7f8a93"));
    }

    @Override
    public String getUseMessage() {
        return "Battery charged the flashlight.";
    }
}
