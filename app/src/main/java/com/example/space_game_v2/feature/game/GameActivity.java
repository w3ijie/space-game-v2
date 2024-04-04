package com.example.space_game_v2.feature.game;

import android.view.Gravity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.space_game_v2.R;
import com.example.space_game_v2.BackgroundMusicService;
import com.example.space_game_v2.feature.main.MainActivity;

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
        runOnUiThread(() -> {
            scrollingBackgroundView.stopGame();

            // Inflate the custom toast layout with 'null' as the parent view since it's going into a toast
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast_game_over, null);

            // Find and set up the "Return to Main Menu" button in the custom toast layout
            Button returnToMenuButton = layout.findViewById(R.id.toast_button);
            returnToMenuButton.setOnClickListener(view -> returnToMainMenu());

            // Create and show the toast with the custom layout
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

            buttonApprove.setEnabled(false);
            buttonDisapprove.setEnabled(false);

            // Stop the music service
            stopService(new Intent(GameActivity.this, BackgroundMusicService.class));
        });
    }




    // Update the gameOver method to call endGame
    private void gameOver() {
        endGame();
        returnToMainMenu();
    }



    private void updateEconomyDisplay() {
        tvEconomy.setText("$" + scrollingBackgroundView.getEconomy());
    }

    private void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class); // Replace MainActivity.class with your main menu activity class
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
