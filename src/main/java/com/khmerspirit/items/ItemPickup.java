package com.khmerspirit.items;

import com.khmerspirit.config.Constants;
import com.khmerspirit.player.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ItemPickup {

    private static final double PICKUP_RADIUS = 46.0;

    private final Item item;
    private final double x;
    private final double y;

    public ItemPickup(Item item, double tileColumn, double tileRow) {
        this.item = item;
        this.x = tileColumn * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
        this.y = tileRow * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public Item getItem() {
        return item;
    }

    public boolean isNear(double worldX, double worldY) {
        double deltaX = x - worldX;
        double deltaY = y - worldY;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY) <= PICKUP_RADIUS;
    }

    public void render(GraphicsContext graphics, Camera camera) {
        double screenX = x - camera.getX();
        double screenY = y - camera.getY();
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        graphics.fillOval(screenX - 14, screenY - 8, 28, 16);
        graphics.setFill(item.getColor());
        graphics.fillRect(Math.round(screenX - 10), Math.round(screenY - 20), 20, 20);
        graphics.setStroke(Color.web("#f1d28a"));
        graphics.strokeRect(Math.round(screenX - 10) + 0.5, Math.round(screenY - 20) + 0.5, 19, 19);
    }
}
