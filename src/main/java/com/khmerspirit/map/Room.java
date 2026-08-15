package com.khmerspirit.map;

import com.khmerspirit.config.Constants;
import javafx.geometry.Rectangle2D;

public class Room {

    private final String id;
    private final String displayName;
    private final int column;
    private final int row;
    private final int width;
    private final int height;

    public Room(String id, String displayName, int column, int row, int width, int height) {
        this.id = id;
        this.displayName = displayName;
        this.column = column;
        this.row = row;
        this.width = width;
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean containsWorldPoint(double worldX, double worldY) {
        double tileSize = Constants.TILE_SIZE;
        return worldX >= column * tileSize
                && worldX < (column + width) * tileSize
                && worldY >= row * tileSize
                && worldY < (row + height) * tileSize;
    }

    public Rectangle2D toWorldBounds() {
        return new Rectangle2D(
                column * Constants.TILE_SIZE,
                row * Constants.TILE_SIZE,
                width * Constants.TILE_SIZE,
                height * Constants.TILE_SIZE
        );
    }
}
