package com.khmerspirit.inventory;

import com.khmerspirit.items.Item;
import com.khmerspirit.items.ItemRegistry;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryUI {

    private static final int SLOT_SIZE = 54;
    private static final int SLOT_GAP = 8;
    private static final int MAX_VISIBLE_SLOTS = 10;

    public void render(GraphicsContext graphics, Inventory inventory, double canvasWidth, double canvasHeight) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(inventory.getItemCounts().entrySet());
        double panelWidth = MAX_VISIBLE_SLOTS * SLOT_SIZE + (MAX_VISIBLE_SLOTS - 1) * SLOT_GAP + 24;
        double panelX = (canvasWidth - panelWidth) / 2.0;
        double panelY = canvasHeight - 86;

        graphics.setFill(Color.rgb(4, 7, 7, 0.82));
        graphics.fillRect(panelX, panelY, panelWidth, 74);
        graphics.setStroke(Color.web("#7b633b"));
        graphics.strokeRect(panelX + 0.5, panelY + 0.5, panelWidth - 1, 73);

        for (int slot = 0; slot < MAX_VISIBLE_SLOTS; slot++) {
            double x = panelX + 12 + slot * (SLOT_SIZE + SLOT_GAP);
            double y = panelY + 10;
            renderSlot(graphics, x, y, slot, slot < entries.size() ? entries.get(slot) : null);
        }
    }

    private void renderSlot(GraphicsContext graphics, double x, double y, int slot, Map.Entry<String, Integer> entry) {
        graphics.setFill(Color.web("#101817"));
        graphics.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);
        graphics.setStroke(Color.web("#5c4a2f"));
        graphics.strokeRect(x + 0.5, y + 0.5, SLOT_SIZE - 1, SLOT_SIZE - 1);

        graphics.setFill(Color.web("#d8c393"));
        graphics.fillText(slot == 9 ? "0" : Integer.toString(slot + 1), x + 5, y + 13);

        if (entry == null) {
            return;
        }

        Item item = ItemRegistry.findById(entry.getKey()).orElse(null);
        if (item == null) {
            return;
        }

        graphics.setFill(item.getColor());
        graphics.fillRect(x + 17, y + 15, 24, 24);
        graphics.setStroke(Color.web("#f2d99b"));
        graphics.strokeRect(x + 17.5, y + 15.5, 23, 23);

        graphics.setFill(Color.web("#efe2bf"));
        graphics.fillText("x" + entry.getValue(), x + 30, y + 49);
        graphics.fillText(shortName(item.getDisplayName()), x + 4, y + 64);
    }

    private String shortName(String name) {
        return name.length() <= 8 ? name : name.substring(0, 8);
    }
}
