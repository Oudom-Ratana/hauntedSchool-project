package com.khmerspirit.items;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ItemRegistry {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    static {
        register(new Flashlight());
        register(new Battery());
        register(new Key());
        register(new MasterKey());
        register(new HolyCharm());
        register(new FirstAidKit());
        register(new Notebook());
        register(new Lighter());
        register(new MapItem());
        register(new Toolbox());
    }

    private ItemRegistry() {
    }

    public static Optional<Item> findById(String id) {
        return Optional.ofNullable(ITEMS.get(id));
    }

    public static Collection<Item> getAllItems() {
        return ITEMS.values();
    }

    private static void register(Item item) {
        ITEMS.put(item.getId(), item);
    }
}
