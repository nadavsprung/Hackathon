package com.nadavsprung.hackathon;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubjectSummariesFragment extends Fragment {

    private RecyclerView recyclerSummaries;
    private EditText etSearch;
    private TextView tvNoResults;
    private SummaryAdapter adapter;
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
        View view = inflater.inflate(R.layout.fragment_subject_summaries, container, false);

        recyclerSummaries = view.findViewById(R.id.recycler_summaries);
        etSearch = view.findViewById(R.id.et_search);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        adapter = new SummaryAdapter();
        recyclerSummaries.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSummaries.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSummaries(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Only load summaries when fragment is visible
        if (getView() != null && isAdded() && getContext() != null) {
            loadSummaries();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload summaries when fragment becomes visible
        if (isAdded() && getContext() != null) {
            loadSummaries();
        }
    }

    private void loadSummaries() {
        if (getContext() == null || !isAdded() || db == null || subject == null) {
            return;
        }

        try {
            db.collection("summaries")
                    .whereEqualTo("subject", subject)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (getContext() == null || !isAdded()) {
                            return;
                        }
                        try {
                            List<SummaryModel> summaries = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                SummaryModel summary = doc.toObject(SummaryModel.class);
                                if (summary != null) {
                                    summaries.add(summary);
                                }
                            }
                            // Sort by creation date (newest first)
                            summaries.sort((s1, s2) -> Long.compare(s2.getCreatedAt(), s1.getCreatedAt()));
                            if (adapter != null) {
                                adapter.setSummaries(summaries);
                            }
                            updateNoResultsVisibility(summaries.isEmpty());
                        } catch (Exception e) {
                            updateNoResultsVisibility(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded()) {
                            updateNoResultsVisibility(true);
                            // Silent fail for summaries loading - don't show error toast for public data
                        }
                    });
        } catch (Exception e) {
            if (getContext() != null && isAdded()) {
                updateNoResultsVisibility(true);
            }
        }
    }

    private void filterSummaries(String query) {
        List<SummaryModel> allSummaries = new ArrayList<>();
        db.collection("summaries")
                .whereEqualTo("subject", subject)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SummaryModel summary = doc.toObject(SummaryModel.class);
                        if (summary != null) {
                            allSummaries.add(summary);
                        }
                    }
                    
                    if (query.isEmpty()) {
                        adapter.setSummaries(allSummaries);
                    } else {
                        List<SummaryModel> filtered = new ArrayList<>();
                        String lowerQuery = query.toLowerCase();
                        for (SummaryModel summary : allSummaries) {
                            if (summary.getTitle().toLowerCase().contains(lowerQuery) ||
                                summary.getContent().toLowerCase().contains(lowerQuery)) {
                                filtered.add(summary);
                            }
                        }
                        adapter.setSummaries(filtered);
                        updateNoResultsVisibility(filtered.isEmpty());
                    }
                });
    }

    private void updateNoResultsVisibility(boolean show) {
        if (show) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerSummaries.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerSummaries.setVisibility(View.VISIBLE);
        }
    }
}
