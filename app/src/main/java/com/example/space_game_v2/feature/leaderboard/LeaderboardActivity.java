package com.example.space_game_v2.feature.leaderboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.space_game_v2.BuildConfig;
import com.example.space_game_v2.R;
import com.example.space_game_v2.utils.JwtUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LeaderboardActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> items = new ArrayList<>();

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

        recyclerView = findViewById(R.id.recyclerview_leaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(items);
        recyclerView.setAdapter(adapter);

        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        OkHttpClient client = new OkHttpClient();
        String baseURL = BuildConfig.SERVER_URL;
        String url = baseURL + "/api/v1/leaderboard?limit=100";

        String jwtToken = JwtUtils.generateSignedToken(BuildConfig.TOKEN);
        String authHeaderValue = "Bearer " + jwtToken;

        Log.i("LeaderboardAPI","Fetching leaderboard");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", authHeaderValue)
                .build();

        // Asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseBody = response.body().string();
                    Log.i("LeaderboardAPI", "Call to leaderboard is successful");
                    // Parse JSON and update RecyclerView
                    Gson gson = new Gson();
                    Type listOfEntriesType = new TypeToken<List<LeaderboardEntry>>(){}.getType();
                    List<LeaderboardEntry> leaderboardEntries = gson.fromJson(responseBody, listOfEntriesType);

                    runOnUiThread(() -> {
                        // remove any items
                        items.clear();
                        // add the retrieved
                        items.addAll(leaderboardEntries);
                        adapter.notifyDataSetChanged();
                    });
                }else {
                    Log.e("LeaderboardAPI", "Response was not successful or body was null");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e("LeaderboardAPI", "Error fetching leaderboard data", e);
                }
            }
        });
    }
}