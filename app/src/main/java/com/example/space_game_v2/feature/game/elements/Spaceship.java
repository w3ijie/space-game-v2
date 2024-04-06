package com.example.space_game_v2.feature.game.elements;

import java.util.Random;

public class Spaceship {
    private float x, y;
    public String type;

    public Spaceship() {
        this.type = new Random().nextBoolean() ? "money" : "bomb";
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
}
