package com.khmerspirit.map;

import javafx.scene.paint.Color;

public enum Tile {

    FLOOR('.', false, Color.web("#242827"), Color.web("#1a1f1e")),
    WALL('#', true, Color.web("#111716"), Color.web("#34302b")),
    DESK('D', true, Color.web("#3a2a1f"), Color.web("#5b3d24")),
    CHAIR('h', true, Color.web("#211916"), Color.web("#6b4b2d")),
    BLACKBOARD('K', true, Color.web("#101816"), Color.web("#305044")),
    LOCKER('M', true, Color.web("#1d2528"), Color.web("#53656a")),
    CARPET('C', false, Color.web("#2e1719"), Color.web("#5a2422")),
    DOOR('O', false, Color.web("#6c5230"), Color.web("#a1773c")),
    ENTRANCE('N', false, Color.web("#27332e"), Color.web("#67825f")),
    EXIT('X', false, Color.web("#382828"), Color.web("#a64d3d")),
    SHELF('S', true, Color.web("#2b2118"), Color.web("#765536")),
    LAB_TABLE('L', true, Color.web("#263238"), Color.web("#5f7a82")),
    COMPUTER('P', true, Color.web("#1e2735"), Color.web("#6680a8")),
    BED('B', true, Color.web("#302937"), Color.web("#7c6a8c")),
    STAIRS('T', false, Color.web("#1c1716"), Color.web("#6d5b4b")),
    RITUAL_MARK('Y', false, Color.web("#241111"), Color.web("#8a1f1f")),
    CRACKED_FLOOR('R', false, Color.web("#1f2423"), Color.web("#3c4240"));

    private final char symbol;
    private final boolean solid;
    private final Color baseColor;
    private final Color accentColor;

    Tile(char symbol, boolean solid, Color baseColor, Color accentColor) {
        this.symbol = symbol;
        this.solid = solid;
        this.baseColor = baseColor;
        this.accentColor = accentColor;
    }

    public static Tile fromSymbol(char symbol) {
        for (Tile tile : values()) {
            if (tile.symbol == symbol) {
                return tile;
            }
        }
        return FLOOR;
    }

    public boolean isSolid() {
        return solid;
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }
}
