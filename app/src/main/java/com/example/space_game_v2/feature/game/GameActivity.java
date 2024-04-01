package com.example.space_game_v2.feature.game;

import android.view.Gravity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.space_game_v2.R;
import com.example.space_game_v2.BackgroundMusicService;
import android.content.Intent;


public class GameActivity extends AppCompatActivity implements SpaceshipEventListener {
    private ImageView heart1, heart2, heart3;
    private TextView tvEconomy;
    private ScrollingBackgroundView scrollingBackgroundView;
    private Button buttonApprove, buttonDisapprove;
    private int lives = 3; // Start with 3 lives

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        scrollingBackgroundView = findViewById(R.id.scrolling_background_view);
        tvEconomy = findViewById(R.id.tvEconomy);
        buttonApprove = findViewById(R.id.button_approve);
        buttonDisapprove = findViewById(R.id.button_disapprove);

        // Ensure ScrollingBackgroundView is ready before setting the listener
        if (scrollingBackgroundView != null) {
            scrollingBackgroundView.setSpaceshipEventListener(this);
        }

        buttonApprove.setOnClickListener(v -> approveSpaceship());
        buttonDisapprove.setOnClickListener(v -> disapproveSpaceship());
    }

    private void approveSpaceship() {
        boolean approvedCorrectly = scrollingBackgroundView.approveNearestSpaceship();
        if (!approvedCorrectly) {
            decrementLives(); // Decrement lives if a bomb ship is incorrectly approved
        }
        updateEconomyDisplay();
    }

    private void disapproveSpaceship() {
        boolean disapprovedCorrectly = scrollingBackgroundView.disapproveNearestSpaceship();
        if (!disapprovedCorrectly) {
            decrementLives(); // Decrement lives if a money ship is incorrectly disapproved
        }
    }
    @Override
    public void onSpaceshipReachedBase(Spaceship spaceship) {
        // This method is called from the game thread, UI updates must be run on the main thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                decrementLives();
            }
        });
    }

    private void decrementLives() {
        lives--;
        updateLivesDisplay();
        if (lives <= 0) {
            gameOver();
        }
    }

    private void updateLivesDisplay() {
        heart1.setVisibility(lives >= 1 ? View.VISIBLE : View.INVISIBLE);
        heart2.setVisibility(lives >= 2 ? View.VISIBLE : View.INVISIBLE);
        heart3.setVisibility(lives >= 3 ? View.VISIBLE : View.INVISIBLE);
    }

    private void endGame() {
        // Ensure this is called on the main thread as it will update the UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scrollingBackgroundView.stopGame();
                Toast toast = Toast.makeText(GameActivity.this, "Game Over, you've failed the Singaporean Economy", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                buttonApprove.setEnabled(false);
                buttonDisapprove.setEnabled(false);

                // Stop the music service
                stopService(new Intent(GameActivity.this, BackgroundMusicService.class));

                // Optional: If you want to finish the activity and return to the previous one.
                // finish();
            }
        });
    }


    // Update the gameOver method to call endGame
    private void gameOver() {
        endGame();
    }



    private void updateEconomyDisplay() {
        tvEconomy.setText("$" + scrollingBackgroundView.getEconomy());
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stop music when activity is not visible
        stopService(new Intent(this, BackgroundMusicService.class));
        scrollingBackgroundView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music when activity is back
        startService(new Intent(this, BackgroundMusicService.class));
        scrollingBackgroundView.resume();
    }

}
