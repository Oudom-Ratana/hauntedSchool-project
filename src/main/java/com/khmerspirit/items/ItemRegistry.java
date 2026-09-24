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
        register(new Key("key_classroomB", "Teachers' Lounge Key"));
        register(new Key("key_computer", "Music & Art Key"));
        register(new Key("key_entrance", "Restroom Key"));
        register(new Key("key_dormitory", "Infirmary Key"));
        register(new Key("key_basement", "Storage Key"));
        register(new Key("key_laboratory", "Science Lab Key"));
        register(new Key("key_library", "Library Key"));
        register(new Key("key_principal", "Headmaster's Key"));
        register(new HolyCharm());
        register(new FirstAidKit());
        register(new Notebook());
        register(new Lighter());
        register(new MapItem());
        register(new Toolbox());
        register(new AcidBottle());
        register(new Crowbar());
        register(new ElectricFuse());
        register(new AncientTome());
        register(new MedicineBottle());
        register(new SheetMusic());
        register(new BronzeBell());
        register(new NightVision());
    }

    private ItemRegistry() {
    }

    public static Optional<Item> findById(String id) {
        if (id == null) return Optional.empty();
        Item item = ITEMS.get(id);
        if (item != null) return Optional.of(item);
        for (Map.Entry<String, Item> entry : ITEMS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(id)) {
                return Optional.of(entry.getValue());
            }
        }
        if (id.toLowerCase().startsWith("key_")) {
            String roomPart = id.substring(4);
            Key dynamicKey = new Key(id, roomPart.substring(0, 1).toUpperCase() + roomPart.substring(1) + " Key");
            ITEMS.put(id, dynamicKey);
            return Optional.of(dynamicKey);
        }
        return Optional.empty();
    }

    public static Collection<Item> getAllItems() {
        return ITEMS.values();
    }

    private static void register(Item item) {
        ITEMS.put(item.getId(), item);
    }
}
