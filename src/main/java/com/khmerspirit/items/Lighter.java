package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Lighter extends Item {

    public Lighter() {
        super("lighter", "Brass Lighter", false, Color.web("#d87834"));
    }

    @Override
    public String getUseMessage() {
        return "Toggles a warm 360° firelight glow (+140px) around player.";
    }

    @Override
    public String getAbilityDescription() {
        return "Vintage brass lighter providing warm 360-degree ambient firelight (+140px radius) even when flashlight battery is drained.";
    }
}
