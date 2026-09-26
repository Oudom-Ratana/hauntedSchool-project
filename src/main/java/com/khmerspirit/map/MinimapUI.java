package com.khmerspirit.map;

import com.khmerspirit.config.Constants;
import com.khmerspirit.entities.Ghost;
import com.khmerspirit.items.ItemPickup;
import com.khmerspirit.player.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GTA-style live circular/rounded minimap HUD and interactive full-screen school floor plan.
 */
public class MinimapUI {

    public static final double MINIMAP_X = 18.0;
    public static final double MINIMAP_Y = 18.0;
    public static final double MINIMAP_SIZE = 144.0;
    private static final double RADAR_RADIUS = MINIMAP_SIZE / 2.0;
    private static final double RADAR_CENTER_X = MINIMAP_X + RADAR_RADIUS;
    private static final double RADAR_CENTER_Y = MINIMAP_Y + RADAR_RADIUS;

    private boolean fullMapOpen = false;
    private final Map<String, Image> roomImageCache = new HashMap<>();

    private Image getRoomImage(String path) {
        if (path == null) return null;
        return roomImageCache.computeIfAbsent(path, p -> {
            try {
                var is = MinimapUI.class.getResourceAsStream(p);
                if (is != null) {
                    return new Image(is);
                }
            } catch (Exception ignored) {}
            return null;
        });
    }

    public boolean isFullMapOpen() {
        return fullMapOpen;
    }

    public void setFullMapOpen(boolean open) {
        this.fullMapOpen = open;
    }

    public void toggleFullMap() {
        this.fullMapOpen = !this.fullMapOpen;
    }

    public boolean handleMouseClick(double mouseX, double mouseY) {
        if (fullMapOpen) {
            // Clicking anywhere while full map is open closes it
            fullMapOpen = false;
            return true;
        }

        // Clicking on the top-left minimap radar opens the full map
        double dx = mouseX - RADAR_CENTER_X;
        double dy = mouseY - RADAR_CENTER_Y;
        if (dx * dx + dy * dy <= (RADAR_RADIUS + 8.0) * (RADAR_RADIUS + 8.0)) {
            fullMapOpen = true;
            return true;
        }
        return false;
    }

    public void renderMinimap(GraphicsContext g, TileMap tileMap, Player player,
                              List<ItemPickup> pickups, List<Ghost> ghosts,
                              String currentMapId, double pulseTimer) {
        renderMinimap(g, tileMap, player, pickups, ghosts, currentMapId, pulseTimer, false);
    }

    /**
     * Renders the live GTA-style circular radar on the top-left HUD.
     * Stealth mechanic: Ghosts only appear on radar if player has Night Vision Goggles!
     */
    public void renderMinimap(GraphicsContext g, TileMap tileMap, Player player,
                              List<ItemPickup> pickups, List<Ghost> ghosts,
                              String currentMapId, double pulseTimer, boolean hasNightVision) {
        if (fullMapOpen) {
            return; // Suppress small minimap when full map is displayed
        }

        g.save();

        // 1. Radar background shadow & outer chassis
        g.setFill(Color.rgb(2, 6, 12, 0.85));
        g.fillOval(MINIMAP_X - 6, MINIMAP_Y - 6, MINIMAP_SIZE + 12, MINIMAP_SIZE + 12);

        // Outer Tactical Bezel with cyber notches
        g.setStroke(Color.rgb(15, 30, 48, 0.95));
        g.setLineWidth(5.0);
        g.strokeOval(MINIMAP_X - 2.5, MINIMAP_Y - 2.5, MINIMAP_SIZE + 5, MINIMAP_SIZE + 5);

        // 2. Circular clipping for live radar content
        g.beginPath();
        g.arc(RADAR_CENTER_X, RADAR_CENTER_Y, RADAR_RADIUS, RADAR_RADIUS, 0, 360);
        g.closePath();
        g.clip();

        // High-tech dark glass backdrop
        RadialGradient bgGrad = new RadialGradient(0, 0, RADAR_CENTER_X, RADAR_CENTER_Y, RADAR_RADIUS, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(8, 20, 32, 0.96)),
                new Stop(0.70, Color.rgb(4, 12, 22, 0.98)),
                new Stop(1.0, Color.rgb(2, 6, 14, 1.00)));
        g.setFill(bgGrad);
        g.fillRect(MINIMAP_X, MINIMAP_Y, MINIMAP_SIZE, MINIMAP_SIZE);

