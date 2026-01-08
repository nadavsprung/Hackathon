package com.nadavsprung.hackathon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FourthFragment extends Fragment {

    private RecyclerView recyclerLeaderboard;
    private Button btnRefresh;
    private TextView tvLeaderboardInfo;
    private LeaderboardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fourth, container, false);

        recyclerLeaderboard = view.findViewById(R.id.recycler_leaderboard);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvLeaderboardInfo = view.findViewById(R.id.tv_leaderboard_info);

        adapter = new LeaderboardAdapter();
        recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerLeaderboard.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> loadLeaderboard());

        // Load leaderboard on fragment creation
        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        // Load all test results from Firebase
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        db.collection("testResults")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    final int[] loadedCount = {0};
                    final int totalCount = queryDocumentSnapshots.size();
                    
                    if (totalCount == 0) {
                        tvLeaderboardInfo.setText("אין תוצאות להצגה");
                        adapter.setEntries(entries);
                        return;
                    }
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        TestResult result = doc.toObject(TestResult.class);
                        if (result != null) {
                            // Get test name
                            db.collection("tests").document(result.getTestId()).get()
                                    .addOnSuccessListener(testDoc -> {
                                        String testName = "מבחן";
                                        if (testDoc.exists()) {
                                            TestModel test = testDoc.toObject(TestModel.class);
                                            if (test != null) {
                                                testName = test.getTitle();
                                            }
                                        }
                                        
                                        entries.add(new LeaderboardEntry(
                                                result.getStudentName(),
                                                testName,
                                                result.getPercentage()
                                        ));
                                        
                                        loadedCount[0]++;
                                        if (loadedCount[0] == totalCount) {
                                            // All loaded, sort and display
                                            Collections.sort(entries, (e1, e2) -> Integer.compare(e2.getScore(), e1.getScore()));
                                            adapter.setEntries(entries);
                                            tvLeaderboardInfo.setText("נמצאו " + entries.size() + " תוצאות");
                                        }
                                    });
                        } else {
                            loadedCount[0]++;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    tvLeaderboardInfo.setText("שגיאה בטעינת התוצאות");
                });
    }
}