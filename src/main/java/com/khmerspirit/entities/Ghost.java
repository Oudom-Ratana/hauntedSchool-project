package com.khmerspirit.entities;

import com.khmerspirit.animation.GhostAnimation;
import com.khmerspirit.core.Game;
import com.khmerspirit.player.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ghost {

    public enum State {IDLE, PATROL, SEARCH, CHASE, ATTACK, RETURN}

    private double x;
    private double y;
    private final double spawnX;
    private final double spawnY;
    private State state = State.IDLE;

    private double speedIdle = 10.0;
    private double speedPatrol = 25.0;
    private double speedChase = 80.0;

    private double stateTimer = 0.0;
    private double searchTimer = 0.0;
    private double attackCooldown = 0.0;

    private double lastKnownPlayerX;
    private double lastKnownPlayerY;

    private double patrolTargetX;
    private double patrolTargetY;

    private final GhostAnimation animation = new GhostAnimation();
    private GhostAnimation.State animationState = GhostAnimation.State.FLOAT;

    public Ghost(double x, double y) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        pickNewPatrolTarget();
    }

    private void pickNewPatrolTarget() {
        double r = 120.0;
        double angle = Math.random() * Math.PI * 2.0;
        patrolTargetX = spawnX + Math.cos(angle) * r;
        patrolTargetY = spawnY + Math.sin(angle) * r;
    }

    public void update(double deltaSeconds, Player player, Game game) {
        animation.update(deltaSeconds, animationState);
        
        if (animation.isDisappeared()) {
            return;
        }

        if (attackCooldown > 0.0) attackCooldown = Math.max(0.0, attackCooldown - deltaSeconds);

        double dx = player.getCenterX() - x;
        double dy = player.getCenterY() - y;
        double distToPlayer = Math.sqrt(dx * dx + dy * dy);
        double distToSpawn = Math.hypot(x - spawnX, y - spawnY);

        double detectionRadius = 160.0;
        boolean canSeePlayer = distToPlayer <= detectionRadius;

        switch (state) {
            case IDLE -> {
                animationState = GhostAnimation.State.FLOAT;
                stateTimer += deltaSeconds;
                if (canSeePlayer) {
                    state = State.CHASE;
                } else if (stateTimer > 2.0) {
                    stateTimer = 0.0;
                    state = State.PATROL;
                }
            }
            case PATROL -> {
                animationState = GhostAnimation.State.FLOAT;
                moveTowards(patrolTargetX, patrolTargetY, speedPatrol, deltaSeconds);
                if (Math.hypot(x - patrolTargetX, y - patrolTargetY) < 8.0) {
                    pickNewPatrolTarget();
                }
                if (canSeePlayer) {
                    state = State.CHASE;
                }
            }
            case CHASE -> {
                animationState = GhostAnimation.State.FLOAT;
                lastKnownPlayerX = player.getCenterX();
                lastKnownPlayerY = player.getCenterY();
                moveTowards(lastKnownPlayerX, lastKnownPlayerY, speedChase, deltaSeconds);
                if (distToPlayer <= 22.0) {
                    state = State.ATTACK;
                } else if (!canSeePlayer) {
                    searchTimer = 0.0;
                    state = State.SEARCH;
                }
            }
            case SEARCH -> {
                animationState = GhostAnimation.State.FLOAT;
                moveTowards(lastKnownPlayerX, lastKnownPlayerY, speedPatrol, deltaSeconds);
                searchTimer += deltaSeconds;
                if (canSeePlayer) {
                    state = State.CHASE;
                } else if (searchTimer > 5.0) {
                    state = State.RETURN;
                }
            }
            case ATTACK -> {
                animationState = GhostAnimation.State.ATTACK;
                if (attackCooldown <= 0.0) {
                    if (distToPlayer <= 30.0) {
                        player.loseHeart();
                        game.showNotification("You've been hit! Hearts: " + player.getHearts());
                        if (player.isDead()) {
                            game.gameOver();
                        }
                        attackCooldown = 1.2;
                    }
                }
                moveTowards(player.getCenterX(), player.getCenterY(), -20.0, deltaSeconds);
                if (distToPlayer > 40.0) state = State.CHASE;
            }
            case RETURN -> {
                animationState = GhostAnimation.State.FLOAT;
                moveTowards(spawnX, spawnY, speedPatrol, deltaSeconds);
                if (distToSpawn < 8.0) {
                    state = State.IDLE;
                    animationState = GhostAnimation.State.DISAPPEAR;
                }
                if (canSeePlayer) state = State.CHASE;
            }
        }
    }

    private void moveTowards(double tx, double ty, double spd, double deltaSeconds) {
        double dx = tx - x;
        double dy = ty - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1e-6) return;
        double dirX = dx / dist;
        double dirY = dy / dist;
        double moveSpeed = Math.abs(spd);
        if (spd < 0) {
            dirX = -dirX; dirY = -dirY;
        }
        x += dirX * moveSpeed * deltaSeconds;
        y += dirY * moveSpeed * deltaSeconds;
    }

    public void render(GraphicsContext g, double cameraX, double cameraY) {
        if (animation.isDisappeared()) {
            return;
        }

        double baseSize = 18.0;
        double scale = animation.getScale();
        double size = baseSize * scale;
        double alpha = animation.getOpacity();

        g.setFill(Color.rgb(200, 200, 255, Math.max(0.1, Math.min(0.95, alpha))));
        g.fillOval(x - cameraX - size/2.0, y - cameraY - size/2.0, size, size);
        g.setStroke(Color.rgb(140, 140, 200, alpha));
        g.setLineWidth(1.5 * scale);
        g.strokeOval(x - cameraX - size/2.0, y - cameraY - size/2.0, size, size);

        if (state == State.CHASE || state == State.ATTACK) {
            g.setFill(Color.rgb(20, 20, 30));
            g.fillOval(x - cameraX - 3, y - cameraY - 3, 6, 6);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public State getState() { return state; }
}
