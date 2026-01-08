package com.nadavsprung.hackathon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubjectQAFragment extends Fragment {

    private RecyclerView recyclerQA;
    private TextView tvNoResults;
    private QAAdapter adapter;
    private FirebaseFirestore db;
    private String subject;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            subject = getArguments().getString("subject", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_qa, container, false);

        recyclerQA = view.findViewById(R.id.recycler_qa);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        adapter = new QAAdapter();
        recyclerQA.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerQA.setAdapter(adapter);

        loadQA();

        return view;
    }

    private void loadQA() {
        db.collection("qa")
                .whereEqualTo("subject", subject)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QAModel> qaList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        QAModel qa = doc.toObject(QAModel.class);
                        if (qa != null) {
                            qaList.add(qa);
                        }
                    }
                    // Sort by creation date (newest first)
                    qaList.sort((q1, q2) -> Long.compare(q2.getCreatedAt(), q1.getCreatedAt()));
                    adapter.setQAList(qaList);
                    updateNoResultsVisibility(qaList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    updateNoResultsVisibility(true);
                });
    }

    private void updateNoResultsVisibility(boolean show) {
        if (show) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerQA.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerQA.setVisibility(View.VISIBLE);
        }
    }
}
