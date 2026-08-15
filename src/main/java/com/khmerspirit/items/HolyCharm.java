package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class HolyCharm extends Item {

    public HolyCharm() {
        super("holy_charm", "Holy Charm", true, Color.web("#b08ce0"));
    }

    @Override
    public String getUseMessage() {
        return "The holy charm warms your hand and pushes fear away.";
    }
}
