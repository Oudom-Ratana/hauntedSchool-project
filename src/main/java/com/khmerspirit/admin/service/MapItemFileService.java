package com.khmerspirit.admin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.khmerspirit.admin.model.MapItemModel;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated File Service for managing map item pickups placed by the Admin.
 */
public class MapItemFileService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String RELATIVE_RESOURCE_PATH = "src/main/resources/config/map_items.json";
    private static final String RUNTIME_PATH = "config/map_items.json";
    private static final String CLASSPATH_RESOURCE = "/config/map_items.json";

    public List<MapItemModel> loadMapItems() {
        // 1. Try external/runtime file first
        File runtimeFile = new File(RUNTIME_PATH);
        if (runtimeFile.exists()) {
            List<MapItemModel> items = readFromFile(runtimeFile);
            if (items != null && !items.isEmpty()) return items;
        }

        // 2. Try src/main/resources file next
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.exists()) {
            List<MapItemModel> items = readFromFile(srcFile);
            if (items != null && !items.isEmpty()) return items;
        }

        // 3. Fallback to classpath resource
        try (InputStream is = getClass().getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<ArrayList<MapItemModel>>() {}.getType();
                    List<MapItemModel> items = gson.fromJson(reader, listType);
                    if (items != null && !items.isEmpty()) return items;
                }
            }
        } catch (Exception e) {
            System.err.println("[MapItemFileService] Error reading map items from classpath: " + e.getMessage());
        }

        // 4. Fallback: generate and save rich default placements
        List<MapItemModel> defaults = createDefaultMapItems();
        saveMapItems(defaults);
        return defaults;
    }

    public List<MapItemModel> loadMapItemsForRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) return new ArrayList<>();
        String target = roomId.toLowerCase().trim();

        return loadMapItems().stream()
                .filter(MapItemModel::isActive)
                .filter(item -> matchesRoom(item.getRoomId(), target))
                .collect(Collectors.toList());
    }

    private boolean matchesRoom(String itemRoom, String targetRoom) {
        if (itemRoom == null) return false;
        String ir = itemRoom.toLowerCase().trim();
        if (ir.equals(targetRoom)) return true;

        // Common room aliases
        if (ir.equals("classrooma") && (targetRoom.equals("classroom") || targetRoom.equals("classrooma"))) return true;
        if (ir.equals("classroomb") && (targetRoom.equals("teachers_lounge") || targetRoom.equals("classroomb"))) return true;
        if (ir.equals("computer") && (targetRoom.equals("music_art_room") || targetRoom.equals("computer"))) return true;
        if (ir.equals("laboratory") && (targetRoom.equals("science_lab") || targetRoom.equals("laboratory"))) return true;
        if (ir.equals("teacher") && (targetRoom.equals("principal_office") || targetRoom.equals("teacher"))) return true;
        if (ir.equals("dormitory") && (targetRoom.equals("infirmary") || targetRoom.equals("dormitory"))) return true;
        if (ir.equals("basement") && (targetRoom.equals("storage_room") || targetRoom.equals("basement"))) return true;
        if (ir.equals("entrance") && (targetRoom.equals("restroom") || targetRoom.equals("entrance"))) return true;
        if (ir.equals("hall") && (targetRoom.equals("main_hall") || targetRoom.equals("school") || targetRoom.equals("hall"))) return true;

        return false;
    }

    public boolean saveMapItems(List<MapItemModel> items) {
        if (items == null) return false;

        Path targetPath = resolveWritePath();
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Create backup file if existing file exists
            if (Files.exists(targetPath)) {
                Path backupPath = targetPath.getParent().resolve("map_items_backup.json");
                Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[MapItemFileService] Created backup at: " + backupPath.toAbsolutePath());
            }

            // Write JSON
            String jsonContent = gson.toJson(items);
            Files.writeString(targetPath, jsonContent, StandardCharsets.UTF_8);
            System.out.println("[MapItemFileService] Successfully saved map items to: " + targetPath.toAbsolutePath());

            // Mirror save to runtime path if target was src/main/resources
            if (!targetPath.toString().equals(RUNTIME_PATH)) {
                Path mirrorPath = Path.of(RUNTIME_PATH);
                if (mirrorPath.getParent() != null) {
                    Files.createDirectories(mirrorPath.getParent());
                }
                Files.writeString(mirrorPath, jsonContent, StandardCharsets.UTF_8);
            }

            return true;
        } catch (IOException e) {
            System.err.println("[MapItemFileService] Error saving map items: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<MapItemModel> readFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<MapItemModel>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("[MapItemFileService] Failed to read " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    private Path resolveWritePath() {
        Path srcPath = Path.of(RELATIVE_RESOURCE_PATH);
        if (Files.exists(srcPath.getParent())) {
            return srcPath;
        }
        return Path.of(RUNTIME_PATH);
    }

    private List<MapItemModel> createDefaultMapItems() {
        List<MapItemModel> list = new ArrayList<>();
        // Classroom A
        list.add(new MapItemModel("ITEM_001", "flashlight", "Flashlight", "classroomA", 24.0, 21.0, "On the front study desk", true));
        list.add(new MapItemModel("ITEM_002", "notebook", "Student Diary", "classroomA", 23.5, 14.5, "On wooden school bench", true));

        // Teachers' Lounge
        list.add(new MapItemModel("ITEM_003", "bronze_bell", "Sacred Bronze Bell", "classroomB", 34.0, 14.0, "Resting on faculty coffee table (Stuns ghosts 5s)", true));
        list.add(new MapItemModel("ITEM_004", "battery", "Flashlight Battery", "classroomB", 24.0, 10.0, "Inside the teacher's filing cabinet", true));

        // Music & Art Studio
        list.add(new MapItemModel("ITEM_005", "holy_charm", "Ancient Holy Charm", "computer", 14.0, 14.0, "Beside the antique grand piano (Scares ghosts away)", true));
        list.add(new MapItemModel("ITEM_006", "sheet_music", "Haunted Melody Sheet", "computer", 24.0, 10.0, "On the wooden art easel", true));

        // Lore Library
        list.add(new MapItemModel("ITEM_007", "ancient_tome", "Angkorian Scripture", "library", 24.0, 10.0, "On the reading podium", true));
        list.add(new MapItemModel("ITEM_008", "battery", "Flashlight Battery", "library", 34.0, 12.0, "Between old reference encyclopedias", true));

        // Science Lab
        list.add(new MapItemModel("ITEM_009", "acid_bottle", "Holy Acid Bottle", "laboratory", 24.0, 10.0, "Beside the glass chemical distillation rack", true));
        list.add(new MapItemModel("ITEM_010", "lighter", "Brass Lighter", "laboratory", 14.0, 14.0, "Near the Bunsen burner station", true));

        // Storage Vault
        list.add(new MapItemModel("ITEM_011", "night_vision", "Night Vision Goggles", "basement", 24.0, 10.0, "Inside the unlocked security vault (Reveals ghosts on radar)", true));
        list.add(new MapItemModel("ITEM_012", "crowbar", "Heavy Iron Crowbar", "basement", 14.0, 14.0, "Propped against wooden storage crates", true));
        list.add(new MapItemModel("ITEM_013", "toolbox", "Maintenance Toolbox", "basement", 34.0, 14.0, "On the workbench", true));

        // School Infirmary
        list.add(new MapItemModel("ITEM_014", "first_aid_kit", "First Aid Kit", "dormitory", 24.0, 10.0, "On the nurse's examination counter (+1 Heart)", true));
        list.add(new MapItemModel("ITEM_015", "medicine_bottle", "Herbal Tonic", "dormitory", 14.0, 14.0, "Beside the hospital bed", true));

        // Restroom
        list.add(new MapItemModel("ITEM_016", "battery", "Flashlight Battery", "entrance", 14.0, 14.0, "Next to the shattered mirror basin", true));

        // Principal's Office
        list.add(new MapItemModel("ITEM_017", "master_key", "Master Key", "teacher", 24.0, 10.0, "Resting on the Headmaster's mahogany desk", true));
        list.add(new MapItemModel("ITEM_018", "map", "Architectural Blueprint", "teacher", 34.0, 14.0, "Framed on the wall", true));

        // Central Main Hall
        list.add(new MapItemModel("ITEM_019", "flashlight", "Flashlight", "hall", 42.0, 22.0, "Dropped near central stone pillar", true));
        list.add(new MapItemModel("ITEM_020", "battery", "Flashlight Battery", "hall", 20.0, 22.0, "West Wing hallway corner", true));
        list.add(new MapItemModel("ITEM_021", "battery", "Flashlight Battery", "hall", 65.0, 22.0, "East Wing hallway corner", true));
        list.add(new MapItemModel("ITEM_022", "notebook", "Student Diary", "hall", 44.0, 35.0, "Near the entrance archway", true));

        return list;
    }
}
