package com.example.space_game_v2.feature.game.logic;

public class Game {
    private Queue spaceshipQueue = new Queue(5);

    private int points = 0;
    private int hearts = 3;

    public void addSpaceship() {}
    public void inspectSpaceship() {}

    public boolean checkFull() {return true;}

    public int getPoints() {return points;}

    public int getHearts() {return hearts;}
}
