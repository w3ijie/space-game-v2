package com.example.space_game_v2.feature.leaderboard;

public interface LeaderboardInsertCallback {
    void onSuccess(Boolean isInserted);
    void onError(String errorMessage);
}