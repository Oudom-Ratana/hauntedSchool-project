package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class AcidBottle extends Item {

    public AcidBottle() {
        super("acid_bottle", "Acid Bottle", true, Color.web("#4ade80"));
    }

    @Override
    public String getUseMessage() {
        return "You throw the acid bottle! Melts adjacent door locks or dissolves pursuing spirits in contact!";
    }

    @Override
    public String getAbilityDescription() {
        return "Corrosive holy alchemical solvent that dissolves locked doors without keys, or banishes nearby spirits upon direct contact.";
    }
}
