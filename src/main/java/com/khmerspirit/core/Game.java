package com.khmerspirit.core;

import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.animation.ObjectAnimation;
import com.khmerspirit.config.Constants;
import com.khmerspirit.inventory.Inventory;
import com.khmerspirit.inventory.InventoryUI;
import com.khmerspirit.items.ItemPickup;
import com.khmerspirit.items.ItemRegistry;
import com.khmerspirit.map.CollisionMap;
import com.khmerspirit.map.Room;
import com.khmerspirit.map.Tile;
import com.khmerspirit.map.TileMap;
import com.khmerspirit.player.Camera;
import com.khmerspirit.player.Player;
import com.khmerspirit.player.PlayerController;
import com.khmerspirit.save.SaveData;
import com.khmerspirit.save.SaveManager;
import com.khmerspirit.education.EducationManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game {

    private final Canvas canvas;
    private final GraphicsContext graphics;
    private final AssetManager assetManager;
    private final PlayerController playerController;
    private final TileMap tileMap;
    private final CollisionMap collisionMap;
    private final Camera camera;
    private final Player player;
    private final GameLoop gameLoop;
    private final Inventory inventory;
    private final InventoryUI inventoryUI;
    private final SaveManager saveManager;
    private final List<ItemPickup> itemPickups;
    private final EducationManager educationManager;
    private String currentRoomName;
    private String notificationMessage;
    private double notificationSeconds;
    private double playTimeSeconds = 0.0;
    private double autoSaveAccumulator = 0.0;
    private final double autoSaveInterval = 10.0; // seconds
    private boolean gameOver = false;
    private Runnable gameOverHandler;
    private final Random effectRandom = new Random(7);
    private final List<RainDrop> rainDrops = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FogPatch> fogPatches = new ArrayList<>();
    private final ObjectAnimation rainAnimation = new ObjectAnimation(ObjectAnimation.Type.RAIN);
    private final ObjectAnimation torchAnimation = new ObjectAnimation(ObjectAnimation.Type.TORCH);
    private double lightningTimer = 2.5;
    private double lightningFlash = 0.0;

    public Game(Canvas canvas, String selectedCharacter) {
        this(canvas, selectedCharacter, null);
    }

    public Game(Canvas canvas, String selectedCharacter, SaveData saveData) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.assetManager = new AssetManager();
        this.playerController = new PlayerController();
        this.tileMap = TileMap.createSchoolMap();
        this.collisionMap = new CollisionMap(tileMap);
        this.camera = new Camera(canvas.getWidth(), canvas.getHeight(), tileMap.getPixelWidth(), tileMap.getPixelHeight());

        this.saveManager = new SaveManager();
        this.inventory = new Inventory();
        this.inventoryUI = new InventoryUI();

        // player
        if (saveData == null) {
            this.player = new Player(40 * Constants.TILE_SIZE, 50 * Constants.TILE_SIZE, selectedCharacter, assetManager.loadPlayerSprite(selectedCharacter));
        } else {
            this.player = new Player(saveData.getPlayerX(), saveData.getPlayerY(), saveData.getCharacterName(), assetManager.loadPlayerSprite(saveData.getCharacterName()));
            this.inventory.replaceAll(saveData.getInventoryItems());
        }

        this.gameLoop = new GameLoop(this);
        this.itemPickups = createItemPickups();
        this.educationManager = new EducationManager(tileMap, assetManager, this);
        this.currentRoomName = "Entrance";
        this.notificationMessage = "Press E near an item to pick it up. Use 1-0 for inventory.";
        this.notificationSeconds = 5.0;
        this.playTimeSeconds = saveData == null ? 0.0 : saveData.getPlayTimeSeconds();

        // restore hearts
        if (saveData != null) {
            this.player.setHearts(saveData.getHearts());
        }

        // unlock doors for completed rooms
        if (saveData != null && !saveData.getCompletedRooms().isEmpty()) {
            for (String roomId : saveData.getCompletedRooms()) {
                tileMap.getDoors().stream()
                        .filter(d -> d.getFromRoomId().equals(roomId))
                        .findFirst()
                        .ifPresent(door -> tileMap.setTile(door.getColumn(), door.getRow(), Tile.FLOOR));
            }
        }

        // if no save supplied, load inventory from storage
        if (saveData == null) {
            loadInventory();
        }
        initializeEffects();
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
        AudioManager.getInstance().stopAll();
    }

    public void setGameOverHandler(Runnable gameOverHandler) {
        this.gameOverHandler = gameOverHandler;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void playSound(String key) {
        AudioManager.getInstance().playOneShot(key);
    }

    public String getCurrentRoomId() {
        return tileMap.findRoomAt(player.getCenterX(), player.getCenterY()).map(Room::getId).orElse("");
    }

    public java.util.List<String> getEducationCompletedRooms() {
        return educationManager.getCompletedRooms();
    }

    public String getEducationActiveRoomId() {
        return educationManager.getActiveRoomId();
    }

    public int getEducationActiveCorrectCount() {
        return educationManager.getActiveTaskCorrectCount();
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public double getPlayTimeSeconds() { return playTimeSeconds; }

    public void update(double deltaSeconds) {
        if (gameOver) {
            return;
        }

        player.update(deltaSeconds, playerController, collisionMap);
        camera.follow(player.getCenterX(), player.getCenterY());
        String newRoomName = tileMap.findRoomAt(player.getCenterX(), player.getCenterY())
                .map(Room::getDisplayName)
                .orElse("Main Hall");
        if (!newRoomName.equals(this.currentRoomName)) {
            this.currentRoomName = newRoomName;
            educationManager.onPlayerRoomChanged(currentRoomName, player.getCenterX(), player.getCenterY());
        }
        educationManager.update(deltaSeconds, player);
        updateInventoryInput();
        updateEffects(deltaSeconds);
        // play time and auto-save
        playTimeSeconds += deltaSeconds;
        autoSaveAccumulator += deltaSeconds;
        if (autoSaveAccumulator >= autoSaveInterval) {
            autoSaveAccumulator = 0.0;
            saveNow();
        }

        if (notificationSeconds > 0.0) {
            notificationSeconds = Math.max(0.0, notificationSeconds - deltaSeconds);
        }
    }

    public void render() {
        graphics.setImageSmoothing(false);
        graphics.setFill(Color.web("#020303"));
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderRain();
        tileMap.render(graphics, camera);
        renderFog();
        renderFlashlightLighting();
        renderPickups();
        player.render(graphics, camera);
        renderParticles();
        renderDebugText();
        educationManager.render(graphics, camera, canvas.getWidth(), canvas.getHeight());
        inventoryUI.render(graphics, inventory, canvas.getWidth(), canvas.getHeight());
        renderNotification();
    }

    public void gameOver() {
        if (gameOver) {
            return;
        }

        gameOver = true;
        saveManager.deleteSave();
        showNotification("Game Over");
        stop();
        if (gameOverHandler != null) {
            gameOverHandler.run();
        }
    }

    private void renderDebugText() {
        graphics.setFill(Color.rgb(0, 0, 0, 0.42));
        graphics.fillRoundRect(18, 18, 320, 70, 12, 12);
        graphics.setFill(Color.web("#f2e4b7"));
        graphics.fillText("ROOM: " + currentRoomName.toUpperCase(), 34, 44);
        graphics.fillText("HEARTS: " + player.getHearts() + "  |  E: INTERACT", 34, 66);
    }

    private void initializeEffects() {
        for (int i = 0; i < 110; i++) {
            RainDrop drop = new RainDrop();
            drop.x = effectRandom.nextDouble() * canvas.getWidth();
            drop.y = effectRandom.nextDouble() * canvas.getHeight();
            drop.speed = 220 + effectRandom.nextDouble() * 180;
            drop.length = 8 + effectRandom.nextDouble() * 16;
            rainDrops.add(drop);
        }
        for (int i = 0; i < 24; i++) {
            particles.add(new Particle(effectRandom.nextDouble() * canvas.getWidth(), effectRandom.nextDouble() * canvas.getHeight()));
        }
        for (int i = 0; i < 6; i++) {
            fogPatches.add(new FogPatch(effectRandom.nextDouble() * canvas.getWidth(), effectRandom.nextDouble() * canvas.getHeight()));
        }
    }

    private void updateEffects(double deltaSeconds) {
        rainAnimation.update(deltaSeconds);
        torchAnimation.update(deltaSeconds);
        lightningTimer -= deltaSeconds;
        if (lightningTimer <= 0.0) {
            lightningTimer = 2.0 + effectRandom.nextDouble() * 4.0;
            lightningFlash = 0.25;
        }
        if (lightningFlash > 0.0) {
            lightningFlash = Math.max(0.0, lightningFlash - deltaSeconds * 1.6);
        }

        for (RainDrop drop : rainDrops) {
            drop.y += drop.speed * deltaSeconds;
            if (drop.y > canvas.getHeight()) {
                drop.y = -drop.length;
                drop.x = effectRandom.nextDouble() * canvas.getWidth();
            }
        }

        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.update(deltaSeconds);
            if (particle.isDead()) {
                particleIterator.remove();
            }
        }
        while (particles.size() < 40) {
            particles.add(new Particle(effectRandom.nextDouble() * canvas.getWidth(), effectRandom.nextDouble() * canvas.getHeight()));
        }

        for (FogPatch patch : fogPatches) {
            patch.update(deltaSeconds);
        }
    }

    private void renderRain() {
        double offsetY = rainAnimation.getOffsetY();
        double flicker = rainAnimation.getFlicker();
        graphics.setStroke(Color.rgb(190, 220, 250, 0.26 * flicker));
        graphics.setLineWidth(1.0);
        for (RainDrop drop : rainDrops) {
            double y = (drop.y + offsetY) % (canvas.getHeight() + 100);
            graphics.strokeLine(drop.x, y, drop.x + 0.8, y + drop.length);
        }
    }

    private void renderFog() {
        for (FogPatch patch : fogPatches) {
            patch.render(graphics);
        }
    }

    private void renderFlashlightLighting() {
        double screenX = player.getCenterX() - camera.getX();
        double screenY = player.getCenterY() - camera.getY();
        RadialGradient gradient = new RadialGradient(0, 0, screenX, screenY, 180, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 226, 150, 0.11)),
                new Stop(0.26, Color.rgb(105, 55, 42, 0.08)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.91)));
        graphics.setFill(gradient);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (lightningFlash > 0.0) {
            graphics.setFill(Color.rgb(255, 255, 255, lightningFlash * 0.45));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    private void renderParticles() {
        for (Particle particle : particles) {
            particle.render(graphics);
        }
    }

    private void renderPickups() {
        for (ItemPickup pickup : itemPickups) {
            pickup.render(graphics, camera);
        }
    }

    private void renderNotification() {
        if (notificationSeconds <= 0.0 || notificationMessage == null || notificationMessage.isBlank()) {
            return;
        }

        double width = Math.min(760, Math.max(360, notificationMessage.length() * 8.0 + 48));
        double x = (canvas.getWidth() - width) / 2.0;
        double y = 24;
        graphics.setFill(Color.rgb(0, 0, 0, 0.72));
        graphics.fillRect(x, y, width, 42);
        graphics.setStroke(Color.web("#8b7145"));
        graphics.strokeRect(x + 0.5, y + 0.5, width - 1, 41);
        graphics.setFill(Color.web("#f0dfb7"));
        graphics.fillText(notificationMessage, x + 20, y + 27);
    }

    private void updateInventoryInput() {
        if (playerController.consumePressed(KeyCode.E)) {
            // Try interact with education system first (start/advance room task)
            if (educationManager.tryInteractAt(player.getCenterX(), player.getCenterY())) {
                return;
            }
            pickUpNearbyItem();
        }

        for (int slot = 0; slot < 10; slot++) {
            if (playerController.consumePressed(keyForSlot(slot))) {
                if (educationManager.isActive()) {
                    educationManager.submitAnswer(slot);
                } else {
                    String message = inventory.useSlot(slot);
                    showNotification(message);
                    saveNow();
                }
            }
        }
    }

    private void pickUpNearbyItem() {
        Iterator<ItemPickup> iterator = itemPickups.iterator();
        while (iterator.hasNext()) {
            ItemPickup pickup = iterator.next();
            if (pickup.isNear(player.getCenterX(), player.getCenterY())) {
                inventory.addItem(pickup.getItem());
                iterator.remove();
                saveNow();
                showNotification("Picked up " + pickup.getItem().getDisplayName() + ".");
                return;
            }
        }
        showNotification("No item nearby.");
    }

    public void showNotification(String message) {
        notificationMessage = message;
        notificationSeconds = 3.0;
    }

    private void loadInventory() {
        SaveData saveData = saveManager.load();
        inventory.replaceAll(saveData.getInventoryItems());
    }

    private List<ItemPickup> createItemPickups() {
        List<ItemPickup> pickups = new ArrayList<>();
        addPickup(pickups, "flashlight", 41, 50);
        addPickup(pickups, "battery", 14, 12);
        addPickup(pickups, "key", 34, 12);
        addPickup(pickups, "master_key", 78, 32);
        addPickup(pickups, "holy_charm", 56, 12);
        addPickup(pickups, "first_aid_kit", 33, 33);
        addPickup(pickups, "notebook", 52, 14);
        addPickup(pickups, "lighter", 75, 13);
        addPickup(pickups, "map", 35, 48);
        addPickup(pickups, "toolbox", 16, 32);
        return pickups;
    }

    // Allow education manager to spawn pickups into the world
    public void spawnItemPickup(ItemPickup pickup) {
        if (pickup != null) {
            this.itemPickups.add(pickup);
            saveNow();
        }
    }

    private void addPickup(List<ItemPickup> pickups, String itemId, double tileColumn, double tileRow) {
        ItemRegistry.findById(itemId).ifPresent(item -> pickups.add(new ItemPickup(item, tileColumn, tileRow)));
    }

    private KeyCode keyForSlot(int slot) {
        return switch (slot) {
            case 0 -> KeyCode.DIGIT1;
            case 1 -> KeyCode.DIGIT2;
            case 2 -> KeyCode.DIGIT3;
            case 3 -> KeyCode.DIGIT4;
            case 4 -> KeyCode.DIGIT5;
            case 5 -> KeyCode.DIGIT6;
            case 6 -> KeyCode.DIGIT7;
            case 7 -> KeyCode.DIGIT8;
            case 8 -> KeyCode.DIGIT9;
            default -> KeyCode.DIGIT0;
        };
    }

    private void saveNow() {
        try {
            saveManager.saveGame(GameSaveHelper.buildFrom(this));
        } catch (Exception e) {
            showNotification("Save failed: " + e.getMessage());
        }
    }

    private static final class RainDrop {
        double x;
        double y;
        double speed;
        double length;
    }

    private static final class Particle {
        double x;
        double y;
        double vx;
        double vy;
        double life;
        double maxLife;

        Particle(double x, double y) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() - 0.5) * 40.0;
            this.vy = -20.0 - Math.random() * 30.0;
            this.life = 0.7 + Math.random() * 1.0;
            this.maxLife = life;
        }

        void update(double deltaSeconds) {
            x += vx * deltaSeconds;
            y += vy * deltaSeconds;
            vy += 40.0 * deltaSeconds;
            life -= deltaSeconds;
        }

        boolean isDead() {
            return life <= 0.0;
        }

        void render(GraphicsContext g) {
            double alpha = Math.max(0.0, life / maxLife);
            g.setFill(Color.rgb(220, 220, 255, alpha * 0.35));
            g.fillOval(x, y, 2.0 + alpha * 1.5, 2.0 + alpha * 1.5);
        }
    }

    private static final class FogPatch {
        double x;
        double y;
        double drift;
        double size;
        double alpha;

        FogPatch(double x, double y) {
            this.x = x;
            this.y = y;
            this.drift = 10.0 + Math.random() * 12.0;
            this.size = 50.0 + Math.random() * 100.0;
            this.alpha = 0.10 + Math.random() * 0.12;
        }

        void update(double deltaSeconds) {
            x += drift * deltaSeconds;
            if (x > 900) x = -120;
        }

        void render(GraphicsContext g) {
            g.setFill(Color.rgb(220, 224, 224, alpha));
            g.fillOval(x, y, size, size * 0.55);
        }
    }
}
