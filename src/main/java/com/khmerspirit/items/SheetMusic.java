package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class SheetMusic extends Item {

    public SheetMusic() {
        super("sheet_music", "Sheet Music", false, Color.web("#fde047"));
    }

    @Override
    public String getUseMessage() {
        return "Played Angkorian classical melody! Haunted spirits calmed into peaceful wandering for 12 seconds!";
    }

    @Override
    public String getAbilityDescription() {
        return "Melodic Khmer classical score whose haunting harmonics pacify enraged ghosts back into tranquil wandering for 12 seconds.";
    }
}
