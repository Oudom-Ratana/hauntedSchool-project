package com.khmerspirit.map;

import com.khmerspirit.config.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MapLoader {

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
