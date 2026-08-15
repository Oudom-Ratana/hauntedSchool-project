package com.khmerspirit.player;

import javafx.scene.input.KeyCode;

import java.util.EnumSet;
import java.util.Set;

public class PlayerController {

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final Set<KeyCode> justPressedKeys = EnumSet.noneOf(KeyCode.class);

    public void press(KeyCode keyCode) {
        if (!pressedKeys.contains(keyCode)) {
            justPressedKeys.add(keyCode);
        }
        pressedKeys.add(keyCode);
    }

    public void release(KeyCode keyCode) {
        pressedKeys.remove(keyCode);
    }

    public double getHorizontalAxis() {
        double axis = 0.0;
        if (pressedKeys.contains(KeyCode.A)) {
            axis -= 1.0;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            axis += 1.0;
        }
        return axis;
    }

    public double getVerticalAxis() {
        double axis = 0.0;
        if (pressedKeys.contains(KeyCode.W)) {
            axis -= 1.0;
        }
        if (pressedKeys.contains(KeyCode.S)) {
            axis += 1.0;
        }
        return axis;
    }

    public boolean consumePressed(KeyCode keyCode) {
        return justPressedKeys.remove(keyCode);
    }
}
