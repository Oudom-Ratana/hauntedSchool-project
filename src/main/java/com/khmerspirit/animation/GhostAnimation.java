package com.khmerspirit.animation;

public class GhostAnimation {

    public enum State {
        FLOAT,
        ATTACK,
        DISAPPEAR
    }

    private State state = State.FLOAT;
    private double elapsedSeconds = 0.0;
    private double scale = 1.0;
    private double opacity = 0.8;

    public void update(double deltaSeconds, State targetState) {
        state = targetState;
        elapsedSeconds += deltaSeconds;

        switch (state) {
            case FLOAT -> {
                scale = 0.9 + 0.15 * Math.sin(elapsedSeconds * 4.0);
                opacity = 0.6 + 0.25 * Math.sin(elapsedSeconds * 3.0);
            }
            case ATTACK -> {
                if (elapsedSeconds < 0.4) {
                    scale = 1.0 + (elapsedSeconds / 0.4) * 0.3;
                    opacity = 0.9;
                } else {
                    scale = 1.3 - ((elapsedSeconds - 0.4) / 0.2) * 0.3;
                    opacity = 0.9 - ((elapsedSeconds - 0.4) / 0.2) * 0.4;
                }
            }
            case DISAPPEAR -> {
                double progress = Math.min(1.0, elapsedSeconds / 0.8);
                scale = 1.0 * (1.0 - progress * 0.4);
                opacity = 0.8 * (1.0 - progress);
            }
        }
    }

    public void reset() {
        elapsedSeconds = 0.0;
        scale = 1.0;
        opacity = 0.8;
    }

    public double getScale() {
        return Math.max(0.0, scale);
    }

    public double getOpacity() {
        return Math.max(0.0, Math.min(1.0, opacity));
    }

    public State getState() {
        return state;
    }

    public boolean isDisappeared() {
        return state == State.DISAPPEAR && elapsedSeconds > 0.8;
    }
}
