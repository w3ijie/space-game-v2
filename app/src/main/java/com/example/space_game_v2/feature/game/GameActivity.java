package com.example.space_game_v2.feature.game;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.space_game_v2.R;
import com.example.space_game_v2.feature.game.audio.BackgroundMusicService;
import com.example.space_game_v2.feature.game.background.BackgroundView;
import com.example.space_game_v2.feature.game.elements.SpaceshipEventListener;
import com.example.space_game_v2.feature.game.logic.GameController;
import com.example.space_game_v2.feature.main.MainActivity;
import com.example.space_game_v2.feature.game.elements.Spaceship;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

public class GameActivity extends AppCompatActivity implements SpaceshipEventListener {
    private ImageView heart1, heart2, heart3;
    private TextView tvEconomy;
    private Button buttonApprove, buttonDisapprove;
    private BackgroundView backgroundView;
    private long lastLifeDecrementTime = 0;
    private static final long LIFE_DECREMENT_COOLDOWN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar if present (for activities with ActionBar).
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_game);

        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        tvEconomy = findViewById(R.id.tvEconomy);
        buttonApprove = findViewById(R.id.button_approve);
        buttonDisapprove = findViewById(R.id.button_disapprove);

        backgroundView = findViewById(R.id.background_view);
        backgroundView.setSpaceshipEventListener(this);
        GameController.getInstance().setExplosionEventListener(backgroundView);

        buttonApprove.setOnClickListener(v -> approveSpaceship());
        buttonDisapprove.setOnClickListener(v -> disapproveSpaceship());

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                GameController.getInstance().stopGame();
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        GameController.getInstance().startGame();
    }

    private void approveSpaceship() {
        GameController.getInstance().approveNearestSpaceship();
        runOnUiThread(() -> {
            updatePointsDisplay();
            updateHeartsDisplay();
        });

    }

    private void disapproveSpaceship() {
        GameController.getInstance().disapproveNearestSpaceship();
        runOnUiThread(this::updateHeartsDisplay);
    }
    @Override
    public void onSpaceshipReachedBase(Spaceship spaceship) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLifeDecrementTime >= LIFE_DECREMENT_COOLDOWN) {
            runOnUiThread(() -> {
                lastLifeDecrementTime = currentTime;
                decrementLives();
            });
        }
    }

    private void decrementLives() {
        updateHeartsDisplay();
    }

    private void updateHeartsDisplay() {
        // Fetch hearts from GameController
        int hearts = GameController.getInstance().getHearts();

        if (hearts <= 0) {
            gameOver();
        }

        heart1.setVisibility(hearts >= 1 ? View.VISIBLE : View.INVISIBLE);
        heart2.setVisibility(hearts >= 2 ? View.VISIBLE : View.INVISIBLE);
        heart3.setVisibility(hearts >= 3 ? View.VISIBLE : View.INVISIBLE);
    }


    private void updatePointsDisplay() {
        tvEconomy.setText("$" + String.valueOf(GameController.getInstance().getPoints()));
    }

    // Update the gameOver method to call endGame
    private void gameOver() {
        // Ensure this is called on the main thread as it will update the UI
        runOnUiThread(() -> {
            GameController.getInstance().endGame();
            buttonApprove.setEnabled(false);
            buttonDisapprove.setEnabled(false);

            // Stop the music service
            stopService(new Intent(GameActivity.this, BackgroundMusicService.class));

            // Show a dialog instead of a toast
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setTitle("Game Over");
            builder.setMessage("You've failed the Singaporean Economy.");
            builder.setPositiveButton("Return to Main Menu", (dialog, which) -> returnToMainMenu());
            builder.setCancelable(false); // Prevents the dialog from being dismissed on back press
            builder.show();
        });

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
        GameController.getInstance().stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music when activity is back
        startService(new Intent(this, BackgroundMusicService.class));

        if (!GameController.getInstance().isGameActive()) {
            GameController.getInstance().resumeGame();
        }

        Window window = getWindow();
        if (window != null) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, BackgroundMusicService.class));
        GameController.getInstance().stopGame();
    }


}
