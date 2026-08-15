package com.khmerspirit.player;

import com.khmerspirit.audio.AudioManager;
import com.khmerspirit.config.Constants;
import com.khmerspirit.map.CollisionMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Player {

    private final String characterName;
    private final Image spriteSheet;
    private final PlayerAnimation animation;
    private double x;
    private double y;
    private int hearts = 5;
    private double invulnerableSeconds = 0.0;
    private double footstepAccumulator = 0.0;

    public Player(double x, double y, String characterName, Image spriteSheet) {
        this.x = x;
        this.y = y;
        this.characterName = characterName;
        this.spriteSheet = spriteSheet;
        this.animation = new PlayerAnimation();
    }

    public void update(double deltaSeconds, PlayerController controller, CollisionMap collisionMap) {
        double axisX = controller.getHorizontalAxis();
        double axisY = controller.getVerticalAxis();
        double length = Math.sqrt(axisX * axisX + axisY * axisY);

        if (length > 0.0) {
            axisX /= length;
            axisY /= length;
        }

        double velocityX = axisX * Constants.PLAYER_SPEED;
        double velocityY = axisY * Constants.PLAYER_SPEED;
        move(velocityX * deltaSeconds, velocityY * deltaSeconds, collisionMap);
        animation.update(deltaSeconds, velocityX, velocityY);

        if (length > 0.0) {
            footstepAccumulator += deltaSeconds;
            if (footstepAccumulator >= 0.36) {
                AudioManager.getInstance().playOneShot("footsteps");
                footstepAccumulator = 0.0;
            }
        } else {
            footstepAccumulator = 0.0;
        }

        if (invulnerableSeconds > 0.0) {
            invulnerableSeconds = Math.max(0.0, invulnerableSeconds - deltaSeconds);
        }
    }

    public void render(GraphicsContext graphics, Camera camera) {
        graphics.drawImage(
                spriteSheet,
                animation.getViewport().getMinX(),
                animation.getViewport().getMinY(),
                animation.getViewport().getWidth(),
                animation.getViewport().getHeight(),
                Math.round(x - camera.getX()),
                Math.round(y - camera.getY()),
                Constants.PLAYER_WIDTH,
                Constants.PLAYER_HEIGHT
        );
    }

    public double getCenterX() {
        return x + Constants.PLAYER_WIDTH / 2.0;
    }

    public double getCenterY() {
        return y + Constants.PLAYER_HEIGHT / 2.0;
    }

    public String getCharacterName() {
        return characterName;
    }

    public int getHearts() {
        return hearts;
    }

    public boolean isInvulnerable() {
        return invulnerableSeconds > 0.0;
    }

    public void loseHeart() {
        if (isInvulnerable()) return;
        hearts = Math.max(0, hearts - 1);
        invulnerableSeconds = 1.5; // short invulnerability after hit
        AudioManager.getInstance().playOneShot("heartbeat");
    }

    public void setHearts(int h) {
        hearts = Math.max(0, h);
    }

    public boolean isDead() {
        return hearts <= 0;
    }

    private void move(double deltaX, double deltaY, CollisionMap collisionMap) {
        double nextX = x + deltaX;
        if (!collisionMap.isBlocked(nextX, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT)) {
            x = nextX;
        }

        double nextY = y + deltaY;
        if (!collisionMap.isBlocked(x, nextY, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT)) {
            y = nextY;
        }
    }
}
