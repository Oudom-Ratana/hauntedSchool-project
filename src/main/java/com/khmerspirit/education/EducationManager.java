package com.khmerspirit.education;

import com.khmerspirit.core.Game;
import com.khmerspirit.entities.Ghost;
import com.khmerspirit.items.ItemPickup;
import com.khmerspirit.items.ItemRegistry;
import com.khmerspirit.map.Door;
import com.khmerspirit.map.Tile;
import com.khmerspirit.map.TileMap;
import com.khmerspirit.player.Player;
import com.khmerspirit.player.Camera;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EducationManager {

    private final TileMap tileMap;
    private final Game game;
    private final QuestionLoader loader = new QuestionLoader();
    private final Random rng = new Random();

    private final Map<String, RoomTask> tasks = new HashMap<>();
    private RoomTask activeTask = null;
    private String activeRoomId = null;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final List<String> completedRooms = new ArrayList<>();

    public EducationManager(TileMap tileMap, Object assetManager, Game game) {
        this.tileMap = tileMap;
        this.game = game;
    }

    public void onPlayerRoomChanged(String roomDisplayName, double playerX, double playerY) {
        // Convert display name back to id by searching rooms
        tileMap.getRooms().stream()
                .filter(r -> r.getDisplayName().equals(roomDisplayName))
                .findFirst()
                .ifPresent(r -> ensureRoomTask(r.getId()));
    }

    private void ensureRoomTask(String roomId) {
        if (!tasks.containsKey(roomId)) {
            List<Question> all = loader.loadQuestionsForRoom(roomId);
            RoomTask t = new RoomTask(roomId, all, rng);
            tasks.put(roomId, t);
        }
    }

    public void update(double deltaSeconds, Player player) {
        // update ghosts
        List<Ghost> remove = new ArrayList<>();
        for (Ghost g : ghosts) {
            g.update(deltaSeconds, player, game);
        }
        ghosts.removeAll(remove);
    }

    public void render(GraphicsContext g, Camera camera, double canvasWidth, double canvasHeight) {
        // render ghosts
        for (Ghost ghost : ghosts) {
            ghost.render(g, camera.getX(), camera.getY());
        }

        // render question overlay if active
        if (activeTask != null) {
            Question q = activeTask.getCurrentQuestion();
            if (q != null) {
                double width = Math.min(760, Math.max(360, q.getText().length() * 8.0 + 48));
                double x = (canvasWidth - width) / 2.0;
                double y = canvasHeight - 180;
                g.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.85));
                g.fillRect(x, y, width, 140);
                g.setFill(javafx.scene.paint.Color.web("#f0dfb7"));
                g.fillText(q.getText(), x + 20, y + 28);
                List<String> opts = q.getOptions();
                for (int i = 0; i < opts.size(); i++) {
                    g.fillText((i + 1) + ". " + opts.get(i), x + 24, y + 52 + i * 18);
                }
                g.fillText("Press 1-4 to answer.", x + 20, y + 120);
            }
        }
    }

    public boolean tryInteractAt(double px, double py) {
        // If a task for the room exists and not active, start it when player interacts (E)
        tileMap.findRoomAt(px, py).ifPresent(room -> {
            ensureRoomTask(room.getId());
            if (activeTask == null) {
                RoomTask t = tasks.get(room.getId());
                if (t != null && !t.isCompleted()) {
                    activeTask = t;
                    activeRoomId = room.getId();
                    // shuffle questions each playthrough already done by RoomTask with RNG
                }
            }
        });
        return activeTask != null;
    }

    public boolean isActive() {
        return activeTask != null;
    }

    public void submitAnswer(int slotZeroBased) {
        if (activeTask == null) return;
        int choice = slotZeroBased; // slot 0 -> choice 0
        boolean correct = activeTask.submitAnswer(choice);
        if (correct) {
            game.playSound("puzzle_complete");
            game.showNotification("Correct! (" + activeTask.getCorrectCount() + "/5)");
            if (activeTask.isCompleted()) {
                // drop key and unlock next door
                dropRoomKeyAndUnlock(activeRoomId);
                completedRooms.add(activeRoomId);
                activeTask = null;
                activeRoomId = null;
            }
        } else {
            game.playSound("ghost");
            game.showNotification("Wrong answer! A ghost spawned.");
            spawnGhostNearPlayer();
        }
    }

    private void spawnGhostNearPlayer() {
        Player p = game.getPlayer();
        Ghost g = new Ghost(p.getCenterX() + 120, p.getCenterY() + 80);
        ghosts.add(g);
    }

    private void dropRoomKeyAndUnlock(String roomId) {
        // Find room center and door to unlock
        tileMap.getDoors().stream()
                .filter(d -> d.getFromRoomId().equals(roomId))
                .findFirst()
                .ifPresent(door -> {
                    tileMap.setTile(door.getColumn(), door.getRow(), Tile.FLOOR);
                });

        game.playSound("door");

        // drop a key at room center
        tileMap.getRooms().stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .ifPresent(room -> {
                    double centerX = (room.getColumn() + room.getWidth() / 2.0) * com.khmerspirit.config.Constants.TILE_SIZE;
                    double centerY = (room.getRow() + room.getHeight() / 2.0) * com.khmerspirit.config.Constants.TILE_SIZE;
                    ItemRegistry.findById("key").ifPresent(item -> {
                        ItemPickup pickup = new ItemPickup(item, centerX / com.khmerspirit.config.Constants.TILE_SIZE, centerY / com.khmerspirit.config.Constants.TILE_SIZE);
                        game.spawnItemPickup(pickup);
                    });
                });

        game.showNotification("Room solved! A key has dropped.");
    }

    public List<String> getCompletedRooms() {
        return new ArrayList<>(completedRooms);
    }

    public String getActiveRoomId() {
        return activeRoomId;
    }

    public int getActiveTaskCorrectCount() {
        return activeTask == null ? 0 : activeTask.getCorrectCount();
    }

}