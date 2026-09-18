package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class FirstAidKit extends Item {

    public FirstAidKit() {
        super("first_aid_kit", "First Aid Kit", true, Color.web("#c9483e"));
    }

    @Override
    public String getUseMessage() {
        return "Used First Aid Kit! Bandaged wounds and restored 2 Life Hearts (up to max 5)!";
    }

    @Override
    public String getAbilityDescription() {
        return "Sterile medical emergency kit containing bandages and antiseptics that restores 2 Life Hearts.";
    }
}
