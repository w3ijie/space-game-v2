package com.example.space_game_v2.feature.game.elements;

import java.util.Random;

public class Spaceship {
    private float x, y;
    private float velocity = 1;
    public String type;

    public Spaceship(float velocity) {
        this.type = new Random().nextBoolean() ? "money" : "bomb";
        this.velocity = velocity;
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

    public float getVelocity() {
        return velocity;
    }

}
