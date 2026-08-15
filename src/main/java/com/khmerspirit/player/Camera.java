package com.khmerspirit.player;

public class Camera {

    private final double viewportWidth;
    private final double viewportHeight;
    private final double worldWidth;
    private final double worldHeight;
    private double x;
    private double y;

    public Camera(double viewportWidth, double viewportHeight, double worldWidth, double worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void follow(double targetX, double targetY) {
        x = clamp(targetX - viewportWidth / 2.0, 0.0, Math.max(0.0, worldWidth - viewportWidth));
        y = clamp(targetY - viewportHeight / 2.0, 0.0, Math.max(0.0, worldHeight - viewportHeight));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getViewportWidth() {
        return viewportWidth;
    }

    public double getViewportHeight() {
        return viewportHeight;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
