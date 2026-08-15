package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Lighter extends Item {

    public Lighter() {
        super("lighter", "Lighter", true, Color.web("#d87834"));
    }

    @Override
    public String getUseMessage() {
        return "A small flame flickers in the stale air.";
    }
}
