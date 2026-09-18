package com.khmerspirit.map;

import javafx.geometry.Rectangle2D;

public class Door {

    private final String id;
    private final int column;
    private final int row;
    private final String fromRoomId;
    private final String toRoomId;
    private boolean open;
    private boolean locked;
    private String requiredKeyId;
    private Rectangle2D bounds;
    private Rectangle2D collisionBox;
    private Rectangle2D wallPatchBounds;
    private Rectangle2D wallPatchSource;

    public Door(int column, int row, String fromRoomId, String toRoomId) {
        this("door_" + column + "_" + row, column, row, fromRoomId, toRoomId, false, true, "key", null);
    }

    public Door(String id, int column, int row, String fromRoomId, String toRoomId,
                boolean open, boolean locked, String requiredKeyId, Rectangle2D bounds) {
        this.id = id;
        this.column = column;
        this.row = row;
        this.fromRoomId = fromRoomId;
        this.toRoomId = toRoomId;
        this.open = open;
        this.locked = locked;
        this.requiredKeyId = requiredKeyId;
        this.bounds = bounds;
        this.collisionBox = bounds;
    }

    public String getId() {
        return id;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getFromRoomId() {
        return fromRoomId;
    }

    public String getToRoomId() {
        return toRoomId;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public void setRequiredKeyId(String requiredKeyId) {
        this.requiredKeyId = requiredKeyId;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
        if (this.collisionBox == null) {
            this.collisionBox = bounds;
        }
    }

    public Rectangle2D getCollisionBox() {
        return collisionBox != null ? collisionBox : bounds;
    }

    public void setCollisionBox(Rectangle2D collisionBox) {
        this.collisionBox = collisionBox;
    }

    public Rectangle2D getWallPatchBounds() {
        return wallPatchBounds;
    }

    public void setWallPatchBounds(Rectangle2D wallPatchBounds) {
        this.wallPatchBounds = wallPatchBounds;
    }

    public Rectangle2D getWallPatchSource() {
        return wallPatchSource;
    }

    public void setWallPatchSource(Rectangle2D wallPatchSource) {
        this.wallPatchSource = wallPatchSource;
    }

    public boolean isNear(double worldX, double worldY, double maxDistance) {
        Rectangle2D targetBox = bounds != null ? bounds : collisionBox;
        if (targetBox != null) {
            double closestX = Math.max(targetBox.getMinX(), Math.min(worldX, targetBox.getMaxX()));
            double closestY = Math.max(targetBox.getMinY(), Math.min(worldY, targetBox.getMaxY()));
            double dx = worldX - closestX;
            double dy = worldY - closestY;
            return (dx * dx + dy * dy) <= (maxDistance * maxDistance);
        }
        double cx = (column + 0.5) * com.khmerspirit.config.Constants.TILE_SIZE;
        double cy = (row + 0.5) * com.khmerspirit.config.Constants.TILE_SIZE;
        double dx = worldX - cx;
        double dy = worldY - cy;
        return (dx * dx + dy * dy) <= (maxDistance * maxDistance);
    }
}
