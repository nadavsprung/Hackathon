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

        // Only load QA when fragment is visible
        if (getView() != null && isAdded() && getContext() != null) {
            loadQA();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload QA when fragment becomes visible
        if (isAdded() && getContext() != null) {
            loadQA();
        }
    }

    private void loadQA() {
        if (getContext() == null || !isAdded() || db == null || subject == null) {
            return;
        }

        try {
            db.collection("qa")
                    .whereEqualTo("subject", subject)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (getContext() == null || !isAdded()) {
                            return;
                        }
                        try {
                            List<QAModel> qaList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                QAModel qa = doc.toObject(QAModel.class);
                                if (qa != null) {
                                    qaList.add(qa);
                                }
                            }
                            // Sort by creation date (newest first)
                            qaList.sort((q1, q2) -> Long.compare(q2.getCreatedAt(), q1.getCreatedAt()));
                            if (adapter != null) {
                                adapter.setQAList(qaList);
                            }
                            updateNoResultsVisibility(qaList.isEmpty());
                        } catch (Exception e) {
                            updateNoResultsVisibility(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded()) {
                            updateNoResultsVisibility(true);
                            // Silent fail for QA loading - don't show error toast for public data
                        }
                    });
        } catch (Exception e) {
            if (getContext() != null && isAdded()) {
                updateNoResultsVisibility(true);
            }
        }
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
