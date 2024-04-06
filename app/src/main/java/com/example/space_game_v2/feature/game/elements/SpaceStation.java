package com.example.space_game_v2.feature.game.elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.example.space_game_v2.R;

public class SpaceStation {
    private Bitmap scaledSpaceStationBitmap;

    private float spaceStationX, spaceStationY;
    private final int SCALE_FACTOR = 2;

    public SpaceStation(Context context, int screenWidth, int screenHeight) {
        Bitmap originalSpaceStationBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.space_station);

        // to scale
        scaledSpaceStationBitmap = Bitmap.createScaledBitmap(originalSpaceStationBitmap, originalSpaceStationBitmap.getWidth() / SCALE_FACTOR, originalSpaceStationBitmap.getHeight() / SCALE_FACTOR, false);
        originalSpaceStationBitmap.recycle();

        this.spaceStationX = (screenWidth - scaledSpaceStationBitmap.getWidth()) / 2f;
        this.spaceStationY = screenHeight - scaledSpaceStationBitmap.getHeight() / 2f;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);
    }

    public float getYOfSpaceStation() {
        return spaceStationY;
    }
}

