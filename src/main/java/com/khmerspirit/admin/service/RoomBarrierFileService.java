package com.khmerspirit.admin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.khmerspirit.admin.model.RoomBarrierModel;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File persistence service for custom room collision barriers stored in 1376x768 native pixel coordinates.
 */
public class RoomBarrierFileService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String RELATIVE_RESOURCE_PATH = "src/main/resources/rooms/room_barriers.json";
    private static final String RUNTIME_PATH = "rooms/room_barriers.json";
    private static final String CLASSPATH_RESOURCE = "/rooms/room_barriers.json";

    public Map<String, List<RoomBarrierModel>> loadAllBarriers() {
        // 1. Try external/runtime file first
        File runtimeFile = new File(RUNTIME_PATH);
        if (runtimeFile.exists()) {
            Map<String, List<RoomBarrierModel>> map = readFromFile(runtimeFile);
            if (map != null && !map.isEmpty()) return map;
        }

        // 2. Try src/main/resources file next
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.exists()) {
            Map<String, List<RoomBarrierModel>> map = readFromFile(srcFile);
            if (map != null && !map.isEmpty()) return map;
        }

        // 3. Fallback to classpath resource
        try (InputStream is = getClass().getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Type mapType = new TypeToken<HashMap<String, ArrayList<RoomBarrierModel>>>() {}.getType();
                    Map<String, List<RoomBarrierModel>> map = gson.fromJson(reader, mapType);
                    if (map != null) return map;
                }
            }
        } catch (Exception ignored) {
        }

        return new HashMap<>();
    }

    public boolean hasCustomBarriers(String roomId) {
        if (roomId == null) return false;
        Map<String, List<RoomBarrierModel>> all = loadAllBarriers();
        List<RoomBarrierModel> list = all.get(roomId.toLowerCase());
        return list != null && !list.isEmpty();
    }

    public List<RoomBarrierModel> loadBarriersForRoom(String roomId) {
        if (roomId == null) return new ArrayList<>();
        Map<String, List<RoomBarrierModel>> all = loadAllBarriers();
        List<RoomBarrierModel> list = all.get(roomId.toLowerCase());
        if (list != null && !list.isEmpty()) {
            return new ArrayList<>(list);
        }
        return getDefaultBarriersForRoom(roomId);
    }

    public boolean saveBarriersForRoom(String roomId, List<RoomBarrierModel> barriers) {
        if (roomId == null) return false;
        Map<String, List<RoomBarrierModel>> all = loadAllBarriers();
        all.put(roomId.toLowerCase(), new ArrayList<>(barriers));
        return saveAllBarriers(all);
    }

    public boolean saveAllBarriers(Map<String, List<RoomBarrierModel>> barriersMap) {
        if (barriersMap == null) return false;

        Path targetPath = resolveWritePath();
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            if (Files.exists(targetPath)) {
                Path backup = targetPath.getParent().resolve("room_barriers_backup.json");
                Files.copy(targetPath, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            String jsonContent = gson.toJson(barriersMap);
            Files.writeString(targetPath, jsonContent, StandardCharsets.UTF_8);

            // Mirror save to runtime path if writing to src/main/resources
            if (!targetPath.toString().equals(RUNTIME_PATH)) {
                Path mirrorPath = Path.of(RUNTIME_PATH);
                if (mirrorPath.getParent() != null) {
                    Files.createDirectories(mirrorPath.getParent());
                }
                Files.writeString(mirrorPath, jsonContent, StandardCharsets.UTF_8);
            }

            return true;
        } catch (IOException e) {
            System.err.println("[RoomBarrierFileService] Error saving room barriers: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<RoomBarrierModel> getDefaultBarriersForRoom(String roomId) {
        List<RoomBarrierModel> boxes = new ArrayList<>();
        String r = roomId == null ? "" : roomId.toLowerCase();

        if (r.equals("hall") || r.equals("main_hall") || r.equals("hallway")) {
            // Hallway boundary walls
            boxes.add(new RoomBarrierModel(0, 0, 1376, 140, "North Wall"));
            boxes.add(new RoomBarrierModel(0, 0, 80, 768, "West Wall"));
            boxes.add(new RoomBarrierModel(1296, 0, 80, 768, "East Wall"));
            boxes.add(new RoomBarrierModel(0, 700, 620, 68, "South Left Wall"));
            boxes.add(new RoomBarrierModel(756, 700, 620, 68, "South Right Wall"));
            // Hallway Stone Pillars
            boxes.add(new RoomBarrierModel(365, 160, 55, 110, "Stone Pillar NW"));
            boxes.add(new RoomBarrierModel(365, 460, 55, 110, "Stone Pillar SW"));
            boxes.add(new RoomBarrierModel(595, 160, 55, 110, "Stone Pillar NE"));
            boxes.add(new RoomBarrierModel(595, 460, 55, 110, "Stone Pillar SE"));
            return boxes;
        }

        // Standard Room Boundary Walls
        boxes.add(new RoomBarrierModel(0, 0, 1376, 175, "Top Wall"));
        boxes.add(new RoomBarrierModel(0, 0, 145, 768, "Left Wall"));
        boxes.add(new RoomBarrierModel(1335, 0, 41, 768, "Right Wall"));
        boxes.add(new RoomBarrierModel(0, 665, 648, 103, "Bottom Left Wall"));
        boxes.add(new RoomBarrierModel(710, 665, 666, 103, "Bottom Right Wall"));
        boxes.add(new RoomBarrierModel(640, 665, 78, 103, "Door Frame Barrier"));

        // Room specific props / tables
        switch (r) {
            case "classrooma", "classroom" -> {
                boxes.add(new RoomBarrierModel(605, 180, 155, 75, "Teacher Desk"));
                boxes.add(new RoomBarrierModel(595, 310, 70, 50, "Student Table Top-Left"));
                boxes.add(new RoomBarrierModel(720, 310, 70, 50, "Student Table Top-Right"));
                boxes.add(new RoomBarrierModel(590, 370, 75, 55, "Student Table Bottom-Left"));
                boxes.add(new RoomBarrierModel(720, 370, 75, 55, "Student Table Bottom-Right"));
            }
            case "classroomb", "teachers_lounge" -> {
                boxes.add(new RoomBarrierModel(580, 280, 220, 160, "Faculty Desk"));
            }
            case "computer", "music_art_room" -> {
                boxes.add(new RoomBarrierModel(560, 270, 250, 170, "Grand Piano"));
            }
            case "entrance", "restroom" -> {
                boxes.add(new RoomBarrierModel(580, 260, 220, 150, "Restroom Sink"));
            }
            case "dormitory", "infirmary" -> {
                boxes.add(new RoomBarrierModel(580, 280, 220, 160, "Medical Beds"));
            }
            case "basement", "storage_room" -> {
                boxes.add(new RoomBarrierModel(580, 280, 220, 160, "Storage Crates"));
            }
            case "laboratory", "science_lab" -> {
                boxes.add(new RoomBarrierModel(570, 270, 240, 170, "Lab Workstation"));
            }
            case "library" -> {
                boxes.add(new RoomBarrierModel(570, 270, 240, 170, "Library Reading Table"));
            }
            case "teacher", "principal_office" -> {
                boxes.add(new RoomBarrierModel(570, 260, 240, 160, "Principal Desk"));
            }
            default -> {
                boxes.add(new RoomBarrierModel(580, 280, 220, 160, "Room Furniture"));
            }
        }

        return boxes;
    }

    private Path resolveWritePath() {
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.getParentFile().exists()) {
            return srcFile.toPath();
        }
        return Path.of(RUNTIME_PATH);
    }

    private Map<String, List<RoomBarrierModel>> readFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type mapType = new TypeToken<HashMap<String, ArrayList<RoomBarrierModel>>>() {}.getType();
            return gson.fromJson(reader, mapType);
        } catch (Exception e) {
            System.err.println("[RoomBarrierFileService] Error reading from " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }
}
