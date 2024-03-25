package com.example.space_game_v2.feature.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.core.content.ContextCompat;
import java.util.*;
import android.os.SystemClock;
import android.graphics.drawable.Drawable;

import com.example.space_game_v2.R;


public class ScrollingBackgroundView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private int economy = 0; // Starting economy value

    private Thread thread;
    private boolean isRunning = false;
    private Bitmap backgroundBitmap;
    private Bitmap scaledSpaceStationBitmap; // The scaled bitmap for the space station
    private float backgroundY = 0;
    private SurfaceHolder holder;
    private final int scrollSpeed = 3; // Adjust this value for different scroll speeds

    private Bitmap bombShipBitmap, moneyShipBitmap;
    // Declare the explosions list and explosionAnimation here
    List<Explosion> explosions;
    AnimationDrawable explosionAnimation;

    private List<Spaceship> spaceships;
    private int spaceshipSpeed = 1; // Initial speed of the spaceship
    private long lastSpawnTime = System.currentTimeMillis(); // Last spawn time for a spaceship
// Inside ScrollingBackgroundView class

    public boolean approveNearestSpaceship() {
        if (!spaceships.isEmpty()) {
            Spaceship nearestSpaceship = spaceships.get(0); // Assuming this is the nearest one
            spaceships.remove(0); // Remove the nearest spaceship

            if ("bomb".equals(nearestSpaceship.type)) {
                // Mistakenly approved a bombship
                return false;
            } else if ("money".equals(nearestSpaceship.type)) {
                economy += 100; // Increment economy for moneyship
                return true; // Correctly approved a moneyship
            }
        }
        return true; // Return true by default to not decrement a life if no spaceship is present
    }


    // Getter for economy value to be used in GameActivity
    public int getEconomy() {
        return economy;
    }

    public boolean disapproveNearestSpaceship() {
        if (!spaceships.isEmpty()) {
            Spaceship nearestSpaceship = spaceships.get(0); // Assuming this is the nearest one
            // Capture the spaceship's position before removing it, to use for explosion
            float explosionX = nearestSpaceship.x + bombShipBitmap.getWidth() / 2f;
            float explosionY = nearestSpaceship.y + bombShipBitmap.getHeight() / 2f;

            spaceships.remove(0); // Remove the nearest spaceship

            if ("money".equals(nearestSpaceship.type)) {
                // Mistakenly disapproved a moneyship
                return false;
            } else if ("bomb".equals(nearestSpaceship.type)) {
                // Trigger explosion at the bomb ship's location
                triggerExplosion(explosionX, explosionY);
                // Correctly disapproved a bombship
                return true;
            }
        }
        return true; // Return true by default to not decrement a life if no spaceship is present
    }




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



        // Initialize the explosions list
        explosions = new ArrayList<>();

        // Initialize the explosionAnimation
        explosionAnimation = (AnimationDrawable) ContextCompat.getDrawable(context, R.drawable.explosion_animation);
        if (explosionAnimation == null || ! (explosionAnimation instanceof AnimationDrawable)) {
            throw new AssertionError("Explosion animation drawable could not be loaded or is not an AnimationDrawable.");
        }

        explosionAnimation.setOneShot(true); // Assuming you want the animation to play only once
        explosionAnimation.setVisible(false, false); // Set the visibility to false initially
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
    public void stopGame() {
        isRunning = false;  // This will stop the run loop on the next iteration
        try {
            if (thread != null) {
                thread.join();  // Wait for the game loop thread to finish
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Optional: Reset game state if needed
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
    public void triggerExplosion(float x, float y) {
        AnimationDrawable animDrawable = (AnimationDrawable) ContextCompat.getDrawable(getContext(), R.drawable.explosion_animation);
        if (animDrawable != null) {
            animDrawable.start();
            explosions.add(new Explosion(animDrawable, x, y));
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

                    // Update the background position for the scrolling effect
                    backgroundY += scrollSpeed;
                    if (backgroundY >= backgroundBitmap.getHeight()) {
                        backgroundY = 0;
                    }

                    // Draw the space station at the bottom of the screen, centered
                    float spaceStationX = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;
                    // Draw the space station so that half of it is above the bottom edge of the screen
                    float spaceStationY = canvas.getHeight() - (scaledSpaceStationBitmap.getHeight() / 2f);
                    canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);

                    // Spawn new spaceships at intervals
                    if (System.currentTimeMillis() - lastSpawnTime >= 2000) {
                        spaceships.add(new Spaceship(canvas.getWidth()));
                        lastSpawnTime = System.currentTimeMillis();
                        spaceshipSpeed++; // Increment the spaceship speed for a gradual difficulty increase
                    }

                    // Iterate over and draw spaceships
                    for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext();) {
                        Spaceship spaceship = iterator.next();
                        spaceship.y += spaceshipSpeed;

                        Bitmap shipBitmap = spaceship.type.equals("money") ? moneyShipBitmap : bombShipBitmap;
                        float shipX = spaceship.x;
                        float shipY = spaceship.y;

                        canvas.drawBitmap(shipBitmap, shipX, shipY, null);

                        if (shipY > spaceStationY - shipBitmap.getHeight()) {
                            iterator.remove(); // Remove the spaceship when it reaches the space station
                        }
                    }

                    // Update and draw explosions
                    Iterator<Explosion> explosionIterator = explosions.iterator();
                    while (explosionIterator.hasNext()) {
                        Explosion explosion = explosionIterator.next();
                        explosion.update();
                        explosion.draw(canvas);

                        if (!explosion.isActive) {
                            explosionIterator.remove(); // Remove the explosion if it is no longer active
                        }
                    }

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


    // Inner class to handle explosion logic
    private class Explosion {
        private final AnimationDrawable animationDrawable;
        private final float x;
        private final float y;
        private boolean isActive;
        private int currentFrame = 0;
        private long lastFrameChangeTime = System.currentTimeMillis();

        public Explosion(AnimationDrawable animationDrawable, float x, float y) {
            this.animationDrawable = animationDrawable;
            this.x = x - animationDrawable.getIntrinsicWidth() / 2f;
            this.y = y - animationDrawable.getIntrinsicHeight() / 2f;
            this.isActive = true;
        }

        public void update() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameChangeTime > animationDrawable.getDuration(currentFrame)) {
                currentFrame++;
                lastFrameChangeTime = currentTime;
                if (currentFrame >= animationDrawable.getNumberOfFrames()) {
                    isActive = false; // Animation done
                }
            }
        }

        public void draw(Canvas canvas) {
            if (!isActive) return;

            Drawable frame = animationDrawable.getFrame(currentFrame);
            frame.setBounds((int) x, (int) y, (int) (x + frame.getIntrinsicWidth()), (int) (y + frame.getIntrinsicHeight()));
            frame.draw(canvas);
        }

        public boolean isActive() {
            return isActive;
        }
    }


}

