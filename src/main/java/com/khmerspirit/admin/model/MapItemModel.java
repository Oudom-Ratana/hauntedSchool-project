package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing an item pickup placed directly on the live game map by the Admin.
 */
public class MapItemModel {

    private String id;
    private String itemId;
    private String itemName;
    private String roomId;
    private double tileX;
    private double tileY;
    private String description;
    private boolean active;

    public MapItemModel() {
        this.active = true;
    }

    public MapItemModel(String id, String itemId, String itemName, String roomId, double tileX, double tileY, String description, boolean active) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.roomId = roomId;
        this.tileX = tileX;
        this.tileY = tileY;
        this.description = description;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public double getTileX() {
        return tileX;
    }

    public void setTileX(double tileX) {
        this.tileX = tileX;
    }

    public double getTileY() {
        return tileY;
    }

    public void setTileY(double tileY) {
        this.tileY = tileY;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapItemModel that = (MapItemModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
