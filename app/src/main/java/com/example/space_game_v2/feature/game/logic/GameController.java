package com.example.space_game_v2.feature.game.logic;


import com.example.space_game_v2.feature.game.elements.Spaceship;
import com.example.space_game_v2.feature.game.elements.ExplosionEventListener;

import java.util.ArrayList;
import java.util.List;

/** GameController
 * - Implement game logic here, handle spaceship management, economy, user interactions.
 * - Interact with backgroundview here
 * - Interact with other views such as spaceship, explosions etc
 * - Interacts with ShipProducer through a queue that it listens to
 */
public class GameController {
    // implement as singleton
    private static GameController instance;

    private Queue spaceshipQueue;
    private int points;
    private int hearts;

    private final int POINTS_PER_SPACESHIP = 100;
    private final int MAX_QUEUE_SIZE = 6;
    private ExplosionEventListener explosionEventListener;


    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    private GameController() {
        spaceshipQueue = new Queue(MAX_QUEUE_SIZE);
        resetSpaceshipQueue();
    }

    public void setExplosionEventListener(ExplosionEventListener listener) {
        this.explosionEventListener = listener;
    }

    public void resetSpaceshipQueue() {
        points = 0;
        hearts = 3;
    }

    public void addSpaceship(Spaceship spaceship) {
        spaceshipQueue.add(spaceship);
    }
    public void inspectSpaceship() {}

    public boolean checkFull() {
        return spaceshipQueue.size() >= MAX_QUEUE_SIZE;
    }

    public boolean approveNearestSpaceship() {
        if (!spaceshipQueue.isEmpty()) {
            // queue removes the first in
            Spaceship current = spaceshipQueue.remove();

            if ("bomb".equals(current.type)) {
                return false;
            } else if ("money".equals(current.type)) {
                points += POINTS_PER_SPACESHIP;
                return true;
            }
        }
        return true;
    }
    public boolean disapproveNearestSpaceship() {
        if (!spaceshipQueue.isEmpty()) {
            Spaceship nearestSpaceship = spaceshipQueue.remove();

            if ("money".equals(nearestSpaceship.type)) {
                return false;
            } else if ("bomb".equals(nearestSpaceship.type)) {
//                float explosionX = nearestSpaceship.x + bombShipBitmap.getWidth() / 2f;
//                float explosionY = nearestSpaceship.y + bombShipBitmap.getHeight() / 2f;
                if (explosionEventListener != null) {
                    explosionEventListener.onExplosionTrigger(nearestSpaceship);
                }
                return true;
            }
        }
        return true;
    }

    public synchronized List<Spaceship> getCurrentSpaceships() {
        return new ArrayList<>(spaceshipQueue.getAll());
    }

    public Spaceship renderSpaceship(int screenWidth, int shipWidth) {

        if (!spaceshipQueue.isEmpty()) {
            Spaceship ship = spaceshipQueue.remove();
            ship.setX((float) (screenWidth - shipWidth) / 2);
            ship.setY(-shipWidth);
            return ship;
        }
        return null;
    }



}
