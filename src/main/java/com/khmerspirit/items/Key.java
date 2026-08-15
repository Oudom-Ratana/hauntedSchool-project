package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Key extends Item {

    public Key() {
        super("key", "Key", false, Color.web("#d4a947"));
    }

    @Override
    public String getUseMessage() {
        return "The key feels cold and old.";
    }
}
