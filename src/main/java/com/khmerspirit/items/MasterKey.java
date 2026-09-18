package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class MasterKey extends Item {

    public MasterKey() {
        super("master_key", "Grand Master Key", false, Color.web("#f0cf65"));
    }

    @Override
    public String getUseMessage() {
        return "Grand Master Key: Unlocks ANY door in the entire school, and the final Grand Exit Gate!";
    }

    @Override
    public String getAbilityDescription() {
        return "Sacred golden key of the Principal. Bypasses all locks in the school and unlocks the final Grand Exit Gate to win!";
    }
}
