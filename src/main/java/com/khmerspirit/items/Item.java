package com.khmerspirit.items;

import javafx.scene.paint.Color;

public abstract class Item {

    private final String id;
    private final String displayName;
    private final boolean consumable;
    private final Color color;

    protected Item(String id, String displayName, boolean consumable, Color color) {
        this.id = id;
        this.displayName = displayName;
        this.consumable = consumable;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public Color getColor() {
        return color;
    }

    public abstract String getUseMessage();
}
