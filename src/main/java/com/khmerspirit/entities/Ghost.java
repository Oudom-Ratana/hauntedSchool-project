package com.khmerspirit.entities;

import com.khmerspirit.animation.GhostAnimation;
import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.core.Game;
import com.khmerspirit.player.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Haunted Horror Ghost Entity (Khmer Vengeful Spirit / Prey Ghost).
 * Features intelligent state-machine AI (Patrol, Chase, Attack, Search, Return),
 * dynamic spectral floating physics, trailing ectoplasm particles, glowing blood-red
 * tracking eyes, a screaming void jaw, reaching skeletal claws, and terrifying audio cues.
 */
public class Ghost {

    public enum State {IDLE, PATROL, SEARCH, CHASE, ATTACK, RETURN}

    private double x;
    private double y;
    private final double spawnX;
    private final double spawnY;
    private State state = State.IDLE;

    private double speedIdle = 14.0;
    private double speedPatrol = 30.0;
    private double speedChase = 105.0;

    private double stateTimer = 0.0;
    private double searchTimer = 0.0;
    private double attackCooldown = 0.0;
    private double screechCooldown = 0.0;
    private double heartbeatTimer = 0.0;
    private double lifeTime = 0.0;
    private double chasePersistenceTimer = 0.0;
    private double stunTimer = 0.0;
    private double fleeTimer = 0.0;
    private double fleeFromX = 0.0;
    private double fleeFromY = 0.0;
    private double pacifyTimer = 0.0;

    private double lastKnownPlayerX;
    private double lastKnownPlayerY;

    private double patrolTargetX;
    private double patrolTargetY;

    private boolean facingLeft = false;
    private int currentDirectionMode = 0; // 0: Down (Forward), 1: Left, 2: Right, 3: Up (Backward)
    public enum GhostType {
        PREY("/images/ghost/transparent_ghost_prey_sprite.png"),
        STUDENT("/images/ghost/transparent_ghost_student_sprite.png"),
        TEACHER("/images/ghost/transparent_ghost_teacher_sprite.png");

        private final String resourcePath;
        private javafx.scene.image.Image cachedImage;

        GhostType(String path) {
            this.resourcePath = path;
        }

        public javafx.scene.image.Image getImage() {
            if (cachedImage == null) {
                try {
                    var is = Ghost.class.getResourceAsStream(resourcePath);
                    if (is != null) {
                        cachedImage = new javafx.scene.image.Image(is);
                    }
                } catch (Exception ignored) {}
            }
            return cachedImage;
        }
    }

    private final GhostType ghostType;
    private final GhostAnimation animation = new GhostAnimation();
    private GhostAnimation.State animationState = GhostAnimation.State.FLOAT;

    // Trailing mist and ectoplasm particles
    private final List<SpectralParticle> particles = new ArrayList<>();
    private final Random rng = new Random();

    public Ghost(double x, double y) {
        this(x, y, GhostType.values()[new Random().nextInt(GhostType.values().length)]);
    }

