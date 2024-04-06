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
 *
 */
public class BackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Bitmap backgroundBitmap;
    private SpaceStation spaceStation;

    private float backgroundY = 0;
    private final int scrollSpeed = 3;
    private boolean isRunning = true;
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
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);}


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // draw the station once bg has bee created
        spaceStation = new SpaceStation(getContext(), getWidth(), getHeight());

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

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
                        Log.i("Background View", "Drawing space station");
                    }
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

}