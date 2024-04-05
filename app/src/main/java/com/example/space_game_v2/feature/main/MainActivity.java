package com.example.space_game_v2.feature.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.space_game_v2.BackgroundMusicService;
import com.example.space_game_v2.R;
import com.example.space_game_v2.feature.game.GameActivity;
import com.example.space_game_v2.feature.leaderboard.LeaderboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void accessScoreBoard(View v) {
        // Code to access scoreboard here
        startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
    }

    public void startGame(View view) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
        // Start music service here if not already playing
        startService(new Intent(this, BackgroundMusicService.class));
    }

}