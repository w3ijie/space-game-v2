package com.example.space_game_v2.feature.leaderboard;

public class LeaderboardEntry {

    private int rank;
    private String username;
    private int score;

    public LeaderboardEntry(String username, int score, int rank) {
        this.username = username;
        this.score = score;
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }
}
