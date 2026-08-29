package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing a game Reward configuration in the Admin Panel.
 */
public class RewardModel {

    private String id;
    private String name;
    private String type; // e.g. Item, Key, Health, Buff, Battery
    private String description;
    private int quantity;

    public RewardModel() {
        this.quantity = 1;
    }

    public RewardModel(String id, String name, String type, String description, int quantity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.quantity = quantity;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardModel that = (RewardModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
