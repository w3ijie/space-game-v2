package com.example.space_game_v2.feature.game.utils;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class GameEffects {

    public static void vibrate(Context context, long durationInMilliseconds) {
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            // for 500 milliseconds
            vibrator.vibrate(VibrationEffect.createOneShot(durationInMilliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            Log.d("VibrationEvent", "Alien spaceship appeared. Starting vibration.");
        }
    }
}
