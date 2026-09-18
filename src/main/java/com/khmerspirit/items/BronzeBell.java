package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class BronzeBell extends Item {

    public BronzeBell() {
        super("bronze_bell", "Bronze Bell", false, Color.web("#eab308"));
    }

    @Override
    public String getUseMessage() {
        return "You chime the sacred Bronze Bell! Nearby spirits are stunned and frozen for 5 seconds!";
    }

    @Override
    public String getAbilityDescription() {
        return "Sacred temple bronze chime. Emits a resonant sonic boom freezing and stunning all spirits within 450px for 5.0 seconds.";
    }
}
