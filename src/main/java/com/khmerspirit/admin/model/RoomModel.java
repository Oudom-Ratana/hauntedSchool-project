package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing a game Room configuration in the Admin Panel.
 */
public class RoomModel {

    private String id;
    private String name;
    private String description;
    private int requiredQs; // Default: 5
    private String reward;
    private String nextRoomId;
    private boolean active;

    public RoomModel() {
        this.requiredQs = 5;
        this.active = true;
    }

    public RoomModel(String id, String name, String description, int requiredQs, String reward, String nextRoomId, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredQs = requiredQs > 0 ? requiredQs : 5;
        this.reward = reward;
        this.nextRoomId = nextRoomId;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRequiredQs() {
        return requiredQs;
    }

    public void setRequiredQs(int requiredQs) {
        this.requiredQs = requiredQs;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getNextRoomId() {
        return nextRoomId;
    }

    public void setNextRoomId(String nextRoomId) {
        this.nextRoomId = nextRoomId;
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
        RoomModel roomModel = (RoomModel) o;
        return Objects.equals(id, roomModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name != null ? name + " (" + id + ")" : id;
    }
}
