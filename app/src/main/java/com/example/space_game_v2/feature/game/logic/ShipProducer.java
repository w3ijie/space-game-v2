package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

import com.example.space_game_v2.feature.game.elements.Spaceship;

public class ShipProducer extends Thread {
    private GameController game;

    public ShipProducer(GameController game) {
        this.game = game;
    }

    public void run() {

        // loop to add ships to the queue every 2 seconds until the queue is full
        while (!game.checkFull()) {

            // Add a spaceship to the queue
            game.addSpaceship(new Spaceship());

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
