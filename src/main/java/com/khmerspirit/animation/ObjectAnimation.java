package com.khmerspirit.animation;

public class ObjectAnimation {

    public enum Type {
        DOOR,
        LAMP,
        RAIN,
        TORCH
    }

    private final Type type;
    private double elapsedSeconds = 0.0;
    private double rotation = 0.0;
    private double scale = 1.0;
    private double flicker = 1.0;
    private double offsetY = 0.0;

    public ObjectAnimation(Type type) {
        this.type = type;
    }

    public void update(double deltaSeconds) {
        elapsedSeconds += deltaSeconds;

        switch (type) {
            case DOOR -> {
                offsetY = Math.sin(elapsedSeconds * 2.0) * 2.0;
            }
            case LAMP -> {
                flicker = 0.7 + 0.3 * Math.abs(Math.sin(elapsedSeconds * 6.0));
                rotation = Math.sin(elapsedSeconds * 1.5) * 3.0;
            }
            case RAIN -> {
                offsetY = (elapsedSeconds * 200.0) % 600.0;
            }
            case TORCH -> {
                flicker = 0.6 + 0.4 * Math.sin(elapsedSeconds * 5.0);
                scale = 0.95 + 0.05 * Math.sin(elapsedSeconds * 3.0);
            }
        }
    }

    public double getRotation() {
        return rotation;
    }

    public double getScale() {
        return Math.max(0.5, scale);
    }

    public double getFlicker() {
        return Math.max(0.0, Math.min(1.0, flicker));
    }

    public double getOffsetY() {
        return offsetY;
    }

    public Type getType() {
        return type;
    }

    public void reset() {
        elapsedSeconds = 0.0;
        rotation = 0.0;
        scale = 1.0;
        flicker = 1.0;
        offsetY = 0.0;
    }
}
