package com.nadavsprung.hackathon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardEntry> entries = new ArrayList<>();

    public void setEntries(List<LeaderboardEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.bind(entry, position + 1);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvStudentName;
        TextView tvTestInfo;
        TextView tvScore;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvTestInfo = itemView.findViewById(R.id.tv_test_info);
            tvScore = itemView.findViewById(R.id.tv_score);
        }

        void bind(LeaderboardEntry entry, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvStudentName.setText(entry.getStudentName());
            tvTestInfo.setText("מבחן: " + entry.getTestName());
            tvScore.setText(entry.getScore() + "%");
        }
    }
}