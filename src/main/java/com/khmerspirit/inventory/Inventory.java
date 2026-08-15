package com.khmerspirit.inventory;

import com.khmerspirit.items.Item;
import com.khmerspirit.items.ItemRegistry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Inventory {

    private final Map<String, Integer> itemCounts = new LinkedHashMap<>();

    public void addItem(Item item) {
        itemCounts.merge(item.getId(), 1, Integer::sum);
    }

    public boolean hasItem(String itemId) {
        return itemCounts.getOrDefault(itemId, 0) > 0;
    }

    public Optional<Item> getItemAtSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= itemCounts.size()) {
            return Optional.empty();
        }

        int index = 0;
        for (String itemId : itemCounts.keySet()) {
            if (index == slotIndex) {
                return ItemRegistry.findById(itemId);
            }
            index++;
        }
        return Optional.empty();
    }

    public String useSlot(int slotIndex) {
        Optional<Item> item = getItemAtSlot(slotIndex);
        if (item.isEmpty()) {
            return "No item in that slot.";
        }

        Item usableItem = item.get();
        if (!hasItem(usableItem.getId())) {
            return "No item in that slot.";
        }

        if (usableItem.isConsumable()) {
            removeOne(usableItem.getId());
        }
        return usableItem.getUseMessage();
    }

    public Map<String, Integer> getItemCounts() {
        return Collections.unmodifiableMap(itemCounts);
    }

    public void replaceAll(Map<String, Integer> loadedItems) {
        itemCounts.clear();
        loadedItems.forEach((itemId, count) -> {
            if (count > 0 && ItemRegistry.findById(itemId).isPresent()) {
                itemCounts.put(itemId, count);
            }
        });
    }

    private void removeOne(String itemId) {
        int remaining = itemCounts.getOrDefault(itemId, 0) - 1;
        if (remaining <= 0) {
            itemCounts.remove(itemId);
        } else {
            itemCounts.put(itemId, remaining);
        }
    }
}
