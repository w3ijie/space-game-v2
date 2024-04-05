package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

public class ShipInspector extends Thread {
    private GameController game;

    public ShipInspector(GameController game) {
        this.game = game;
    }

    public void run() {
        game.inspectSpaceship();
        Log.i("Ship Inspector", "ship inspector thread is done");
    }
}
