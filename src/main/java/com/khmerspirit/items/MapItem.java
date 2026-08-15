package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class MapItem extends Item {

    public MapItem() {
        super("map", "Map", false, Color.web("#83a876"));
    }

    @Override
    public String getUseMessage() {
        return "The map shows the rooms connected by the main hall.";
    }
}
