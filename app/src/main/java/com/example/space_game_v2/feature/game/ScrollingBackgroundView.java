package com.example.space_game_v2.feature.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.space_game_v2.R;

public class ScrollingBackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread thread;
    private boolean isRunning = false;
    private Bitmap backgroundBitmap;
    private Bitmap scaledSpaceStationBitmap; // The scaled bitmap for the space station
    private float backgroundY = 0;
    private SurfaceHolder holder;
    private final int scrollSpeed = 3; // Adjust this value for different scroll speeds

    // Constructor used when creating the view programmatically
    public ScrollingBackgroundView(Context context) {
        super(context);
        init(context, null);
    }

    // Constructor used when inflating the view from XML
    public ScrollingBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Optional: Constructor used when inflating the view from XML with a style
    public ScrollingBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // Initialization method
    private void init(Context context, AttributeSet attrs) {
        holder = getHolder();
        holder.addCallback(this);

        // Load the background bitmap
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);

        // Load and scale the space station bitmap
        Bitmap originalSpaceStationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.space_station);
        int scaleFactor = 2; // Example scale factor
        int scaledWidth = originalSpaceStationBitmap.getWidth() / scaleFactor;
        int scaledHeight = originalSpaceStationBitmap.getHeight() / scaleFactor;
        scaledSpaceStationBitmap = Bitmap.createScaledBitmap(originalSpaceStationBitmap, scaledWidth, scaledHeight, false);
        originalSpaceStationBitmap.recycle(); // Recycle the original bitmap if it's no longer needed
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // Implement your logic here if needed when the surface size changes
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        isRunning = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Handle the exception
            }
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!holder.getSurface().isValid()) {
                continue;
            }

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                synchronized (holder) {
                    // Draw the scrolling background
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY, null);
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY - backgroundBitmap.getHeight(), null);

                    // Update the position of the background for scrolling effect
                    backgroundY += scrollSpeed;
                    if (backgroundY >= backgroundBitmap.getHeight()) {
                        backgroundY = 0;
                    }

                    // Calculate the x position to center the space station
                    float x = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;

                    // Calculate the y position so that only half of the space station is visible
                    // The y position will be the canvas height minus half of the space station's height
                    float y = canvas.getHeight() - (scaledSpaceStationBitmap.getHeight() / 2);

                    // Draw the scaled space station bitmap so that half is sticking out from the bottom
                    canvas.drawBitmap(scaledSpaceStationBitmap, x, y, null);

                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void pause() {
        isRunning = false;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                // Handle the exception
            }
        }
    }

    public void resume() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }
}
