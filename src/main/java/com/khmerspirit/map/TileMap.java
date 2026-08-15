package com.khmerspirit.map;

import com.khmerspirit.config.Constants;
import com.khmerspirit.player.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TileMap {

    private final Tile[][] tiles;
    private final int columns;
    private final int rows;
    private final List<Room> rooms;
    private final List<Door> doors;

    public TileMap(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.tiles = new Tile[rows][columns];
        this.rooms = new ArrayList<>();
        this.doors = new ArrayList<>();
        fill(Tile.FLOOR);
    }

    public static TileMap createSchoolMap() {
        return new MapLoader().loadAbandonedSchool();
    }

    public void render(GraphicsContext graphics, Camera camera) {
        int tileSize = Constants.TILE_SIZE;
        int startColumn = Math.max(0, (int) (camera.getX() / tileSize) - 1);
        int endColumn = Math.min(columns - 1, (int) ((camera.getX() + camera.getViewportWidth()) / tileSize) + 1);
        int startRow = Math.max(0, (int) (camera.getY() / tileSize) - 1);
        int endRow = Math.min(rows - 1, (int) ((camera.getY() + camera.getViewportHeight()) / tileSize) + 1);

        for (int row = startRow; row <= endRow; row++) {
            for (int column = startColumn; column <= endColumn; column++) {
                renderTile(graphics, camera, column, row, tiles[row][column]);
            }
        }

        renderRoomLabels(graphics, camera);
    }

    public Tile getTileAt(int column, int row) {
        if (column < 0 || row < 0 || column >= columns || row >= rows) {
            return Tile.WALL;
        }
        return tiles[row][column];
    }

    public void setTile(int column, int row, Tile tile) {
        if (column < 0 || row < 0 || column >= columns || row >= rows) {
            return;
        }
        tiles[row][column] = tile;
    }

    public void fill(Tile tile) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tiles[row][column] = tile;
            }
        }
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public void clearRoomsAndDoors() {
        rooms.clear();
        doors.clear();
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Door> getDoors() {
        return Collections.unmodifiableList(doors);
    }

    public Optional<Room> findRoomAt(double worldX, double worldY) {
        return rooms.stream()
                .filter(room -> room.containsWorldPoint(worldX, worldY))
                .findFirst();
    }

    public int getPixelWidth() {
        return columns * Constants.TILE_SIZE;
    }

    public int getPixelHeight() {
        return rows * Constants.TILE_SIZE;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    private void renderTile(GraphicsContext graphics, Camera camera, int column, int row, Tile tile) {
        int tileSize = Constants.TILE_SIZE;
        double screenX = column * tileSize - camera.getX();
        double screenY = row * tileSize - camera.getY();

        graphics.setFill(tile.getBaseColor());
        graphics.fillRect(screenX, screenY, tileSize, tileSize);
        graphics.setStroke(tile.getAccentColor());
        graphics.strokeRect(screenX + 0.5, screenY + 0.5, tileSize - 1.0, tileSize - 1.0);

        switch (tile) {
            case WALL -> renderWall(graphics, screenX, screenY, tileSize);
            case DESK, LAB_TABLE, COMPUTER, BED, SHELF, CHAIR, BLACKBOARD, LOCKER -> renderFurniture(graphics, screenX, screenY, tileSize, tile);
            case DOOR -> renderDoor(graphics, screenX, screenY, tileSize);
            case STAIRS -> renderStairs(graphics, screenX, screenY, tileSize);
            case EXIT -> renderExit(graphics, screenX, screenY, tileSize);
            case ENTRANCE -> renderEntrance(graphics, screenX, screenY, tileSize);
            case RITUAL_MARK -> renderRitualMark(graphics, screenX, screenY, tileSize);
            case CRACKED_FLOOR -> renderCracks(graphics, screenX, screenY, tileSize);
            default -> renderFloorDetail(graphics, screenX, screenY, tileSize);
        }
    }

    private void renderRoomLabels(GraphicsContext graphics, Camera camera) {
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        for (Room room : rooms) {
            double x = room.getColumn() * Constants.TILE_SIZE + 16 - camera.getX();
            double y = room.getRow() * Constants.TILE_SIZE + 28 - camera.getY();
            if (x < -200 || y < -40 || x > camera.getViewportWidth() + 40 || y > camera.getViewportHeight() + 40) {
                continue;
            }

            graphics.fillRect(x - 8, y - 18, 168, 26);
            graphics.setFill(Color.web("#dfc894"));
            graphics.fillText(room.getDisplayName(), x, y);
            graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        }
    }

    private void renderWall(GraphicsContext graphics, double x, double y, int size) {
        graphics.setFill(Color.web("#1d1a18"));
        graphics.fillRect(x + 3, y + 3, size - 6, size - 6);
        graphics.setStroke(Color.web("#4a4238"));
        graphics.strokeLine(x + 6, y + size / 2.0, x + size - 6, y + size / 2.0);
    }

    private void renderFurniture(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        switch (tile) {
            case CHAIR -> renderChair(graphics, x, y, size, tile);
            case BLACKBOARD -> renderBlackboard(graphics, x, y, size, tile);
            case LOCKER -> renderLocker(graphics, x, y, size, tile);
            case COMPUTER -> renderComputer(graphics, x, y, size, tile);
            case LAB_TABLE -> renderLabTable(graphics, x, y, size, tile);
            case BED -> renderBed(graphics, x, y, size, tile);
            case SHELF -> renderShelf(graphics, x, y, size, tile);
            default -> renderDesk(graphics, x, y, size, tile);
        }
    }

    private void renderDesk(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(Color.rgb(0, 0, 0, 0.28));
        graphics.fillRect(x + 10, y + 17, size - 16, size - 17);
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 7, y + 10, size - 14, size - 22);
        graphics.setStroke(Color.web("#21140d"));
        graphics.strokeRect(x + 7.5, y + 10.5, size - 15, size - 23);
        graphics.strokeLine(x + 13, y + size - 12, x + 13, y + size - 5);
        graphics.strokeLine(x + size - 13, y + size - 12, x + size - 13, y + size - 5);
    }

    private void renderChair(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 14, y + 14, size - 28, size - 26);
        graphics.fillRect(x + 12, y + 9, size - 24, 6);
        graphics.setStroke(Color.web("#130d0a"));
        graphics.strokeLine(x + 16, y + 30, x + 13, y + 40);
        graphics.strokeLine(x + size - 16, y + 30, x + size - 13, y + 40);
    }

    private void renderBlackboard(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 5, y + 8, size - 10, size - 18);
        graphics.setStroke(Color.web("#b9a879"));
        graphics.strokeRect(x + 5.5, y + 8.5, size - 11, size - 19);
        graphics.setStroke(Color.rgb(235, 235, 220, 0.55));
        graphics.strokeLine(x + 13, y + 20, x + 31, y + 16);
        graphics.strokeLine(x + 17, y + 29, x + 36, y + 30);
    }

    private void renderLocker(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 9, y + 5, size - 18, size - 10);
        graphics.setStroke(Color.web("#1a2022"));
        graphics.strokeLine(x + size / 2.0, y + 7, x + size / 2.0, y + size - 7);
        graphics.strokeRect(x + 9.5, y + 5.5, size - 19, size - 11);
        graphics.setFill(Color.web("#182022"));
        graphics.fillRect(x + 15, y + 14, 4, 16);
        graphics.fillRect(x + size - 19, y + 14, 4, 16);
    }

    private void renderComputer(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        renderDesk(graphics, x, y, size, Tile.DESK);
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 13, y + 8, size - 26, 16);
        graphics.setFill(Color.web("#89d8ff"));
        graphics.fillRect(x + 16, y + 11, size - 32, 10);
        graphics.setFill(Color.web("#1b2530"));
        graphics.fillRect(x + 22, y + 25, size - 44, 5);
    }

    private void renderLabTable(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 6, y + 13, size - 12, size - 25);
        graphics.setFill(Color.web("#a7d5dc"));
        graphics.fillOval(x + 12, y + 16, 8, 8);
        graphics.setFill(Color.web("#b84b45"));
        graphics.fillOval(x + size - 20, y + 17, 7, 7);
    }

    private void renderBed(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 7, y + 9, size - 14, size - 16);
        graphics.setFill(Color.web("#d2c1a7"));
        graphics.fillRect(x + 10, y + 12, size - 20, 10);
        graphics.setFill(Color.rgb(65, 13, 18, 0.72));
        graphics.fillOval(x + size - 19, y + size - 21, 8, 8);
    }

    private void renderShelf(GraphicsContext graphics, double x, double y, int size, Tile tile) {
        graphics.setFill(tile.getAccentColor());
        graphics.fillRect(x + 8, y + 6, size - 16, size - 12);
        graphics.setStroke(Color.web("#25170d"));
        for (int shelfY = 15; shelfY < size - 7; shelfY += 10) {
            graphics.strokeLine(x + 10, y + shelfY, x + size - 10, y + shelfY);
        }
        graphics.setFill(Color.web("#9b7e54"));
        graphics.fillRect(x + 13, y + 10, 5, 24);
        graphics.fillRect(x + 23, y + 10, 4, 24);
    }

    private void renderDoor(GraphicsContext graphics, double x, double y, int size) {
        graphics.setFill(Color.web("#8b693a"));
        graphics.fillRect(x + 10, y + 6, size - 20, size - 12);
        graphics.setFill(Color.web("#e0c172"));
        graphics.fillOval(x + size - 18, y + size / 2.0 - 3, 6, 6);
    }

    private void renderStairs(GraphicsContext graphics, double x, double y, int size) {
        graphics.setStroke(Color.web("#9a8061"));
        for (int i = 9; i < size - 6; i += 8) {
            graphics.strokeLine(x + 8, y + i, x + size - 8, y + i);
        }
    }

    private void renderExit(GraphicsContext graphics, double x, double y, int size) {
        graphics.setFill(Color.web("#703a31"));
        graphics.fillRect(x + 7, y + 7, size - 14, size - 14);
        graphics.setFill(Color.web("#f0d088"));
        graphics.fillText("EXIT", x + 11, y + 29);
    }

    private void renderEntrance(GraphicsContext graphics, double x, double y, int size) {
        graphics.setStroke(Color.web("#75966d"));
        graphics.strokeOval(x + 10, y + 8, size - 20, size - 16);
    }

    private void renderCracks(GraphicsContext graphics, double x, double y, int size) {
        graphics.setStroke(Color.web("#4f5753"));
        graphics.strokeLine(x + 9, y + 10, x + 19, y + 24);
        graphics.strokeLine(x + 19, y + 24, x + 14, y + 38);
        graphics.strokeLine(x + 26, y + 14, x + 38, y + 34);
    }

    private void renderRitualMark(GraphicsContext graphics, double x, double y, int size) {
        renderCracks(graphics, x, y, size);
        graphics.setStroke(Color.rgb(160, 28, 28, 0.85));
        graphics.setLineWidth(2.0);
        graphics.strokeOval(x + 10, y + 10, size - 20, size - 20);
        graphics.strokeLine(x + size / 2.0, y + 11, x + 13, y + size - 14);
        graphics.strokeLine(x + size / 2.0, y + 11, x + size - 13, y + size - 14);
        graphics.strokeLine(x + 13, y + size - 14, x + size - 13, y + size - 14);
        graphics.setLineWidth(1.0);
    }

    private void renderFloorDetail(GraphicsContext graphics, double x, double y, int size) {
        graphics.setStroke(Color.rgb(255, 255, 255, 0.04));
        graphics.strokeLine(x + 8, y + size - 8, x + size - 8, y + size - 8);
    }
}
