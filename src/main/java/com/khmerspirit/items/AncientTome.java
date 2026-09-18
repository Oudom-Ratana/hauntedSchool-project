package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class AncientTome extends Item {

    public AncientTome() {
        super("ancient_tome", "Ancient Occult Tome", false, Color.web("#c084fc"));
    }

    @Override
    public String getUseMessage() {
        return "Chanted Pali sacred scriptures! Divine Aegis grants 12 seconds of invulnerability from ghosts!";
    }

    @Override
    public String getAbilityDescription() {
        return "Sacred palm-leaf scripture that invokes a divine shield, protecting the player from ghost strikes and damage for 12 seconds.";
    }
}
