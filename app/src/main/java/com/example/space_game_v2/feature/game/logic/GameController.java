package com.example.space_game_v2.feature.game.logic;


import android.content.Context;
import android.util.Log;

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
 * - Uses the singleton design pattern
 */
public class GameController {
    // implement as singleton
    private static GameController instance;
    private Context appContext; // Use application context to avoid memory leaks

    // this is used to know the state of the game when the user goes in and out of the activity
    private boolean isGameActive = true;
    private boolean isGamePaused = false;


    // a queue that is using the producer-consumer pattern
    private Queue spaceshipQueue;
    // a list that is used to insert into by scheduled executor
    private List<Spaceship> aliens;

    // states to manage
    private int points;
    private int hearts;

    // speed to increase over time for more dynamic game play
    private float spaceshipSpeed = 1;
    private long lastSpeedIncreaseTime = System.currentTimeMillis();


    private final int MAX_QUEUE_SIZE = 10;

    // listnener to update the game states from the state manager to the ui render
    // design pattern for code quality
    private ExplosionEventListener explosionEventListener;

    // producer pattern
    private ShipProducer shipProducer;

    // advanced android feature
    private final ScheduledExecutorService alienScheduler = Executors.newSingleThreadScheduledExecutor();

    // necessary for singleton
    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    // initialise the game state to be 0 at the start
    private GameController() {
        spaceshipQueue = new Queue(MAX_QUEUE_SIZE);
        aliens = new ArrayList<>();
        points = 0;
        hearts = 3;
    }

    // to add a listener
    public void setExplosionEventListener(ExplosionEventListener listener) {
        this.explosionEventListener = listener;
    }

    public void addSpaceship(Spaceship spaceship) {
        spaceshipQueue.add(spaceship);
    }

    // this is to check the pausing condition for the producer when queue is full
    public boolean checkFull() {
        return spaceshipQueue.size() >= MAX_QUEUE_SIZE;
    }

    // action to update the game state
    public void approveNearestSpaceship() {
        if (!spaceshipQueue.isEmpty()) {
            // queue removes the first in
            Spaceship current = spaceshipQueue.remove();

            if (current.getSpaceshipType() == Spaceship.SpaceshipType.BOMB) {
                decrementHeart();
            } else if (current.getSpaceshipType() == Spaceship.SpaceshipType.MONEY) {
                // defined the game points
                int POINTS_PER_SPACESHIP = 100;
                points += POINTS_PER_SPACESHIP;
            }
        }
    }

    // action to update the game state
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

    // inform the listener for an update of state
    public void triggerExplosion(Spaceship spaceship) {
        if (explosionEventListener != null) {
            explosionEventListener.onExplosionTrigger(spaceship);
        }
    }

    // synchronised for entering the critical section
    public synchronized List<Spaceship> getCurrentSpaceships() {
        return new ArrayList<>(spaceshipQueue.getAll());
    }

    // when the user failed to press any buttons on time
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

    // start the game by resetting all conditions and start the producers
    public void startGame() {
        isGamePaused = false;
        isGameActive = true;
        points = 0;
        hearts = 3;
        spaceshipSpeed = 1;
        startShipProduction();
        startAlienProduction();
    }

    // start the game but do not reset the state
    public void resumeGame() {
        isGamePaused = false;
        lastSpeedIncreaseTime = System.currentTimeMillis();
        startShipProduction();
        startAlienProduction();
    }

    // end the game by stopping everything and reset the states
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

    // pause the game by stopping the producers but do not reset the state
    public void pauseGame() {
        isGamePaused = true;
        stopShipProduction();
        stopAlienProduction();
    }

    // check if there is a producer attached before starting it
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

    // critical section
    public void addAliens() {
        Spaceship alienSpaceship = new Spaceship(Spaceship.SpaceshipType.ALIEN);
        alienSpaceship.setIsNew(true);
        synchronized (aliens) {
            aliens.add(alienSpaceship);
        }
    }

    // use of advanced android multithreading here
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

    // stop the production
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

    public void setPoints(int points) {
        this.points = points;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
    }
  
    // dynamic element to make the game fun, increase speed over time
    public void increaseSpaceshipSpeed() {
        long currentTime = System.currentTimeMillis();
        long SPEED_INCREASE_INTERVAL = 2000;
        float SPEED_INCREASE = 0.7f;
        if (currentTime - lastSpeedIncreaseTime >= SPEED_INCREASE_INTERVAL) {
            spaceshipSpeed += SPEED_INCREASE;
            lastSpeedIncreaseTime = currentTime;
        }
    }

    public float getSpaceshipSpeed() {
        return spaceshipSpeed;
    }

    public void setSpaceshipSpeed(int speed) {
        this.spaceshipSpeed = speed;
    }

}
