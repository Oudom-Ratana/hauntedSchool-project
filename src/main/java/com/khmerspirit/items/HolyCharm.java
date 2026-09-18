package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class HolyCharm extends Item {

    public HolyCharm() {
        super("holy_charm", "Holy Charm", true, Color.web("#b08ce0"));
    }

    @Override
    public String getUseMessage() {
        return "Holy Charm flared with divine light! Spirits shrieked and fled for 8 seconds!";
    }

    @Override
    public String getAbilityDescription() {
        return "Angkorian protective amulet that bursts with holy solar light, terrifying all spirits within 500px and forcing them to flee for 8s.";
    }
}
