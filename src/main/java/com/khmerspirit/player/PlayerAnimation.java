package com.khmerspirit.player;

import com.khmerspirit.config.Constants;
import javafx.geometry.Rectangle2D;

public class PlayerAnimation {

    public enum State {
        IDLE,
        WALK,
        RUN
    }

    private static final double FRAME_DURATION_WALK = 0.14;
    private static final double FRAME_DURATION_RUN = 0.08;
    private static final double RUN_THRESHOLD = 180.0;

    private State state = State.IDLE;
    private int directionRow;
    private int frameIndex;
    private double elapsedSeconds;
    private double lastVelocityMagnitude;

    public void update(double deltaSeconds, double velocityX, double velocityY) {
        lastVelocityMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        boolean moving = lastVelocityMagnitude > 0.01;

        updateDirection(velocityX, velocityY);

        if (!moving) {
            state = State.IDLE;
            frameIndex = 0;
            elapsedSeconds = 0.0;
            return;
        }

        state = lastVelocityMagnitude > RUN_THRESHOLD ? State.RUN : State.WALK;

        double frameDuration = state == State.RUN ? FRAME_DURATION_RUN : FRAME_DURATION_WALK;
        elapsedSeconds += deltaSeconds;

        if (elapsedSeconds >= frameDuration) {
            frameIndex = (frameIndex + 1) % 4;
            elapsedSeconds = 0.0;
        }
    }

    public Rectangle2D getViewport() {
        return new Rectangle2D(
                frameIndex * Constants.PLAYER_WIDTH,
                directionRow * Constants.PLAYER_HEIGHT,
                Constants.PLAYER_WIDTH,
                Constants.PLAYER_HEIGHT
        );
    }

    public State getState() {
        return state;
    }

    private void updateDirection(double velocityX, double velocityY) {
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            directionRow = velocityX < 0 ? 1 : 2;
        } else if (Math.abs(velocityY) > 0.01) {
            directionRow = velocityY < 0 ? 3 : 0;
        }
    }
}
