package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

public class ShipProducer extends Thread {
    private Game game;

    public ShipProducer(Game game) {
        this.game = game;
    }

    public void run() {

        // loop to add ships to the queue every 2 seconds until the queue is full
        while (!game.checkFull()) {

            // Add a spaceship to the queue
            game.addSpaceship();

            // Sleep for 2 seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e("Ship Producer", "Error", e);
            }
        }

        // If the queue is full, print a message and end the game
        Log.i("Ship Producer", "ship producer thread is done");
    }
}
