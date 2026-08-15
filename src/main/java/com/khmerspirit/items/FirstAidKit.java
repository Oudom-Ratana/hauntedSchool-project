package com.khmerspirit.items;

import javafx.scene.paint.Color;

public class FirstAidKit extends Item {

    public FirstAidKit() {
        super("first_aid_kit", "First Aid Kit", true, Color.web("#c9483e"));
    }

    @Override
    public String getUseMessage() {
        return "First aid restored your confidence.";
    }
}
