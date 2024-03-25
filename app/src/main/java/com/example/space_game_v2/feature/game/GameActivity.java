package com.example.space_game_v2.feature.game;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.space_game_v2.R;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private ScrollingBackgroundView scrollingBackgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize your ScrollingBackgroundView
        scrollingBackgroundView = findViewById(R.id.scrolling_background_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the background scrolling when the activity is paused
        scrollingBackgroundView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the background scrolling when the activity is resumed
        scrollingBackgroundView.resume();
    }
}
