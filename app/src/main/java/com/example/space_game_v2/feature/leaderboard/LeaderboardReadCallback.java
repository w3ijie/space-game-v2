package com.example.space_game_v2.feature.leaderboard;

import java.util.List;

public interface LeaderboardReadCallback {
    void onSuccess(List<LeaderboardEntry> leaderboardEntries);
    void onError(String errorMessage);
}