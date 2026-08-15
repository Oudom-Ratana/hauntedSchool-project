package com.khmerspirit.map;

import com.khmerspirit.config.Constants;

public class CollisionMap {

    private final TileMap tileMap;

    public CollisionMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public boolean isBlocked(double x, double y, double width, double height) {
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