        // Concentric Tactical Distance Rings
        g.setLineWidth(1.0);
        double[] rangeRings = {24.0, 48.0};
        for (double r : rangeRings) {
            g.setStroke(Color.rgb(30, 75, 110, 0.35));
            g.strokeOval(RADAR_CENTER_X - r, RADAR_CENTER_Y - r, r * 2.0, r * 2.0);
        }
        g.setStroke(Color.rgb(30, 75, 110, 0.25));
        g.strokeLine(RADAR_CENTER_X - RADAR_RADIUS, RADAR_CENTER_Y, RADAR_CENTER_X + RADAR_RADIUS, RADAR_CENTER_Y);
        g.strokeLine(RADAR_CENTER_X, RADAR_CENTER_Y - RADAR_RADIUS, RADAR_CENTER_X, RADAR_CENTER_Y + RADAR_RADIUS);

        // Live radar content centered on player
        double px = player.getCenterX();
        double py = player.getCenterY();
        double zoom = 0.085; // 100px in world = 8.5px on radar

        // Draw collision boxes / walls on radar in sleek cyan-slate
        g.setFill(Color.rgb(56, 110, 160, 0.55));
        for (Rectangle2D box : tileMap.getCollisionBoxes()) {
            double bx = RADAR_CENTER_X + (box.getMinX() - px) * zoom;
            double by = RADAR_CENTER_Y + (box.getMinY() - py) * zoom;
            double bw = box.getWidth() * zoom;
            double bh = box.getHeight() * zoom;
            g.fillRect(bx, by, Math.max(2.0, bw), Math.max(2.0, bh));
        }

        // Draw doors on radar with glowing status
        for (Door door : tileMap.getDoors()) {
            double dx;
            double dy;
            if (door.getBounds() != null) {
                dx = door.getBounds().getMinX() + door.getBounds().getWidth() / 2.0;
                dy = door.getBounds().getMinY() + door.getBounds().getHeight() / 2.0;
            } else {
                dx = (door.getColumn() + 0.5) * Constants.TILE_SIZE;
                dy = (door.getRow() + 0.5) * Constants.TILE_SIZE;
            }
            double rx = RADAR_CENTER_X + (dx - px) * zoom;
            double ry = RADAR_CENTER_Y + (dy - py) * zoom;

            if (door.isOpen()) {
                g.setFill(Color.rgb(34, 197, 94, 0.95)); // Neon Green
            } else {
                g.setFill(Color.rgb(245, 158, 11, 0.95)); // Amber Gold
            }
            g.fillRect(rx - 3, ry - 3, 6, 6);
        }

        // Rotating Sweeping Radar Beam (Authentic Tactical Sonar)
        double sweepAngle = (pulseTimer * 130.0) % 360.0;
        g.setFill(Color.rgb(0, 245, 212, 0.08));
        g.fillArc(RADAR_CENTER_X - RADAR_RADIUS, RADAR_CENTER_Y - RADAR_RADIUS, MINIMAP_SIZE, MINIMAP_SIZE, -sweepAngle, 35.0, javafx.scene.shape.ArcType.ROUND);

        double sweepRad = Math.toRadians(sweepAngle);
        g.setStroke(Color.rgb(0, 245, 212, 0.55));
        g.setLineWidth(1.5);
        g.strokeLine(RADAR_CENTER_X, RADAR_CENTER_Y,
                RADAR_CENTER_X + Math.cos(sweepRad) * RADAR_RADIUS,
                RADAR_CENTER_Y + Math.sin(sweepRad) * RADAR_RADIUS);

        // Expanding Sonar Ping Ring from Player
        double pingR = (pulseTimer * 26.0) % RADAR_RADIUS;
        double pingAlpha = Math.max(0.0, 1.0 - (pingR / RADAR_RADIUS)) * 0.40;
        g.setStroke(Color.rgb(0, 245, 212, pingAlpha));
        g.setLineWidth(1.2);
        g.strokeOval(RADAR_CENTER_X - pingR, RADAR_CENTER_Y - pingR, pingR * 2.0, pingR * 2.0);

