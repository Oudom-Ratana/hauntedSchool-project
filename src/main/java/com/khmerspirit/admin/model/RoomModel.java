package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing a game Room configuration in the Admin Panel,
 * including story guide instructions and key progression rewards.
 */
public class RoomModel {

    private String id;
    private String name;
    private String description;
    private int requiredQs; // Default: 5
    private String reward;
    private String nextRoomId;
    private boolean active;

    // Admin-managed Story & Quest Guide Panel data
    private String guideTitle;
    private String guideNarrative;
    private String guideNextStep;
    private String keyReward; // e.g. "key" or "master_key"

    public RoomModel() {
        this.requiredQs = 5;
        this.active = true;
        this.keyReward = "key";
    }

    public RoomModel(String id, String name, String description, int requiredQs, String reward, String nextRoomId, boolean active) {
        this(id, name, description, requiredQs, reward, nextRoomId, active, null, null, null, "key");
    }

    public RoomModel(String id, String name, String description, int requiredQs, String reward, String nextRoomId,
                     boolean active, String guideTitle, String guideNarrative, String guideNextStep, String keyReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredQs = requiredQs > 0 ? requiredQs : 5;
        this.reward = reward;
        this.nextRoomId = nextRoomId;
        this.active = active;
        this.guideTitle = guideTitle;
        this.guideNarrative = guideNarrative;
        this.guideNextStep = guideNextStep;
        this.keyReward = keyReward != null ? keyReward : "key";
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

    public String getGuideTitle() {
        return guideTitle;
    }

    public void setGuideTitle(String guideTitle) {
        this.guideTitle = guideTitle;
    }

    public String getGuideNarrative() {
        return guideNarrative;
    }

    public void setGuideNarrative(String guideNarrative) {
        this.guideNarrative = guideNarrative;
    }

    public String getGuideNextStep() {
        return guideNextStep;
    }

    public void setGuideNextStep(String guideNextStep) {
        this.guideNextStep = guideNextStep;
    }

    public String getKeyReward() {
        return keyReward != null ? keyReward : "key";
    }

    public void setKeyReward(String keyReward) {
        this.keyReward = keyReward;
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
        return name != null ? name + " [" + id + "]" : id;
    }
}
