package com.example.space_game_v2.feature.game.elements;

import java.util.Random;

public class Spaceship {
    public float x, y;
    public String type; // Add "alien" as a possible type

    // Existing constructor
    public Spaceship(int screenWidth, int shipWidth) {
        this.x = (float) (screenWidth - shipWidth) / 2;
        this.y = -shipWidth;
        this.type = new Random().nextBoolean() ? "money" : "bomb";
    }

    // New constructor for alien spaceship
    public Spaceship(float x, float y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
