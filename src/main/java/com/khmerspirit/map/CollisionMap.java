package com.khmerspirit.map;

import com.khmerspirit.config.Constants;
import javafx.geometry.Rectangle2D;

public class CollisionMap {

    private final TileMap tileMap;

    public CollisionMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public boolean isBlocked(double x, double y, double width, double height) {
        // World boundary check: prevent player from leaving map
        if (x < 0 || y < 0 || x + width > tileMap.getPixelWidth() || y + height > tileMap.getPixelHeight()) {
            return true;
        }

        // 1. Precise bounding-box collisions (for custom room layouts and objects)
        if (tileMap.hasPreciseCollision()) {
            for (Rectangle2D box : tileMap.getCollisionBoxes()) {
                if (box.intersects(x, y, width, height)) {
                    return true;
                }
            }
            return false;
        }

        // 2. Tile grid collision fallback
        int left = (int) Math.floor(x / Constants.TILE_SIZE);
        int right = (int) Math.floor((x + width - 1) / Constants.TILE_SIZE);
        int top = (int) Math.floor(y / Constants.TILE_SIZE);
        int bottom = (int) Math.floor((y + height - 1) / Constants.TILE_SIZE);

        for (int row = top; row <= bottom; row++) {
            for (int column = left; column <= right; column++) {
                if (tileMap.getTileAt(column, row).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}