        // Draw nearby Item pickups as glowing gold diamond blips
        double pulse = 0.5 + 0.5 * Math.sin(pulseTimer * 5.0);
        if (pickups != null) {
            for (ItemPickup pickup : pickups) {
                double rx = RADAR_CENTER_X + (pickup.getX() - px) * zoom;
                double ry = RADAR_CENTER_Y + (pickup.getY() - py) * zoom;
                g.setFill(Color.rgb(250, 204, 21, 0.80 + 0.20 * pulse));
                g.fillOval(rx - 3.5, ry - 3.5, 7, 7);
                g.setStroke(Color.rgb(255, 255, 255, 0.90));
                g.setLineWidth(1.0);
                g.strokeOval(rx - 3.5, ry - 3.5, 7, 7);
            }
        }

        // Draw roaming Ghosts as menacing crimson blips ONLY if player is using Night Vision Goggles!
        boolean ghostVeryClose = false;
        if (hasNightVision && ghosts != null) {
            for (Ghost ghost : ghosts) {
                double dist = Math.hypot(ghost.getX() - px, ghost.getY() - py);
                if (dist < 260.0) {
                    ghostVeryClose = true;
                }
                double gx = RADAR_CENTER_X + (ghost.getX() - px) * zoom;
                double gy = RADAR_CENTER_Y + (ghost.getY() - py) * zoom;

                // Pulsing red threat halo
                g.setFill(Color.rgb(239, 68, 68, 0.35 * pulse));
                g.fillOval(gx - 7.0, gy - 7.0, 14, 14);

                g.setFill(Color.rgb(239, 68, 68, 0.95));
                g.fillOval(gx - 4.0, gy - 4.0, 8, 8);
                g.setStroke(Color.rgb(255, 180, 180, 0.95));
                g.setLineWidth(1.0);
                g.strokeOval(gx - 4.0, gy - 4.0, 8, 8);
            }
        }

