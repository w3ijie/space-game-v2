package com.example.space_game_v2;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class BackgroundMusicService extends Service {

    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        // Replace with your music file
        player = MediaPlayer.create(this, R.raw.background_music);
        player.setLooping(true); // Set looping
        player.setVolume(100, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.background_music);
            player.setLooping(true); // Set looping
            player.setVolume(1.0f, 1.0f);
        }
        if (!player.isPlaying()) {
            player.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
