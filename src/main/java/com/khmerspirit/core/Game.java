package com.khmerspirit.core;

import com.khmerspirit.admin.model.MapItemModel;
import com.khmerspirit.admin.service.MapItemFileService;
import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.animation.ObjectAnimation;
import com.khmerspirit.config.Constants;
import com.khmerspirit.inventory.Inventory;
import com.khmerspirit.inventory.InventoryUI;
import com.khmerspirit.items.Item;
import com.khmerspirit.items.ItemPickup;
import com.khmerspirit.items.ItemRegistry;
import com.khmerspirit.map.CollisionMap;
import com.khmerspirit.map.Door;
import com.khmerspirit.map.MinimapUI;
import com.khmerspirit.map.Room;
import com.khmerspirit.map.Tile;
import com.khmerspirit.map.TileMap;
import com.khmerspirit.player.Camera;
import com.khmerspirit.player.Player;
import com.khmerspirit.player.PlayerController;
import com.khmerspirit.save.SaveData;
import com.khmerspirit.save.SaveManager;
import com.khmerspirit.education.EducationManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Game {

    private final Canvas canvas;
    private final GraphicsContext graphics;
    private final AssetManager assetManager;
    private final PlayerController playerController;
    private TileMap tileMap;
    private CollisionMap collisionMap;
    private Camera camera;
    private final Player player;
    private final GameLoop gameLoop;
    private final Inventory inventory;
    private final InventoryUI inventoryUI;
    private final SaveManager saveManager;
    private List<ItemPickup> itemPickups;
    private final EducationManager educationManager;
    private final MapItemFileService mapItemFileService = new MapItemFileService();
    private String currentMapId = "classroomA";
    private final com.khmerspirit.map.MapLoader mapLoader = new com.khmerspirit.map.MapLoader();
    private final Map<String, List<ItemPickup>> roomItemPickups = new HashMap<>();
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
    private final ObjectAnimation rainAnimation = new ObjectAnimation(ObjectAnimation.Type.RAIN);
    private final ObjectAnimation torchAnimation = new ObjectAnimation(ObjectAnimation.Type.TORCH);
    private double lightningTimer = 2.5;
    private double lightningFlash = 0.0;
    private final MinimapUI minimapUI = new MinimapUI();
    private double pulseTimer = 0.0;
    private boolean flashlightOn = true;
    private double flashlightBattery = 100.0;
    private final double batteryDrainPerSec = 0.8;
    private boolean hudVisible = true;

    // Item ability state and cooldown timers
    private boolean nightVisionActive = false;
    private boolean lighterOn = false;
    private double speedBoostTimer = 0.0;
    private double hallwayPowerTimer = 0.0;
    private double bellCooldownTimer = 0.0;
    private double tomeCooldownTimer = 0.0;
    private double musicCooldownTimer = 0.0;
    private double batteryEfficiencyTimer = 0.0;
    private final Set<String> unlockedDoorIds = new HashSet<>();

    // Ambient ghost suffering audio timer (played once every 15 to 20 seconds throughout the project)
    private double ambientGhostAudioTimer = 0.0;
    private double nextAmbientGhostAudioInterval = 15.0 + new Random().nextDouble() * 5.0;

    public Game(Canvas canvas, String selectedCharacter) {
        this(canvas, selectedCharacter, null);
    }

    public Game(Canvas canvas, String selectedCharacter, SaveData saveData) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.assetManager = new AssetManager();
        this.playerController = new PlayerController();
        String startRoom = (saveData != null && saveData.getCurrentRoomId() != null && !saveData.getCurrentRoomId().isBlank())
                ? saveData.getCurrentRoomId()
                : "classroomA";
        this.currentMapId = startRoom;
        this.tileMap = mapLoader.loadMapForId(startRoom);
        this.collisionMap = new CollisionMap(tileMap);
        this.camera = new Camera(canvas.getWidth(), canvas.getHeight(), tileMap.getPixelWidth(), tileMap.getPixelHeight());
        this.currentRoomName = tileMap.getRooms().isEmpty() ? "Central Hallway" : tileMap.getRooms().getFirst().getDisplayName();

        this.saveManager = new SaveManager();
        this.inventory = new Inventory();
        this.inventoryUI = new InventoryUI();
        this.educationManager = new EducationManager(tileMap, assetManager, this);
        if (saveData != null && !saveData.getCompletedRooms().isEmpty()) {
            this.educationManager.restoreCompletedRooms(saveData.getCompletedRooms());
        }

        // Mouse click handling for question panel, minimap radar, and full blueprint map
        this.canvas.setOnMouseClicked(e -> {
            if (educationManager.isPanelVisible()) {
                if (educationManager.handleMouseClick(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight())) {
                    return;
                }
            }
            minimapUI.handleMouseClick(e.getX(), e.getY());
        });

        // player spawn in front of classroom entrance looking up the aisle or at exact saved position
        if (saveData == null) {
            this.player = new Player(1138.0, 1025.0, selectedCharacter, assetManager.loadPlayerSprite(selectedCharacter));
        } else {
            this.player = new Player(saveData.getPlayerX(), saveData.getPlayerY(), saveData.getCharacterName(), assetManager.loadPlayerSprite(saveData.getCharacterName()));
            this.inventory.replaceAll(saveData.getInventoryItems());
            this.player.setHearts(saveData.getHearts());
        }

        this.gameLoop = new GameLoop(this);
        this.itemPickups = createItemPickupsForRoom(startRoom);
        this.roomItemPickups.put(startRoom.toLowerCase(), this.itemPickups);
        this.notificationMessage = "WASD/Arrows: Move | SHIFT: Run | G: Pick Up | E: Interact | F: Flashlight | M: Map | H: HUD";
        this.notificationSeconds = 8.0;
        this.playTimeSeconds = saveData == null ? 0.0 : saveData.getPlayTimeSeconds();

        // unlock doors for completed rooms
        if (saveData != null && !saveData.getCompletedRooms().isEmpty()) {
            for (String compRoom : saveData.getCompletedRooms()) {
                tileMap.getDoors().stream()
                        .filter(d -> d.getFromRoomId().equalsIgnoreCase(compRoom) || d.getToRoomId().equalsIgnoreCase(compRoom))
                        .forEach(door -> {
                            door.setLocked(false);
                            tileMap.setDoorOpen(door, true);
                            unlockedDoorIds.add(door.getId());
                        });
            }
        }

        // if no save supplied, load inventory from storage
        if (saveData == null) {
            loadInventory();
        }
        camera.follow(player.getCenterX(), player.getCenterY());
        AudioManager.getInstance().updateRoomMusic(startRoom);
        initializeEffects();
    }

    public void start() {
        paused = false;
        gameLoop.start();
    }

    public void stop() {
        paused = true;
        gameLoop.stop();
        AudioManager.getInstance().stopAll();
    }

    private boolean paused = false;

    public void pause() {
        paused = true;
        gameLoop.stop();
        if (playerController != null) {
            playerController.resetKeys();
        }
    }

    public void resume() {
        paused = false;
        gameLoop.start();
    }

    public boolean isPaused() {
        return paused;
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

    public String getCurrentMapId() {
        return currentMapId;
    }

    public int getEducationActiveCorrectCount() {
        return educationManager.getActiveTaskCorrectCount();
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public double getPlayTimeSeconds() { return playTimeSeconds; }
    public void addUnlockedDoor(String doorId) { if (doorId != null) unlockedDoorIds.add(doorId); }

    public void onResize(double newWidth, double newHeight) {
        if (camera != null && player != null) {
            camera.setViewportSize(newWidth, newHeight);
            camera.follow(player.getCenterX(), player.getCenterY());
        }
    }

    public void update(double deltaSeconds) {
        if (gameOver) {
            return;
        }

        pulseTimer += deltaSeconds;

        player.update(deltaSeconds, playerController, collisionMap);
        if (player.isDead()) {
            gameOver();
            return;
        }
        camera.follow(player.getCenterX(), player.getCenterY());
        String newRoomName = determineRoomName(player.getCenterX(), player.getCenterY());
        if (!newRoomName.equals(this.currentRoomName)) {
            this.currentRoomName = newRoomName;
            educationManager.onPlayerRoomChanged(currentRoomName, player.getCenterX(), player.getCenterY());
            tileMap.findRoomAt(player.getCenterX(), player.getCenterY()).ifPresent(r -> {
                AudioManager.getInstance().updateRoomMusic(r.getId());
            });
        }
        educationManager.update(deltaSeconds, player);
        checkDoorTransitions();
        updateInventoryInput();

        // Flashlight battery drainage (efficiency halved when toolbox used)
        double drainMultiplier = (batteryEfficiencyTimer > 0.0) ? 0.5 : 1.0;
        if (flashlightOn && inventory.hasItem("flashlight")) {
            if (flashlightBattery > 0.0) {
                flashlightBattery = Math.max(0.0, flashlightBattery - batteryDrainPerSec * drainMultiplier * deltaSeconds);
                if (flashlightBattery <= 0.0) {
                    flashlightBattery = 0.0;
                    showNotification("Flashlight battery died! Find or use a Battery (Slots 1-9).");
                }
            }
        }

        // Active item effect countdowns
        if (speedBoostTimer > 0.0) {
            speedBoostTimer = Math.max(0.0, speedBoostTimer - deltaSeconds);
            if (speedBoostTimer <= 0.0) {
                player.setSpeedMultiplier(1.0);
                showNotification("Speed boost effect wore off.");
            }
        }
        if (hallwayPowerTimer > 0.0) {
            hallwayPowerTimer = Math.max(0.0, hallwayPowerTimer - deltaSeconds);
            if (hallwayPowerTimer <= 0.0) {
                showNotification("Emergency power generator shut off... Darkness returns.");
            }
        }
        if (batteryEfficiencyTimer > 0.0) {
            batteryEfficiencyTimer = Math.max(0.0, batteryEfficiencyTimer - deltaSeconds);
        }
        if (bellCooldownTimer > 0.0) {
            bellCooldownTimer = Math.max(0.0, bellCooldownTimer - deltaSeconds);
        }
        if (tomeCooldownTimer > 0.0) {
            tomeCooldownTimer = Math.max(0.0, tomeCooldownTimer - deltaSeconds);
        }
        if (musicCooldownTimer > 0.0) {
            musicCooldownTimer = Math.max(0.0, musicCooldownTimer - deltaSeconds);
        }

        // Ambient ghost suffering audio (shuffled every 15 to 20 seconds)
        ambientGhostAudioTimer += deltaSeconds;
        if (ambientGhostAudioTimer >= nextAmbientGhostAudioInterval) {
            ambientGhostAudioTimer = 0.0;
            nextAmbientGhostAudioInterval = 15.0 + effectRandom.nextDouble() * 5.0;
            AudioManager.getInstance().playRandomAmbientGhostSuffering();
        }

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

    private String determineRoomName(double x, double y) {
        var roomOpt = tileMap.findRoomAt(x, y);
        if (roomOpt.isPresent()) {
            return roomOpt.get().getDisplayName();
        }
        if (currentMapId.equalsIgnoreCase("hall") || currentMapId.equalsIgnoreCase("school")) {
            int tx = (int) (x / Constants.TILE_SIZE);
            int ty = (int) (y / Constants.TILE_SIZE);
            if (ty >= 40) {
                return "Entrance Lobby";
            } else if (tx < 33) {
                return "West Wing Corridor";
            } else if (tx > 58) {
                return "East Wing Corridor";
            } else {
                return "Central Grand Hallway";
            }
        }
        return "Main Hall";
    }

    public void render() {
        graphics.setImageSmoothing(false);
        graphics.setFill(Color.web("#020303"));
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderRain();
        tileMap.render(graphics, camera);

        double lightRadius = getCurrentLightRadius();

        // 1. In-world stations, pickups & ghosts (obscured outside light unless night vision is active)
        educationManager.renderQuizStation(graphics, camera, player.getCenterX(), player.getCenterY(), lightRadius);
        renderPickups(lightRadius);
        educationManager.renderGhosts(graphics, camera, player.getCenterX(), player.getCenterY(), lightRadius, nightVisionActive);
        player.render(graphics, camera);
        tileMap.renderForeground(graphics, camera);

        // 2. World particles
        renderParticles();

        // 3. Darkness mask
        renderFlashlightLighting();

        // Door English Labels & Prompts in Main Hall (drawn clearly over darkness)
        renderDoorLabels(graphics, camera);

        // 4. Clean UI Overlays (drawn on top of darkness)
        educationManager.renderQuestionPanel(graphics, canvas.getWidth(), canvas.getHeight());
        inventoryUI.render(graphics, inventory, canvas.getWidth(), canvas.getHeight(), assetManager);
        renderNotification();

        // Live GTA-style minimap radar & full architectural blueprint overlay (red ghost dots only appear when wearing night vision)
        boolean hasNightVision = nightVisionActive && inventory.hasItem("night_vision");
        minimapUI.renderMinimap(graphics, tileMap, player, itemPickups, educationManager.getGhosts(), currentMapId, pulseTimer, hasNightVision);
        minimapUI.renderFullMap(graphics, currentMapId, player.getCenterX(), player.getCenterY(), canvas.getWidth(), canvas.getHeight(), pulseTimer);

        renderDebugText();
    }

    public void gameOver() {
        if (gameOver) {
            return;
        }

        gameOver = true;
        saveManager.deleteSave();
        AudioManager.getInstance().playJumpscare();
        showNotification("Game Over");
        stop();
        if (gameOverHandler != null) {
            gameOverHandler.run();
        }
    }

    private void renderDebugText() {
        if (!hudVisible || minimapUI.isFullMapOpen()) {
            return;
        }
        double dx = 176.0;
        double dy = 18.0;
        graphics.setFill(Color.rgb(0, 0, 0, 0.68));
        graphics.fillRoundRect(dx, dy, 460, 88, 10, 10);
        graphics.setStroke(Color.web("#8b7145"));
        graphics.strokeRoundRect(dx + 0.5, dy + 0.5, 459, 87, 10, 10);

        // Room + Health Hearts + Active Buffs
        graphics.setFill(Color.web("#ffd972"));
        StringBuilder heartsStr = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            heartsStr.append(i < player.getHearts() ? "❤ " : "🖤 ");
        }
        boolean hasNV = inventory.hasItem("night_vision");
        String nvBadge = nightVisionActive ? " | 🥽 NV ON" : (hasNV ? " | 🥽 NV OFF" : "");
        String speedBadge = (speedBoostTimer > 0.0) ? " ⚡+45%" : "";
        String shieldBadge = player.isInvulnerable() ? " 🛡️SHIELD" : "";
        String lighterBadge = lighterOn ? " 🔥" : "";
        graphics.fillText("ROOM: " + currentRoomName.toUpperCase() + "  |  HP: " + heartsStr + nvBadge + speedBadge + shieldBadge + lighterBadge, dx + 16, dy + 24);

        // Flashlight status & Battery
        graphics.setFill(Color.web("#f2e4b7"));
        boolean hasFlash = inventory.hasItem("flashlight");
        String flashStatus = hasFlash
                ? (flashlightOn && flashlightBattery > 0 ? "🔦 ON (" + (int) flashlightBattery + "%) [F]" : "🔦 OFF (" + (int) flashlightBattery + "%) [F]")
                : "🔦 NONE";
        String sprintStatus = playerController.isSprinting() ? " [RUN]" : " [WALK]";
        graphics.fillText("MOVE: WASD" + sprintStatus + " | " + flashStatus, dx + 16, dy + 46);

        // Key guidance
        graphics.fillText("G: PICK UP | E: ENTER | Q: QUEST GUIDE | M: MAP | H: HUD", dx + 16, dy + 68);
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
        while (particles.size() < 40) {
            particles.add(new Particle(effectRandom.nextDouble() * canvas.getWidth(), effectRandom.nextDouble() * canvas.getHeight()));
        }
    }

    private void updateEffects(double deltaSeconds) {
        rainAnimation.update(deltaSeconds);
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

    public double getCurrentLightRadius() {
        boolean hasFlashlight = inventory.hasItem("flashlight");
        boolean isFlashlightActive = hasFlashlight && flashlightOn && flashlightBattery > 0.0;
        double lightRadius = isFlashlightActive ? 330.0 : 120.0;
        if (lighterOn) {
            lightRadius += 140.0; // Brass lighter adds 360 firelight glow
        }
        if (hallwayPowerTimer > 0.0) {
            lightRadius = Math.max(lightRadius, 960.0); // Emergency fuse fully illuminates the room
        }
        if (isFlashlightActive && flashlightBattery < 18.0 && effectRandom.nextDouble() < 0.20) {
            lightRadius *= (0.70 + effectRandom.nextDouble() * 0.30);
        }
        return lightRadius;
    }

    private void renderFlashlightLighting() {
        double screenX = player.getCenterX() - camera.getX();
        double screenY = player.getCenterY() - camera.getY();

        boolean hasFlashlight = inventory.hasItem("flashlight");
        boolean isFlashlightActive = hasFlashlight && flashlightOn && flashlightBattery > 0.0;
        double lightRadius = getCurrentLightRadius();

        // If emergency power generator is running, darkness is reduced to ambient haze
        double edgeAlpha1 = hallwayPowerTimer > 0.0 ? 0.20 : 0.76;
        double edgeAlpha2 = hallwayPowerTimer > 0.0 ? 0.35 : 0.96;
        double edgeAlpha3 = hallwayPowerTimer > 0.0 ? 0.45 : 1.0;

        RadialGradient gradient = new RadialGradient(0, 0, screenX, screenY, lightRadius, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, isFlashlightActive ? Color.rgb(255, 240, 185, 0.05) : Color.rgb(190, 210, 225, 0.03)),
                new Stop(0.35, isFlashlightActive ? Color.rgb(190, 140, 70, 0.08) : Color.rgb(60, 45, 75, 0.07)),
                new Stop(0.70, Color.rgb(8, 6, 14, edgeAlpha1)),
                new Stop(0.92, Color.rgb(2, 1, 4, edgeAlpha2)),
                new Stop(1.0, Color.rgb(0, 0, 0, edgeAlpha3)));
        graphics.setFill(gradient);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (lightningFlash > 0.0) {
            graphics.setFill(Color.rgb(255, 255, 255, lightningFlash * 0.45));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        if (nightVisionActive) {
            // Tactical green night-vision scan visor & HUD borders
            graphics.setFill(Color.rgb(10, 45, 20, 0.16));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setStroke(Color.rgb(40, 240, 110, 0.35));
            graphics.setLineWidth(1.5);
            graphics.strokeRect(12, 12, canvas.getWidth() - 24, canvas.getHeight() - 24);
        }
    }

    private void renderParticles() {
        for (Particle particle : particles) {
            particle.render(graphics);
        }
    }

    private void renderPickups(double lightRadius) {
        for (ItemPickup pickup : itemPickups) {
            double dist = Math.hypot(pickup.getX() - player.getCenterX(), pickup.getY() - player.getCenterY());
            // Invisibility in darkness: items outside the hero's illuminated area are not visible
            if (dist > lightRadius + 20.0) {
                continue;
            }
            javafx.scene.image.Image sprite = assetManager.loadItemSprite(pickup.getItem().getId());
            pickup.render(graphics, camera, sprite);

            // Floating [G] Pick Up prompt when player is near the pickup
            if (pickup.isNear(player.getCenterX(), player.getCenterY())) {
                double sx = pickup.getX() - camera.getX();
                double sy = pickup.getY() - camera.getY();
                String prompt = "[G] Pick up " + pickup.getItem().getDisplayName();
                double textW = prompt.length() * 7.2 + 20.0;
                double bx = sx - textW / 2.0;
                double by = sy - 44.0;

                graphics.setFill(Color.rgb(12, 10, 15, 0.90));
                graphics.fillRoundRect(bx, by, textW, 22.0, 6, 6);
                graphics.setStroke(Color.rgb(225, 175, 55, 0.92));
                graphics.setLineWidth(1.5);
                graphics.strokeRoundRect(bx, by, textW, 22.0, 6, 6);

                graphics.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 11));
                graphics.setFill(Color.web("#ffeaa7"));
                graphics.fillText(prompt, bx + 10, by + 15);
            }
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
        if (educationManager.isStoryGuideVisible()) {
            if (playerController.consumePressed(KeyCode.SPACE) || playerController.consumePressed(KeyCode.ENTER) || playerController.consumePressed(KeyCode.E) || playerController.consumePressed(KeyCode.ESCAPE)) {
                educationManager.dismissStoryGuide();
                return;
            }
        }

        if (playerController.consumePressed(KeyCode.Q) || playerController.consumePressed(KeyCode.L)) {
            educationManager.toggleStoryGuide();
            return;
        }

        if (playerController.consumePressed(KeyCode.M) || playerController.consumePressed(KeyCode.TAB)) {
            minimapUI.toggleFullMap();
        }
        if (playerController.consumePressed(KeyCode.ESCAPE)) {
            if (minimapUI.isFullMapOpen()) {
                minimapUI.setFullMapOpen(false);
            }
        }

        if (playerController.consumePressed(KeyCode.F)) {
            if (inventory.hasItem("flashlight")) {
                flashlightOn = !flashlightOn;
                AudioManager.getInstance().playFlashlight();
                showNotification("Flashlight: " + (flashlightOn ? "ON [Battery: " + (int) flashlightBattery + "%]" : "OFF (Conserving battery)"));
            } else {
                showNotification("You don't have a Flashlight yet! Find one in Classroom A.");
            }
        }

        if (playerController.consumePressed(KeyCode.C)) {
            if (!tryCloseNearbyDoor()) {
                showNotification("No open door nearby to close.");
            }
        }

        if (playerController.consumePressed(KeyCode.F3)) {
            tileMap.toggleDebugCollision();
            showNotification("Collision Grid Overlay: " + (tileMap.isDebugCollision() ? "ON (Red Box Outlines)" : "OFF"));
        }

        if (playerController.consumePressed(KeyCode.G)) {
            pickUpNearbyItem();
        }

        if (playerController.consumePressed(KeyCode.H)) {
            hudVisible = !hudVisible;
            showNotification("Status HUD: " + (hudVisible ? "SHOWN" : "HIDDEN (Press H to show)"));
        }

        if (playerController.consumePressed(KeyCode.E)) {
            // 1. Try door interaction first if standing near any door
            if (tryInteractWithDoor()) {
                return;
            }
            // 2. Try interact with examination desk / quiz station
            if (educationManager.tryInteractAt(player.getCenterX(), player.getCenterY())) {
                return;
            }
        }

        for (int slot = 0; slot < 10; slot++) {
            if (playerController.consumePressed(keyForSlot(slot))) {
                if (educationManager.isPanelVisible()) {
                    educationManager.submitAnswer(slot);
                } else {
                    useInventoryItem(slot);
                }
            }
        }
    }

    private void useInventoryItem(int slot) {
        var opt = inventory.getItemAtSlot(slot);
        if (opt.isEmpty()) {
            showNotification("No item in slot " + (slot == 9 ? 0 : slot + 1));
            return;
        }
        Item item = opt.get();
        String id = item.getId().toLowerCase();

        switch (id) {
            case "battery" -> {
                if (flashlightBattery >= 99.0) {
                    showNotification("Flashlight battery is already 100% full! 🔋");
                } else {
                    inventory.useSlot(slot);
                    flashlightBattery = 100.0;
                    flashlightOn = true;
                    playSound("door");
                    showNotification("Battery inserted! Flashlight recharged to 100% 🔋⚡");
                    saveNow();
                }
            }
            case "flashlight" -> {
                if (flashlightBattery <= 0.0) {
                    showNotification("Flashlight battery is depleted (0%)! Use a Battery or Toolbox to recharge.");
                } else {
                    flashlightOn = !flashlightOn;
                    AudioManager.getInstance().playFlashlight();
                    showNotification("Flashlight: " + (flashlightOn ? "ON [Battery: " + (int) flashlightBattery + "%] 🔦" : "OFF 🔦"));
                }
            }
            case "night_vision" -> {
                nightVisionActive = !nightVisionActive;
                playSound("door");
                if (nightVisionActive) {
                    showNotification("Night Vision Goggles: ACTIVE [Thermal Ghost Tracking ON] 🥽🟢");
                } else {
                    showNotification("Night Vision Goggles: DEACTIVATED 🥽");
                }
            }
            case "lighter" -> {
                lighterOn = !lighterOn;
                playSound("door");
                if (lighterOn) {
                    showNotification("Brass Lighter flickered to life! Warm 360° firelight glow ON (+140px) 🔥");
                } else {
                    showNotification("Brass Lighter extinguished. 🔥");
                }
            }
            case "first_aid_kit" -> {
                if (player.getHearts() >= 5) {
                    showNotification("Health is already full (5/5 Hearts ❤️)!");
                } else {
                    inventory.useSlot(slot);
                    player.heal(2);
                    playSound("door");
                    showNotification("Used First Aid Kit! Healed +2 Hearts ❤️❤️ (Health: " + player.getHearts() + "/5)");
                    saveNow();
                }
            }
            case "medicine_bottle", "medicine" -> {
                inventory.useSlot(slot);
                player.heal(1);
                speedBoostTimer = 10.0;
                player.setSpeedMultiplier(1.45);
                playSound("door");
                showNotification("Drank Ancient Herbal Medicine! Healed 1 Heart ❤️ & gained +45% Speed for 10s! 🏃💨");
                saveNow();
            }
            case "holy_charm" -> {
                boolean repelledAny = false;
                for (com.khmerspirit.entities.Ghost ghost : educationManager.getGhosts()) {
                    double dist = Math.hypot(ghost.getX() - player.getCenterX(), ghost.getY() - player.getCenterY());
                    if (dist <= 500.0) {
                        ghost.scareAway(player.getCenterX(), player.getCenterY(), 8.0);
                        repelledAny = true;
                    }
                }
                inventory.useSlot(slot);
                playSound("puzzle_complete");
                if (repelledAny) {
                    showNotification("Holy Charm flared with blinding solar brilliance! Ghosts shrieked and fled! 🧿⚡");
                } else {
                    showNotification("Holy Charm radiated divine solar energy... No spirits nearby to repel.");
                }
                saveNow();
            }
            case "bronze_bell" -> {
                if (bellCooldownTimer > 0.0) {
                    showNotification("Bronze Bell is cooling down (" + (int) Math.ceil(bellCooldownTimer) + "s remaining) 🔔⏳");
                    return;
                }
                boolean stunnedAny = false;
                for (com.khmerspirit.entities.Ghost ghost : educationManager.getGhosts()) {
                    double dist = Math.hypot(ghost.getX() - player.getCenterX(), ghost.getY() - player.getCenterY());
                    if (dist <= 450.0) {
                        ghost.stun(5.0);
                        stunnedAny = true;
                    }
                }
                bellCooldownTimer = 15.0;
                AudioManager.getInstance().playBellRing();
                if (stunnedAny) {
                    showNotification("Sacred Bronze Bell chimed! Nearby spirits are STUNNED for 5 seconds! 🔔✨");
                } else {
                    showNotification("Sacred Bronze Bell echoed solemnly... No spirits nearby to stun.");
                }
            }
            case "ancient_tome" -> {
                if (tomeCooldownTimer > 0.0) {
                    showNotification("Ancient Tome scripture cooldown (" + (int) Math.ceil(tomeCooldownTimer) + "s remaining) 📖⏳");
                    return;
                }
                player.setInvulnerableSeconds(12.0);
                tomeCooldownTimer = 25.0;
                playSound("puzzle_complete");
                showNotification("Chanted Sacred Pali Scripture! Divine Aegis shields you from all ghost harm for 12s! 📖🛡️✨");
            }
            case "sheet_music" -> {
                if (musicCooldownTimer > 0.0) {
                    showNotification("Sheet Music melody cooldown (" + (int) Math.ceil(musicCooldownTimer) + "s remaining) 🎵⏳");
                    return;
                }
                for (com.khmerspirit.entities.Ghost ghost : educationManager.getGhosts()) {
                    ghost.pacify(12.0);
                }
                musicCooldownTimer = 20.0;
                playSound("puzzle_complete");
                showNotification("Played classical Angkorian melody! Haunted spirits calmed into peaceful wandering for 12s! 🎵🕊️");
            }
            case "electric_fuse" -> {
                inventory.useSlot(slot);
                hallwayPowerTimer = 20.0;
                playSound("door");
                showNotification("Loaded Electric Fuse into circuit breaker! Emergency generator running — full illumination for 20s! 💡⚡");
                saveNow();
            }
            case "toolbox" -> {
                inventory.useSlot(slot);
                flashlightBattery = Math.min(100.0, flashlightBattery + 50.0);
                batteryEfficiencyTimer = 60.0;
                playSound("door");
                showNotification("Flashlight tuned with Toolbox! Battery recharged +50% & drain rate halved for 60s! 🧰🔋");
                saveNow();
            }
            case "crowbar" -> {
                if (tryUnlockAdjacentDoorWithTool("Crowbar")) {
                    saveNow();
                } else {
                    showNotification("Crowbar ready: Stand facing any locked door and press slot hotkey to pry it open! 🪓");
                }
            }
            case "acid_bottle" -> {
                double px = player.getCenterX();
                double py = player.getCenterY();

                // 1. Check if near the Music & Art Room door:
                Door artDoor = null;
                for (Door d : tileMap.getDoors()) {
                    if (d.isNear(px, py, 110.0) && isMusicArtDoor(d) && d.isLocked()) {
                        artDoor = d;
                        break;
                    }
                }

                if (artDoor != null) {
                    artDoor.setLocked(false);
                    tileMap.setDoorOpen(artDoor, true);
                    unlockedDoorIds.add(artDoor.getId());
                    inventory.useSlot(slot);
                    playSound("door");
                    playSound("puzzle_complete");
                    showNotification("You splashed the Acid Bottle on the padlock! The acid melted the lock of the Music & Art Room! 🧪🚪");
                    saveNow();
                    break;
                }

                // Check if standing near any OTHER locked door:
                Door otherLockedDoor = null;
                for (Door d : tileMap.getDoors()) {
                    if (d.isNear(px, py, 110.0) && !isMusicArtDoor(d) && d.isLocked()) {
                        otherLockedDoor = d;
                        break;
                    }
                }

                if (otherLockedDoor != null) {
                    showNotification("The Acid Bottle can only dissolve the lock of the Music & Art Room! It cannot open other rooms. 🧪");
                    break;
                }

                // 2. Kill one ghost:
                com.khmerspirit.entities.Ghost closestGhost = null;
                double closestDist = 320.0;
                for (com.khmerspirit.entities.Ghost g : educationManager.getGhosts()) {
                    if (g.isDisappeared()) continue;
                    double d = Math.hypot(g.getX() - player.getCenterX(), g.getY() - player.getCenterY());
                    if (d <= closestDist) {
                        closestDist = d;
                        closestGhost = g;
                    }
                }
                if (closestGhost != null) {
                    closestGhost.banish();
                    educationManager.getGhosts().remove(closestGhost);
                    inventory.useSlot(slot);
                    playSound("puzzle_complete");
                    showNotification("You threw the Acid Bottle! The corrosive acid dissolved and killed the ghost! 🧪💀👻");
                    saveNow();
                } else {
                    showNotification("Acid Bottle ready: Use at the Music & Art Room door to open it, or throw near a ghost to kill it! 🧪");
                }
            }
            case "key", "master_key" -> {
                if (tryInteractWithDoor()) {
                    showNotification("Unlocked door with " + item.getDisplayName() + "! 🗝️");
                } else {
                    showNotification(item.getDisplayName() + ": Walk up to any locked door and press [E] to unlock!");
                }
            }
            case "notebook" -> {
                int rem = educationManager.getRemainingQuestionsCount(currentMapId);
                if (rem > 0) {
                    showNotification("Notebook Clue: " + rem + " questions remaining at the study altar in this room! 📓🔍");
                } else {
                    showNotification("Notebook Clue: Room purified! Head to the next chamber or unlock the Grand Exit! 📓✨");
                }
            }
            case "map" -> {
                minimapUI.setFullMapOpen(!minimapUI.isFullMapOpen());
                showNotification(minimapUI.isFullMapOpen() ? "Architectural Blueprint OPENED [Press M or slot to close] 🗺️" : "Architectural Blueprint closed.");
            }
            default -> {
                String message = inventory.useSlot(slot);
                showNotification(message);
                saveNow();
            }
        }
    }

    private boolean isMusicArtDoor(Door door) {
        if (door == null) return false;
        String from = door.getFromRoomId() == null ? "" : door.getFromRoomId().toLowerCase();
        String to = door.getToRoomId() == null ? "" : door.getToRoomId().toLowerCase();
        String id = door.getId() == null ? "" : door.getId().toLowerCase();
        return from.equals("computer") || from.contains("music") || from.contains("art")
                || to.equals("computer") || to.contains("music") || to.contains("art")
                || id.contains("music") || id.contains("art");
    }

    private boolean tryUnlockAdjacentDoorWithTool(String toolName) {
        double px = player.getCenterX();
        double py = player.getCenterY();
        for (Door door : tileMap.getDoors()) {
            if (door.isNear(px, py, 100.0) && door.isLocked()) {
                door.setLocked(false);
                tileMap.setDoorOpen(door, true);
                playSound("door");
                showNotification("You forced the locked door open using " + toolName + "!");
                return true;
            }
        }
        return false;
    }

    private boolean tryInteractWithDoor() {
        double px = player.getCenterX();
        double py = player.getCenterY();

        // In Main Hall: interact with room doors
        if (currentMapId.equalsIgnoreCase("hall") || currentMapId.equalsIgnoreCase("school") || currentMapId.equalsIgnoreCase("main_hall")) {
            for (Door door : tileMap.getDoors()) {
                if (door.isNear(px, py, 75.0)) {
                    String target = door.getFromRoomId();
                    String name = getEnglishRoomName(target);

                    if (!door.isOpen()) {
                        // Closed door: check if locked
                        if ("exit".equalsIgnoreCase(target)) {
                            if (inventory.hasItem("master_key")) {
                                door.setLocked(false);
                                tileMap.setDoorOpen(door, true);
                                unlockedDoorIds.add(door.getId());
                                playSound("door");
                                showNotification("VICTORY! You unlocked the Grand School Exit Gate with the Master Key! [Press E to Escape]");
                                saveNow();
                                return true;
                            } else {
                                playSound("ghost");
                                showNotification("The Grand Exit Gate is chained shut! You need the Master Key from the Principal's Office.");
                                return true;
                            }
                        }

                        if (door.isLocked()) {
                            String req = door.getRequiredKeyId();
                            boolean isMusicArt = isMusicArtDoor(door);
                            boolean hasKey = (req == null || req.isBlank())
                                    || inventory.hasItem(req)
                                    || (!"exit".equalsIgnoreCase(target) && inventory.hasItem("master_key"))
                                    || (isMusicArt && inventory.hasItem("acid_bottle"));
                            if (hasKey) {
                                if (isMusicArt && !inventory.hasItem(req) && !inventory.hasItem("master_key") && inventory.hasItem("acid_bottle")) {
                                    inventory.removeOne("acid_bottle");
                                    showNotification("You poured Acid on the padlock! The acid dissolved the lock of the Music & Art Room! 🧪🚪");
                                } else {
                                    showNotification("Unlocked and opened the door to " + name + "! [Press E to Enter]");
                                }
                                door.setLocked(false);
                                tileMap.setDoorOpen(door, true);
                                unlockedDoorIds.add(door.getId());
                                playSound("door");
                                playSound("puzzle_complete");
                                saveNow();
                                return true;
                            } else {
                                playSound("ghost");
                                String keyNeeded = getKeyDisplayName(req);
                                if (isMusicArt) {
                                    showNotification("The door to " + name + " is locked! You need the " + keyNeeded + " or an Acid Bottle to melt it.");
                                } else {
                                    showNotification("The door to " + name + " is locked! You need the " + keyNeeded + ".");
                                }
                                return true;
                            }
                        } else {
                            tileMap.setDoorOpen(door, true);
                            playSound("door");
                            showNotification("Opened the door to " + name + "! [Press E to Enter | C to Close]");
                            saveNow();
                            return true;
                        }
                    } else {
                        // Open door: Pressing E enters the room!
                        if ("exit".equalsIgnoreCase(target)) {
                            playSound("door");
                            showNotification("VICTORY! You escaped the Haunted School alive with all ancient relics!");
                            return true;
                        }

                        if (!target.equalsIgnoreCase("hall")) {
                            transitionToRoom(target, 1138.0, 1025.0);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        // Inside a detailed room: door interaction
        for (Door door : tileMap.getDoors()) {
            if (door.isNear(px, py, 100.0)) {
                if (!door.isOpen()) {
                    if (door.isLocked()) {
                        String reqKey = door.getRequiredKeyId();
                        boolean hasKey = (reqKey == null || reqKey.isBlank())
                                || inventory.hasItem(reqKey)
                                || inventory.hasItem("master_key");

                        if (hasKey) {
                            door.setLocked(false);
                            tileMap.setDoorOpen(door, true);
                            unlockedDoorIds.add(door.getId());
                            playSound("door");
                            showNotification("Unlocked and opened the door! [Press E to Exit to Main Hall | C to Close]");
                            saveNow();
                            return true;
                        } else {
                            playSound("ghost");
                            showNotification("The door is locked! Complete the quiz station to earn the Key.");
                            return true;
                        }
                    } else {
                        tileMap.setDoorOpen(door, true);
                        playSound("door");
                        showNotification("Opened the door! [Press E to Exit to Main Hall | C to Close]");
                        saveNow();
                        return true;
                    }
                } else {
                    // When door is open: Pressing E exits smoothly back to Main Hall!
                    exitToMainHallFromCurrentRoom();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCloseNearbyDoor() {
        double px = player.getCenterX();
        double py = player.getCenterY();
        for (Door door : tileMap.getDoors()) {
            if (door.isOpen() && door.isNear(px, py, 75.0)) {
                tileMap.setDoorOpen(door, false);
                playSound("door");
                String name = getEnglishRoomName(door.getFromRoomId());
                showNotification("You closed the door to " + name + ".");
                saveNow();
                return true;
            }
        }
        return false;
    }

    public String getEnglishRoomName(String roomId) {
        if (roomId == null) return "Unknown Room";
        return switch (roomId.toLowerCase()) {
            case "classrooma", "classroom" -> "Classroom A";
            case "laboratory", "science_lab" -> "Science Lab";
            case "classroomb", "teachers_lounge" -> "Teachers' Lounge";
            case "computer", "music_art_room" -> "Music & Art Room";
            case "dormitory", "infirmary" -> "School Infirmary";
            case "basement", "storage_room" -> "Storage Vault";
            case "teacher", "principal_office" -> "Principal's Office";
            case "entrance", "restroom" -> "Restroom";
            case "library" -> "Lore Library";
            case "exit" -> "Grand Exit Gate";
            case "hall", "main_hall", "school" -> "Main Hall";
            default -> roomId;
        };
    }

    public String getKeyDisplayName(String keyId) {
        if (keyId == null || keyId.isBlank()) return "Key";
        return switch (keyId.toLowerCase()) {
            case "key_classroomb" -> "Teachers' Lounge Key";
            case "key_computer" -> "Music & Art Key";
            case "key_entrance" -> "Restroom Key";
            case "key_dormitory" -> "Infirmary Key";
            case "key_basement" -> "Storage Vault Key";
            case "key_laboratory" -> "Science Lab Key";
            case "key_library" -> "Lore Library Key";
            case "key_principal" -> "Headmaster's Key (Finish all 8 rooms first)";
            case "master_key" -> "Golden Master Key";
            case "key_classrooma" -> "Classroom Key";
            default -> "Room Key";
        };
    }

    private void renderDoorLabels(GraphicsContext g, Camera cam) {
        boolean isHall = currentMapId.equalsIgnoreCase("hall") || currentMapId.equalsIgnoreCase("school") || currentMapId.equalsIgnoreCase("main_hall");

        double px = player.getCenterX();
        double py = player.getCenterY();

        for (Door door : tileMap.getDoors()) {
            Rectangle2D bounds = door.getBounds();
            if (bounds == null) continue;

            double doorCenterX = bounds.getMinX() + bounds.getWidth() / 2.0;
            double doorTopY = bounds.getMinY();
            double sx = doorCenterX - cam.getX();
            double sy = doorTopY - cam.getY();

            // English Room Title / Exit Title
            String englishName;
            if (isHall) {
                String target = door.getFromRoomId();
                englishName = getEnglishRoomName(target).toUpperCase();
            } else {
                englishName = "EXIT TO MAIN HALL";
            }

            double plaqueW = Math.max(130.0, englishName.length() * 8.0 + 28.0);
            double plaqueH = 24.0;
            double plaqueX = sx - plaqueW / 2.0;
            double plaqueY = isHall ? (sy - 28.0) : (sy - 32.0);

            // Stone plaque background
            g.setFill(Color.rgb(15, 12, 16, 0.90));
            g.fillRoundRect(plaqueX, plaqueY, plaqueW, plaqueH, 6, 6);
            g.setStroke(Color.rgb(212, 168, 67, 0.92));
            g.setLineWidth(1.5);
            g.strokeRoundRect(plaqueX, plaqueY, plaqueW, plaqueH, 6, 6);

            // Suspension chain rivets
            g.setFill(Color.rgb(255, 215, 0, 0.85));
            g.fillOval(plaqueX + 5, plaqueY + 5, 4, 4);
            g.fillOval(plaqueX + plaqueW - 9, plaqueY + 5, 4, 4);

            // Door text
            g.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 11));
            g.setFill(Color.web("#ffeaa7"));
            g.fillText(englishName, plaqueX + 14, plaqueY + 16);

            // Proximity interaction hint banner (stay safely behind the door)
            if (door.isNear(px, py, 80.0)) {
                String prompt;
                Color statusColor;
                if (!door.isOpen()) {
                    if (door.isLocked()) {
                        String reqKey = door.getRequiredKeyId();
                        boolean hasKey = reqKey != null && (inventory.hasItem(reqKey) || (!"exit".equalsIgnoreCase(door.getFromRoomId()) && inventory.hasItem("master_key")));
                        String keyName = getKeyDisplayName(reqKey);
                        if (hasKey) {
                            prompt = "[E] Unlock (Use " + keyName + ")";
                            statusColor = Color.web("#ffeaa7");
                        } else {
                            prompt = "[E] Locked (Requires " + keyName + ")";
                            statusColor = Color.web("#ff7675");
                        }
                    } else {
                        prompt = "[E] Open Door";
                        statusColor = Color.web("#ffeaa7");
                    }
                } else {
                    prompt = isHall ? "[E] Enter Room   |   [C] Close Door" : "[E] Exit to Main Hall   |   [C] Close Door";
                    statusColor = Color.web("#55efc4");
                }

                double promptW = prompt.length() * 7.2 + 24.0;
                double prX = sx - promptW / 2.0;
                double prY = isHall ? (sy + bounds.getHeight() + 8.0) : (sy - 62.0);

                g.setFill(Color.rgb(10, 8, 12, 0.92));
                g.fillRoundRect(prX, prY, promptW, 22.0, 5, 5);
                g.setStroke(statusColor);
                g.setLineWidth(1.2);
                g.strokeRoundRect(prX, prY, promptW, 22.0, 5, 5);

                g.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 10));
                g.setFill(statusColor);
                g.fillText(prompt, prX + 12, prY + 15);
            }
        }
    }

    public void transitionToRoom(String targetRoomId, double targetPlayerX, double targetPlayerY) {
        // Save current room remaining pickups
        roomItemPickups.put(currentMapId.toLowerCase(), new ArrayList<>(itemPickups));

        // Load new map
        TileMap newMap = mapLoader.loadMapForId(targetRoomId);
        this.tileMap = newMap;
        this.collisionMap = new CollisionMap(newMap);
        this.camera = new Camera(canvas.getWidth(), canvas.getHeight(), newMap.getPixelWidth(), newMap.getPixelHeight());
        this.player.setX(targetPlayerX);
        this.player.setY(targetPlayerY);
        this.currentMapId = targetRoomId;

        this.currentRoomName = newMap.getRooms().isEmpty() ? "Central Hallway" : newMap.getRooms().getFirst().getDisplayName();

        // Update education manager with the new map and room change
        if (educationManager != null) {
            educationManager.setTileMap(newMap);
            educationManager.onPlayerRoomChanged(currentRoomName, targetPlayerX, targetPlayerY);
        }

        // Switch to room-specific music (e.g., music&art.mp3) or back to ambient (Tili Tili Bom)
        AudioManager.getInstance().updateRoomMusic(targetRoomId);

        // Restore or initialize pickups for target room
        if (roomItemPickups.containsKey(targetRoomId.toLowerCase())) {
            this.itemPickups = roomItemPickups.get(targetRoomId.toLowerCase());
        } else {
            this.itemPickups = createItemPickupsForRoom(targetRoomId);
            roomItemPickups.put(targetRoomId.toLowerCase(), this.itemPickups);
        }

        // Restore door unlock states (for completed rooms or doors previously unlocked)
        for (Door door : this.tileMap.getDoors()) {
            boolean isCompleted = educationManager != null && (
                    educationManager.isRoomCompleted(door.getFromRoomId()) ||
                    educationManager.isRoomCompleted(door.getToRoomId()) ||
                    educationManager.isRoomCompleted(targetRoomId)
            );
            if (unlockedDoorIds.contains(door.getId()) || isCompleted) {
                door.setLocked(false);
                this.tileMap.setDoorOpen(door, true);
            }
        }

        camera.follow(player.getCenterX(), player.getCenterY());
        playSound("door");
        showNotification("Entered: " + currentRoomName);
        saveNow();
    }

    public void exitToMainHallFromCurrentRoom() {
        playSound("door");
        // Return smoothly to the Main Hall right in front of the corresponding room doorway
        switch (currentMapId.toLowerCase()) {
            case "teacher", "principal_office" -> transitionToRoom("hall", 390.0, 223.0);
            case "laboratory", "science_lab" -> transitionToRoom("hall", 390.0, 503.0);
            case "classrooma", "classroom" -> transitionToRoom("hall", 390.0, 783.0);
            case "computer", "music_art_room" -> transitionToRoom("hall", 390.0, 1063.0);
            case "dormitory", "infirmary" -> transitionToRoom("hall", 1900.0, 223.0);
            case "basement", "storage_room" -> transitionToRoom("hall", 1900.0, 503.0);
            case "classroomb", "teachers_lounge" -> transitionToRoom("hall", 1900.0, 783.0);
            case "entrance", "restroom" -> transitionToRoom("hall", 1900.0, 1063.0);
            case "library" -> transitionToRoom("hall", 1151.0, 375.0);
            default -> transitionToRoom("hall", 1152.0, 648.0);
        }
    }

    private void checkDoorTransitions() {
        if (currentMapId.equalsIgnoreCase("hall") || currentMapId.equalsIgnoreCase("school") || currentMapId.equalsIgnoreCase("main_hall")) {
            return;
        }

        // Inside a detailed room: check if player steps into the open doorway threshold
        for (Door door : tileMap.getDoors()) {
            if (door.isOpen() && player.getCenterY() >= 1065.0 && Math.abs(player.getCenterX() - 1136.0) <= 65.0) {
                exitToMainHallFromCurrentRoom();
                return;
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
                String itemId = pickup.getItem().getId().toLowerCase();
                if (itemId.contains("key")) {
                    AudioManager.getInstance().playGetKey();
                } else {
                    AudioManager.getInstance().playItemPickup();
                }
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

    public static boolean isKeyItem(String itemId) {
        if (itemId == null) return false;
        String id = itemId.toLowerCase().trim();
        return id.contains("key");
    }

    private List<ItemPickup> createItemPickupsForRoom(String roomId) {
        List<ItemPickup> pickups = new ArrayList<>();
        boolean isCompleted = educationManager != null && educationManager.isRoomCompleted(roomId);

        // 1. Load active items placed on live map by Admin
        try {
            List<MapItemModel> placed = mapItemFileService.loadMapItemsForRoom(roomId);
            if (placed != null && !placed.isEmpty()) {
                for (MapItemModel item : placed) {
                    if (item.isActive()) {
                        boolean isKey = isKeyItem(item.getItemId());
                        // Key items only appear if the room's quiz has already been completed!
                        if (isKey && !isCompleted) {
                            continue;
                        }
                        if (isKey && inventory.hasItem(item.getItemId())) {
                            continue;
                        }
                        addPickup(pickups, item.getItemId(), item.getTileX(), item.getTileY());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Game] Error loading admin map items for room " + roomId + ": " + e.getMessage());
        }

        if (!pickups.isEmpty()) {
            return pickups;
        }

        // 2. Fallback default item placements
        switch (roomId.toLowerCase()) {
            case "classrooma", "classroom" -> {
                addPickup(pickups, "flashlight", 24, 21);
                addPickup(pickups, "notebook", 23.5, 14.5);
            }
            case "library" -> {
                addPickup(pickups, "ancient_tome", 24, 10.0);
                addPickup(pickups, "battery", 34, 12.0);
            }
            case "laboratory", "science_lab" -> {
                addPickup(pickups, "acid_bottle", 24, 10.0);
                addPickup(pickups, "lighter", 14, 14.0);
            }
            case "teacher", "principal_office" -> {
                if (isCompleted && !inventory.hasItem("master_key")) {
                    addPickup(pickups, "master_key", 24, 10.0);
                }
                addPickup(pickups, "map", 34, 14.0);
            }
            case "dormitory", "infirmary" -> {
                addPickup(pickups, "first_aid_kit", 24, 10.0);
                addPickup(pickups, "medicine_bottle", 14, 14.0);
            }
            case "classroomb", "teachers_lounge" -> {
                addPickup(pickups, "notebook", 24, 10.0);
                addPickup(pickups, "battery", 34, 12.0);
            }
            case "computer", "music_art_room" -> {
                addPickup(pickups, "sheet_music", 24, 10.0);
                addPickup(pickups, "holy_charm", 14, 14.0);
                addPickup(pickups, "bronze_bell", 34, 14.0);
            }
            case "entrance", "restroom" -> {
                addPickup(pickups, "battery", 14, 14.0);
            }
            case "basement", "storage_room" -> {
                addPickup(pickups, "crowbar", 24, 10.0);
                addPickup(pickups, "electric_fuse", 14, 14.0);
                addPickup(pickups, "toolbox", 34, 14.0);
            }
            case "hall", "hallway", "school" -> {
                addPickup(pickups, "flashlight", 42, 22);
                addPickup(pickups, "battery", 20, 22);
                addPickup(pickups, "battery", 65, 22);
                addPickup(pickups, "notebook", 44, 35);
            }
        }
        return pickups;
    }

    public void spawnRoomKeyOnMap(String roomId, String defaultKeyId) {
        double spawnTileX = -1;
        double spawnTileY = -1;
        String keyToSpawn = defaultKeyId;

        // 1. Check if Admin placed a key in Map Item Spawner for this room
        try {
            List<MapItemModel> placed = mapItemFileService.loadMapItemsForRoom(roomId);
            if (placed != null) {
                for (MapItemModel m : placed) {
                    if (m.isActive() && isKeyItem(m.getItemId())) {
                        spawnTileX = m.getTileX();
                        spawnTileY = m.getTileY();
                        keyToSpawn = m.getItemId();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Game] Error checking admin key position: " + e.getMessage());
        }

        // 2. Fallback coordinates if no key was placed in Map Item Spawner
        if (spawnTileX < 0 || spawnTileY < 0) {
            spawnTileX = 24.0;
            spawnTileY = 14.5;
        }

        final String finalKey = keyToSpawn;
        boolean inInventory = inventory.hasItem(finalKey);
        boolean onMap = itemPickups.stream()
                .anyMatch(p -> p.getItem().getId().equalsIgnoreCase(finalKey));

        if (!inInventory && !onMap) {
            addPickup(itemPickups, finalKey, spawnTileX, spawnTileY);
            roomItemPickups.put(roomId.toLowerCase(), new ArrayList<>(itemPickups));
            saveNow();
        }
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

    public void saveNow() {
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
}
