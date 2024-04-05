package com.example.space_game_v2.feature.leaderboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.space_game_v2.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> items = new ArrayList<>();
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // initialise progress bar
        progressBar = findViewById(R.id.progressBar);
        // set visible first
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recyclerview_leaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(items);
        recyclerView.setAdapter(adapter);

        LeaderboardController.getLeaderboardEntries(new LeaderboardReadCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<LeaderboardEntry> leaderboardEntries) {
                runOnUiThread(() -> {
                    // remove any items
                    items.clear();
                    // add the retrieved
                    items.addAll(leaderboardEntries);
                    adapter.notifyDataSetChanged();
                    // hide bar
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(LeaderboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    // hide bar
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }
}