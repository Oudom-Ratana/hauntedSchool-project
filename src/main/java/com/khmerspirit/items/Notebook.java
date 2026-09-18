package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Notebook extends Item {

    public Notebook() {
        super("notebook", "Notebook", false, Color.web("#d8c9a7"));
    }

    @Override
    public String getUseMessage() {
        return "Investigator's Notebook: Examined school records and quiz clues for this room!";
    }

    @Override
    public String getAbilityDescription() {
        return "Leather journal recording research notes, historical school records, and question clues for the current room.";
    }
}
