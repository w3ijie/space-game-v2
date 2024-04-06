package com.example.space_game_v2.feature.game.elements;

import java.util.Random;

public class Spaceship {
    private float x, y;
    public enum SpaceshipType {
        MONEY, BOMB, ALIEN
    }
    private SpaceshipType spaceshipType;
    private boolean isNew;

    public Spaceship() {
        this.spaceshipType = new Random().nextBoolean() ? SpaceshipType.MONEY : SpaceshipType.BOMB;
    }

    public Spaceship(SpaceshipType type) {
        this.spaceshipType = type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    public boolean isNew() {
        return isNew;
    }

    public SpaceshipType getSpaceshipType() {
        return spaceshipType;
    }
}
