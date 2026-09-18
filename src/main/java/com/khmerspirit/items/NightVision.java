package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class NightVision extends Item {

    public NightVision() {
        super("night_vision", "Night Vision Goggles", false, Color.web("#52c41a"));
    }

    @Override
    public String getUseMessage() {
        return "Night Vision active! Spectral ghost radar tracking engaged.";
    }

    @Override
    public String getAbilityDescription() {
        return "Toggles Night Vision mode. Outlines ghosts with bright emerald thermal signatures through pitch darkness and shows radar blips.";
    }
}
