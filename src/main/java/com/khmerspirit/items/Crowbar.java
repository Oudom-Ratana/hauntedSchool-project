package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class Crowbar extends Item {

    public Crowbar() {
        super("crowbar", "Crowbar", false, Color.web("#94a3b8"));
    }

    @Override
    public String getUseMessage() {
        return "Pried open the locked door mechanism with the heavy steel crowbar!";
    }

    @Override
    public String getAbilityDescription() {
        return "Heavy forged steel crowbar that forces open locked doors and barriers without needing keys.";
    }
}
