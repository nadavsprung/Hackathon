package com.nadavsprung.hackathon;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private List<String> subjects;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String subject);
    }

    public SubjectsAdapter(Context context, List<String> subjects, OnItemClickListener listener) {
        this.context = context;
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        String subject = subjects.get(position);
        holder.button.setText(subject);

        // צבעים שונים לכל כפתור (אפשר להוסיף עוד צבעים אם רוצים)
        String[] colors = {"#FF8C00","#1E90FF","#32CD32","#8A2BE2","#FF1493","#00CED1","#FF4500",
                "#6A5ACD","#20B2AA","#DAA520","#FF69B4","#228B22","#1E90FF","#FF6347","#8B0000"};

        holder.button.setBackgroundColor(android.graphics.Color.parseColor(colors[position % colors.length]));

        holder.button.setOnClickListener(v -> listener.onItemClick(subject));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.subjectButton);
        }
    }
}

