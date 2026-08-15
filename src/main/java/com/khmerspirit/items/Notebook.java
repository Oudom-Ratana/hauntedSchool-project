package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Notebook extends Item {

    public Notebook() {
        super("notebook", "Notebook", false, Color.web("#d8c9a7"));
    }

    @Override
    public String getUseMessage() {
        return "The notebook records clues from the abandoned school.";
    }
}