        // Flashlight field-of-view cone
        RadialGradient coneGrad = new RadialGradient(0, 0, RADAR_CENTER_X, RADAR_CENTER_Y, 48.0, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 240, 160, 0.25)),
                new Stop(1.0, Color.rgb(255, 240, 160, 0.0)));
        g.setFill(coneGrad);
        g.fillArc(RADAR_CENTER_X - 48, RADAR_CENTER_Y - 48, 96, 96, 45, 90, javafx.scene.shape.ArcType.ROUND);

        // Player Tactical Directional Chevron (Rotates according to facing direction: 0=Down, 1=Left, 2=Right, 3=Up)
        int dir = player.getDirectionRow();
        double angleDeg = 90.0; // default down
        if (dir == 1) angleDeg = 180.0; // left
        else if (dir == 2) angleDeg = 0.0; // right
        else if (dir == 3) angleDeg = 270.0; // up
        else if (dir == 0) angleDeg = 90.0; // down

        double radA = Math.toRadians(angleDeg);
        double noseX = RADAR_CENTER_X + Math.cos(radA) * 7.0;
        double noseY = RADAR_CENTER_Y + Math.sin(radA) * 7.0;
        double leftX = RADAR_CENTER_X + Math.cos(radA + 2.4) * 6.0;
        double leftY = RADAR_CENTER_Y + Math.sin(radA + 2.4) * 6.0;
        double rightX = RADAR_CENTER_X + Math.cos(radA - 2.4) * 6.0;
        double rightY = RADAR_CENTER_Y + Math.sin(radA - 2.4) * 6.0;

        g.setFill(Color.rgb(0, 245, 212, 0.98));
        g.fillPolygon(new double[]{noseX, leftX, RADAR_CENTER_X, rightX},
                new double[]{noseY, leftY, RADAR_CENTER_Y, rightY}, 4);
        g.setStroke(Color.rgb(255, 255, 255, 0.95));
        g.setLineWidth(1.2);
        g.strokePolygon(new double[]{noseX, leftX, RADAR_CENTER_X, rightX},
                new double[]{noseY, leftY, RADAR_CENTER_Y, rightY}, 4);

        g.restore(); // restore clip

        // 3. Modern Neon Bezel Frame (Cyan & Amber Cyberpunk Rim)
        g.setStroke(Color.rgb(0, 229, 255, 0.85));
        g.setLineWidth(2.2);
        g.strokeOval(MINIMAP_X, MINIMAP_Y, MINIMAP_SIZE, MINIMAP_SIZE);

        g.setStroke(Color.rgb(14, 165, 233, 0.40));
        g.setLineWidth(1.0);
        g.strokeOval(MINIMAP_X - 3.5, MINIMAP_Y - 3.5, MINIMAP_SIZE + 7, MINIMAP_SIZE + 7);

        // Tactical Cardinal Marks ([N], [S], [E], [W]) with glowing labels
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 10));
        g.setFill(Color.rgb(0, 245, 212, 0.95));
        g.fillText("N", RADAR_CENTER_X - 3.5, MINIMAP_Y + 12);
        g.setFill(Color.rgb(148, 163, 184, 0.85));
        g.fillText("S", RADAR_CENTER_X - 3.0, MINIMAP_Y + MINIMAP_SIZE - 4);
        g.fillText("W", MINIMAP_X + 4, RADAR_CENTER_Y + 3.5);
        g.fillText("E", MINIMAP_X + MINIMAP_SIZE - 10, RADAR_CENTER_Y + 3.5);

        // Proximity Threat Alert (if ghost nearby and night vision on)
        if (ghostVeryClose && hasNightVision) {
            double alertW = 100.0;
            double alertH = 16.0;
            double alertX = RADAR_CENTER_X - alertW / 2.0;
            double alertY = MINIMAP_Y - 14.0;
            g.setFill(Color.rgb(220, 38, 38, 0.90));
            g.fillRoundRect(alertX, alertY, alertW, alertH, 4, 4);
            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            g.fillText("⚠ GHOST NEARBY", alertX + 6, alertY + 12);
        }

        // 4. "RADAR // [M] EXPAND" interactive tactical pill badge
        double badgeW = 110.0;
        double badgeH = 20.0;
        double badgeX = RADAR_CENTER_X - badgeW / 2.0;
        double badgeY = MINIMAP_Y + MINIMAP_SIZE + 6.0;

        g.setFill(Color.rgb(6, 12, 20, 0.92));
        g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
        g.setStroke(Color.rgb(0, 229, 255, 0.70));
        g.setLineWidth(1.0);
        g.strokeRoundRect(badgeX + 0.5, badgeY + 0.5, badgeW - 1, badgeH - 1, 6, 6);

        // Inner glowing text
        g.setFill(Color.rgb(0, 245, 212, 0.95));
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        g.fillText("RADAR  •  [M] MAP", badgeX + 11, badgeY + 14);
    }

    /**
     * Renders the interactive full-screen architectural school blueprint map.
     */
    public void renderFullMap(GraphicsContext g, String currentMapId, double playerWorldX, double playerWorldY,
                              double canvasWidth, double canvasHeight, double pulseTimer) {
        if (!fullMapOpen) {
            return;
        }

        g.save();

        // 1. Dark translucent horror overlay
        g.setFill(Color.rgb(3, 5, 8, 0.92));
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        // 2. Blueprint Outer Frame
        double padX = 40.0;
        double padY = 28.0;
        double mapW = canvasWidth - padX * 2.0;
        double mapH = canvasHeight - padY * 2.0;

        g.setFill(Color.rgb(8, 16, 24, 0.96));
        g.fillRoundRect(padX, padY, mapW, mapH, 16, 16);
        g.setStroke(Color.web("#4a90e2"));
        g.setLineWidth(2.0);
        g.strokeRoundRect(padX + 0.5, padY + 0.5, mapW - 1, mapH - 1, 16, 16);

        // Blueprint subtle grid pattern
        g.setStroke(Color.rgb(40, 80, 120, 0.16));
        g.setLineWidth(1.0);
        for (double gx = padX; gx < padX + mapW; gx += 32.0) {
            g.strokeLine(gx, padY, gx, padY + mapH);
        }
        for (double gy = padY; gy < padY + mapH; gy += 32.0) {
            g.strokeLine(padX, gy, padX + mapW, gy);
        }

        // Title & Header Bar (Cyber-Investigation Tablet)
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        g.setFill(Color.rgb(0, 245, 212, 0.98));
        g.setTextAlign(TextAlignment.LEFT);
        g.fillText("INVESTIGATION TABLET  //  SISOWATH CAMPUS BLUEPRINT (1F)", padX + 24, padY + 34);

        g.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        g.setFill(Color.rgb(148, 163, 184, 0.95));
        g.fillText("Live Tracker Active  •  Press [M] / [ESC] or Click Anywhere to Close", padX + 24, padY + 52);

        // Right-aligned status badges
        double badgeRightX = padX + mapW - 24.0;
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.rgb(34, 197, 94, 0.95)); // Green online badge
        g.fillText("● GPS LOCK: 100%  |  SENSORS: ACTIVE", badgeRightX, padY + 34);
        g.setFill(Color.rgb(250, 204, 21, 0.90));
        g.fillText("CAMPUS SECURITY RADAR LINK", badgeRightX, padY + 52);
        g.setTextAlign(TextAlignment.LEFT);

        // Layout Origin & Dimensions for Schematic
        double scX = padX + 36.0;
        double scY = padY + 74.0;
        double scW = mapW - 72.0;
        double scH = mapH - 140.0;

        // 3. Draw Hallway Corridors (Luminous Navigational Arteries)
        g.setFill(Color.rgb(20, 45, 70, 0.85));
        g.setStroke(Color.rgb(56, 189, 248, 0.40));
        g.setLineWidth(1.5);

        // Central Main Hallway (horizontal)
        double hallMainY = scY + scH * 0.42;
        double hallMainH = scH * 0.16;

        Image hallImg = getRoomImage("/images/maps/main_hall/main_hall_map.png");
        if (hallImg != null) {
            g.save();
            g.beginPath();
            g.rect(scX, hallMainY, scW, hallMainH);
            g.clip();
            g.drawImage(hallImg, scX, hallMainY, scW, hallMainH);
            g.setFill(Color.rgb(10, 25, 45, 0.65));
            g.fillRect(scX, hallMainY, scW, hallMainH);
            g.restore();
        } else {
            g.setFill(Color.rgb(20, 45, 70, 0.85));
            g.fillRect(scX, hallMainY, scW, hallMainH);
        }

        g.setStroke(Color.rgb(56, 189, 248, 0.50));
        g.setLineWidth(1.5);
        g.strokeRect(scX, hallMainY, scW, hallMainH);

        // North Vertical Halls
        g.fillRect(scX + scW * 0.22, scY + scH * 0.24, scW * 0.08, scH * 0.18);
        g.fillRect(scX + scW * 0.70, scY + scH * 0.24, scW * 0.08, scH * 0.18);

        // South Vertical Halls
        g.fillRect(scX + scW * 0.46, hallMainY + hallMainH, scW * 0.08, scH * 0.26);
        g.fillRect(scX + scW * 0.82, hallMainY + hallMainH, scW * 0.08, scH * 0.26);

        // Hallway Labels
        g.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        g.setFill(Color.web("#38bdf8"));
        g.fillText("< WEST WING CORRIDOR", scX + 16, hallMainY + hallMainH / 2.0 + 4);
        g.fillText("CENTRAL MAIN HALLWAY", scX + scW * 0.40, hallMainY + hallMainH / 2.0 + 4);
        g.fillText("EAST WING CORRIDOR >", scX + scW - 180, hallMainY + hallMainH / 2.0 + 4);

        // 4. Draw All 10 Distinct Rooms With Real Generated Artwork Previews
        // Top Row (North / West-to-East)
        drawBlueprintRoom(g, "teacher", "Principal's Office",
                "Golden Master Key & Map | West Wing Top",
                "/images/maps/principal_office/principal_office_map.png",
                scX + scW * 0.02, scY, scW * 0.22, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "laboratory", "Science Lab",
                "Acid Bottle & Bunsen Lighter | West Wing Mid",
                "/images/maps/science_lab/science_lab_map.png",
                scX + scW * 0.26, scY, scW * 0.22, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "library", "Library",
                "Ancient Grimoire & Lore | North Gate",
                "/images/maps/library/library_map.png",
                scX + scW * 0.50, scY, scW * 0.22, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "dormitory", "Infirmary",
                "First Aid Kit & Medicine | East Wing Top",
                "/images/maps/infirmary/infirmary_map.png",
                scX + scW * 0.74, scY, scW * 0.24, scH * 0.38,
                currentMapId, pulseTimer);

        // Bottom Row (South / West-to-East)
        drawBlueprintRoom(g, "computer", "Music & Art",
                "Holy Charm & Bronze Bell | West Wing Bottom",
                "/images/maps/music_art_room/music_art_room_map.png",
                scX + scW * 0.02, scY + scH * 0.62, scW * 0.18, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "classroomA", "Classroom A",
                "Occult Study & Notes | West Wing Lower-Mid",
                "/images/maps/classroom_room.png",
                scX + scW * 0.22, scY + scH * 0.62, scW * 0.18, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "classroomB", "Teachers' Lounge",
                "Staff Lockers & Battery | East Wing Lower-Mid",
                "/images/maps/teachers_lounge/teachers_lounge_map.png",
                scX + scW * 0.42, scY + scH * 0.62, scW * 0.18, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "basement", "Storage Room",
                "Crowbar & Electric Fuse | East Wing Mid",
                "/images/maps/storage_room/storage_room_map.png",
                scX + scW * 0.62, scY + scH * 0.62, scW * 0.17, scH * 0.38,
                currentMapId, pulseTimer);

        drawBlueprintRoom(g, "entrance", "Restroom",
                "Old Mirror & Water | East Wing Bottom",
                "/images/maps/restroom/restroom_map.png",
                scX + scW * 0.81, scY + scH * 0.62, scW * 0.17, scH * 0.38,
                currentMapId, pulseTimer);

        // 5. Doorways with connecting icons
        drawDoorwayIndicator(g, scX + scW * 0.12, scY + scH * 0.38, true);
        drawDoorwayIndicator(g, scX + scW * 0.36, scY + scH * 0.38, true);
        drawDoorwayIndicator(g, scX + scW * 0.61, scY + scH * 0.38, true);
        drawDoorwayIndicator(g, scX + scW * 0.87, scY + scH * 0.38, true);
        drawDoorwayIndicator(g, scX + scW * 0.12, scY + scH * 0.62, true);
        drawDoorwayIndicator(g, scX + scW * 0.35, scY + scH * 0.62, true);
        drawDoorwayIndicator(g, scX + scW * 0.57, scY + scH * 0.62, true);
        drawDoorwayIndicator(g, scX + scW * 0.78, scY + scH * 0.62, true);
        drawDoorwayIndicator(g, scX + scW * 0.935, scY + scH * 0.62, false); // exit lock

        // 6. Hallway Player Marker (if currently inside hallway)
        if (currentMapId.equalsIgnoreCase("hall") || currentMapId.equalsIgnoreCase("school")) {
            double pulse = 0.5 + 0.5 * Math.sin(pulseTimer * 5.0);
            // School map is 90 columns * 48 = 4320px wide, and 60 rows * 48 = 2880px high
            double normalizedX = Math.min(0.95, Math.max(0.05, playerWorldX / 4320.0));
            double hpx = scX + scW * normalizedX;
            double hpy = hallMainY + hallMainH / 2.0;

            // If player is in south entrance corridor (Y > 1200px)
            if (playerWorldY > 1200.0) {
                hpy = hallMainY + hallMainH + scH * 0.16;
            }

            g.setFill(Color.rgb(0, 245, 212, 0.35 * pulse));
            g.fillOval(hpx - 22, hpy - 22, 44, 44);
            g.setFill(Color.rgb(0, 245, 212, 0.95));
            g.fillOval(hpx - 7, hpy - 7, 14, 14);
            g.setStroke(Color.rgb(255, 255, 255, 0.95));
            g.strokeOval(hpx - 7, hpy - 7, 14, 14);

            g.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            g.setFill(Color.web("#ffd972"));
            g.fillText("★ YOU ARE HERE (HALLWAY) ★", Math.max(scX + 10, hpx - 85), hpy - 14);
        }

        // 7. Footer Status Legend
        double footY = padY + mapH - 24.0;
        g.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        g.setFill(Color.web("#ffd972"));
        g.fillText("OBJECTIVE: Unlock Classroom Door -> Explore Rooms for Clues -> Find Master Key in Principal's Office -> Escape Gate!", scX + 8, footY);

        g.restore();
    }

    private void drawBlueprintRoom(GraphicsContext g, String roomId, String title, String subtitle,
                                   String imagePath,
                                   double rx, double ry, double rw, double rh,
                                   String currentMapId, double pulseTimer) {
        boolean isCurrent = currentMapId != null && (
                currentMapId.equalsIgnoreCase(roomId) ||
                (roomId.equalsIgnoreCase("classroomA") && currentMapId.equalsIgnoreCase("classroom")) ||
                (roomId.equalsIgnoreCase("laboratory") && currentMapId.equalsIgnoreCase("science_lab")) ||
                (roomId.equalsIgnoreCase("teacher") && currentMapId.equalsIgnoreCase("principal_office")) ||
                (roomId.equalsIgnoreCase("dormitory") && currentMapId.equalsIgnoreCase("infirmary")) ||
                (roomId.equalsIgnoreCase("basement") && currentMapId.equalsIgnoreCase("storage_room")) ||
                (roomId.equalsIgnoreCase("computer") && currentMapId.equalsIgnoreCase("music_art_room")) ||
                (roomId.equalsIgnoreCase("classroomB") && currentMapId.equalsIgnoreCase("teachers_lounge"))
        );

        g.save();

        // 1. Clip to rounded rectangle
        g.beginPath();
        g.moveTo(rx + 8, ry);
        g.lineTo(rx + rw - 8, ry);
        g.arcTo(rx + rw, ry, rx + rw, ry + rh, 8);
        g.arcTo(rx + rw, ry + rh, rx, ry + rh, 8);
        g.arcTo(rx, ry + rh, rx, ry, 8);
        g.arcTo(rx, ry, rx + rw, ry, 8);
        g.closePath();
        g.clip();

        // 2. Render actual generated room artwork
        Image roomImg = getRoomImage(imagePath);
        if (roomImg != null) {
            g.drawImage(roomImg, rx, ry, rw, rh);
            LinearGradient overlay = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(5, 10, 18, isCurrent ? 0.30 : 0.55)),
                    new Stop(0.6, Color.rgb(8, 14, 22, isCurrent ? 0.25 : 0.45)),
                    new Stop(1.0, Color.rgb(4, 7, 12, isCurrent ? 0.70 : 0.85)));
            g.setFill(overlay);
            g.fillRect(rx, ry, rw, rh);
        } else {
            g.setFill(isCurrent ? Color.rgb(30, 60, 90, 0.85) : Color.rgb(12, 22, 34, 0.82));
            g.fillRect(rx, ry, rw, rh);
        }

        g.restore(); // restore clip

        // 3. Border styling
        if (isCurrent) {
            double pulse = 0.5 + 0.5 * Math.sin(pulseTimer * 5.0);
            g.setStroke(Color.rgb(250, 204, 21, 0.75 + 0.25 * pulse));
            g.setLineWidth(3.0);
            g.strokeRoundRect(rx, ry, rw, rh, 8, 8);

            // Glowing Outer Ring
            g.setStroke(Color.rgb(250, 204, 21, 0.25 * pulse));
            g.setLineWidth(6.0);
            g.strokeRoundRect(rx - 2, ry - 2, rw + 4, rh + 4, 10, 10);

            // Pulsing "YOU ARE HERE" tag
            double tagW = 112.0;
            double tagH = 20.0;
            g.setFill(Color.rgb(234, 179, 8, 0.95));
            g.fillRoundRect(rx + (rw - tagW) / 2.0, ry + 6, tagW, tagH, 6, 6);
            g.setFill(Color.rgb(0, 0, 0, 0.95));
            g.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            g.fillText("★ YOU ARE HERE ★", rx + (rw - tagW) / 2.0 + 8, ry + 20);
        } else {
            g.setStroke(Color.rgb(74, 144, 226, 0.55));
            g.setLineWidth(1.5);
            g.strokeRoundRect(rx, ry, rw, rh, 8, 8);
        }

        // 4. Room title & subtitle at bottom of card
        double bannerH = 34.0;
        double bannerY = ry + rh - bannerH;
        g.setFill(Color.rgb(0, 0, 0, 0.78));
        g.fillRect(rx + 1, bannerY, rw - 2, bannerH - 1);
        g.setStroke(isCurrent ? Color.rgb(250, 204, 21, 0.40) : Color.rgb(74, 144, 226, 0.25));
        g.setLineWidth(1.0);
        g.strokeLine(rx + 1, bannerY, rx + rw - 1, bannerY);

        g.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        g.setFill(isCurrent ? Color.web("#ffd972") : Color.web("#f1f5f9"));
        g.fillText(title, rx + 8, bannerY + 14);

        g.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        g.setFill(Color.web("#94a3b8"));
        g.fillText(subtitle, rx + 8, bannerY + 28);
    }

    private void drawDoorwayIndicator(GraphicsContext g, double dx, double dy, boolean unlocked) {
        g.setFill(unlocked ? Color.rgb(74, 222, 128, 0.95) : Color.rgb(239, 68, 68, 0.95));
        g.fillRect(dx - 6, dy - 3, 12, 6);
        g.setStroke(Color.rgb(255, 255, 255, 0.85));
        g.setLineWidth(1.0);
        g.strokeRect(dx - 6.5, dy - 3.5, 13, 7);
    }
}
