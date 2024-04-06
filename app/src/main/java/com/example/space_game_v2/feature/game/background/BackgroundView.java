package com.example.space_game_v2.feature.game.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.space_game_v2.R;
import com.example.space_game_v2.feature.game.elements.Explosion;
import com.example.space_game_v2.feature.game.elements.ExplosionEventListener;
import com.example.space_game_v2.feature.game.elements.SpaceStation;
import com.example.space_game_v2.feature.game.elements.Spaceship;
import com.example.space_game_v2.feature.game.elements.SpaceshipEventListener;
import com.example.space_game_v2.feature.game.logic.GameController;
import com.example.space_game_v2.feature.game.utils.GameEffects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BackgroundView
 * - In charge of rendering the background images
 * - Uses Threads to draw to offload from the main UI
 * - Listens to GameController and render the state into UI
 */
public class BackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // bitmaps - render all the relevant assets
    private Bitmap backgroundBitmap;
    private Bitmap alienShipBitmap;
    private Bitmap scaledMoneyShipBitmap; // Add this class member for the scaled money ship bitmap
    private Bitmap bombShipBitmap, moneyShipBitmap;



    private SpaceStation spaceStation;

    private float backgroundY = 0;
    private final int scrollSpeed = 3;


    private boolean isRunning = true;

    private ScheduledExecutorService scheduler;
    private Thread thread;


    public BackgroundView(Context context) {
        super(context);
        init(context, null);
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        getHolder().addCallback(this);

        // Load and set the background bitmap
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);
        loadMoneyShipBitmap();
        loadBombShipBitmap();
        loadAlienShipBitmap();

        scheduler = Executors.newSingleThreadScheduledExecutor();

    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // draw the station once bg has been created because it needs to know
        spaceStation = new SpaceStation(getContext(), getWidth(), getHeight());

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        boolean retry = true;
        isRunning = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public void run() {
        SurfaceHolder holder = getHolder();

        while (isRunning) {
            if (!holder.getSurface().isValid()) {
                continue;
            }

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                synchronized (holder) {
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                    updateAndDrawBackground(canvas);
                    if (spaceStation != null) {
                        spaceStation.draw(canvas);
                    }

                    float spaceStationY = spaceStation.getYOfSpaceStation(canvas);
                    drawSpaceships(canvas, spaceStationY);

                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }


    private void updateAndDrawBackground(Canvas canvas) {
        // Update the background position for the scrolling effect
        backgroundY += scrollSpeed;
        if (backgroundY >= backgroundBitmap.getHeight()) {
            backgroundY = 0;
        }
        // Draw the scrolling background
        canvas.drawBitmap(backgroundBitmap, 0, backgroundY, null);
        canvas.drawBitmap(backgroundBitmap, 0, backgroundY - backgroundBitmap.getHeight(), null);
    }


    private void drawSpaceships(Canvas canvas, float spaceStationY) {
        List<Spaceship> spaceships = GameController.getInstance().getCurrentSpaceships();
        for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext(); ) {
            Spaceship spaceship = iterator.next();

            spaceship.setY(spaceship.getY() + spaceship.getVelocity());
            float shipX = 0;
            float shipY = spaceship.getY();

            Bitmap shipBitmap = null;

            if ("money".equals(spaceship.type)) {
                shipBitmap = scaledMoneyShipBitmap;
                shipX = (canvas.getWidth() - shipBitmap.getWidth()) / 2; // Center the money ship
            } else if ("bomb".equals(spaceship.type)) {
                shipBitmap = bombShipBitmap;
                shipX = (canvas.getWidth() - shipBitmap.getWidth()) / 2f;
            }

//            Bitmap shipBitmap = getBitmapForSpaceship(spaceship);

            if (shipBitmap != null) {
                canvas.drawBitmap(shipBitmap, shipX, shipY, null);
            }
            if (shipY + shipBitmap.getHeight() >= spaceStationY) {
                iterator.remove(); // Remove spaceship from the list
//                if (spaceshipEventListener != null) {
//                    spaceshipEventListener.onSpaceshipReachedBase(spaceship);
//                }
            }
        }
    }

    private void loadMoneyShipBitmap() {
        // Load, scale, and set the money ship bitmap
        Bitmap originalMoneyShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneyship);
        int moneyShipScaleFactor = 2; // Adjust this factor to scale the money ship size
        scaledMoneyShipBitmap = Bitmap.createScaledBitmap(originalMoneyShipBitmap, originalMoneyShipBitmap.getWidth() / moneyShipScaleFactor, originalMoneyShipBitmap.getHeight() / moneyShipScaleFactor, false);
        originalMoneyShipBitmap.recycle(); // Recycle the original bitmap as it's no longer needed
    }

    private void loadBombShipBitmap() {
        // Load, scale, and set the bomb ship bitmap
        Bitmap originalBombShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombship);
        int bombShipScaleFactor = 2; // Adjust this factor to scale the bomb ship size
        bombShipBitmap = Bitmap.createScaledBitmap(originalBombShipBitmap, originalBombShipBitmap.getWidth() / bombShipScaleFactor, originalBombShipBitmap.getHeight() / bombShipScaleFactor, false);
        originalBombShipBitmap.recycle(); // Recycle the original bitmap as it's no longer needed
    }

    private void loadAlienShipBitmap() {
        alienShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alien);
    }

    private Bitmap getBitmapForSpaceship(Spaceship spaceship) {
        switch (spaceship.type) {
            case "money":
                return scaledMoneyShipBitmap;
            case "bomb":
                return bombShipBitmap;
            default:
                return null;
        }
    }


}
