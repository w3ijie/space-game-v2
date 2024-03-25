package com.example.space_game_v2.feature.game;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.space_game_v2.R;
import android.widget.TextView;


public class GameActivity extends AppCompatActivity {
    private TextView tvEconomy;
    private ScrollingBackgroundView scrollingBackgroundView;
    private Button buttonApprove, buttonDisapprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize the ScrollingBackgroundView
        scrollingBackgroundView = findViewById(R.id.scrolling_background_view);

        // Initialize TextView for economy
        tvEconomy = findViewById(R.id.tvEconomy);

        // Initialize the approve and disapprove buttons
        buttonApprove = findViewById(R.id.button_approve);
        buttonDisapprove = findViewById(R.id.button_disapprove);


        // Set up the click listener for the approve button
        buttonApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic to approve the spaceship
                approveSpaceship();
            }
        });

        // Set up the click listener for the disapprove button
        buttonDisapprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic to disapprove the spaceship
                disapproveSpaceship();
            }
        });
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

    // Inside GameActivity class

    private void approveSpaceship() {
        scrollingBackgroundView.approveNearestSpaceship();
        updateEconomyDisplay();
    }

    private void disapproveSpaceship() {
        scrollingBackgroundView.disapproveNearestSpaceship();
    }
    // Method to update the economy display
    private void updateEconomyDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEconomy.setText("$" + scrollingBackgroundView.getEconomy());
            }
        });
    }
}

