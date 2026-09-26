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
        render(graphics, inventory, canvasWidth, canvasHeight, null);
    }

    public void render(GraphicsContext graphics, Inventory inventory, double canvasWidth, double canvasHeight, com.khmerspirit.core.AssetManager assetManager) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(inventory.getItemCounts().entrySet());
        double panelWidth = MAX_VISIBLE_SLOTS * SLOT_SIZE + (MAX_VISIBLE_SLOTS - 1) * SLOT_GAP + 24;
        double panelX = (canvasWidth - panelWidth) / 2.0;
        double panelY = canvasHeight - 88;

        graphics.save();

        // 1. Frosted Glassmorphism Panel Body with Soft Shadow
        graphics.setFill(Color.rgb(0, 0, 0, 0.65));
        graphics.fillRoundRect(panelX - 2, panelY - 2, panelWidth + 4, 78, 14, 14);

        javafx.scene.paint.LinearGradient panelGrad = new javafx.scene.paint.LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0.0, Color.rgb(15, 23, 42, 0.92)),
                new javafx.scene.paint.Stop(1.0, Color.rgb(3, 7, 18, 0.96))
        );
        graphics.setFill(panelGrad);
        graphics.fillRoundRect(panelX, panelY, panelWidth, 76, 12, 12);

        // Cyber / Golden Rim Border
        graphics.setStroke(Color.rgb(245, 158, 11, 0.70));
        graphics.setLineWidth(1.4);
        graphics.strokeRoundRect(panelX + 0.5, panelY + 0.5, panelWidth - 1, 75, 12, 12);

        // Top subtle metallic highlight line
        graphics.setStroke(Color.rgb(255, 255, 255, 0.20));
        graphics.setLineWidth(1.0);
        graphics.strokeLine(panelX + 16, panelY + 1.5, panelX + panelWidth - 16, panelY + 1.5);

        // 2. Render each of the 10 quick-slots
        for (int slot = 0; slot < MAX_VISIBLE_SLOTS; slot++) {
            double x = panelX + 12 + slot * (SLOT_SIZE + SLOT_GAP);
            double y = panelY + 10;
            renderSlot(graphics, x, y, slot, slot < entries.size() ? entries.get(slot) : null, assetManager);
        }

        graphics.restore();
    }

    private void renderSlot(GraphicsContext graphics, double x, double y, int slot, Map.Entry<String, Integer> entry, com.khmerspirit.core.AssetManager assetManager) {
        boolean hasItem = (entry != null);

        // Slot container with rounded corners & dark slate inset
        if (hasItem) {
            graphics.setFill(Color.rgb(20, 32, 54, 0.90));
            graphics.fillRoundRect(x, y, SLOT_SIZE, SLOT_SIZE, 8, 8);
            graphics.setStroke(Color.rgb(245, 158, 11, 0.65));
            graphics.setLineWidth(1.2);
            graphics.strokeRoundRect(x + 0.5, y + 0.5, SLOT_SIZE - 1, SLOT_SIZE - 1, 8, 8);
        } else {
            graphics.setFill(Color.rgb(8, 14, 24, 0.70));
            graphics.fillRoundRect(x, y, SLOT_SIZE, SLOT_SIZE, 8, 8);
            graphics.setStroke(Color.rgb(30, 48, 75, 0.50));
            graphics.setLineWidth(1.0);
            graphics.strokeRoundRect(x + 0.5, y + 0.5, SLOT_SIZE - 1, SLOT_SIZE - 1, 8, 8);
        }

        // Slot number indicator badge (1-9, 0)
        graphics.setFont(javafx.scene.text.Font.font("Consolas", javafx.scene.text.FontWeight.BOLD, 10));
        graphics.setFill(hasItem ? Color.rgb(250, 204, 21, 0.95) : Color.rgb(100, 116, 139, 0.70));
        graphics.fillText(slot == 9 ? "0" : Integer.toString(slot + 1), x + 5, y + 12);

        if (!hasItem) {
            // Subtle empty slot crosshair dot
            graphics.setFill(Color.rgb(51, 65, 85, 0.40));
            graphics.fillOval(x + SLOT_SIZE / 2.0 - 2, y + SLOT_SIZE / 2.0 - 2, 4, 4);
            return;
        }

        Item item = ItemRegistry.findById(entry.getKey()).orElse(null);
        if (item == null) {
            return;
        }

        // Item icon sprite
        javafx.scene.image.Image sprite = assetManager != null ? assetManager.loadItemSprite(item.getId()) : null;
        if (sprite != null) {
            graphics.drawImage(sprite, x + 11, y + 10, 32, 32);
        } else {
            graphics.setFill(item.getColor());
            graphics.fillRoundRect(x + 15, y + 13, 24, 24, 4, 4);
            graphics.setStroke(Color.rgb(250, 204, 21, 0.90));
            graphics.setLineWidth(1.0);
            graphics.strokeRoundRect(x + 15.5, y + 13.5, 23, 23, 4, 4);
        }

        // Quantity capsule badge if count > 1
        int count = entry.getValue();
        if (count > 1) {
            String countStr = "x" + count;
            graphics.setFill(Color.rgb(15, 23, 42, 0.90));
            graphics.fillRoundRect(x + 28, y + 36, 23, 14, 4, 4);
            graphics.setStroke(Color.rgb(245, 158, 11, 0.85));
            graphics.setLineWidth(0.8);
            graphics.strokeRoundRect(x + 28.5, y + 36.5, 22, 13, 4, 4);

            graphics.setFont(javafx.scene.text.Font.font("Consolas", javafx.scene.text.FontWeight.BOLD, 9));
            graphics.setFill(Color.rgb(254, 240, 138, 0.98));
            graphics.fillText(countStr, x + 31, y + 47);
        }

        // Clean, legible item name below slot
        graphics.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 9));
        graphics.setFill(Color.rgb(241, 245, 249, 0.95));
        String label = shortName(item.getDisplayName());
        graphics.fillText(label, x + (SLOT_SIZE - label.length() * 5.2) / 2.0, y + 63);
    }

    private String shortName(String name) {
        if (name == null) return "";
        if (name.length() <= 8) return name;
        return name.substring(0, 7) + "…";
    }
}