    public Ghost(double x, double y, GhostType type) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.ghostType = type != null ? type : GhostType.PREY;
        this.lifeTime = rng.nextDouble() * 10.0;
        pickNewPatrolTarget();
    }

    private void pickNewPatrolTarget() {
        double r = 140.0;
        double angle = rng.nextDouble() * Math.PI * 2.0;
        patrolTargetX = spawnX + Math.cos(angle) * r;
        patrolTargetY = spawnY + Math.sin(angle) * r;
    }

    public void update(double deltaSeconds, Player player, Game game) {
        lifeTime += deltaSeconds;
        animation.update(deltaSeconds, animationState);

        if (animation.isDisappeared()) {
            return;
        }

        if (stunTimer > 0.0) {
            stunTimer = Math.max(0.0, stunTimer - deltaSeconds);
            animationState = GhostAnimation.State.FLOAT;
            return;
        }

        if (fleeTimer > 0.0) {
            fleeTimer = Math.max(0.0, fleeTimer - deltaSeconds);
            animationState = GhostAnimation.State.CHASE;
            double fdx = x - fleeFromX;
            double fdy = y - fleeFromY;
            double flen = Math.hypot(fdx, fdy);
            if (flen > 0.1) {
                double targetX = x + (fdx / flen) * 450.0;
                double targetY = y + (fdy / flen) * 450.0;
                moveTowards(targetX, targetY, speedChase * 1.5, deltaSeconds);
            }
            return;
        }

        if (attackCooldown > 0.0) {
            attackCooldown = Math.max(0.0, attackCooldown - deltaSeconds);
        }
        if (screechCooldown > 0.0) {
            screechCooldown = Math.max(0.0, screechCooldown - deltaSeconds);
        }
        if (pacifyTimer > 0.0) {
            pacifyTimer = Math.max(0.0, pacifyTimer - deltaSeconds);
        }

        double dx = player.getCenterX() - x;
        double dy = player.getCenterY() - y;
        double distToPlayer = Math.hypot(dx, dy);
        double distToSpawn = Math.hypot(x - spawnX, y - spawnY);

        double detectionRadius = 230.0;
        boolean canSeePlayer = distToPlayer <= detectionRadius && pacifyTimer <= 0.0;

        // Emit spectral trail particles
        updateParticles(deltaSeconds, state == State.CHASE || state == State.ATTACK);

        switch (state) {
            case IDLE -> {
                animationState = GhostAnimation.State.FLOAT;
                stateTimer += deltaSeconds;
                if (canSeePlayer) {
                    triggerChase();
                } else if (stateTimer > 2.2) {
                    stateTimer = 0.0;
                    state = State.PATROL;
                }
            }
            case PATROL -> {
                animationState = GhostAnimation.State.PATROL;
                moveTowards(patrolTargetX, patrolTargetY, speedPatrol, deltaSeconds);
                if (Math.hypot(x - patrolTargetX, y - patrolTargetY) < 12.0) {
                    pickNewPatrolTarget();
                    state = State.IDLE;
                    stateTimer = 0.0;
                }
                if (canSeePlayer) {
                    triggerChase();
                }
            }
            case CHASE -> {
                animationState = GhostAnimation.State.CHASE;
                lastKnownPlayerX = player.getCenterX();
                lastKnownPlayerY = player.getCenterY();
                moveTowards(lastKnownPlayerX, lastKnownPlayerY, speedChase, deltaSeconds);

                // Proximity heartbeat tension cue
                if (distToPlayer < 95.0) {
                    heartbeatTimer += deltaSeconds;
                    if (heartbeatTimer >= 0.65) {
                        AudioManager.getInstance().playOneShot("heartbeat");
                        heartbeatTimer = 0.0;
                    }
                } else {
                    heartbeatTimer = 0.0;
                }

                if (distToPlayer <= 32.0) {
                    state = State.ATTACK;
                } else if (!canSeePlayer) {
                    chasePersistenceTimer += deltaSeconds;
                    if (chasePersistenceTimer > 4.2 || distToPlayer > 420.0) {
                        chasePersistenceTimer = 0.0;
                        searchTimer = 0.0;
                        state = State.SEARCH;
                    }
                } else {
                    chasePersistenceTimer = 0.0;
                }
            }
            case SEARCH -> {
                animationState = GhostAnimation.State.FLOAT;
                moveTowards(lastKnownPlayerX, lastKnownPlayerY, speedPatrol * 0.9, deltaSeconds);
                searchTimer += deltaSeconds;
                if (canSeePlayer) {
                    triggerChase();
                } else if (searchTimer > 5.0) {
                    state = State.RETURN;
                }
            }
            case ATTACK -> {
                animationState = GhostAnimation.State.ATTACK;
                if (attackCooldown <= 0.0) {
                    if (distToPlayer <= 42.0) {
                        player.loseHeart();
                        game.showNotification("The haunted spirit struck you! Hearts: " + player.getHearts());
                        AudioManager.getInstance().playOneShot("ghost");
                        if (player.isDead()) {
                            game.gameOver();
                        }
                        attackCooldown = 1.35;
                    }
                }
                // Menacing rebound / lunge float
                moveTowards(player.getCenterX(), player.getCenterY(), -25.0, deltaSeconds);
                if (distToPlayer > 48.0) {
                    state = State.CHASE;
                }
            }
            case RETURN -> {
                animationState = GhostAnimation.State.FLOAT;
                moveTowards(spawnX, spawnY, speedPatrol, deltaSeconds);
                if (distToSpawn < 14.0) {
                    state = State.IDLE;
                    pickNewPatrolTarget();
                }
                if (canSeePlayer) {
                    triggerChase();
                }
            }
        }
    }

    private void triggerChase() {
        state = State.CHASE;
        chasePersistenceTimer = 0.0;
        if (screechCooldown <= 0.0) {
            AudioManager.getInstance().playOneShot("ghost");
            screechCooldown = 6.0;
        }
    }

    private void updateDirection(double dirX, double dirY) {
        double absX = Math.abs(dirX);
        double absY = Math.abs(dirY);
        // Clean axis isolation:
        // 0: Down/Forward, 1: Left, 2: Right, 3: Up/Back
        if (absX > absY * 0.9) {
            currentDirectionMode = (dirX < 0) ? 1 : 2; // 1: Left, 2: Right
            facingLeft = (dirX < 0);
        } else if (absY > absX * 0.9) {
            currentDirectionMode = (dirY > 0) ? 0 : 3; // 0: Down/Forward, 3: Up/Back
        }
    }

    private void moveTowards(double tx, double ty, double spd, double deltaSeconds) {
        double dx = tx - x;
        double dy = ty - y;
        double dist = Math.hypot(dx, dy);
        if (dist < 1e-6) return;
        double dirX = dx / dist;
        double dirY = dy / dist;
        double moveSpeed = Math.abs(spd);
        if (spd < 0) {
            dirX = -dirX;
            dirY = -dirY;
        }
        updateDirection(dirX, dirY);
        x += dirX * moveSpeed * deltaSeconds;
        y += dirY * moveSpeed * deltaSeconds;
    }

    private void updateParticles(double deltaSeconds, boolean isHorrorMode) {
        // Spawn 1 particle every few frames
        if (particles.size() < 22 && rng.nextDouble() < 0.65) {
            double pX = x + (rng.nextDouble() * 24.0 - 12.0);
            double pY = y + 10.0 + (rng.nextDouble() * 16.0);
            double vx = (rng.nextDouble() * 16.0 - 8.0);
            double vy = - (12.0 + rng.nextDouble() * 20.0);
            double maxLife = 0.5 + rng.nextDouble() * 0.5;
            double r = isHorrorMode ? 0.9 : 0.2;
            double g = isHorrorMode ? 0.08 : 0.8;
            double b = isHorrorMode ? 0.15 : 0.75;
            particles.add(new SpectralParticle(pX, pY, vx, vy, maxLife, r, g, b));
        }

        Iterator<SpectralParticle> it = particles.iterator();
        while (it.hasNext()) {
            SpectralParticle p = it.next();
            p.age += deltaSeconds;
            if (p.age >= p.maxLife) {
                it.remove();
            } else {
                p.x += p.vx * deltaSeconds;
                p.y += p.vy * deltaSeconds;
            }
        }
    }

    public void render(GraphicsContext g, double cameraX, double cameraY) {
        render(g, cameraX, cameraY, x, y, 99999.0, false);
    }

    public void render(GraphicsContext g, double cameraX, double cameraY, double playerX, double playerY, double lightRadius) {
        render(g, cameraX, cameraY, playerX, playerY, lightRadius, false);
    }

    public void render(GraphicsContext g, double cameraX, double cameraY, double playerX, double playerY, double lightRadius, boolean nightVisionActive) {
        if (animation.isDisappeared()) {
            return;
        }

        double distToPlayer = Math.hypot(x - playerX, y - playerY);
        double edgeFade = 1.0;

        if (nightVisionActive) {
            // Thermal spectral goggles reveal ghost signature across darkness
            edgeFade = 1.0;
        } else {
            // Ghost invisibility in darkness: Ghosts outside the player's light cover are completely hidden
            if (distToPlayer > lightRadius + 25.0) {
                return;
            }

            // Smooth fade-in as ghost enters the illuminated radius
            if (distToPlayer > lightRadius - 35.0) {
                edgeFade = Math.max(0.0, 1.0 - (distToPlayer - (lightRadius - 35.0)) / 60.0);
            }
        }

        boolean isAggressive = (state == State.CHASE || state == State.ATTACK);
        double scale = animation.getScale();
        double alpha = animation.getOpacity() * edgeFade;
        if (alpha <= 0.02) {
            return;
        }

        // Sinusoidal floating and sway
        double hoverY = Math.sin(lifeTime * 3.8) * 6.5;
        double swayX = Math.cos(lifeTime * 2.2) * 3.0;

        // Subtle paranormal glitch micro-jitter during chase
        if (isAggressive && rng.nextDouble() < 0.2) {
            swayX += (rng.nextDouble() - 0.5) * 2.5;
            hoverY += (rng.nextDouble() - 0.5) * 2.0;
        }

        double cx = x - cameraX + swayX;
        double cy = y - cameraY + hoverY;

        // 1. Render trailing spectral particles
        renderParticles(g, cameraX, cameraY);

        // 2. Ominous Ground Shadow
        g.setFill(Color.rgb(5, 2, 8, 0.38 * alpha));
        g.fillOval(cx - 16 * scale, y - cameraY + 28, 32 * scale, 10 * scale);

        // 3. Haunting Necrotic / Spectral Aura (Pulsing Outer Glow)
        double pulse = animation.getPulse();
        if (isAggressive) {
            // Blood-crimson & black necrotic aura
            double auraR = (38 + pulse * 14) * scale;
            g.setFill(Color.rgb(180, 10, 20, 0.14 * alpha));
            g.fillOval(cx - auraR, cy - auraR, auraR * 2, auraR * 2);
            g.setFill(Color.rgb(130, 5, 15, 0.24 * alpha));
            g.fillOval(cx - auraR * 0.75, cy - auraR * 0.75, auraR * 1.5, auraR * 1.5);
        } else {
            // Sickly cyan-violet graveyard mist aura
            double auraR = (32 + pulse * 8) * scale;
            g.setFill(Color.rgb(30, 180, 160, 0.12 * alpha));
            g.fillOval(cx - auraR, cy - auraR, auraR * 2, auraR * 2);
            g.setFill(Color.rgb(70, 30, 100, 0.20 * alpha));
            g.fillOval(cx - auraR * 0.7, cy - auraR * 0.7, auraR * 1.4, auraR * 1.4);
        }

        if (nightVisionActive) {
            // Emerald tactical thermal ghost outline & radar reticle
            double nvR = (42 + pulse * 10) * scale;
            g.setFill(Color.rgb(30, 255, 120, 0.22));
            g.fillOval(cx - nvR, cy - nvR * 0.85, nvR * 2, nvR * 1.7);
            g.setStroke(Color.rgb(50, 255, 130, 0.88));
            g.setLineWidth(1.8);
            g.strokeOval(cx - nvR, cy - nvR * 0.85, nvR * 2, nvR * 1.7);
            g.strokeLine(cx - nvR - 8, cy, cx - nvR + 4, cy);
            g.strokeLine(cx + nvR - 4, cy, cx + nvR + 8, cy);
            g.strokeLine(cx, cy - nvR * 0.85 - 8, cx, cy - nvR * 0.85 + 4);
            g.strokeLine(cx, cy + nvR * 0.85 - 4, cx, cy + nvR * 0.85 + 8);
        }

        // 4. Render Real Ghost Sprite Sheet animation with clean left/right separation
        javafx.scene.image.Image sprite = ghostType.getImage();
        if (sprite != null) {
            int spriteRow;
            boolean flipHorizontal = false;

            switch (currentDirectionMode) {
                case 1 -> {
                    // Left: Row 2 is cleanly facing left across all frames
                    spriteRow = 2;
                    flipHorizontal = false;
                }
                case 2 -> {
                    // Right: Row 2 flipped horizontally guarantees clean right-facing in all frames
                    spriteRow = 2;
                    flipHorizontal = true;
                }
                case 3 -> {
                    // Up / Backward: Row 3
                    spriteRow = 3;
                    flipHorizontal = false;
                }
                default -> {
                    // Down / Forward: Row 0
                    spriteRow = 0;
                    flipHorizontal = false;
                }
            }

            int frameIndex = ((int) (lifeTime * 5.5)) % 4;
            double frameW = sprite.getWidth() / 4.0;
            double frameH = sprite.getHeight() / 4.0;
            double srcX = frameIndex * frameW;
            double srcY = spriteRow * frameH;

            double drawW = 54.0 * scale;
            double drawH = 72.0 * scale;
            double drawX = cx - drawW / 2.0;
            double drawY = cy - drawH + 24.0 * scale;

            g.save();
            g.setGlobalAlpha(Math.max(0.0, Math.min(1.0, alpha * 0.95)));
            if (flipHorizontal) {
                g.translate(drawX + drawW, drawY);
                g.scale(-1.0, 1.0);
                g.drawImage(sprite, srcX, srcY, frameW, frameH, 0, 0, drawW, drawH);
            } else {
                g.drawImage(sprite, srcX, srcY, frameW, frameH, drawX, drawY, drawW, drawH);
            }
            g.restore();
        } else {
            renderShroud(g, cx, cy, scale, alpha, isAggressive);
            renderClaws(g, cx, cy, scale, alpha, isAggressive);
            renderFace(g, cx, cy, scale, alpha, isAggressive);
        }

        // 7. Attack Blood Slash FX
        if (state == State.ATTACK) {
            renderSlashEffect(g, cx, cy, scale, alpha);
        }

        // 8. Stunned Holy Stars / Halo FX
        if (stunTimer > 0.0) {
            double orbitR = 18.0 * scale;
            double starBaseY = cy - 46.0 * scale;
            for (int i = 0; i < 3; i++) {
                double a = lifeTime * 6.0 + i * (Math.PI * 2.0 / 3.0);
                double starX = cx + Math.cos(a) * orbitR;
                double starY = starBaseY + Math.sin(a) * (orbitR * 0.45);
                g.setFill(Color.rgb(255, 230, 80, 0.95));
                g.fillOval(starX - 3.5, starY - 3.5, 7, 7);
                g.setStroke(Color.rgb(255, 255, 255, 0.9));
                g.setLineWidth(1.0);
                g.strokeOval(starX - 3.5, starY - 3.5, 7, 7);
            }
        }
    }

    private void renderParticles(GraphicsContext g, double cameraX, double cameraY) {
        for (SpectralParticle p : particles) {
            double pRatio = 1.0 - (p.age / p.maxLife);
            double size = (3.5 + (1.0 - pRatio) * 3.5);
            g.setFill(Color.color(p.r, p.g, p.b, Math.max(0.0, Math.min(1.0, pRatio * 0.6))));
            g.fillOval(p.x - cameraX - size / 2.0, p.y - cameraY - size / 2.0, size, size);
        }
    }

    private void renderShroud(GraphicsContext g, double cx, double cy, double scale, double alpha, boolean isAggressive) {
        // Main Robe / Shroud Silhouette
        Color robeOuter = isAggressive
                ? Color.rgb(42, 12, 22, 0.92 * alpha)
                : Color.rgb(20, 24, 38, 0.88 * alpha);
        Color robeCore = isAggressive
                ? Color.rgb(18, 4, 8, 0.95 * alpha)
                : Color.rgb(10, 12, 20, 0.92 * alpha);
        Color robeHighlight = isAggressive
                ? Color.rgb(190, 30, 45, 0.55 * alpha)
                : Color.rgb(90, 140, 175, 0.50 * alpha);

        // Outer body fill
        double bw = 24.0 * scale;
        double bh = 34.0 * scale;
        g.setFill(robeOuter);
        g.fillRoundRect(cx - bw / 2.0, cy - 14 * scale, bw, bh, 18 * scale, 18 * scale);

        // Dark shadowy inner core
        g.setFill(robeCore);
        g.fillRoundRect(cx - (bw - 6) / 2.0, cy - 12 * scale, bw - 6, bh - 4, 14 * scale, 14 * scale);

        // Ragged, flowing wisp tendrils at the base (6 undulating strips)
        int stripCount = 6;
        double stripWidth = (bw / stripCount);
        double baseY = cy + bh - 16 * scale;

        for (int i = 0; i < stripCount; i++) {
            double sx = (cx - bw / 2.0) + i * stripWidth;
            // dynamic wavy tail motion
            double wave = Math.sin(lifeTime * 5.2 + i * 1.15) * (7.0 + (isAggressive ? 5.0 : 2.0));
            double tailLen = (14.0 + (i % 2 == 0 ? 8.0 : 3.0) + Math.cos(lifeTime * 3.5 + i) * 3.0) * scale;

            double[] xPoints = {
                    sx,
                    sx + stripWidth + 0.5,
                    sx + stripWidth / 2.0 + wave,
            };
            double[] yPoints = {
                    baseY,
                    baseY,
                    baseY + tailLen
            };

            g.setFill(robeOuter);
            g.fillPolygon(xPoints, yPoints, 3);

            // Shading highlight on tendril tips
            g.setFill(robeHighlight);
            g.fillOval(sx + stripWidth / 2.0 + wave - 2, baseY + tailLen - 3, 4, 4);
        }

        // Spectral rim highlight stroke
        g.setStroke(robeHighlight);
        g.setLineWidth(1.4 * scale);
        g.strokeRoundRect(cx - bw / 2.0, cy - 14 * scale, bw, bh * 0.75, 18 * scale, 18 * scale);
    }

    private void renderClaws(GraphicsContext g, double cx, double cy, double scale, double alpha, boolean isAggressive) {
        if (!isAggressive) {
            // Dormant pale ghostly hands peeking from robe
            g.setFill(Color.rgb(180, 195, 205, 0.7 * alpha));
            g.fillOval(cx - 10 * scale, cy + 8 * scale, 4 * scale, 7 * scale);
            g.fillOval(cx + 6 * scale, cy + 8 * scale, 4 * scale, 7 * scale);
            return;
        }

        // Terrifying elongated reaching skeletal claws extending toward target
        double dir = facingLeft ? -1.0 : 1.0;
        double reach = animation.getClawReach();
        double armBaseX = cx + (dir * 8 * scale);
        double armBaseY = cy + 2 * scale;
        double clawTipX = armBaseX + (dir * (18 + reach * 12) * scale);
        double clawTipY = armBaseY + (4 + Math.sin(lifeTime * 7.0) * 3.0) * scale;

        // Skeletal arm bones
        g.setStroke(Color.rgb(215, 220, 225, 0.9 * alpha));
        g.setLineWidth(2.2 * scale);
        g.strokeLine(armBaseX, armBaseY, (armBaseX + clawTipX) / 2.0, armBaseY + 4 * scale);
        g.strokeLine((armBaseX + clawTipX) / 2.0, armBaseY + 4 * scale, clawTipX, clawTipY);

        // 3 razor-sharp claw fingers
        g.setStroke(Color.rgb(240, 245, 250, 0.95 * alpha));
        g.setLineWidth(1.5 * scale);
        g.strokeLine(clawTipX, clawTipY, clawTipX + dir * 6 * scale, clawTipY - 5 * scale);
        g.strokeLine(clawTipX, clawTipY, clawTipX + dir * 8 * scale, clawTipY);
        g.strokeLine(clawTipX, clawTipY, clawTipX + dir * 6 * scale, clawTipY + 5 * scale);

        // Blood/dark dripping tips
        g.setFill(Color.rgb(200, 15, 25, 0.9 * alpha));
        g.fillOval(clawTipX + dir * 6 * scale - 1, clawTipY - 6 * scale, 3 * scale, 3 * scale);
        g.fillOval(clawTipX + dir * 8 * scale - 1, clawTipY - 1, 3 * scale, 3 * scale);
        g.fillOval(clawTipX + dir * 6 * scale - 1, clawTipY + 4 * scale, 3 * scale, 3 * scale);
    }

    private void renderFace(GraphicsContext g, double cx, double cy, double scale, double alpha, boolean isAggressive) {
        // Hood head cowl
        double hw = 22.0 * scale;
        double hh = 20.0 * scale;
        double headY = cy - 14 * scale;

        // Dark sunken cavern void inside hood
        g.setFill(Color.rgb(3, 1, 4, 0.98 * alpha));
        g.fillOval(cx - (hw - 4) / 2.0, headY, hw - 4, hh);

        // Gaping Screaming Maw / Distorted Jaw
        double jawOpen = animation.getJawOpen();
        if (isAggressive || jawOpen > 0.05) {
            double mw = (7.0 + jawOpen * 5.0) * scale;
            double mh = (6.0 + jawOpen * 11.0) * scale;
            double my = headY + hh * 0.62;

            // Cavernous black-red open void
            g.setFill(Color.rgb(110, 5, 12, 0.95 * alpha));
            g.fillOval(cx - mw / 2.0, my, mw, mh);
            g.setFill(Color.rgb(0, 0, 0, 0.98 * alpha));
            g.fillOval(cx - (mw - 2) / 2.0, my + 1, mw - 2, mh - 2);

            // Razor-sharp needle fangs
            g.setFill(Color.rgb(240, 240, 220, 0.9 * alpha));
            g.fillPolygon(
                    new double[]{cx - 3 * scale, cx - 1.5 * scale, cx},
                    new double[]{my + 1, my + 4 * scale, my + 1}, 3
            );
            g.fillPolygon(
                    new double[]{cx, cx + 1.5 * scale, cx + 3 * scale},
                    new double[]{my + 1, my + 4 * scale, my + 1}, 3
            );
            g.fillPolygon(
                    new double[]{cx - 2 * scale, cx, cx + 2 * scale},
                    new double[]{my + mh - 1, my + mh - 4 * scale, my + mh - 1}, 3
            );
        } else {
            // Ominous subtle stitched / distorted mouth line
            g.setStroke(Color.rgb(50, 10, 15, 0.75 * alpha));
            g.setLineWidth(1.2 * scale);
            g.strokeLine(cx - 3.5 * scale, headY + hh * 0.75, cx + 3.5 * scale, headY + hh * 0.75);
        }

        // Piercing Glowing Blood-Red Eyes
        double eyeSpacing = 4.5 * scale;
        double eyeY = headY + hh * 0.38;
        double lookOffsetX = facingLeft ? -1.5 * scale : 1.5 * scale;

        drawHorrorEye(g, cx - eyeSpacing + lookOffsetX, eyeY, scale, alpha, isAggressive);
        drawHorrorEye(g, cx + eyeSpacing + lookOffsetX, eyeY, scale, alpha, isAggressive);
    }

    private void drawHorrorEye(GraphicsContext g, double ex, double ey, double scale, double alpha, boolean isAggressive) {
        double eyeR = (isAggressive ? 4.2 : 2.8) * scale;

        // Outer fiery glare
        g.setFill(Color.rgb(255, 20, 30, (isAggressive ? 0.45 : 0.25) * alpha));
        g.fillOval(ex - eyeR * 1.5, ey - eyeR * 1.5, eyeR * 3, eyeR * 3);

        // Intense crimson iris
        g.setFill(Color.rgb(255, 0, 20, 0.95 * alpha));
        g.fillOval(ex - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);

        // Blazing white-hot demonic center pinpoint
        double pupilR = eyeR * 0.38;
        g.setFill(Color.rgb(255, 255, 255, 1.0 * alpha));
        g.fillOval(ex - pupilR, ey - pupilR, pupilR * 2, pupilR * 2);

        // Crimson eye flame trail during chase
        if (isAggressive) {
            double trailDir = facingLeft ? 1.0 : -1.0;
            g.setStroke(Color.rgb(230, 15, 25, 0.5 * alpha));
            g.setLineWidth(1.2 * scale);
            g.strokeLine(ex, ey, ex + trailDir * 5.0 * scale, ey - 2.0 * scale);
        }
    }

    private void renderSlashEffect(GraphicsContext g, double cx, double cy, double scale, double alpha) {
        double dir = facingLeft ? -1.0 : 1.0;
        double sx = cx + dir * 14 * scale;
        double sy = cy;

        g.setStroke(Color.rgb(255, 30, 40, 0.85 * alpha));
        g.setLineWidth(3.0 * scale);
        g.strokeArc(sx - 16 * scale, sy - 16 * scale, 32 * scale, 32 * scale, facingLeft ? 130 : -50, 95, javafx.scene.shape.ArcType.OPEN);

        g.setStroke(Color.rgb(255, 255, 255, 0.9 * alpha));
        g.setLineWidth(1.2 * scale);
        g.strokeArc(sx - 14 * scale, sy - 14 * scale, 28 * scale, 28 * scale, facingLeft ? 135 : -45, 80, javafx.scene.shape.ArcType.OPEN);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public State getState() { return state; }
    public boolean isDisappeared() { return animation.isDisappeared(); }
    public void banish() { animation.update(0.0, com.khmerspirit.animation.GhostAnimation.State.DISAPPEAR); }
    public void stun(double seconds) { this.stunTimer = seconds; }
    public void scareAway(double fromX, double fromY, double seconds) {
        this.fleeTimer = seconds;
        this.fleeFromX = fromX;
        this.fleeFromY = fromY;
        this.state = State.PATROL;
    }
    public boolean isStunned() { return stunTimer > 0.0; }
    public void pacify(double seconds) {
        this.pacifyTimer = seconds;
        this.state = State.IDLE;
        this.chasePersistenceTimer = 0.0;
        this.searchTimer = 0.0;
    }
    public boolean isPacified() { return pacifyTimer > 0.0; }
    public GhostType getGhostType() { return ghostType; }

    /** Internal spectral particle structure for smoke and necrotic ember trails */
    private static class SpectralParticle {
        double x;
        double y;
        double vx;
        double vy;
        double age = 0.0;
        double maxLife;
        double r;
        double g;
        double b;

        SpectralParticle(double x, double y, double vx, double vy, double maxLife, double r, double g, double b) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.maxLife = maxLife;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
