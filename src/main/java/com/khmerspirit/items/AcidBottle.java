package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class AcidBottle extends Item {

    public AcidBottle() {
        super("acid_bottle", "Acid Bottle", true, Color.web("#4ade80"));
    }

    @Override
    public String getUseMessage() {
        return "You throw the acid bottle! Dissolves the lock of the Music & Art Room or kills a nearby ghost on contact!";
    }

    @Override
    public String getAbilityDescription() {
        return "Corrosive solvent: Opens ONLY the Music & Art Room door, or throws at a ghost to kill and dissolve it!";
    }
}
