package com.example.space_game_v2.feature.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import com.example.space_game_v2.R;

// Make sure Spaceship class is accessible to other classes
class Spaceship {
    public float x, y;
    public String type; // "money" for money ship, "bomb" for bomb ship

    public Spaceship(int screenWidth, int shipWidth) {
        this.x = (screenWidth - shipWidth) / 2; // Center the spaceship
        this.y = -shipWidth; // Start above the screen
        this.type = new Random().nextBoolean() ? "money" : "bomb"; // Randomize type
    }
}

// Listener interface for spaceship events
interface SpaceshipEventListener {
    void onSpaceshipReachedBase(Spaceship spaceship);
}

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

        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);

        Bitmap originalSpaceStationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.space_station);
        int scaleFactor = 2;
        int scaledWidth = originalSpaceStationBitmap.getWidth() / scaleFactor;
        int scaledHeight = originalSpaceStationBitmap.getHeight() / scaleFactor;
        scaledSpaceStationBitmap = Bitmap.createScaledBitmap(originalSpaceStationBitmap, scaledWidth, scaledHeight, false);
        originalSpaceStationBitmap.recycle();

        bombShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombship);
        moneyShipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneyship);

        spaceships = new ArrayList<>();
        explosions = new ArrayList<>();
        explosionAnimation = (AnimationDrawable) ContextCompat.getDrawable(context, R.drawable.explosion_animation);
        explosionAnimation.setOneShot(true);
        explosionAnimation.setVisible(false, false);
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
        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY, null);
                    canvas.drawBitmap(backgroundBitmap, 0, backgroundY - backgroundBitmap.getHeight(), null);
                    backgroundY += scrollSpeed;
                    if (backgroundY >= backgroundBitmap.getHeight()) {
                        backgroundY = 0;
                    }

                    float spaceStationX = (canvas.getWidth() - scaledSpaceStationBitmap.getWidth()) / 2;
                    float spaceStationY = canvas.getHeight() - (scaledSpaceStationBitmap.getHeight() / 2f);
                    canvas.drawBitmap(scaledSpaceStationBitmap, spaceStationX, spaceStationY, null);

                    if (System.currentTimeMillis() - lastSpawnTime >= 2000) {
                        spaceships.add(new Spaceship(canvas.getWidth(), bombShipBitmap.getWidth()));
                        lastSpawnTime = System.currentTimeMillis();
                        spaceshipSpeed++;
                    }

                    for (Iterator<Spaceship> iterator = spaceships.iterator(); iterator.hasNext();) {
                        Spaceship spaceship = iterator.next();
                        spaceship.y += spaceshipSpeed;

                        Bitmap shipBitmap = spaceship.type.equals("money") ? moneyShipBitmap : bombShipBitmap;
                        float shipX = spaceship.x;
                        float shipY = spaceship.y;

                        canvas.drawBitmap(shipBitmap, shipX, shipY, null);

                        if (shipY > spaceStationY - shipBitmap.getHeight()) {
                            if (spaceshipEventListener != null) {
                                spaceshipEventListener.onSpaceshipReachedBase(spaceship);
                            }
                            iterator.remove();
                        }
                    }

                    Iterator<Explosion> explosionIterator = explosions.iterator();
                    while (explosionIterator.hasNext()) {
                        Explosion explosion = explosionIterator.next();
                        explosion.update();
                        explosion.draw(canvas);

                        if (!explosion.isActive) {
                            explosionIterator.remove();
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
            }
        }
    }

    public void resume() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

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
                    isActive = false;
                }
            }
        }

        public void draw(Canvas canvas) {
            if (!isActive) return;

            Drawable frame = animationDrawable.getFrame(currentFrame);
            frame.setBounds((int) x, (int) y, (int) (x + frame.getIntrinsicWidth()), (int) (y + frame.getIntrinsicHeight()));
            frame.draw(canvas);
        }
    }
}
