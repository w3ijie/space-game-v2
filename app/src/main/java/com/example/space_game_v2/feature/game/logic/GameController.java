package com.example.space_game_v2.feature.game.logic;

import android.media.MediaPlayer;

import com.example.space_game_v2.feature.game.elements.Spaceship;

/**
 * Implement game logic here, handle spaceship management, economy, user interactions.
 * Interact with backgroundview here
 * Interact with other views such as spaceship, explosions etc
 */
public class GameController {
    private Queue spaceshipQueue;
    private MediaPlayer mediaPlayer;

    private int points = 0;
    private int hearts = 3;

    public GameController() {
        spaceshipQueue = new Queue(5);
    }

    public void addSpaceship(Spaceship spaceship) {
        spaceshipQueue.add(spaceship);
    }
    public void inspectSpaceship() {}

    public boolean checkFull() {return true;}

    public int getPoints() {return points;}

    public int getHearts() {return hearts;}

    public void updateAndRender() {
        // Update game state
        // Example: Move spaceships, check for collisions, etc.

        // Render updated state
//        backgroundView.invalidate(); // Assuming the background view handles its own rendering based on the game state
    }
}
