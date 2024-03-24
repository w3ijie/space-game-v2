package com.example.space_game_v2.feature.leaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.space_game_v2.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder> {
    List<LeaderboardEntry> leaderboardEntryList;

    public LeaderboardAdapter(List<LeaderboardEntry> items) {
        this.leaderboardEntryList = items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_record_view,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardEntryList.get(position);
        holder.usernameView.setText(entry.getUsername());
        holder.scoreView.setText(String.valueOf(entry.getScore()));
    }

    @Override
    public int getItemCount() {
        return leaderboardEntryList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView usernameView, scoreView;

        public MyViewHolder(View view) {
            super(view);
            usernameView = view.findViewById(R.id.username);
            scoreView = view.findViewById(R.id.score);
        }
    }
}
