package com.example.space_game_v2.feature.game.logic;


import android.util.Log;
import android.widget.Space;

import com.example.space_game_v2.feature.game.elements.Spaceship;
import com.example.space_game_v2.feature.game.elements.ExplosionEventListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** GameController
 * - Implement game logic here, handle spaceship management, economy, user interactions.
 * - Interact with backgroundview here
 * - Interact with other views such as spaceship, explosions etc
 * - Interacts with ShipProducer through a queue that it listens to
 */
public class GameController {
    // implement as singleton
    private static GameController instance;

    private boolean isGameActive = true;
    private boolean isGamePaused = false;


    private Queue spaceshipQueue;
    private List<Spaceship> aliens;
    private int points;
    private int hearts;

    private int spaceshipSpeed = 1;
    private long lastSpeedIncreaseTime = System.currentTimeMillis();
    private final long SPEED_INCREASE_INTERVAL = 2000;


    private final int POINTS_PER_SPACESHIP = 100;
    private final int MAX_QUEUE_SIZE = 6;
    private ExplosionEventListener explosionEventListener;
    private ShipProducer shipProducer;
    private final ScheduledExecutorService alienScheduler = Executors.newSingleThreadScheduledExecutor();


    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    private GameController() {
        spaceshipQueue = new Queue(MAX_QUEUE_SIZE);
        aliens = new ArrayList<>();
        points = 0;
        hearts = 3;
    }

    public void setExplosionEventListener(ExplosionEventListener listener) {
        this.explosionEventListener = listener;
    }

    public void addSpaceship(Spaceship spaceship) {
        spaceshipQueue.add(spaceship);
    }

    public boolean checkFull() {
        return spaceshipQueue.size() >= MAX_QUEUE_SIZE;
    }

    public void approveNearestSpaceship() {
        if (!spaceshipQueue.isEmpty()) {
            // queue removes the first in
            Spaceship current = spaceshipQueue.remove();

            if (current.getSpaceshipType() == Spaceship.SpaceshipType.BOMB) {
                decrementHeart();
            } else if (current.getSpaceshipType() == Spaceship.SpaceshipType.MONEY) {
                points += POINTS_PER_SPACESHIP;
            }
        }
    }

    public void disapproveNearestSpaceship() {
        if (!spaceshipQueue.isEmpty()) {
            Spaceship nearestSpaceship = spaceshipQueue.remove();

            if (nearestSpaceship.getSpaceshipType() == Spaceship.SpaceshipType.MONEY) {
                decrementHeart();
            } else if (nearestSpaceship.getSpaceshipType() == Spaceship.SpaceshipType.BOMB) {
                triggerExplosion(nearestSpaceship);
            }
        }
    }

    public void triggerExplosion(Spaceship spaceship) {
        if (explosionEventListener != null) {
            explosionEventListener.onExplosionTrigger(spaceship);
        }
    }

    public synchronized List<Spaceship> getCurrentSpaceships() {
        return new ArrayList<>(spaceshipQueue.getAll());
    }

    public void nearestSpaceshipTouchedBase() {

        if (!spaceshipQueue.isEmpty()) {
            spaceshipQueue.remove();
            Log.d("GameController", "Spaceship has crashed into the base.");
            decrementHeart();
        }
    }

    public boolean isGamePaused() {
        return isGamePaused;
    }
    public boolean isGameActive() {
        return isGameActive;
    }

    public void startGame() {
        isGamePaused = false;
        isGameActive = true;
        points = 0;
        hearts = 3;
        spaceshipSpeed = 1;
        startShipProduction();
        startAlienProduction();
    }

    public void resumeGame() {
        isGamePaused = false;
        lastSpeedIncreaseTime = System.currentTimeMillis();
        startShipProduction();
        startAlienProduction();
    }

    public void endGame() {
        isGameActive = false;
        stopShipProduction();
        stopAlienProduction();
        spaceshipQueue.clear();
        aliens.clear();
        points = 0;
        hearts = 3;
        spaceshipSpeed = 1;
    }

    public void pauseGame() {
        isGamePaused = true;
        stopShipProduction();
        stopAlienProduction();
    }

    public void startShipProduction() {
        if (shipProducer == null || !shipProducer.isAlive()) {
            shipProducer = new ShipProducer(this);
            shipProducer.start();
        }
    }

    public void stopShipProduction() {
        if (shipProducer != null) {
            shipProducer.interrupt();
        }
    }


    public synchronized List<Spaceship> getAliens() {
        return aliens;
    }

    public void addAliens() {
        Spaceship alienSpaceship = new Spaceship(Spaceship.SpaceshipType.ALIEN);
        alienSpaceship.setIsNew(true);
        synchronized (aliens) {
            aliens.add(alienSpaceship);
        }
    }

    public void startAlienProduction() {
        alienScheduler.scheduleAtFixedRate(() -> {
            if (isGameActive && !isGamePaused) {
                try {
                    addAliens();
                    Log.i("GameController", "Alien added");
                } catch (Exception e) {
                    Log.e("GameController", "Error adding alien", e);
                }
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    public void stopAlienProduction() {
        alienScheduler.shutdownNow();
    }

    // In GameController
    public void decrementHeart() {
        if (hearts > 0) {
            hearts--;
        }
    }

    public int getPoints() {
        return points;
    }

    public int getHearts() {
        return hearts;
    }


    public void increaseSpaceshipSpeed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeedIncreaseTime >= SPEED_INCREASE_INTERVAL) {
            spaceshipSpeed++;
            lastSpeedIncreaseTime = currentTime;
        }
    }

    public int getSpaceshipSpeed() {
        return spaceshipSpeed;
    }

}
