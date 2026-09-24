package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing a collision barrier rectangle in 1376x768 native map coordinates.
 */
public class RoomBarrierModel {

    private double x;
    private double y;
    private double width;
    private double height;
    private String label;

    public RoomBarrierModel() {
        this(0, 0, 100, 100, "Obstacle");
    }

    public RoomBarrierModel(double x, double y, double width, double height, String label) {
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.width = Math.max(10, Math.round(width));
        this.height = Math.max(10, Math.round(height));
        this.label = (label == null || label.isBlank()) ? "Obstacle" : label;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = Math.round(x);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = Math.round(y);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = Math.max(10, Math.round(width));
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = Math.max(10, Math.round(height));
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomBarrierModel that = (RoomBarrierModel) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.width, width) == 0 &&
                Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return label + " (" + (int) x + ", " + (int) y + ") " + (int) width + "x" + (int) height;
    }
}
