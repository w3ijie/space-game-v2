package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

public class ShipInspector extends Thread {
    private Game game;

    public ShipInspector(Game game) {
        this.game = game;
    }

    public void run() {
        game.inspectSpaceship();
        Log.i("Ship Inspector", "ship inspector thread is done");
    }
}
