package com.khmerspirit.map;

import com.khmerspirit.config.Constants;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {

    public TileMap loadBigClassroom() {
        InputStream stream = MapLoader.class.getResourceAsStream(Constants.CLASSROOM_MAP_RESOURCE);
        TileMap map;
        if (stream == null) {
            map = createDefaultBigClassroom();
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                List<String> rows = reader.lines()
                        .filter(line -> !line.isBlank())
                        .toList();
                map = buildClassroomFromRows(rows);
            } catch (IOException exception) {
                map = createDefaultBigClassroom();
            }
        }

        // Attach high-res rendered classroom artwork background
        try {
            InputStream bgStream = MapLoader.class.getResourceAsStream(Constants.CLASSROOM_BACKGROUND);
            if (bgStream != null) {
                map.setBackgroundImage(new Image(bgStream));
            }
        } catch (Exception ignored) {
        }

        // Configure pixel-accurate collision boxes matching classroom_room.png objects
        map.setCollisionBoxes(buildClassroomCollisionBoxes(map.getPixelWidth(), map.getPixelHeight()));
        map.initializeDoorCollisions();
        return map;
    }

    private List<Rectangle2D> buildClassroomCollisionBoxes(double mapWidth, double mapHeight) {
        // The background artwork is 1376 x 768 native resolution
        double sx = mapWidth / 1376.0;
        double sy = mapHeight / 768.0;
        List<Rectangle2D> boxes = new ArrayList<>();

        // 1. ROOM BOUNDARY WALLS (Keep player inside the visible classroom)
        boxes.add(new Rectangle2D(0 * sx, 0 * sy, 1376 * sx, 175 * sy));    // Top wall across full width
        boxes.add(new Rectangle2D(0 * sx, 0 * sy, 145 * sx, 768 * sy));     // Left wall
        boxes.add(new Rectangle2D(1335 * sx, 0 * sy, 41 * sx, 768 * sy));   // Right wall
        boxes.add(new Rectangle2D(0 * sx, 665 * sy, 648 * sx, 103 * sy));   // Bottom left wall
        boxes.add(new Rectangle2D(710 * sx, 665 * sy, 666 * sx, 103 * sy)); // Bottom right wall (Entrance opening at x=648..710)

        // 2. TEACHER'S TABLE (PODIUM DESK)
        boxes.add(new Rectangle2D(605 * sx, 180 * sy, 155 * sx, 75 * sy));

        // 3. STUDENT TABLES (EXACTLY 4 TABLES IN 2X2 CLUSTER, LEAVING VAST OPEN SPACE)
        boxes.add(new Rectangle2D(595 * sx, 310 * sy, 70 * sx, 50 * sy)); // Top-Left table
        boxes.add(new Rectangle2D(720 * sx, 310 * sy, 70 * sx, 50 * sy)); // Top-Right table
        boxes.add(new Rectangle2D(590 * sx, 370 * sy, 75 * sx, 55 * sy)); // Bottom-Left table
        boxes.add(new Rectangle2D(720 * sx, 370 * sy, 75 * sx, 55 * sy)); // Bottom-Right table

        // Bottom doorway barrier (prevents walking past the door frame onto bottom border)
        boxes.add(new Rectangle2D(640 * sx, 665 * sy, 78 * sx, 103 * sy));

        return boxes;
    }

    private void setupClassroomDoor(TileMap map, int mapRows) {
        double sx = map.getPixelWidth() / 1376.0;
        double sy = map.getPixelHeight() / 768.0;

        Rectangle2D doorVisualBounds = new Rectangle2D(643 * sx, 626 * sy, 71 * sx, 142 * sy);
        Rectangle2D doorCollisionBox = new Rectangle2D(648 * sx, 665 * sy, 62 * sx, 103 * sy);
        Rectangle2D wallPatchBounds = new Rectangle2D(713 * sx, 626 * sy, 57 * sx, 142 * sy);
        Rectangle2D wallPatchSource = new Rectangle2D(713, 485, 57, 142);

        Door entranceDoor = new Door("classroom_entrance", 24, mapRows - 2, "classroomA", "hall", false, true, "key", doorVisualBounds);
        entranceDoor.setCollisionBox(doorCollisionBox);
        entranceDoor.setWallPatchBounds(wallPatchBounds);
        entranceDoor.setWallPatchSource(wallPatchSource);
        map.addDoor(entranceDoor);
    }

    private TileMap buildClassroomFromRows(List<String> rows) {
        int mapRows = rows.size();
        int mapColumns = rows.stream().mapToInt(String::length).max().orElse(48);
        TileMap map = new TileMap(mapColumns, mapRows);

        for (int row = 0; row < mapRows; row++) {
            String line = rows.get(row);
            for (int column = 0; column < mapColumns; column++) {
                char symbol = column < line.length() ? line.charAt(column) : '#';
                map.setTile(column, row, Tile.fromSymbol(symbol));
            }
        }

        map.clearRoomsAndDoors();
        map.addRoom(new Room("classroomA", "Classroom (ថ្នាក់រៀន)", 0, 0, mapColumns, mapRows));
        setupClassroomDoor(map, mapRows);
        return map;
    }

    private TileMap createDefaultBigClassroom() {
        TileMap map = new TileMap(48, 30);
        map.fill(Tile.FLOOR);
        buildRoom(map, "classroomA", "Classroom (ថ្នាក់រៀន)", 0, 0, 48, 30, Tile.FLOOR);
        try {
            InputStream bgStream = MapLoader.class.getResourceAsStream(Constants.CLASSROOM_BACKGROUND);
            if (bgStream != null) {
                map.setBackgroundImage(new Image(bgStream));
            }
        } catch (Exception ignored) {
        }
        setupClassroomDoor(map, 30);
        map.setCollisionBoxes(buildClassroomCollisionBoxes(map.getPixelWidth(), map.getPixelHeight()));
        map.initializeDoorCollisions();
        return map;
    }

    public TileMap loadMapForId(String id) {
        return switch (id.toLowerCase()) {
            case "classrooma", "classroom" -> loadBigClassroom();
            case "hall", "hallway", "school", "main_hall" -> loadMainHall();
            case "library" -> loadDetailedRoom("library", "Library (បណ្ណាល័យ)", "/images/maps/library/library_map.png");
            case "laboratory", "science_lab" -> loadDetailedRoom("laboratory", "Science Lab (បន្ទប់ពិសោធន៍)", "/images/maps/science_lab/science_lab_map.png");
            case "teacher", "principal_office" -> loadDetailedRoom("teacher", "Principal's Office (ការិយាល័យនាយក)", "/images/maps/principal_office/principal_office_map.png");
            case "dormitory", "infirmary" -> loadDetailedRoom("dormitory", "Infirmary (បន្ទប់សុខាភិបាល)", "/images/maps/infirmary/infirmary_map.png");
            case "basement", "storage_room" -> loadDetailedRoom("basement", "Storage Room (បន្ទប់ឃ្លាំង)", "/images/maps/storage_room/storage_room_map.png");
            case "computer", "music_art_room" -> loadDetailedRoom("computer", "Music & Art Room (បន្ទប់តន្ត្រី)", "/images/maps/music_art_room/music_art_room_map.png");
            case "classroomb", "teachers_lounge" -> loadDetailedRoom("classroomb", "Teachers' Lounge (បន្ទប់គ្រូ)", "/images/maps/teachers_lounge/teachers_lounge_map.png");
            case "entrance", "restroom" -> loadDetailedRoom("entrance", "Restroom (បន្ទប់ទឹក)", "/images/maps/restroom/restroom_map.png");
            default -> loadMainHall();
        };
    }

    public TileMap loadMainHall() {
        TileMap map = new TileMap(48, 27);
        map.fill(Tile.FLOOR);
        map.clearRoomsAndDoors();
        map.addRoom(new Room("hall", "Main Hall (សាលធំកណ្តាល)", 0, 0, 48, 27));

        try {
            InputStream bgStream = MapLoader.class.getResourceAsStream("/images/maps/main_hall/main_hall_map.png");
            if (bgStream != null) {
                map.setBackgroundImage(new Image(bgStream));
            }
        } catch (Exception ignored) {}

        double sx = map.getPixelWidth() / 1376.0;
        double sy = map.getPixelHeight() / 768.0;

        // West Wing Doors (Left Column) - aligned precisely to main_hall_map.png
        // 1. Principal's Office (Top-Left, "HEADMASTER'S OFFICE (W)")
        Rectangle2D doorPrincipal = new Rectangle2D(158 * sx, 84 * sy, 60 * sx, 96 * sy);
        Door doorPrin = new Door("hall_door_principal", 8, 4, "teacher", "hall", false, false, null, doorPrincipal);
        doorPrin.setCollisionBox(doorPrincipal);
        doorPrin.setDrawClosedSprite(false);
        doorPrin.setDrawOpenOverlay(true);
        map.addDoor(doorPrin);

        // 2. Science Lab (Upper-Mid-Left, "SPELLS & POTIONS (W)")
        Rectangle2D doorSci = new Rectangle2D(158 * sx, 250 * sy, 60 * sx, 96 * sy);
        Door doorSciLab = new Door("hall_door_scilab", 8, 10, "laboratory", "hall", false, false, null, doorSci);
        doorSciLab.setCollisionBox(doorSci);
        doorSciLab.setDrawClosedSprite(false);
        doorSciLab.setDrawOpenOverlay(true);
        map.addDoor(doorSciLab);

        // 3. Classroom A (Lower-Mid-Left, "OCCULT STUDY (W)")
        Rectangle2D doorClassroom = new Rectangle2D(158 * sx, 416 * sy, 60 * sx, 96 * sy);
        Door doorA = new Door("hall_door_classroomA", 8, 16, "classroomA", "hall", false, false, null, doorClassroom);
        doorA.setCollisionBox(doorClassroom);
        doorA.setDrawClosedSprite(false);
        doorA.setDrawOpenOverlay(true);
        map.addDoor(doorA);

        // 4. Music & Art Room (Bottom-Left, "ART STUDIO (W)")
        Rectangle2D doorMusic = new Rectangle2D(158 * sx, 582 * sy, 60 * sx, 96 * sy);
        Door doorC = new Door("hall_door_music", 8, 22, "computer", "hall", false, false, null, doorMusic);
        doorC.setCollisionBox(doorMusic);
        doorC.setDrawClosedSprite(false);
        doorC.setDrawOpenOverlay(true);
        map.addDoor(doorC);

        // East Wing Doors (Right Column) - aligned precisely to main_hall_map.png
        // 5. School Infirmary (Top-Right, "STUDENT DORM (A) (E)")
        Rectangle2D doorInfirmary = new Rectangle2D(1158 * sx, 84 * sy, 60 * sx, 96 * sy);
        Door doorInf = new Door("hall_door_infirmary", 40, 4, "dormitory", "hall", false, false, null, doorInfirmary);
        doorInf.setCollisionBox(doorInfirmary);
        doorInf.setDrawClosedSprite(false);
        doorInf.setDrawOpenOverlay(true);
        map.addDoor(doorInf);

        // 6. Storage Vault (Upper-Mid-Right, "STUDENT DORM (B) (E)")
        Rectangle2D doorStorage = new Rectangle2D(1158 * sx, 250 * sy, 60 * sx, 96 * sy);
        Door doorStg = new Door("hall_door_storage", 40, 10, "basement", "hall", false, false, null, doorStorage);
        doorStg.setCollisionBox(doorStorage);
        doorStg.setDrawClosedSprite(false);
        doorStg.setDrawOpenOverlay(true);
        map.addDoor(doorStg);

        // 7. Teachers' Lounge (Lower-Mid-Right, "MUSIC HALL (E)")
        Rectangle2D doorLounge = new Rectangle2D(1158 * sx, 416 * sy, 60 * sx, 96 * sy);
        Door doorB = new Door("hall_door_lounge", 40, 16, "classroomB", "hall", false, false, null, doorLounge);
        doorB.setCollisionBox(doorLounge);
        doorB.setDrawClosedSprite(false);
        doorB.setDrawOpenOverlay(true);
        map.addDoor(doorB);

        // 8. Restroom & Mirror (Bottom-Right, "DANCING PARLOR (E)")
        Rectangle2D doorRestroom = new Rectangle2D(1158 * sx, 582 * sy, 60 * sx, 96 * sy);
        Door doorRest = new Door("hall_door_restroom", 40, 22, "entrance", "hall", false, false, null, doorRestroom);
        doorRest.setCollisionBox(doorRestroom);
        doorRest.setDrawClosedSprite(false);
        doorRest.setDrawOpenOverlay(true);
        map.addDoor(doorRest);

        // North Center: Lore Library
        Rectangle2D doorNorth = new Rectangle2D(645 * sx, 95 * sy, 85 * sx, 110 * sy);
        Door doorN = new Door("hall_corridor_north", 24, 2, "library", "hall", false, false, null, doorNorth);
        doorN.setCollisionBox(doorNorth);
        doorN.setDrawClosedSprite(false);
        doorN.setDrawOpenOverlay(false);
        map.addDoor(doorN);

        // South Center: Grand School Exit Gate
        Rectangle2D doorSouth = new Rectangle2D(548 * sx, 650 * sy, 280 * sx, 105 * sy);
        Door doorS = new Door("hall_corridor_south", 24, 25, "exit", "hall", false, true, "master_key", doorSouth);
        doorS.setCollisionBox(doorSouth);
        doorS.setDrawClosedSprite(false);
        doorS.setDrawOpenOverlay(false);
        map.addDoor(doorS);

        // Hallway boundaries and stone colonnades
        List<Rectangle2D> boxes = new ArrayList<>();
        // Outer boundaries
        boxes.add(new Rectangle2D(0, 0, 150 * sx, 768 * sy));
        boxes.add(new Rectangle2D(1226 * sx, 0, 150 * sx, 768 * sy));
        boxes.add(new Rectangle2D(0, 0, 640 * sx, 80 * sy));
        boxes.add(new Rectangle2D(735 * sx, 0, 641 * sx, 80 * sy));

        // Stone pillars
        boxes.add(new Rectangle2D(365 * sx, 160 * sy, 55 * sx, 110 * sy));
        boxes.add(new Rectangle2D(365 * sx, 460 * sy, 55 * sx, 110 * sy));
        boxes.add(new Rectangle2D(595 * sx, 160 * sy, 55 * sx, 110 * sy));
        boxes.add(new Rectangle2D(595 * sx, 460 * sy, 55 * sx, 110 * sy));

        map.setCollisionBoxes(boxes);
        map.initializeDoorCollisions();
        return map;
    }

    public TileMap loadDetailedRoom(String roomId, String displayName, String backgroundResource) {
        TileMap map = new TileMap(48, 27);
        map.fill(Tile.FLOOR);
        map.clearRoomsAndDoors();
        map.addRoom(new Room(roomId, displayName, 0, 0, 48, 27));

        try {
            InputStream bgStream = MapLoader.class.getResourceAsStream(backgroundResource);
            if (bgStream != null) {
                map.setBackgroundImage(new Image(bgStream));
            }
        } catch (Exception ignored) {
        }

        double sx = map.getPixelWidth() / 1376.0;
        double sy = map.getPixelHeight() / 768.0;

        // Bottom doorway bounds
        Rectangle2D doorVisualBounds = new Rectangle2D(643 * sx, 626 * sy, 71 * sx, 142 * sy);
        Rectangle2D doorCollisionBox = new Rectangle2D(648 * sx, 665 * sy, 62 * sx, 103 * sy);
        Rectangle2D wallPatchBounds = new Rectangle2D(713 * sx, 626 * sy, 57 * sx, 142 * sy);
        Rectangle2D wallPatchSource = new Rectangle2D(713, 485, 57, 142);

        // In detailed rooms, the exit door back to hallway starts closed so player stays behind it
        Door exitDoor = new Door(roomId + "_exit", 24, 25, roomId, "hall", false, false, null, doorVisualBounds);
        exitDoor.setCollisionBox(doorCollisionBox);
        exitDoor.setWallPatchBounds(wallPatchBounds);
        exitDoor.setWallPatchSource(wallPatchSource);
        exitDoor.setDrawClosedSprite(true);
        map.addDoor(exitDoor);

        map.setCollisionBoxes(buildStandardRoomCollisionBoxes(map.getPixelWidth(), map.getPixelHeight()));
        map.initializeDoorCollisions();
        return map;
    }

    private List<Rectangle2D> buildStandardRoomCollisionBoxes(double mapWidth, double mapHeight) {
        double sx = mapWidth / 1376.0;
        double sy = mapHeight / 768.0;
        List<Rectangle2D> boxes = new ArrayList<>();

        // Boundary walls
        boxes.add(new Rectangle2D(0 * sx, 0 * sy, 1376 * sx, 175 * sy));    // Top wall
        boxes.add(new Rectangle2D(0 * sx, 0 * sy, 145 * sx, 768 * sy));     // Left wall
        boxes.add(new Rectangle2D(1335 * sx, 0 * sy, 41 * sx, 768 * sy));   // Right wall
        boxes.add(new Rectangle2D(0 * sx, 665 * sy, 648 * sx, 103 * sy));   // Bottom left wall
        boxes.add(new Rectangle2D(710 * sx, 665 * sy, 666 * sx, 103 * sy)); // Bottom right wall

        // Bottom doorway barrier (prevents walking past the door frame onto bottom border)
        boxes.add(new Rectangle2D(640 * sx, 665 * sy, 78 * sx, 103 * sy));

        // Center room furniture / workstation cluster (leaving wide corridors on all sides)
        boxes.add(new Rectangle2D(580 * sx, 280 * sy, 220 * sx, 160 * sy));

        return boxes;
    }

    public TileMap loadAbandonedSchool() {
        InputStream stream = MapLoader.class.getResourceAsStream(Constants.SCHOOL_MAP_RESOURCE);
        if (stream == null) {
            return createDefaultAbandonedSchool();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> rows = reader.lines()
                    .filter(line -> !line.isBlank())
                    .toList();
            return buildFromRows(rows);
        } catch (IOException exception) {
            return createDefaultAbandonedSchool();
        }
    }

    private TileMap buildFromRows(List<String> rows) {
        int mapRows = rows.size();
        int mapColumns = rows.stream().mapToInt(String::length).max().orElse(Constants.MAP_COLUMNS);
        TileMap map = new TileMap(mapColumns, mapRows);

        for (int row = 0; row < mapRows; row++) {
            String line = rows.get(row);
            for (int column = 0; column < mapColumns; column++) {
                char symbol = column < line.length() ? line.charAt(column) : '#';
                map.setTile(column, row, Tile.fromSymbol(symbol));
            }
        }

        addDefaultRoomsAndDoors(map);
        decorateSchool(map);
        return map;
    }

    private TileMap createDefaultAbandonedSchool() {
        TileMap map = new TileMap(Constants.MAP_COLUMNS, Constants.MAP_ROWS);
        map.fill(Tile.WALL);
        buildRoom(map, "entrance", "Entrance", 34, 45, 16, 10, Tile.ENTRANCE);
        buildRoom(map, "classroomA", "Classroom A", 5, 5, 18, 13, Tile.FLOOR);
        buildRoom(map, "classroomB", "Classroom B", 25, 5, 18, 13, Tile.FLOOR);
        buildRoom(map, "library", "Library", 47, 5, 19, 13, Tile.FLOOR);
        buildRoom(map, "laboratory", "Laboratory", 68, 5, 17, 13, Tile.FLOOR);
        buildRoom(map, "computer", "Computer Room", 5, 25, 18, 13, Tile.FLOOR);
        buildRoom(map, "teacher", "Teacher Room", 25, 25, 18, 13, Tile.FLOOR);
        buildRoom(map, "dormitory", "Dormitory", 47, 25, 19, 13, Tile.FLOOR);
        buildRoom(map, "basement", "Basement", 68, 25, 17, 13, Tile.CRACKED_FLOOR);
        buildRoom(map, "exit", "Exit", 74, 45, 11, 8, Tile.EXIT);

        carveHallway(map, 8, 20, 81, 23);
        carveHallway(map, 39, 18, 44, 49);
        carveHallway(map, 72, 18, 77, 49);
        carveHallway(map, 49, 49, 74, 52);

        addDoor(map, 14, 17, "classroomA", "hall");
        addDoor(map, 34, 17, "classroomB", "hall");
        addDoor(map, 56, 17, "library", "hall");
        addDoor(map, 76, 17, "laboratory", "hall");
        addDoor(map, 14, 25, "computer", "hall");
        addDoor(map, 34, 25, "teacher", "hall");
        addDoor(map, 56, 25, "dormitory", "hall");
        addDoor(map, 76, 25, "basement", "hall");
        addDoor(map, 42, 45, "entrance", "hall");
        addDoor(map, 76, 45, "exit", "hall");

        decorateSchool(map);

        return map;
    }

    private void decorateSchool(TileMap map) {
        decorateClassrooms(map);
        decorateLibrary(map);
        decorateLaboratory(map);
        decorateComputerRoom(map);
        decorateTeacherRoom(map);
        decorateDormitory(map);
        decorateBasement(map);
        decorateHallways(map);
        decorateEntrance(map);
    }

    private void addDefaultRoomsAndDoors(TileMap map) {
        map.clearRoomsAndDoors();
        map.addRoom(new Room("entrance", "Entrance", 34, 45, 16, 10));
        map.addRoom(new Room("classroomA", "Classroom A", 5, 5, 18, 13));
        map.addRoom(new Room("classroomB", "Classroom B", 25, 5, 18, 13));
        map.addRoom(new Room("library", "Library", 47, 5, 19, 13));
        map.addRoom(new Room("laboratory", "Laboratory", 68, 5, 17, 13));
        map.addRoom(new Room("computer", "Computer Room", 5, 25, 18, 13));
        map.addRoom(new Room("teacher", "Teacher Room", 25, 25, 18, 13));
        map.addRoom(new Room("dormitory", "Dormitory", 47, 25, 19, 13));
        map.addRoom(new Room("basement", "Basement", 68, 25, 17, 13));
        map.addRoom(new Room("exit", "Exit", 74, 45, 11, 8));
        map.addDoor(new Door(14, 17, "classroomA", "hall"));
        map.addDoor(new Door(34, 17, "classroomB", "hall"));
        map.addDoor(new Door(56, 17, "library", "hall"));
        map.addDoor(new Door(76, 17, "laboratory", "hall"));
        map.addDoor(new Door(14, 25, "computer", "hall"));
        map.addDoor(new Door(34, 25, "teacher", "hall"));
        map.addDoor(new Door(56, 25, "dormitory", "hall"));
        map.addDoor(new Door(76, 25, "basement", "hall"));
        map.addDoor(new Door(42, 45, "entrance", "hall"));
        map.addDoor(new Door(76, 45, "exit", "hall"));
    }

    private void buildRoom(TileMap map, String id, String name, int column, int row, int width, int height, Tile floorTile) {
        map.addRoom(new Room(id, name, column, row, width, height));
        for (int y = row; y < row + height; y++) {
            for (int x = column; x < column + width; x++) {
                boolean wall = x == column || y == row || x == column + width - 1 || y == row + height - 1;
                map.setTile(x, y, wall ? Tile.WALL : floorTile);
            }
        }
    }

    private void carveHallway(TileMap map, int startColumn, int startRow, int endColumn, int endRow) {
        for (int row = startRow; row <= endRow; row++) {
            for (int column = startColumn; column <= endColumn; column++) {
                map.setTile(column, row, Tile.CARPET);
            }
        }
    }

    private void addDoor(TileMap map, int column, int row, String fromRoomId, String toRoomId) {
        map.setTile(column, row, Tile.DOOR);
        map.addDoor(new Door(column, row, fromRoomId, toRoomId));
    }

    private void decorateClassrooms(TileMap map) {
        for (int column = 8; column <= 18; column += 5) {
            map.setTile(column, 9, Tile.DESK);
            map.setTile(column, 10, Tile.CHAIR);
            map.setTile(column, 12, Tile.DESK);
            map.setTile(column, 13, Tile.CHAIR);
            map.setTile(column + 20, 9, Tile.DESK);
            map.setTile(column + 20, 10, Tile.CHAIR);
            map.setTile(column + 20, 12, Tile.DESK);
            map.setTile(column + 20, 13, Tile.CHAIR);
        }
        map.setTile(10, 6, Tile.BLACKBOARD);
        map.setTile(11, 6, Tile.BLACKBOARD);
        map.setTile(30, 6, Tile.BLACKBOARD);
        map.setTile(31, 6, Tile.BLACKBOARD);
        map.setTile(21, 15, Tile.RITUAL_MARK);
        map.setTile(42, 15, Tile.RITUAL_MARK);
    }

    private void decorateLibrary(TileMap map) {
        for (int row = 8; row <= 15; row += 3) {
            map.setTile(50, row, Tile.SHELF);
            map.setTile(62, row, Tile.SHELF);
        }
        map.setTile(54, 11, Tile.DESK);
        map.setTile(55, 11, Tile.CHAIR);
        map.setTile(59, 14, Tile.RITUAL_MARK);
    }

    private void decorateLaboratory(TileMap map) {
        for (int column = 72; column <= 80; column += 4) {
            map.setTile(column, 10, Tile.LAB_TABLE);
            map.setTile(column, 14, Tile.LAB_TABLE);
        }
        map.setTile(70, 7, Tile.LOCKER);
        map.setTile(71, 7, Tile.LOCKER);
        map.setTile(82, 16, Tile.RITUAL_MARK);
    }

    private void decorateComputerRoom(TileMap map) {
        for (int row = 29; row <= 35; row += 3) {
            map.setTile(9, row, Tile.COMPUTER);
            map.setTile(10, row, Tile.CHAIR);
            map.setTile(17, row, Tile.COMPUTER);
            map.setTile(18, row, Tile.CHAIR);
        }
        map.setTile(7, 27, Tile.BLACKBOARD);
        map.setTile(8, 27, Tile.BLACKBOARD);
    }

    private void decorateTeacherRoom(TileMap map) {
        map.setTile(33, 31, Tile.DESK);
        map.setTile(34, 31, Tile.DESK);
        map.setTile(33, 32, Tile.CHAIR);
        map.setTile(28, 34, Tile.SHELF);
        map.setTile(39, 34, Tile.SHELF);
        map.setTile(27, 27, Tile.LOCKER);
        map.setTile(41, 27, Tile.LOCKER);
    }

    private void decorateDormitory(TileMap map) {
        for (int column = 50; column <= 62; column += 6) {
            map.setTile(column, 29, Tile.BED);
            map.setTile(column, 34, Tile.BED);
        }
        map.setTile(60, 31, Tile.RITUAL_MARK);
    }

    private void decorateBasement(TileMap map) {
        map.setTile(75, 31, Tile.STAIRS);
        map.setTile(76, 31, Tile.STAIRS);
        map.setTile(79, 34, Tile.SHELF);
        map.setTile(73, 29, Tile.RITUAL_MARK);
        map.setTile(78, 33, Tile.RITUAL_MARK);
    }

    private void decorateHallways(TileMap map) {
        for (int column = 12; column <= 76; column += 8) {
            map.setTile(column, 20, Tile.LOCKER);
        }
        map.setTile(40, 22, Tile.RITUAL_MARK);
        map.setTile(72, 41, Tile.RITUAL_MARK);
    }

    private void decorateEntrance(TileMap map) {
        map.setTile(36, 46, Tile.LOCKER);
        map.setTile(47, 46, Tile.LOCKER);
        map.setTile(42, 51, Tile.RITUAL_MARK);
    }
}
