package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class MasterKey extends Item {

    public MasterKey() {
        super("master_key", "Master Key", false, Color.web("#f0cf65"));
    }

    @Override
    public String getUseMessage() {
        return "The master key can open restricted school doors.";
    }
}
