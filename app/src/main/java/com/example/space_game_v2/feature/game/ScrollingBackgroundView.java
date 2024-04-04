package com.example.space_game_v2.feature.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.space_game_v2.R;
import android.media.MediaPlayer;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;


// Make sure Spaceship class is accessible to other classes
class Spaceship {
    public float x, y;
    public String type; // Add "alien" as a possible type

    // Existing constructor
    public Spaceship(int screenWidth, int shipWidth) {
        this.x = (screenWidth - shipWidth) / 2;
        this.y = -shipWidth;
        this.type = new Random().nextBoolean() ? "money" : "bomb";
    }

    // New constructor for alien spaceship
    public Spaceship(float x, float y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}


// Listener interface for spaceship events
interface SpaceshipEventListener {
    void onSpaceshipReachedBase(Spaceship spaceship);
}

public class ScrollingBackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Bitmap alienShipBitmap;
    private List<Spaceship> alienSpaceships = new ArrayList<>();
    private ScheduledExecutorService scheduler;

    private int economy = 0; // Starting economy value

    private MediaPlayer mediaPlayer;
    private Bitmap scaledMoneyShipBitmap; // Add this class member for the scaled money ship bitmap


    private Bitmap gunBitmap; // Add this as a class member
    private Thread thread;
    private boolean isRunning = false;
    private Bitmap backgroundBitmap;
    private Bitmap scaledSpaceStationBitmap; // The scaled bitmap for the space station
    private float backgroundY = 0;
    private SurfaceHolder holder;
    private final int scrollSpeed = 3; // Adjust this value for different scroll speeds

    private Bitmap bombShipBitmap, moneyShipBitmap;
    List<Explosion> explosions;
    AnimationDrawable explosionAnimation;

    private List<Spaceship> spaceships;
    private int spaceshipSpeed = 1; // Initial speed of the spaceship
    private long lastSpawnTime = System.currentTimeMillis(); // Last spawn time for a spaceship

    private SpaceshipEventListener spaceshipEventListener;

    public void setSpaceshipEventListener(SpaceshipEventListener listener) {
        this.spaceshipEventListener = listener;
    }

    public boolean approveNearestSpaceship() {
        if (!spaceships.isEmpty()) {
            Spaceship nearestSpaceship = spaceships.get(0);
            spaceships.remove(0);

            if ("bomb".equals(nearestSpaceship.type)) {
                return false;
            } else if ("money".equals(nearestSpaceship.type)) {
                economy += 100;
                return true;
            }
        }
        return true;
    }

    public int getEconomy() {
        return economy;
    }

    // New Explosion class that handles sprite sheet animation
    private class Explosion {
        private Bitmap spriteSheet;
        private int frameCount; // Set this based on sprite sheet
        private int frameWidth;
        private int frameHeight;
        private int currentFrame;
        private long lastFrameChangeTime;
        private long frameDuration; // Duration for each frame in milliseconds
        private boolean isActive;
        private float x;
        private float y;

        public Explosion(Context context, float x, float y) {
            this.spriteSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion_sprite);
            this.frameCount = 12;
            this.frameWidth = this.spriteSheet.getWidth() / frameCount;
            this.frameHeight = this.spriteSheet.getHeight();
            this.currentFrame = 0;
            this.lastFrameChangeTime = System.currentTimeMillis();
            this.isActive = true;
            this.x = x - frameWidth / 2f; // Center the explosion
            this.y = y - frameHeight / 2f; // Center the explosion
            this.frameDuration = 10;
        }

        public void update() {
            long time = System.currentTimeMillis();
            if (time > lastFrameChangeTime + frameDuration) {
                currentFrame++;
                lastFrameChangeTime = time;
                if (currentFrame >= frameCount) {
                    isActive = false;
                }
            }
        }

        public void draw(Canvas canvas) {
            if (!isActive) {
                return;
            }
            int frameX = currentFrame * frameWidth;
            Rect frameToDraw = new Rect(frameX, 0, frameX + frameWidth, frameHeight);
            RectF whereToDraw = new RectF(x, y, x + frameWidth, y + frameHeight);
            canvas.drawBitmap(spriteSheet, frameToDraw, whereToDraw, null);
        }

