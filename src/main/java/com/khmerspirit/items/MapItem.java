package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class MapItem extends Item {

    public MapItem() {
        super("map", "School Blueprint", false, Color.web("#83a876"));
    }

    @Override
    public String getUseMessage() {
        return "Architectural Blueprint: Displays full school floor layout, rooms, and quiz stations! [Press M to toggle]";
    }

    @Override
    public String getAbilityDescription() {
        return "Detailed architectural blueprint of the haunted school showing room layout, connected wings, and exit gates.";
    }
}
