package com.khmerspirit.core;

import com.khmerspirit.config.Constants;
import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {

    private static final double FRAME_TIME_SECONDS = 1.0 / Constants.TARGET_FPS;
    private static final long FRAME_TIME_NANOS = 1_000_000_000L / Constants.TARGET_FPS;

    private final Game game;
    private long lastFrameTime;
    private long frameAccumulator;

    public GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void start() {
        lastFrameTime = 0L;
        frameAccumulator = 0L;
        super.start();
    }

    @Override
    public void handle(long now) {
        if (lastFrameTime == 0L) {
            lastFrameTime = now;
            game.render();
            return;
        }

        frameAccumulator += now - lastFrameTime;
        lastFrameTime = now;

        while (frameAccumulator >= FRAME_TIME_NANOS) {
            game.update(FRAME_TIME_SECONDS);
            frameAccumulator -= FRAME_TIME_NANOS;
        }

        game.render();
    }
}
