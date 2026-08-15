package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Toolbox extends Item {

    public Toolbox() {
        super("toolbox", "Toolbox", false, Color.web("#a85e3a"));
    }

    @Override
    public String getUseMessage() {
        return "The toolbox is ready for broken school equipment.";
    }
}
