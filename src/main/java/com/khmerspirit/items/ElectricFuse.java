package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class ElectricFuse extends Item {

    public ElectricFuse() {
        super("electric_fuse", "Electric Fuse", true, Color.web("#38bdf8"));
    }

    @Override
    public String getUseMessage() {
        return "Loaded electric fuse! Emergency generator running — room fully illuminated for 20 seconds!";
    }

    @Override
    public String getAbilityDescription() {
        return "Heavy-duty ceramic fuse that powers emergency circuit breakers, flooding the current room with illumination for 20 seconds.";
    }
}
