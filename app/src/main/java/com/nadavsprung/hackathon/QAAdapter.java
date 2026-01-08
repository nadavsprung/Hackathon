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

public class QAAdapter extends RecyclerView.Adapter<QAAdapter.QAViewHolder> {

    private List<QAModel> qaList = new ArrayList<>();

    public void setQAList(List<QAModel> qaList) {
        this.qaList = qaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qa, parent, false);
        return new QAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QAViewHolder holder, int position) {
        QAModel qa = qaList.get(position);
        holder.bind(qa);
    }

    @Override
    public int getItemCount() {
        return qaList.size();
    }

    static class QAViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        TextView tvAnswer;
        TextView tvUploader;
        TextView tvDate;

        QAViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_qa_question);
            tvAnswer = itemView.findViewById(R.id.tv_qa_answer);
            tvUploader = itemView.findViewById(R.id.tv_qa_uploader);
            tvDate = itemView.findViewById(R.id.tv_qa_date);
        }

        void bind(QAModel qa) {
            tvQuestion.setText("שאלה: " + qa.getQuestion());
            tvAnswer.setText("תשובה: " + qa.getAnswer());
            tvUploader.setText("הועלה על ידי: " + qa.getUserName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(qa.getCreatedAt())));
        }
    }
}
