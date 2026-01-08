package com.nadavsprung.hackathon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FourthFragment extends Fragment {

    private RecyclerView recyclerTests;
    private Button btnRefresh;
    private TextView tvInfo;
    private TestsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fourth, container, false);

        recyclerTests = view.findViewById(R.id.recycler_leaderboard);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvInfo = view.findViewById(R.id.tv_leaderboard_info);

        adapter = new TestsAdapter(new TestsAdapter.OnTestClickListener() {
            @Override
            public void onViewLeaderboard(TestModel test) {
                showTestLeaderboard(test);
            }

            @Override
            public void onShareTest(TestModel test) {
                // Share test link (already implemented in ThirdFragment)
                shareTest(test);
            }
        });

        recyclerTests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTests.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> loadMyTests());

        // Only load tests when fragment is visible
        if (getView() != null && isAdded() && getContext() != null) {
            loadMyTests();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload tests when fragment becomes visible
        if (isAdded() && getContext() != null) {
            loadMyTests();
        }
    }

    private void loadMyTests() {
        if (getContext() == null || !isAdded() || db == null || auth == null) {
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (tvInfo != null) {
                tvInfo.setText("אנא התחבר כדי לראות את המבחנים שלך");
            }
            if (adapter != null) {
                adapter.setTests(new ArrayList<>());
            }
            return;
        }

        try {
            db.collection("tests")
                    .whereEqualTo("creatorId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (getContext() == null || !isAdded()) {
                            return;
                        }
                        try {
                            List<TestModel> tests = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                TestModel test = doc.toObject(TestModel.class);
                                if (test != null) {
                                    tests.add(test);
                                }
                            }
                            // Sort by creation date (newest first)
                            tests.sort((t1, t2) -> Long.compare(t2.getCreatedAt(), t1.getCreatedAt()));
                            if (adapter != null) {
                                adapter.setTests(tests);
                            }
                            
                            if (tvInfo != null) {
                                if (tests.isEmpty()) {
                                    tvInfo.setText("אין מבחנים. צור מבחן חדש בלשונית 'מבחנים'");
                                } else {
                                    tvInfo.setText("נמצאו " + tests.size() + " מבחנים שלך. לחץ על מבחן כדי לראות תוצאות");
                                }
                            }
                        } catch (Exception e) {
                            if (getContext() != null && isAdded() && tvInfo != null) {
                                tvInfo.setText("שגיאה בעיבוד המבחנים");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded()) {
                            if (tvInfo != null) {
                                tvInfo.setText("שגיאה בטעינת המבחנים");
                            }
                            String errorMsg = "שגיאה בטעינת המבחנים";
                            if (e != null && e.getMessage() != null) {
                                String msg = e.getMessage().toLowerCase();
                                if (msg.contains("permission") || msg.contains("permission-denied")) {
                                    errorMsg = "אין הרשאה לגשת למבחנים. אנא בדוק את הגדרות מסד הנתונים";
                                } else if (msg.contains("network") || msg.contains("unavailable")) {
                                    errorMsg = "שגיאת רשת. בדוק את החיבור לאינטרנט";
                                } else {
                                    errorMsg = "שגיאה: " + e.getMessage();
                                }
                            }
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            if (getContext() != null && isAdded() && tvInfo != null) {
                tvInfo.setText("שגיאה בהתחברות למסד הנתונים");
            }
        }
    }

    private void showTestLeaderboard(TestModel test) {
        // Load results for this test
        db.collection("testResults")
                .whereEqualTo("testId", test.getTestId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TestResult> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        TestResult result = doc.toObject(TestResult.class);
                        if (result != null) {
                            results.add(result);
                        }
                    }

                    // Sort by score descending
                    Collections.sort(results, (r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));

                    // Show leaderboard dialog
                    StringBuilder leaderboardText = new StringBuilder("לוח תוצאות - " + test.getTitle() + "\n\n");
                    if (results.isEmpty()) {
                        leaderboardText.append("אין תוצאות עדיין עבור מבחן זה");
                    } else {
                        for (int i = 0; i < results.size(); i++) {
                            TestResult result = results.get(i);
                            leaderboardText.append((i + 1)).append(". ").append(result.getStudentName())
                                    .append(" - ").append(result.getPercentage()).append("%")
                                    .append(" (").append(result.getScore()).append("/").append(result.getTotalQuestions()).append(")\n");
                        }
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("לוח תוצאות")
                            .setMessage(leaderboardText.toString())
                            .setPositiveButton("סגור", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת התוצאות", Toast.LENGTH_SHORT).show();
                });
    }

    private void shareTest(TestModel test) {
        String shareLink = test.getShareLink();
        if (shareLink == null || shareLink.isEmpty()) {
            shareLink = "hackathon://test/" + test.getTestId();
        }

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Test Link", shareLink);
        clipboard.setPrimaryClip(clip);

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "בואו לקחת את המבחן: " + shareLink);
        startActivity(android.content.Intent.createChooser(shareIntent, "שתף מבחן"));

        Toast.makeText(getContext(), "קישור הועתק ללוח", Toast.LENGTH_SHORT).show();
    }
}