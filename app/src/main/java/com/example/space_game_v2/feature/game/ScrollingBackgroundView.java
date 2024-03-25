package com.example.space_game_v2.feature.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.*;
import com.example.space_game_v2.R;

public class ScrollingBackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread thread;
    private boolean isRunning = false;
    private Bitmap backgroundBitmap;
    private Bitmap scaledSpaceStationBitmap; // The scaled bitmap for the space station
    private float backgroundY = 0;
    private SurfaceHolder holder;
    private final int scrollSpeed = 3; // Adjust this value for different scroll speeds

    private Bitmap bombShipBitmap, moneyShipBitmap;
    private List<Spaceship> spaceships;
    private int spaceshipSpeed = 1; // Initial speed of the spaceship
    private long lastSpawnTime = System.currentTimeMillis(); // Last spawn time for a spaceship

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


        // Load spaceship bitmaps
        bombShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombship);
        moneyShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneyship);

        // Initialize the spaceship list
        spaceships = new ArrayList<>();
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
                    // Clear the canvas
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

                    // Draw the scrolling background
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY, null);
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY - backgroundBitmap.getHeight(), null);

                    // Update the position of the background for scrolling effect
                    backgroundY += scrollSpeed;
                    if (backgroundY >= backgroundBitmap.getHeight()) {
                        backgroundY = 0;
                    }

                    // Draw the scaled space station bitmap at the bottom of the screen, centered
                    float spaceStationX = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;
                    // Draw the space station half above the bottom edge of the screen
                    float spaceStationY = canvas.getHeight() - (scaledSpaceStationBitmap.getHeight() / 2);
                    canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);

                    // Add new spaceship at random positions and reset spawn time
                    if (System.currentTimeMillis() - lastSpawnTime >= 2000) {
                        spaceships.add(new Spaceship(canvas.getWidth()));
                        lastSpawnTime = System.currentTimeMillis();
                        spaceshipSpeed += 1; // Increase the speed for the next spaceship
                    }

                    // Iterate over spaceships and draw them
                    for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext();) {
                        Spaceship spaceship = iterator.next();
                        spaceship.y += spaceshipSpeed; // Move the spaceship downwards

                        // Choose the bitmap based on the type of spaceship
                        Bitmap shipBitmap = spaceship.type.equals("money") ? moneyShipBitmap : bombShipBitmap;
                        float shipX = (canvas.getWidth() - shipBitmap.getWidth()) / 2; // Center the spaceship horizontally
                        float shipY = spaceship.y; // Current y position of spaceship

                        // Draw the spaceship
                        canvas.drawBitmap(shipBitmap, shipX, shipY, null);

                        // Remove spaceship when it reaches the base
                        // Here, the ship disappears as it reaches the top edge of the space station bitmap
                        if (shipY > spaceStationY - shipBitmap.getHeight()) {
                            iterator.remove();
                        }
                    }

                    // Unlock and post the canvas content
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

    private class Spaceship {
        public float x, y;
        public String type; // "money" for money ship, "bomb" for bomb ship

        public Spaceship(int screenWidth) {
            this.x = (screenWidth - bombShipBitmap.getWidth()) / 2; // Center the spaceship
            this.y = -bombShipBitmap.getHeight(); // Start above the screen
            this.type = new Random().nextBoolean() ? "money" : "bomb"; // Randomize type
        }
    }
}