        public boolean isActive() {
            return isActive;
        }

    }


    public boolean disapproveNearestSpaceship() {
        if (!spaceships.isEmpty()) {
            Spaceship nearestSpaceship = spaceships.get(0);
            float explosionX = nearestSpaceship.x + bombShipBitmap.getWidth() / 2f;
            float explosionY = nearestSpaceship.y + bombShipBitmap.getHeight() / 2f;

            spaceships.remove(0);

            if ("money".equals(nearestSpaceship.type)) {
                return false;
            } else if ("bomb".equals(nearestSpaceship.type)) {
                triggerExplosion(explosionX, explosionY);
                return true;
            }
        }
        return true;
    }

    public ScrollingBackgroundView(Context context) {
        super(context);
        init(context, null);
    }

    public ScrollingBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollingBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    private void init(Context context, AttributeSet attrs) {
        holder = getHolder();
        holder.addCallback(this);

        // Initialize media player for background music
        mediaPlayer = MediaPlayer.create(context, R.raw.background_music);
        mediaPlayer.setLooping(true); // Music will loop
        mediaPlayer.setVolume(1.0f, 1.0f); // Set volume

        // Load and scale the gun bitmap
        gunBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.weapon);

        // Load and set the background bitmap
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);

        // Load, scale, and set the space station bitmap
        Bitmap originalSpaceStationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.space_station);
        int spaceStationScaleFactor = 2; // Adjust this factor to scale the space station size
        scaledSpaceStationBitmap = Bitmap.createScaledBitmap(originalSpaceStationBitmap, originalSpaceStationBitmap.getWidth() / spaceStationScaleFactor, originalSpaceStationBitmap.getHeight() / spaceStationScaleFactor, false);
        originalSpaceStationBitmap.recycle(); // Recycle the original bitmap as it's no longer needed

        // Load, scale, and set the money ship bitmap
        Bitmap originalMoneyShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneyship);
        int moneyShipScaleFactor = 2; // Adjust this factor to scale the money ship size
        scaledMoneyShipBitmap = Bitmap.createScaledBitmap(originalMoneyShipBitmap, originalMoneyShipBitmap.getWidth() / moneyShipScaleFactor, originalMoneyShipBitmap.getHeight() / moneyShipScaleFactor, false);
        originalMoneyShipBitmap.recycle(); // Recycle the original bitmap as it's no longer needed

        // Load, scale, and set the bomb ship bitmap
        Bitmap originalBombShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombship);
        int bombShipScaleFactor = 2; // Adjust this factor to scale the bomb ship size
        bombShipBitmap = Bitmap.createScaledBitmap(originalBombShipBitmap, originalBombShipBitmap.getWidth() / bombShipScaleFactor, originalBombShipBitmap.getHeight() / bombShipScaleFactor, false);
        originalBombShipBitmap.recycle(); // Recycle the original bitmap as it's no longer needed




        // Initialize lists for spaceships and explosions
        spaceships = new ArrayList<>();
        explosions = new ArrayList<>();

        // Load and set the explosion animation
        explosionAnimation = (AnimationDrawable) ContextCompat.getDrawable(context, R.drawable.explosion_animation);
        explosionAnimation.setOneShot(true); // The explosion animation should only play once




        //load alien bitmap
        alienShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alien);

        // Initialize the scheduler and schedule the alien spaceship spawning task
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Make sure to post the task on the UI thread
                post(new Runnable() {
                    @Override
                    public void run() {
                        spawnAlienSpaceship();
                    }
                });
            }
        }, 10, 10, TimeUnit.SECONDS); // Schedules the task to run every 10 seconds
    }





    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    public void stopGame() {
        isRunning = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            // Handle the exception
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
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

    // Trigger explosion method will now create a new Explosion object using the sprite sheet
    public void triggerExplosion(float x, float y) {
        explosions.add(new Explosion(getContext(), x, y));
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
                    updateAndDrawBackground(canvas);

                    // Draw the space station and the turret
                    float spaceStationY = drawSpaceStationAndTurret(canvas);

                    // Spawn and draw spaceships, check for base reach
                    updateAndDrawSpaceships(canvas, spaceStationY);

                    // Update and draw explosions
                    updateAndDrawExplosions(canvas);

                    drawAlienSpaceships(canvas);

                    // Release the canvas for the next frame
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

    private float drawSpaceStationAndTurret(Canvas canvas) {
        // Draw the space station
        float spaceStationX = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;
        float spaceStationY = canvas.getHeight() - scaledSpaceStationBitmap.getHeight() + 200 + gunBitmap.getHeight();
        canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);

        // Draw the turret (gun) on top of the base (assume the turret image is centered on the base image)
        float gunX = spaceStationX + (scaledSpaceStationBitmap.getWidth() - 685 - (gunBitmap.getWidth()) / 2);
        float gunY = spaceStationY - gunBitmap.getHeight() / 2f; // Adjust this to position the turret correctly
        canvas.drawBitmap(gunBitmap, gunX, gunY, null);

        return spaceStationY - gunBitmap.getHeight() / 2f; // Return the Y position where spaceship should disappear
    }
    private void updateAndDrawSpaceships(Canvas canvas, float spaceStationY) {
        // Check and spawn new spaceships
        if (System.currentTimeMillis() - lastSpawnTime >= 2000) {
            spaceships.add(new Spaceship(canvas.getWidth(), bombShipBitmap.getWidth()));
            lastSpawnTime = System.currentTimeMillis();
            spaceshipSpeed++; // Increase speed for difficulty
        }

        // Iterate over spaceships to update and draw
        for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext();) {
            Spaceship spaceship = iterator.next();
            spaceship.y += spaceshipSpeed;

            Bitmap shipBitmap = null;
            float shipX = 0;

            // Use the scaled bitmap for money ships and center it horizontally
            if ("money".equals(spaceship.type)) {
                shipBitmap = scaledMoneyShipBitmap;
                shipX = (canvas.getWidth() - shipBitmap.getWidth()) / 2; // Center the money ship
            } else if ("bomb".equals(spaceship.type)) {
                shipBitmap = bombShipBitmap;
                shipX = spaceship.x; // Use the x position of the bomb ship
            }

            float shipY = spaceship.y;

            // Draw the spaceship bitmap
            if (shipBitmap != null) {
                canvas.drawBitmap(shipBitmap, shipX, shipY, null);
            }

            // Check if the spaceship reaches the base
            if (shipY + shipBitmap.getHeight() >= spaceStationY) {
                iterator.remove(); // Remove spaceship from the list
                if (spaceshipEventListener != null) {
                    spaceshipEventListener.onSpaceshipReachedBase(spaceship);
                }
            }
        }
    }
    private void spawnAlienSpaceship() {
        Random random = new Random();
        int x = random.nextInt(getWidth() - alienShipBitmap.getWidth());
        int y = random.nextInt(getHeight() - alienShipBitmap.getHeight());
        // Use the new constructor
        Spaceship alienSpaceship = new Spaceship(x, y, "alien");
        alienSpaceships.add(alienSpaceship);
    }
    private void drawAlienSpaceships(Canvas canvas) {
        for (Spaceship alienSpaceship : alienSpaceships) {
            canvas.drawBitmap(alienShipBitmap, alienSpaceship.x, alienSpaceship.y, null);
        }
    }




    private void updateAndDrawExplosions(Canvas canvas) {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();
            explosion.draw(canvas);

            if (!explosion.isActive) {
                explosionIterator.remove(); // Remove the explosion if it is no longer active
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
            }
        }
    }

    public void resume() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    // Override the onTouchEvent method here
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Check if the event is a touch down action
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Get the x and y coordinates of the touch event
            float touchX = event.getX();
            float touchY = event.getY();

            // Create an iterator to safely remove items from the list while iterating
            Iterator<Spaceship> iterator = alienSpaceships.iterator();

            while (iterator.hasNext()) {
                Spaceship spaceship = iterator.next();

                // Calculate the bounding box of the current spaceship
                float left = spaceship.x;
                float top = spaceship.y;
                float right = left + alienShipBitmap.getWidth();
                float bottom = top + alienShipBitmap.getHeight();

                // Check if the touch coordinates are within the bounding box of the spaceship
                if (touchX >= left && touchX <= right && touchY >= top && touchY <= bottom) {
                    // Touch is within the spaceship bounds, so remove the spaceship
                    iterator.remove();

                    // You can add an explosion or sound effect here if desired

                    // Only remove one spaceship per touch event, so break the loop
                    break;
                }
            }

            // Indicate that the touch event was handled
            return true;
        }

        // Pass the event up to the parent class if it's not a touch down action
        return super.onTouchEvent(event);
    }

}
