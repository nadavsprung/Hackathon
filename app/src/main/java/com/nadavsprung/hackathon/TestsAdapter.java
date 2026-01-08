package com.nadavsprung.hackathon;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.TestViewHolder> {

    private List<TestModel> tests = new ArrayList<>();
    private OnTestClickListener listener;

    public interface OnTestClickListener {
        void onViewLeaderboard(TestModel test);
        void onShareTest(TestModel test);
    }

    public TestsAdapter(OnTestClickListener listener) {
        this.listener = listener;
    }

    public void setTests(List<TestModel> tests) {
        this.tests = tests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        TestModel test = tests.get(position);
        holder.bind(test, listener);
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTestTitle;
        TextView tvTestSubject;
        TextView tvTestQuestions;
        Button btnViewLeaderboard;
        Button btnShareTest;

        TestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTestTitle = itemView.findViewById(R.id.tv_test_title);
            tvTestSubject = itemView.findViewById(R.id.tv_test_subject);
            tvTestQuestions = itemView.findViewById(R.id.tv_test_questions);
            btnViewLeaderboard = itemView.findViewById(R.id.btn_view_leaderboard);
            btnShareTest = itemView.findViewById(R.id.btn_share_test);
        }

        void bind(TestModel test, OnTestClickListener listener) {
            tvTestTitle.setText(test.getTitle());
            tvTestSubject.setText("נושא: " + test.getSubject());
            int numQuestions = test.getQuestions() != null ? test.getQuestions().size() : 0;
            tvTestQuestions.setText(numQuestions + " שאלות");

            btnViewLeaderboard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewLeaderboard(test);
                }
            });

            btnShareTest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShareTest(test);
                }
            });
        }
    }
}