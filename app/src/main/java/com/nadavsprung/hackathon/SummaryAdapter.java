package com.nadavsprung.hackathon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {

    private List<SummaryModel> summaries = new ArrayList<>();

    public void setSummaries(List<SummaryModel> summaries) {
        this.summaries = summaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        SummaryModel summary = summaries.get(position);
        holder.bind(summary);
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvUploader;
        TextView tvDate;

        SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_summary_title);
            tvContent = itemView.findViewById(R.id.tv_summary_content);
            tvUploader = itemView.findViewById(R.id.tv_summary_uploader);
            tvDate = itemView.findViewById(R.id.tv_summary_date);
        }

        void bind(SummaryModel summary) {
            tvTitle.setText(summary.getTitle());
            
            // Show first 200 characters of content
            String content = summary.getContent();
            if (content.length() > 200) {
                content = content.substring(0, 200) + "...";
            }
            tvContent.setText(content);
            
            tvUploader.setText("הועלה על ידי: " + summary.getUserName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(summary.getCreatedAt())));
        }
    }
}
