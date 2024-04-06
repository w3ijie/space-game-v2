package com.example.space_game_v2.feature.game.elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.example.space_game_v2.R;

public class SpaceStation {
    private Bitmap scaledSpaceStationBitmap;
    private Bitmap gunBitmap;

    private float spaceStationX, spaceStationY;
    private float gunX, gunY;
    private final int SCALE_FACTOR = 2;

    public SpaceStation(Context context, int screenWidth, int screenHeight) {
        Bitmap originalSpaceStationBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.space_station);

        // to scale
        scaledSpaceStationBitmap = Bitmap.createScaledBitmap(originalSpaceStationBitmap, originalSpaceStationBitmap.getWidth() / SCALE_FACTOR, originalSpaceStationBitmap.getHeight() / SCALE_FACTOR, false);
        originalSpaceStationBitmap.recycle();

        // draw gun first
        gunBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.weapon);

        this.spaceStationX = (screenWidth - scaledSpaceStationBitmap.getWidth()) / 2f;
        this.spaceStationY = screenHeight - scaledSpaceStationBitmap.getHeight() + 200 + gunBitmap.getHeight();

        // Draw the turret (gun) on top of the base (assume the turret image is centered on the base image)
        this.gunX = spaceStationX + (scaledSpaceStationBitmap.getWidth() - 685 - (gunBitmap.getWidth()) / 2f);
        this.gunY = spaceStationY - gunBitmap.getHeight() / 2f; // Adjust this to position the turret correctly
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);
        canvas.drawBitmap(gunBitmap, gunX, gunY, null);
    }

    private float getYOfSpaceStation(Canvas canvas) {
        // Draw the space station
        float spaceStationX = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;
        float spaceStationY = canvas.getHeight() - scaledSpaceStationBitmap.getHeight() + 200 + gunBitmap.getHeight();
        canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);

        // Draw the turret (gun) on top of the base (assume the turret image is centered on the base image)
        float gunX = spaceStationX + (scaledSpaceStationBitmap.getWidth() - 685 - (gunBitmap.getWidth()) / 2);
        float gunY = spaceStationY - gunBitmap.getHeight() / 2f; // Adjust this to position the turret correctly
        canvas.drawBitmap(gunBitmap, gunX, gunY, null);

        return spaceStationY - gunBitmap.getHeight() / 2f;
    }
}

