package com.example.space_game_v2.feature.leaderboard;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.space_game_v2.BuildConfig;
import com.example.space_game_v2.utils.JwtUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LeaderboardController {

    // async nature
    public static void getLeaderboardEntries(LeaderboardReadCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String baseURL = BuildConfig.SERVER_URL;
        String url = baseURL + "/api/v1/leaderboard?limit=100";

        String jwtToken = JwtUtils.generateSignedToken(BuildConfig.TOKEN);
        String authHeaderValue = "Bearer " + jwtToken;

        Log.i("LeaderboardAPI", "Fetching leaderboard");

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
                    // parse JSON and update RecyclerView
                    Gson gson = new Gson();
                    Type listOfEntriesType = new TypeToken<List<LeaderboardEntry>>() {}.getType();
                    List<LeaderboardEntry> leaderboardEntries = gson.fromJson(responseBody, listOfEntriesType);
                    callback.onSuccess(leaderboardEntries);
                    Log.i("LeaderboardAPI", "Response is successful");
                } else {
                    callback.onError("Response was not successful or body was null");
                    Log.e("LeaderboardAPI", "Response was not successful or body was null");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
                if (BuildConfig.DEBUG) {
                    Log.e("LeaderboardAPI", "Error fetching leaderboard data", e);
                }
            }
        });
    }

    public static void insertLeaderboardEntry(LeaderboardEntry entry, LeaderboardInsertCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // build url
        String baseURL = BuildConfig.SERVER_URL;
        String url = baseURL + "/api/v1/scores";

        // jwt
        String jwtToken = JwtUtils.generateSignedToken(BuildConfig.TOKEN);
        String authHeaderValue = "Bearer " + jwtToken;

        // convert entry to json
        Gson gson = new Gson();
        String json = gson.toJson(entry);
        Log.i("LeaderboardAPI", "insertLeaderboardEntry: " + json);

        // create a req body
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Log.i("LeaderboardAPI", "Insert entry into leaderboard");

        // build request for POST
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", authHeaderValue)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    callback.onSuccess(true);
                    Log.i("LeaderboardAPI", "Response is successful:" + responseBody);
                } else {
                    callback.onError("Request failed: " + response);
                    Log.e("LeaderboardAPI", "Error inserting leaderboard entry");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
                Log.e("LeaderboardAPI", "Error inserting leaderboard entry", e);
            }
        });

    }
}
