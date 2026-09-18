package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class MedicineBottle extends Item {

    public MedicineBottle() {
        super("medicine_bottle", "Medicine Bottle", true, Color.web("#fb923c"));
    }

    @Override
    public String getUseMessage() {
        return "You drink the medicine tonic. Restored 1 Heart and gained +45% movement speed for 10s!";
    }

    @Override
    public String getAbilityDescription() {
        return "Ancient herbal tonic. Heals 1 Life Heart and grants a +45% sprint movement speed boost for 10 seconds.";
    }
}
