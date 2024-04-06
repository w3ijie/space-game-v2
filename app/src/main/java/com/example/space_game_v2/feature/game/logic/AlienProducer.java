package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

import com.example.space_game_v2.feature.game.elements.Spaceship;

/** ShipProducer
 * - Sole focus is to produce spaceships
 */
public class AlienProducer extends Thread {
    private GameController game;

    public AlienProducer(GameController game) {
        this.game = game;
    }

    public void run() {

        while (game.isGameActive() && !game.isGamePaused()) {

            try {
                Thread.sleep(1000);
                game.addAliens();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("Alien Producer", "Thread was interrupted", e);
                break;
            }
        }
    }
}
