package com.example.space_game_v2.feature.game.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

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
import java.util.Objects;
import java.util.Random;

/**
 * BackgroundView
 * - In charge of rendering the background images
 * - Uses Threads to draw to offload from the main UI
 * - Listens to GameController and render the state into UI
 */
public class BackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable, ExplosionEventListener {

    // bitmaps - render all the relevant assets
    private Bitmap backgroundBitmap;
    private Bitmap alienShipBitmap;
    private Bitmap scaledMoneyShipBitmap;
    private Bitmap bombShipBitmap;



    private SpaceStation spaceStation;
    private SpaceshipEventListener spaceshipEventListener;


    private float backgroundY = 0;
    private final int scrollSpeed = 3;


    private boolean isRunning = true;
    private List<Explosion> explosions = new ArrayList<>();


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
    }



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // draw the station once bg has been created because it needs to know
        spaceStation = new SpaceStation(getContext(), getWidth(), getHeight());

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        isRunning = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    @Override
    public void run() {

//        final int FPS = 60;
//        final long frameTime = 1000 / FPS;
//        long startTime, timeMillis, waitTime;

        SurfaceHolder holder = getHolder();

        while (isRunning) {
            if (!holder.getSurface().isValid()) {
                continue;
            }
//            startTime = System.nanoTime();

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                synchronized (holder) {
                    // canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                    updateAndDrawBackground(canvas);
                    if (spaceStation != null) {
                        spaceStation.draw(canvas);
                    }

                    float spaceStationY = spaceStation.getYOfSpaceStation();
                    drawSpaceships(canvas, spaceStationY);

                    drawExplosions(canvas);
                    drawAlienSpaceships(canvas);

                    GameController.getInstance().increaseSpaceshipSpeed();

                    holder.unlockCanvasAndPost(canvas);
                }
            }

//            timeMillis = (System.nanoTime() - startTime) / 1000000;
//            waitTime = frameTime - timeMillis;
//
//            try {
//                if (waitTime > 0) {
//                    Thread.sleep(waitTime);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }
    }

    // for game activity to call
    public void pauseDrawing() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                if (thread != null) {
                    thread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // for the game activity to call
    public void resumeDrawing() {
        if (!isRunning && getHolder().getSurface().isValid()) {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }


    private void updateAndDrawBackground(Canvas canvas) {
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
        float spaceshipSpeed = GameController.getInstance().getSpaceshipSpeed();

        for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext(); ) {
            Spaceship spaceship = iterator.next();
            Bitmap shipBitmap = getBitmapForSpaceship(spaceship);

            spaceship.setY(spaceship.getY() + spaceshipSpeed);
            spaceship.setX((canvas.getWidth() - shipBitmap.getWidth()) / 2f);

            float shipX = spaceship.getX();
            float shipY = spaceship.getY();

            canvas.drawBitmap(shipBitmap, shipX, shipY, null);

            if (shipY + shipBitmap.getHeight() >= spaceStationY) {
                iterator.remove();
                GameController.getInstance().nearestSpaceshipTouchedBase();
                if (spaceshipEventListener != null) {
                    spaceshipEventListener.onSpaceshipReachedBase(spaceship);
                }
            }
        }
    }

    private void loadMoneyShipBitmap() {
        // Load, scale, and set the money ship bitmap
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.moneyship);
        scaledMoneyShipBitmap = Bitmap.createScaledBitmap(original, original.getWidth() / 2, original.getHeight() / 2, false);
        original.recycle();
    }

    private void loadBombShipBitmap() {
        Bitmap originalBombShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombship);
        bombShipBitmap = Bitmap.createScaledBitmap(originalBombShipBitmap, originalBombShipBitmap.getWidth() / 2, originalBombShipBitmap.getHeight() / 2, false);
        originalBombShipBitmap.recycle();
    }

    private void loadAlienShipBitmap() {
        alienShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alien);
    }

    private Bitmap getBitmapForSpaceship(Spaceship spaceship) {
        if (Objects.requireNonNull(spaceship.getSpaceshipType()) == Spaceship.SpaceshipType.BOMB) {
            return bombShipBitmap;
        }
        return scaledMoneyShipBitmap;
    }

    public void setSpaceshipEventListener(SpaceshipEventListener listener) {
        this.spaceshipEventListener = listener;
    }

    @Override
    public void onExplosionTrigger(Spaceship spaceship) {
        float explosionX = spaceship.getX();
        float explosionY = spaceship.getY();
        explosions.add(new Explosion(getContext(), explosionX, explosionY));
    }

    private void drawExplosions(Canvas canvas) {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();

            if (!explosion.getStatus()) {
                explosionIterator.remove();
            } else {
                explosion.draw(canvas);
            }
        }
    }

    private void drawAlienSpaceships(Canvas canvas) {
        synchronized (GameController.getInstance().getAliens()) {
            for (Spaceship alienSpaceship : GameController.getInstance().getAliens()) {

                if (alienSpaceship.getX() == 0f) {
                    Random random = new Random();
                    int x = random.nextInt(getWidth() - alienShipBitmap.getWidth());
                    int y = random.nextInt(getHeight() - alienShipBitmap.getHeight());
                    alienSpaceship.setX(x);
                    alienSpaceship.setY(y);
                }
                canvas.drawBitmap(alienShipBitmap, alienSpaceship.getX(), alienSpaceship.getY(), null);

                // trigger vibration if new
                if (alienSpaceship.isNew()) {
                    GameEffects.vibrate(getContext(), 500);
                    playEvilLaughSound();
                    alienSpaceship.setIsNew(false);
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            synchronized (GameController.getInstance().getAliens()) {
                Iterator<Spaceship> iterator = GameController.getInstance().getAliens().iterator();

                while (iterator.hasNext()) {
                    Spaceship spaceship = iterator.next();

                    // bounding box of the current spaceship
                    float left = spaceship.getX();
                    float top = spaceship.getY();
                    float right = left + alienShipBitmap.getWidth();
                    float bottom = top + alienShipBitmap.getHeight();

                    // touch coordinates are within the bounding box of the spaceship
                    if (touchX >= left && touchX <= right && touchY >= top && touchY <= bottom) {
                        // within the spaceship bounds, so remove the spaceship
                        iterator.remove();
                        break;
                    }
                }
            }

            return true;
        }

        return super.onTouchEvent(event);
    }

    private void playEvilLaughSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.evil_laugh);
        mediaPlayer.setVolume(230, 230);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }

}
