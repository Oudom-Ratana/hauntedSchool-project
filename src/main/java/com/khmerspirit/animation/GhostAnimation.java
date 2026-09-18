package com.khmerspirit.animation;

public class GhostAnimation {

    public enum State {
        FLOAT,
        PATROL,
        CHASE,
        ATTACK,
        DISAPPEAR
    }

    private State state = State.FLOAT;
    private double elapsedSeconds = 0.0;
    private double scale = 1.0;
    private double opacity = 0.85;
    private double pulse = 0.0;
    private double jawOpen = 0.0;
    private double clawReach = 0.0;

    public void update(double deltaSeconds, State targetState) {
        if (state != targetState) {
            state = targetState;
            elapsedSeconds = 0.0;
        }
        elapsedSeconds += deltaSeconds;

        switch (state) {
            case FLOAT, PATROL -> {
                scale = 0.95 + 0.08 * Math.sin(elapsedSeconds * 3.5);
                opacity = 0.75 + 0.20 * Math.sin(elapsedSeconds * 2.8);
                pulse = 0.5 + 0.5 * Math.sin(elapsedSeconds * 2.0);
                jawOpen = Math.max(0.0, jawOpen - deltaSeconds * 2.0);
                clawReach = Math.max(0.0, clawReach - deltaSeconds * 2.0);
            }
            case CHASE -> {
                // Aggressive shivering, burning intensity
                scale = 1.05 + 0.12 * Math.sin(elapsedSeconds * 8.0);
                opacity = 0.85 + 0.15 * Math.sin(elapsedSeconds * 6.0);
                pulse = 0.6 + 0.4 * Math.sin(elapsedSeconds * 7.5);
                jawOpen = Math.min(1.0, jawOpen + deltaSeconds * 3.0);
                clawReach = Math.min(1.0, clawReach + deltaSeconds * 3.5);
            }
            case ATTACK -> {
                if (elapsedSeconds < 0.35) {
                    scale = 1.1 + (elapsedSeconds / 0.35) * 0.4;
                    opacity = 0.98;
                    pulse = 1.0;
                    jawOpen = 1.0;
                    clawReach = 1.2;
                } else {
                    scale = 1.5 - ((elapsedSeconds - 0.35) / 0.25) * 0.4;
                    opacity = 0.95 - ((elapsedSeconds - 0.35) / 0.25) * 0.3;
                    pulse = 0.8;
                }
            }
            case DISAPPEAR -> {
                double progress = Math.min(1.0, elapsedSeconds / 0.8);
                scale = 1.0 * (1.0 - progress * 0.4);
                opacity = 0.85 * (1.0 - progress);
                pulse = 0.0;
            }
        }
    }

    public void reset() {
        elapsedSeconds = 0.0;
        scale = 1.0;
        opacity = 0.85;
        pulse = 0.0;
        jawOpen = 0.0;
        clawReach = 0.0;
    }

    public double getScale() {
        return Math.max(0.0, scale);
    }

    public double getOpacity() {
        return Math.max(0.0, Math.min(1.0, opacity));
    }

    public double getPulse() {
        return pulse;
    }

    public double getJawOpen() {
        return jawOpen;
    }

    public double getClawReach() {
        return clawReach;
    }

    public State getState() {
        return state;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isDisappeared() {
        return state == State.DISAPPEAR && elapsedSeconds > 0.8;
    }
}
