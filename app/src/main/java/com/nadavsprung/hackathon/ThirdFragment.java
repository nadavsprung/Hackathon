package com.nadavsprung.hackathon;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThirdFragment extends Fragment {

    private RecyclerView recyclerTests;
    private Button btnCreateTest;
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
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        recyclerTests = view.findViewById(R.id.recycler_tests);
        btnCreateTest = view.findViewById(R.id.btn_create_test);

        adapter = new TestsAdapter(new TestsAdapter.OnTestClickListener() {
            @Override
            public void onViewLeaderboard(TestModel test) {
                showLeaderboardDialog(test);
            }

            @Override
            public void onShareTest(TestModel test) {
                shareTest(test);
            }
        });

        recyclerTests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTests.setAdapter(adapter);

        btnCreateTest.setOnClickListener(v -> showCreateTestDialog());

        // Only load tests when fragment is visible
        if (getView() != null && isAdded() && getContext() != null) {
            loadTests();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload tests when fragment becomes visible
        if (isAdded() && getContext() != null) {
            loadTests();
        }
    }

    private void showCreateTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_test, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_test_title);
        EditText etSubject = dialogView.findViewById(R.id.et_test_subject);
        EditText etMaterial = dialogView.findViewById(R.id.et_test_material);
        EditText etNumQuestions = dialogView.findViewById(R.id.et_num_questions);
        Button btnGenerate = dialogView.findViewById(R.id.btn_generate_test);
        TextView tvStatus = dialogView.findViewById(R.id.tv_generation_status);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnGenerate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();
            String material = etMaterial.getText().toString().trim();
            String numQuestionsStr = etNumQuestions.getText().toString().trim();

            if (title.isEmpty() || subject.isEmpty() || material.isEmpty()) {
                Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            int numQuestions = 5;
            try {
                numQuestions = Integer.parseInt(numQuestionsStr);
                if (numQuestions < 1 || numQuestions > 20) {
                    Toast.makeText(getContext(), "מספר שאלות חייב להיות בין 1 ל-20", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "מספר שאלות לא תקין", Toast.LENGTH_SHORT).show();
                return;
            }

            btnGenerate.setEnabled(false);
            tvStatus.setText("יוצר מבחן עם AI...");

            OpenAIService openAIService = new OpenAIService();
            openAIService.generateTest(material, subject, title, numQuestions, new OpenAIService.AICallback() {
                @Override
                public void onSuccess(String response) {
                    parseAndSaveTest(response, title, subject, dialog);
                }

                @Override
                public void onError(String error) {
                    getActivity().runOnUiThread(() -> {
                        tvStatus.setText("שגיאה: " + error);
                        btnGenerate.setEnabled(true);
                    });
                }
            });
        });
    }

    private void parseAndSaveTest(String jsonResponse, String title, String subject, AlertDialog dialog) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            
            String summary = json.has("summary") ? json.get("summary").getAsString() : "";
            JsonArray questionsJson = json.has("questions") ? json.getAsJsonArray("questions") : null;
            
            if (questionsJson == null || questionsJson.size() == 0) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "לא נוצרו שאלות", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                return;
            }

            List<TestModel.Question> questions = new ArrayList<>();
            for (int i = 0; i < questionsJson.size(); i++) {
                JsonObject q = questionsJson.get(i).getAsJsonObject();
                String questionText = q.get("question").getAsString();
                JsonArray optionsJson = q.getAsJsonArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsJson.size(); j++) {
                    options.add(optionsJson.get(j).getAsString());
                }
                int correct = q.get("correct").getAsInt();
                questions.add(new TestModel.Question(questionText, options, correct));
            }

            String testId = UUID.randomUUID().toString();
            String creatorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
            String shareLink = "hackathon://test/" + testId;

            TestModel test = new TestModel(testId, title, subject, summary, questions, creatorId);
            test.setShareLink(shareLink);

            // Save to Firestore
            db.collection("tests").document(testId).set(test)
                    .addOnSuccessListener(aVoid -> {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "המבחן נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadTests();
                        });
                    })
                    .addOnFailureListener(e -> {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "שגיאה בשמירת המבחן: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    });

        } catch (Exception e) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "שגיאה בניתוח התגובה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
    }

    private void loadTests() {
        if (getContext() == null || !isAdded() || db == null) {
            return;
        }

        try {
            // Load all tests (anyone can create and see tests)
            db.collection("tests")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (getContext() == null || !isAdded()) {
                            return;
                        }
                        List<TestModel> tests = new ArrayList<>();
                        try {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                TestModel test = doc.toObject(TestModel.class);
                                if (test != null) {
                                    tests.add(test);
                                }
                            }
                            // Sort by createdAt manually
                            tests.sort((t1, t2) -> Long.compare(t2.getCreatedAt(), t1.getCreatedAt()));
                            adapter.setTests(tests);
                        } catch (Exception e) {
                            if (getContext() != null && isAdded()) {
                                Toast.makeText(getContext(), "שגיאה בעיבוד המבחנים", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded()) {
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
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "שגיאה בהתחברות למסד הנתונים", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLeaderboardDialog(TestModel test) {
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
                    results.sort((r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));

                    // Show leaderboard
                    StringBuilder leaderboardText = new StringBuilder("לוח תוצאות - " + test.getTitle() + "\n\n");
                    if (results.isEmpty()) {
                        leaderboardText.append("אין תוצאות עדיין");
                    } else {
                        for (int i = 0; i < results.size(); i++) {
                            TestResult result = results.get(i);
                            leaderboardText.append((i + 1)).append(". ").append(result.getStudentName())
                                    .append(" - ").append(result.getPercentage()).append("%\n");
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

        // Copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Test Link", shareLink);
        clipboard.setPrimaryClip(clip);

        // Also share via intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "בואו לקחת את המבחן: " + shareLink);
        startActivity(Intent.createChooser(shareIntent, "שתף מבחן"));

        Toast.makeText(getContext(), "קישור הועתק ללוח", Toast.LENGTH_SHORT).show();
    }
}