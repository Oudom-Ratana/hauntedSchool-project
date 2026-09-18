package com.khmerspirit.education;

import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.RoomFileService;
import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.config.Constants;
import com.khmerspirit.core.Game;
import com.khmerspirit.entities.Ghost;
import com.khmerspirit.items.ItemRegistry;
import com.khmerspirit.map.Door;
import com.khmerspirit.map.Room;
import com.khmerspirit.map.Tile;
import com.khmerspirit.map.TileMap;
import com.khmerspirit.player.Camera;
import com.khmerspirit.player.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EducationManager {

    public static class QuizStation {
        private final String roomId;
        private final String displayName;
        private final double x;
        private final double y;
        private final double radius;

        public QuizStation(String roomId, String displayName, double x, double y, double radius) {
            this.roomId = roomId;
            this.displayName = displayName;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public String getRoomId() { return roomId; }
        public String getDisplayName() { return displayName; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getRadius() { return radius; }
    }

    private TileMap tileMap;
    private final Game game;
    private final QuestionLoader loader = new QuestionLoader();
    private final RoomFileService roomFileService = new RoomFileService();
    private final Random rng = new Random();

    private final Map<String, RoomTask> tasks = new HashMap<>();
    private final Map<String, QuizStation> stations = new HashMap<>();
    private RoomTask activeTask = null;
    private String activeRoomId = null;
    private boolean panelVisible = false;

    private final List<Ghost> ghosts = new ArrayList<>();
    private final List<String> completedRooms = new ArrayList<>();
    private double ambientGhostTimer = 0.0;
    private double lifeTime = 0.0;
    private Image questionPanelImage = null;
    private Image quizOptionBarImage = null;
    private Image quizProgressBarImage = null;
    private Image storyGuideImage = null;
    private boolean storyGuideVisible = false;
    private String storyGuideTitle = "";
    private String storyGuideNarrative = "";
    private String storyGuideNextStep = "";

    public void setTileMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public EducationManager(TileMap tileMap, Object assetManager, Game game) {
        this.tileMap = tileMap;
        this.game = game;
        initStations();
        loadUiImages();
        spawnInitialGhosts();
    }

    private void initStations() {
        // Designated examination desks/stations per room
        addStation("classrooma", "Classroom Study Altar", 1104, 768, 85.0);
        addStation("classroom", "Classroom Study Altar", 1104, 768, 85.0);
        addStation("teachers_lounge", "Faculty Desk Altar", 960, 620, 85.0);
        addStation("classroomb", "Faculty Desk Altar", 960, 620, 85.0);
        addStation("library", "Ancient Lore Archive Table", 960, 620, 85.0);
        addStation("science_lab", "Alchemical Experiment Bench", 960, 620, 85.0);
        addStation("laboratory", "Alchemical Experiment Bench", 960, 620, 85.0);
        addStation("music_art_room", "Sacred Easel & Piano Shrine", 960, 620, 85.0);
        addStation("computer", "Sacred Easel & Piano Shrine", 960, 620, 85.0);
        addStation("principal_office", "Principal's Relic Desk", 960, 560, 85.0);
        addStation("teacher", "Principal's Relic Desk", 960, 560, 85.0);
        addStation("infirmary", "Medical Treatment Station", 960, 620, 85.0);
        addStation("dormitory", "Medical Treatment Station", 960, 620, 85.0);
        addStation("storage_room", "Ancient Sealed Relic Chest", 960, 620, 85.0);
        addStation("basement", "Ancient Sealed Relic Chest", 960, 620, 85.0);
        addStation("restroom", "Haunted Mirror Altar", 960, 600, 85.0);
        addStation("entrance", "Haunted Mirror Altar", 960, 600, 85.0);
        addStation("hall", "Grand Hallway Memorial Podium", 1152, 648, 85.0);
        addStation("main_hall", "Grand Hallway Memorial Podium", 1152, 648, 85.0);
    }

    private void addStation(String roomId, String name, double x, double y, double r) {
        stations.put(roomId.toLowerCase(), new QuizStation(roomId.toLowerCase(), name, x, y, r));
    }

    private void loadUiImages() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/ui/question_panel.png");
            if (stream != null) {
                this.questionPanelImage = new Image(stream);
            } else {
                File f = new File("images/ui/question_panel.png");
                if (f.exists()) this.questionPanelImage = new Image(f.toURI().toString());
            }
        } catch (Exception ignored) {}

        try {
            InputStream stream = getClass().getResourceAsStream("/images/ui/quiz_option_bar.png");
            if (stream != null) {
                this.quizOptionBarImage = new Image(stream);
            } else {
                File f = new File("images/ui/quiz_option_bar.png");
                if (f.exists()) this.quizOptionBarImage = new Image(f.toURI().toString());
            }
        } catch (Exception ignored) {}

        try {
            InputStream stream = getClass().getResourceAsStream("/images/ui/quiz_progress_bar.png");
            if (stream != null) {
                this.quizProgressBarImage = new Image(stream);
            } else {
                File f = new File("images/ui/quiz_progress_bar.png");
                if (f.exists()) this.quizProgressBarImage = new Image(f.toURI().toString());
            }
        } catch (Exception ignored) {}

        try {
            InputStream stream = getClass().getResourceAsStream("/images/ui/story_guide_panel.png");
            if (stream != null) {
                this.storyGuideImage = new Image(stream);
            } else {
                File f = new File("images/ui/story_guide_panel.png");
                if (f.exists()) this.storyGuideImage = new Image(f.toURI().toString());
            }
        } catch (Exception ignored) {}
    }

    public QuizStation getCurrentRoomStation(double px, double py) {
        if (tileMap != null) {
            Room r = tileMap.findRoomAt(px, py).orElse(null);
            if (r != null) {
                String rid = r.getId().toLowerCase();
                if (stations.containsKey(rid)) {
                    return stations.get(rid);
                }
                double cx = (r.getColumn() + r.getWidth() / 2.0) * Constants.TILE_SIZE;
                double cy = (r.getRow() + r.getHeight() / 2.0) * Constants.TILE_SIZE;
                return new QuizStation(rid, r.getDisplayName() + " Station", cx, cy, 85.0);
            }
        }
        // Fallback check by physical proximity to all stations
        for (QuizStation s : stations.values()) {
            if (Math.hypot(px - s.getX(), py - s.getY()) <= s.getRadius() + 45.0) {
                return s;
            }
        }
        return null;
    }

    private void spawnInitialGhosts() {
        double hallX = 42 * Constants.TILE_SIZE;
        double hallY = 22 * Constants.TILE_SIZE;
        ghosts.add(new Ghost(hallX, hallY));
    }

    public void onPlayerRoomChanged(String roomDisplayName, double playerX, double playerY) {
        tileMap.getRooms().stream()
                .filter(r -> r.getDisplayName().equals(roomDisplayName))
                .findFirst()
                .ifPresent(r -> {
                    ensureRoomTask(r.getId());
                    if (!completedRooms.contains(r.getId()) && !r.getId().equals("entrance") && ghosts.size() < 3 && rng.nextDouble() < 0.40) {
                        spawnGhostNearPlayer();
                    }
                });
    }

    private void ensureRoomTask(String roomId) {
        if (!tasks.containsKey(roomId)) {
            List<Question> all = loader.loadQuestionsForRoom(roomId);
            RoomTask t = new RoomTask(roomId, all, rng);
            tasks.put(roomId, t);
        }
    }

    public void update(double deltaSeconds, Player player) {
        lifeTime += deltaSeconds;

        // Auto-hide question panel when player moves away from the examination station
        if (activeTask != null && panelVisible) {
            QuizStation station = getCurrentRoomStation(player.getCenterX(), player.getCenterY());
            if (station != null) {
                double dist = Math.hypot(player.getCenterX() - station.getX(), player.getCenterY() - station.getY());
                if (dist > station.getRadius() + 35.0) {
                    panelVisible = false;
                    game.showNotification("Stepped away from examination desk. Return here to resume questions.");
                }
            }
        }

        ambientGhostTimer += deltaSeconds;
        if (ambientGhostTimer >= 50.0) {
            ambientGhostTimer = 0.0;
            if (ghosts.size() < 3) {
                spawnAmbientGhost(player);
            }
        }

        List<Ghost> remove = new ArrayList<>();
        for (Ghost g : ghosts) {
            g.update(deltaSeconds, player, game);
            if (g.isDisappeared()) {
                remove.add(g);
            }
        }
        ghosts.removeAll(remove);
    }

    public boolean isRoomCompleted(String roomId) {
        return roomId != null && completedRooms.contains(roomId.toLowerCase());
    }

    public int getRemainingQuestionsCount(String roomId) {
        if (roomId == null || isRoomCompleted(roomId)) return 0;
        RoomTask task = tasks.get(roomId.toLowerCase());
        if (task != null) {
            return Math.max(0, task.getQuestions().size() - task.getCurrentIndex());
        }
        return 5;
    }

    public void render(GraphicsContext g, Camera camera, double canvasWidth, double canvasHeight) {
        // Fallback backward compatibility render
        renderGhosts(g, camera, 0, 0, 99999.0);
        renderQuestionPanel(g, canvasWidth, canvasHeight);
    }

    public void renderGhosts(GraphicsContext g, Camera camera, double playerX, double playerY, double lightRadius) {
        renderGhosts(g, camera, playerX, playerY, lightRadius, false);
    }

    public void renderGhosts(GraphicsContext g, Camera camera, double playerX, double playerY, double lightRadius, boolean nightVisionActive) {
        for (Ghost ghost : ghosts) {
            ghost.render(g, camera.getX(), camera.getY(), playerX, playerY, lightRadius, nightVisionActive);
        }
    }

    public void renderQuizStation(GraphicsContext g, Camera camera, double playerX, double playerY, double lightRadius) {
        QuizStation station = getCurrentRoomStation(playerX, playerY);
        if (station == null) return;

        double sx = station.getX() - camera.getX();
        double sy = station.getY() - camera.getY();
        double distToPlayer = Math.hypot(station.getX() - playerX, station.getY() - playerY);

        boolean completed = completedRooms.contains(station.getRoomId());

        // Floor Ritual Altar Circle
        double pulse = 0.5 + 0.5 * Math.sin(lifeTime * 2.8);
        double ringR = 48.0;

        if (completed) {
            // Serene emerald & gold sanctified halo
            g.setFill(Color.rgb(40, 160, 90, 0.22));
            g.fillOval(sx - ringR, sy - ringR * 0.55, ringR * 2, ringR * 1.1);
            g.setStroke(Color.rgb(90, 220, 130, 0.70));
            g.setLineWidth(2.0);
            g.strokeOval(sx - ringR, sy - ringR * 0.55, ringR * 2, ringR * 1.1);
        } else {
            // Golden ancient glowing Khmer lotus seal
            g.setFill(Color.rgb(200, 140, 30, 0.18 + 0.10 * pulse));
            g.fillOval(sx - ringR, sy - ringR * 0.55, ringR * 2, ringR * 1.1);
            g.setStroke(Color.rgb(255, 200, 60, 0.75 + 0.25 * pulse));
            g.setLineWidth(2.0);
            g.strokeOval(sx - ringR, sy - ringR * 0.55, ringR * 2, ringR * 1.1);

            double innerR = 28.0;
            g.setStroke(Color.rgb(240, 175, 50, 0.55));
            g.setLineWidth(1.5);
            g.strokeOval(sx - innerR, sy - innerR * 0.55, innerR * 2, innerR * 1.1);
        }

        // Desk / Shrine Ethereal Relic Icon
        double hoverY = Math.sin(lifeTime * 3.2) * 5.0;
        double iconY = sy - 22.0 + hoverY;

        g.setFill(Color.rgb(0, 0, 0, 0.35));
        g.fillOval(sx - 16, sy - 6, 32, 12);

        g.setFill(completed ? Color.rgb(80, 220, 120, 0.9) : Color.rgb(255, 215, 90, 0.95));
        g.fillRoundRect(sx - 14, iconY - 14, 28, 24, 6, 6);
        g.setStroke(Color.web("#ffffff"));
        g.setLineWidth(1.5);
        g.strokeRoundRect(sx - 14, iconY - 14, 28, 24, 6, 6);

        g.setStroke(Color.rgb(50, 35, 15, 0.85));
        g.strokeLine(sx - 9, iconY - 8, sx + 9, iconY - 8);
        g.strokeLine(sx - 9, iconY - 3, sx + 9, iconY - 3);
        g.strokeLine(sx - 9, iconY + 2, sx + 5, iconY + 2);

        // Action Prompts
        if (distToPlayer <= station.getRadius()) {
            if (completed) {
                drawStationPrompt(g, sx, iconY - 26, "✦ Sanctified Desk (Room Key Claimed) ✦", Color.web("#78ffaa"));
            } else if (activeTask != null && !panelVisible) {
                drawStationPrompt(g, sx, iconY - 26, "[E] Resume Ancient Exam (" + activeTask.getCorrectCount() + "/5 Solved)", Color.web("#ffe58f"));
            } else if (activeTask == null || !panelVisible) {
                drawStationPrompt(g, sx, iconY - 26, "[E] Answer Questions at " + station.getDisplayName(), Color.web("#ffe58f"));
            }
        }
    }

    private void drawStationPrompt(GraphicsContext g, double cx, double cy, String text, Color textColor) {
        double textW = text.length() * 7.5 + 24.0;
        double boxH = 24.0;
        double bx = cx - textW / 2.0;
        double by = cy - boxH / 2.0;

        g.setFill(Color.rgb(12, 10, 14, 0.88));
        g.fillRoundRect(bx, by, textW, boxH, 8, 8);
        g.setStroke(Color.rgb(220, 170, 60, 0.85));
        g.setLineWidth(1.5);
        g.strokeRoundRect(bx, by, textW, boxH, 8, 8);

        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        g.setFill(textColor);
        g.fillText(text, bx + 12, by + 16);
    }

    private List<String> wrapText(String text, double maxWidth, double fontSize) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) return lines;
        int maxChars = Math.max(20, (int) (maxWidth / (fontSize * 0.58)));
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxChars) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
            }
            if (currentLine.length() > 0) currentLine.append(" ");
            currentLine.append(word);
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private String truncate(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars - 2) + "..";
    }

    public void renderQuestionPanel(GraphicsContext g, double canvasWidth, double canvasHeight) {
        if (storyGuideVisible) {
            renderStoryGuide(g, canvasWidth, canvasHeight);
            return;
        }
        if (activeTask == null || !panelVisible) {
            return;
        }

        Question q = activeTask.getCurrentQuestion();
        if (q == null) return;

        double panelW = Math.min(740.0, canvasWidth - 40.0);
        double panelH = 410.0;
        double panelX = (canvasWidth - panelW) / 2.0;
        double panelY = canvasHeight - panelH - 12.0;

        // 1. Draw Transparent Ornate Khmer Image Frame
        if (questionPanelImage != null) {
            g.drawImage(questionPanelImage, panelX, panelY, panelW, panelH);
        } else {
            g.setFill(Color.rgb(16, 12, 18, 0.94));
            g.fillRoundRect(panelX, panelY, panelW, panelH, 14, 14);
            g.setStroke(Color.web("#c49a45"));
            g.setLineWidth(2.5);
            g.strokeRoundRect(panelX, panelY, panelW, panelH, 14, 14);
        }

        // 2. Strict Inner Parchment Safe Area (Comfortably shifted downwards to fit inside parchment)
        double insetX = panelX + panelW * 0.145;
        double insetW = panelW * 0.71;
        double insetY = panelY + panelH * 0.245;

        // Header: Room Title
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        g.setFill(Color.web("#ffe082"));
        String roomName = (activeRoomId != null ? activeRoomId.toUpperCase() : "HAUNTED ROOM");
        g.fillText("✦ ANCIENT EXAM: " + roomName + " ✦", insetX, insetY + 16);

        int correct = activeTask.getCorrectCount();
        int remaining = Math.max(0, 5 - correct);
        boolean isFinished = (correct >= 5);

        // Progress Bar (Ornate 5-Gemstone Khmer Bar)
        double progW = 142.0;
        double progH = 40.0;
        double progX = insetX + insetW - progW;
        double progY = insetY - 4.0;

        if (quizProgressBarImage != null) {
            g.drawImage(quizProgressBarImage, progX, progY, progW, progH);
            // Dynamic gemstone socket indicators
            for (int i = 0; i < 5; i++) {
                double gx = progX + 21.0 + i * 25.0;
                double gy = progY + 20.0;
                if (i < correct) {
                    // Glowing golden/emerald aura over solved gems
                    g.setFill(Color.rgb(255, 230, 90, 0.45));
                    g.fillOval(gx - 7, gy - 7, 14, 14);
                } else {
                    // Dark veil over unactivated sockets
                    g.setFill(Color.rgb(15, 12, 18, 0.62));
                    g.fillOval(gx - 8, gy - 8, 16, 16);
                }
            }
        } else {
            // Fallback segmented blocks
            for (int i = 0; i < 5; i++) {
                g.setFill(i < correct ? Color.web("#52c41a") : Color.rgb(60, 45, 30, 0.85));
                g.fillRoundRect(progX + i * 24, progY + 10, 20, 12, 3, 3);
                g.setStroke(Color.web("#d4b106"));
                g.setLineWidth(1.0);
                g.strokeRoundRect(progX + i * 24, progY + 10, 20, 12, 3, 3);
            }
        }

        // "Answers Left" or "Finished" Button Badge
        double btnW = 105.0;
        double btnH = 26.0;
        double btnX = progX - btnW - 10.0;
        double btnY = progY + 7.0;

        if (isFinished) {
            g.setFill(Color.rgb(18, 65, 28, 0.94));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setStroke(Color.rgb(82, 196, 26, 0.95));
            g.setLineWidth(1.5);
            g.strokeRoundRect(btnX, btnY, btnW, btnH, 6, 6);

            g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            g.setFill(Color.web("#73d13d"));
            g.fillText("✔ FINISHED 🔑", btnX + 11, btnY + 17);
        } else {
            g.setFill(Color.rgb(28, 20, 14, 0.92));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setStroke(Color.rgb(212, 160, 23, 0.92));
            g.setLineWidth(1.5);
            g.strokeRoundRect(btnX, btnY, btnW, btnH, 6, 6);

            g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            g.setFill(Color.web("#ffd591"));
            g.fillText("⏳ " + remaining + " LEFT (" + correct + "/5)", btnX + 10, btnY + 17);
        }

        // Question Text (Strictly wrapped inside insetW)
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        List<String> qLines = wrapText(q.getText(), insetW, 14);
        double curY = insetY + 48.0;
        for (String ql : qLines) {
            g.setFill(Color.rgb(0, 0, 0, 0.95));
            g.fillText(ql, insetX + 1, curY + 1);
            g.setFill(Color.web("#fff6de"));
            g.fillText(ql, insetX, curY);
            curY += 20.0;
        }

        // 4 Option Bars (Neatly arranged in 2x2 grid inside parchment)
        List<String> options = q.getOptions();
        double optGapX = 12.0;
        double optGapY = 8.0;
        double colW = (insetW - optGapX) / 2.0;
        double boxH = 46.0;
        double optStartY = Math.max(insetY + 98.0, curY + 10.0);

        for (int i = 0; i < Math.min(4, options.size()); i++) {
            int col = i % 2;
            int row = i / 2;
            double bx = insetX + col * (colW + optGapX);
            double by = optStartY + row * (boxH + optGapY);

            // Draw Ornate Option Bar PNG
            if (quizOptionBarImage != null) {
                g.drawImage(quizOptionBarImage, bx, by, colW, boxH);
            } else {
                g.setFill(Color.rgb(18, 14, 18, 0.90));
                g.fillRoundRect(bx, by, colW, boxH, 6, 6);
                g.setStroke(Color.rgb(185, 140, 55, 0.88));
                g.setLineWidth(1.5);
                g.strokeRoundRect(bx, by, colW, boxH, 6, 6);
            }

            // Gold Number Badge [ 1 ], [ 2 ], [ 3 ], [ 4 ]
            double badgeX = bx + 7.0;
            double badgeY = by + 9.0;
            double badgeSize = 28.0;
            g.setFill(Color.rgb(212, 160, 23, 0.95));
            g.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 5, 5);
            g.setStroke(Color.rgb(255, 235, 150, 0.85));
            g.setLineWidth(1.0);
            g.strokeRoundRect(badgeX, badgeY, badgeSize, badgeSize, 5, 5);

            g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            g.setFill(Color.web("#140d02"));
            g.fillText(String.valueOf(i + 1), badgeX + 9, badgeY + 19);

            // Option text (carefully truncated and positioned on inner card)
            g.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            String optText = truncate(options.get(i), 26);
            double textX = bx + 42.0;
            double textY = by + 28.0;
            g.setFill(Color.rgb(0, 0, 0, 0.98));
            g.fillText(optText, textX + 1, textY + 1);
            g.setFill(Color.web("#fff6de"));
            g.fillText(optText, textX, textY);
        }

        // Footer Guidance
        double footY = optStartY + boxH * 2 + optGapY + 22.0;
        g.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        g.setFill(Color.rgb(225, 205, 155, 0.95));
        g.fillText("Press [1]-[4] or Click Option Bar to Answer • Move away to flee and pause", insetX, footY);
    }

    public boolean handleMouseClick(double mouseX, double mouseY, double canvasWidth, double canvasHeight) {
        if (storyGuideVisible) {
            storyGuideVisible = false;
            game.playSound("door");
            return true;
        }
        if (activeTask == null || !panelVisible) return false;
        Question q = activeTask.getCurrentQuestion();
        if (q == null) return false;

        double panelW = Math.min(740.0, canvasWidth - 40.0);
        double panelH = 410.0;
        double panelX = (canvasWidth - panelW) / 2.0;
        double panelY = canvasHeight - panelH - 12.0;

        double insetX = panelX + panelW * 0.145;
        double insetW = panelW * 0.71;
        double insetY = panelY + panelH * 0.245;

        List<String> qLines = wrapText(q.getText(), insetW, 14);
        double curY = insetY + 48.0 + qLines.size() * 20.0;
        double optStartY = Math.max(insetY + 98.0, curY + 10.0);

        List<String> options = q.getOptions();
        double optGapX = 12.0;
        double optGapY = 8.0;
        double colW = (insetW - optGapX) / 2.0;
        double boxH = 46.0;

        for (int i = 0; i < Math.min(4, options.size()); i++) {
            int col = i % 2;
            int row = i / 2;
            double bx = insetX + col * (colW + optGapX);
            double by = optStartY + row * (boxH + optGapY);

            if (mouseX >= bx && mouseX <= bx + colW && mouseY >= by && mouseY <= by + boxH) {
                submitAnswer(i);
                return true;
            }
        }
        return false;
    }

    public boolean tryInteractAt(double px, double py) {
        QuizStation s = getCurrentRoomStation(px, py);
        if (s == null) return false;

        double dist = Math.hypot(px - s.getX(), py - s.getY());
        if (dist <= s.getRadius()) {
            String roomId = s.getRoomId();
            if (completedRooms.contains(roomId)) {
                game.showNotification("This examination desk is purified! The Room Key is in your inventory.");
                return true;
            }
            ensureRoomTask(roomId);
            RoomTask t = tasks.get(roomId);
            if (t != null && !t.isCompleted()) {
                activeTask = t;
                activeRoomId = roomId;
                panelVisible = true;
                game.playSound("puzzle_complete");
                game.showNotification("Opened " + s.getDisplayName() + ". Answer to unlock the door!");
                return true;
            }
        }
        return false;
    }

    public boolean isActive() {
        return activeTask != null && panelVisible;
    }

    public boolean isPanelVisible() {
        return (activeTask != null && panelVisible) || storyGuideVisible;
    }

    public void setPanelVisible(boolean visible) {
        this.panelVisible = visible;
    }

    public void submitAnswer(int slotZeroBased) {
        if (activeTask == null || !panelVisible) return;
        int choice = slotZeroBased;
        boolean correct = activeTask.submitAnswer(choice);
        if (correct) {
            AudioManager.getInstance().playCorrectAnswer();
            game.playSound("puzzle_complete");
            game.showNotification("Correct! (" + activeTask.getCorrectCount() + "/5)");
            if (activeTask.isCompleted()) {
                String finishedRoom = activeRoomId;
                dropRoomKeyAndUnlock(finishedRoom);
                completedRooms.add(finishedRoom);
                activeTask = null;
                activeRoomId = null;
                panelVisible = false;
                triggerStoryGuide(finishedRoom);
            }
        } else {
            AudioManager.getInstance().playWrongAnswer();
            game.playSound("ghost");
            game.showNotification("Wrong answer! A vengeful ghost spawned nearby!");
            spawnGhostNearPlayer();
        }
    }

    public void triggerStoryGuide(String roomId) {
        this.storyGuideVisible = true;
        this.panelVisible = false;
        String rid = roomId != null ? roomId.toLowerCase().trim() : "";

        // 1. Try loading guide information configured by Admin in RoomModel first
        try {
            List<RoomModel> rooms = roomFileService.loadRooms();
            if (rooms != null) {
                for (RoomModel rm : rooms) {
                    if (matchesRoom(rm.getId(), rid)) {
                        boolean hasCustomGuide = false;
                        if (rm.getGuideTitle() != null && !rm.getGuideTitle().isBlank()) {
                            this.storyGuideTitle = rm.getGuideTitle();
                            hasCustomGuide = true;
                        }
                        if (rm.getGuideNarrative() != null && !rm.getGuideNarrative().isBlank()) {
                            this.storyGuideNarrative = rm.getGuideNarrative();
                            hasCustomGuide = true;
                        }
                        if (rm.getGuideNextStep() != null && !rm.getGuideNextStep().isBlank()) {
                            this.storyGuideNextStep = rm.getGuideNextStep();
                            hasCustomGuide = true;
                        }
                        if (hasCustomGuide) {
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[EducationManager] Error reading custom room guide: " + e.getMessage());
        }

        // 2. Fallback to default Khmer Spirit story lore
        switch (rid) {
            case "classrooma", "classroom" -> {
                this.storyGuideTitle = "✦ CLASSROOM A PURIFIED: SPIRIT OF SOTHEA ✦";
                this.storyGuideNarrative = "By reciting the ancient scripts, the restless student spirit Sothea has found peace.\n"
                        + "Her spectral tears crystallize into the Bronze Door Key. She whispers:\n"
                        + "\"The Headmaster conducted a forbidden dark ritual to seal our souls forever.\n"
                        + "He locked the final school exit with the Master Key...\"";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "The Classroom Door is now unlocked! Step into the Central Main Hall.\n"
                        + "Head north to the Lore Library or enter the Teacher's Lounge on the East Corridor!";
            }
            case "classroomb", "teachers_lounge" -> {
                this.storyGuideTitle = "✦ TEACHER'S LOUNGE PURIFIED: FACULTY ARCHIVE ✦";
                this.storyGuideNarrative = "The ancient faculty attendance ledger reveals the teachers' desperate attempt\n"
                        + "to barricade the school gates before the curse overtook them.\n"
                        + "Among the papers, you discovered the door key!";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Room door unlocked! Proceed to the Music & Art Room or the Lore Library in the North Wing!";
            }
            case "computer", "music_art_room" -> {
                this.storyGuideTitle = "✦ MUSIC & ART STUDIO PURIFIED: HAUNTED HARMONY ✦";
                this.storyGuideNarrative = "The piano echoes its final peaceful chord. A student sketch on the easel\n"
                        + "reveals the location of the Night Vision Goggles locked in the Storage Vault!";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Head across to the East Wing and search the Storage Vault and School Infirmary!";
            }
            case "library" -> {
                this.storyGuideTitle = "✦ LORE ARCHIVE PURIFIED: ANCIENT SCRIPTURES ✦";
                this.storyGuideNarrative = "The palm-leaf manuscripts describe how Angkorian relics counteract the demonic realm.\n"
                        + "The Headmaster holds the Master Key inside his private office.";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Proceed to the Science Lab to brew the spirit solvent, then enter\n"
                        + "the Principal's Office on the East Wing to claim the Master Key!";
            }
            case "laboratory", "science_lab" -> {
                this.storyGuideTitle = "✦ SCIENCE LAB PURIFIED: ALCHEMICAL ESSENCE ✦";
                this.storyGuideNarrative = "The alchemical distillers glow with soothing emerald fire.\n"
                        + "The way forward is revealed.";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Confront the Headmaster in the Principal's Office to claim the Master Key!";
            }
            case "teacher", "principal_office" -> {
                this.storyGuideTitle = "✦ PRINCIPAL'S SANCTUM PURIFIED: MASTER KEY CLAIMED ✦";
                this.storyGuideNarrative = "The Headmaster's spirit sheds tears of remorse for trapping everyone in darkness.\n"
                        + "He places the heavy golden MASTER KEY into your trembling hands.\n"
                        + "\"Go, child... Break the chains and escape back to the living world!\"";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "You possess the MASTER KEY! Return to the Central Main Hall and unlock\n"
                        + "the Grand South Exit Iron Gates to ESCAPE AND WIN THE GAME!";
            }
            case "dormitory", "infirmary" -> {
                this.storyGuideTitle = "✦ SCHOOL INFIRMARY PURIFIED: HEALING WARD ✦";
                this.storyGuideNarrative = "The gentle spirit of the school nurse blesses you with restorative energy.\n"
                        + "The sanctuary has been cleansed of evil.";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "With renewed vitality, explore the remaining wings and secure the Master Key!";
            }
            case "basement", "storage_room" -> {
                this.storyGuideTitle = "✦ STORAGE VAULT PURIFIED: VAULT UNLOCKED ✦";
                this.storyGuideNarrative = "The heavy chains rattle loose as the spirits depart.\n"
                        + "Explore the shelves and corners to find valuable survival equipment!";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Search for items placed in this room, then return to the Main Hall.";
            }
            case "entrance", "restroom" -> {
                this.storyGuideTitle = "✦ HAUNTED MIRROR PURIFIED: CLEAR REFLECTION ✦";
                this.storyGuideNarrative = "The blood on the cracked mirror dissolves into pure crystal water.\n"
                        + "The mirror phantom bows in gratitude before dissolving.";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Return to the corridor to finish purifying the remaining rooms!";
            }
            default -> {
                this.storyGuideTitle = "✦ SACRED SANCTUM PURIFIED ✦";
                this.storyGuideNarrative = "Ancient divine energy radiates across the room, purifying the haunting.";
                this.storyGuideNextStep = "✦ WHAT TO DO NEXT:\n"
                        + "Return to the Main Hall and make your way to the Grand South Exit!";
            }
        }
    }

    public void openCurrentStoryGuide() {
        if (completedRooms.isEmpty()) {
            this.storyGuideTitle = "✦ QUEST PROLOGUE: PURIFY CLASSROOM A ✦";
            this.storyGuideNarrative = "You awaken trapped within Classroom A of the haunted high school.\n"
                    + "The exit doors are locked with dark supernatural seals.\n"
                    + "Ancient knowledge is the key to purifying the spirits and breaking the curse.\n"
                    + "Solve the educational spirit trial at the study altar to claim the Classroom Key!";
            this.storyGuideNextStep = "✦ WHAT TO DO NEXT & WHERE TO GO:\n"
                    + "• Approach the examination altar in Classroom A and press [E] to interact.\n"
                    + "• Answer all 5 spirit questions correctly to receive the Bronze Classroom Key.\n"
                    + "• Press [E] at the classroom door to unlock and enter the Central Main Hall!\n"
                    + "• Press [Q] anytime during gameplay to view this Quest Guide.";
            this.storyGuideVisible = true;
            this.panelVisible = false;
            AudioManager.getInstance().playStoryIntro();
        } else {
            String lastRoom = completedRooms.get(completedRooms.size() - 1);
            triggerStoryGuide(lastRoom);
        }
    }

    public void toggleStoryGuide() {
        if (storyGuideVisible) {
            dismissStoryGuide();
        } else {
            openCurrentStoryGuide();
        }
    }

    public void renderStoryGuide(GraphicsContext g, double canvasWidth, double canvasHeight) {
        if (!storyGuideVisible) return;

        // Dark modal overlay
        g.setFill(Color.rgb(0, 0, 0, 0.82));
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        double panelW = Math.min(840.0, canvasWidth - 40.0);
        double panelH = panelW * (737.0 / 1242.0);
        double panelX = (canvasWidth - panelW) / 2.0;
        double panelY = (canvasHeight - panelH) / 2.0;

        if (storyGuideImage != null) {
            g.drawImage(storyGuideImage, panelX, panelY, panelW, panelH);
        } else {
            g.setFill(Color.rgb(18, 14, 20, 0.96));
            g.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);
            g.setStroke(Color.web("#c49a45"));
            g.setLineWidth(2.5);
            g.strokeRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        }

        // Inner content safe area inside the cleaned stone tablet
        double insetX = panelX + panelW * 0.215;
        double insetW = panelW * 0.57;
        double insetY = panelY + panelH * 0.165;

        // 1. Custom Typography: Header Title
        g.setFont(Font.font("Georgia", FontWeight.BOLD, 15.5));
        g.setFill(Color.rgb(0, 0, 0, 0.95));
        g.fillText(storyGuideTitle, insetX + 1.5, insetY + 1.5);
        g.setFill(Color.web("#ffd591"));
        g.fillText(storyGuideTitle, insetX, insetY);

        // Gold divider line
        g.setStroke(Color.rgb(212, 175, 55, 0.5));
        g.setLineWidth(1.2);
        g.strokeLine(insetX, insetY + 7.0, insetX + insetW, insetY + 7.0);

        // 2. Custom Typography: Lore Narrative
        g.setFont(Font.font("Georgia", FontWeight.NORMAL, 13));
        double curY = insetY + 26.0;
        String[] storyLines = storyGuideNarrative.split("\n");
        for (String line : storyLines) {
            List<String> wrapped = wrapText(line, insetW, 13);
            for (String wl : wrapped) {
                g.setFill(Color.rgb(0, 0, 0, 0.95));
                g.fillText(wl, insetX + 1, curY + 1);
                g.setFill(Color.web("#fff2db"));
                g.fillText(wl, insetX, curY);
                curY += 17.5;
            }
        }

        // 3. Custom Typography: What to do next (Highlighted Objective Box)
        curY += 8.0;
        String[] guideLines = storyGuideNextStep.split("\n");
        double guideBoxH = Math.max(90.0, guideLines.length * 18.0 + 14.0);
        double maxBoxH = (panelY + panelH - 64.0) - curY;
        if (guideBoxH > maxBoxH) guideBoxH = maxBoxH;

        g.setFill(Color.rgb(15, 30, 20, 0.88));
        g.fillRoundRect(insetX - 8, curY - 4, insetW + 16, guideBoxH, 8, 8);
        g.setStroke(Color.rgb(82, 196, 26, 0.90));
        g.setLineWidth(1.5);
        g.strokeRoundRect(insetX - 8, curY - 4, insetW + 16, guideBoxH, 8, 8);

        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        double gy = curY + 14.0;
        for (int i = 0; i < guideLines.length; i++) {
            String gl = guideLines[i];
            g.setFill(Color.rgb(0, 0, 0, 0.95));
            g.fillText(gl, insetX + 4, gy + 1);
            if (i == 0) {
                g.setFill(Color.web("#ffe58f"));
            } else {
                g.setFill(Color.web("#b7eb8f"));
            }
            g.fillText(gl, insetX + 3, gy);
            gy += 17.5;
            if (gy > curY + guideBoxH - 6.0) break;
        }

        // 4. Custom Typography: Continue Button
        double btnW = 320.0;
        double btnH = 38.0;
        double btnX = panelX + (panelW - btnW) / 2.0;
        double btnY = panelY + panelH - 58.0;

        g.setFill(Color.rgb(42, 22, 16, 0.95));
        g.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);
        g.setStroke(Color.web("#ffd591"));
        g.setLineWidth(2.0);
        g.strokeRoundRect(btnX, btnY, btnW, btnH, 8, 8);

        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        String btnText = "✦ CONTINUE QUEST [CLICK / SPACE / E] ✦";
        g.setFill(Color.rgb(0, 0, 0, 0.9));
        g.fillText(btnText, btnX + 16, btnY + 24);
        g.setFill(Color.web("#ffffff"));
        g.fillText(btnText, btnX + 15, btnY + 23);
    }

    public boolean dismissStoryGuide() {
        if (storyGuideVisible) {
            storyGuideVisible = false;
            game.playSound("door");
            return true;
        }
        return false;
    }

    public boolean isStoryGuideVisible() {
        return storyGuideVisible;
    }

    public void spawnGhost(double x, double y) {
        ghosts.add(new Ghost(x, y));
    }

    public void spawnAmbientGhost(Player player) {
        double angle = rng.nextDouble() * Math.PI * 2.0;
        double spawnDist = 280.0 + rng.nextDouble() * 120.0;
        double gx = player.getCenterX() + Math.cos(angle) * spawnDist;
        double gy = player.getCenterY() + Math.sin(angle) * spawnDist;
        ghosts.add(new Ghost(gx, gy));
        game.playSound("ghost");
        game.showNotification("A cold chill fills the room... A spirit manifests!");
    }

    public void spawnGhostNearPlayer() {
        Player p = game.getPlayer();
        double offsetX = (rng.nextBoolean() ? 1 : -1) * (110 + rng.nextDouble() * 50);
        double offsetY = (rng.nextBoolean() ? 1 : -1) * (80 + rng.nextDouble() * 40);
        Ghost g = new Ghost(p.getCenterX() + offsetX, p.getCenterY() + offsetY);
        ghosts.add(g);
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    private void dropRoomKeyAndUnlock(String roomId) {
        tileMap.getDoors().stream()
                .filter(d -> d.getFromRoomId().equalsIgnoreCase(roomId) || d.getToRoomId().equalsIgnoreCase(roomId))
                .findFirst()
                .ifPresent(door -> {
                    door.setLocked(false);
                    tileMap.setDoorOpen(door, true);
                    tileMap.setTile(door.getColumn(), door.getRow(), Tile.FLOOR);
                });

        game.playSound("door");
        AudioManager.getInstance().playDoorUnlock();

        String rid = roomId != null ? roomId.toLowerCase().trim() : "";

        // Determine key reward from Admin RoomModel or fallback
        String keyToGive = null;
        try {
            List<RoomModel> rooms = roomFileService.loadRooms();
            if (rooms != null) {
                for (RoomModel rm : rooms) {
                    if (matchesRoom(rm.getId(), rid)) {
                        keyToGive = rm.getKeyReward();
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        if (keyToGive == null || keyToGive.isBlank()) {
            if (rid.contains("principal") || rid.equals("teacher")) {
                keyToGive = "master_key";
            } else {
                keyToGive = "key";
            }
        }

        // Automatic key reward to open new doors / places (NO extra random item rewards)
        final String finalKey = keyToGive;
        ItemRegistry.findById(finalKey).ifPresent(item -> {
            game.getInventory().addItem(item);
            AudioManager.getInstance().playGetKey();
        });

        String keyName = "master_key".equalsIgnoreCase(finalKey) ? "Master Key" : "Room Key";
        game.showNotification("Quiz Solved! " + keyName + " added to inventory & exit door unlocked!");
    }

    private boolean matchesRoom(String r1, String r2) {
        if (r1 == null || r2 == null) return false;
        String a = r1.trim().toLowerCase();
        String b = r2.trim().toLowerCase();
        if (a.equals(b)) return true;
        if ((a.equals("classrooma") || a.equals("classroom")) && (b.equals("classrooma") || b.equals("classroom"))) return true;
        if ((a.equals("classroomb") || a.equals("teachers_lounge")) && (b.equals("classroomb") || b.equals("teachers_lounge"))) return true;
        if ((a.equals("computer") || a.equals("music_art_room")) && (b.equals("computer") || b.equals("music_art_room"))) return true;
        if ((a.equals("laboratory") || a.equals("science_lab")) && (b.equals("laboratory") || b.equals("science_lab"))) return true;
        if ((a.equals("teacher") || a.equals("principal_office")) && (b.equals("teacher") || b.equals("principal_office"))) return true;
        if ((a.equals("dormitory") || a.equals("infirmary")) && (b.equals("dormitory") || b.equals("infirmary"))) return true;
        if ((a.equals("basement") || a.equals("storage_room")) && (b.equals("basement") || b.equals("storage_room"))) return true;
        if ((a.equals("entrance") || a.equals("restroom")) && (b.equals("entrance") || b.equals("restroom"))) return true;
        if ((a.equals("hall") || a.equals("main_hall") || a.equals("school")) && (b.equals("hall") || b.equals("main_hall") || b.equals("school"))) return true;
        return false;
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