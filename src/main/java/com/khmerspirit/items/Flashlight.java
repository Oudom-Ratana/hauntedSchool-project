package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Flashlight extends Item {

    public Flashlight() {
        super("flashlight", "Flashlight", false, Color.web("#d9d0a4"));
    }

    @Override
    public String getUseMessage() {
        return "Flashlight beam cuts through the dark hallway.";
    }
}
