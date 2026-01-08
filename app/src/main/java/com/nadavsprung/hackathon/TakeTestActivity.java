package com.nadavsprung.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TakeTestActivity extends AppCompatActivity {

    private TextView tvTestTitle;
    private TextView tvTestSubject;
    private TextView tvQuestionNumber;
    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnSubmitTest;

    private TestModel test;
    private List<Integer> userAnswers;
    private int currentQuestionIndex = 0;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_test);

        db = FirebaseFirestore.getInstance();

        tvTestTitle = findViewById(R.id.tv_test_title);
        tvTestSubject = findViewById(R.id.tv_test_subject);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvQuestion = findViewById(R.id.tv_question);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnSubmitTest = findViewById(R.id.btn_submit_test);

        // Get test ID from intent
        String testId = getIntent().getStringExtra("testId");
        if (testId == null) {
            // Try to get from deep link
            Intent intent = getIntent();
            if (intent.getData() != null) {
                String path = intent.getData().getPath();
                if (path != null && path.startsWith("/test/")) {
                    testId = path.substring("/test/".length());
                }
            }
        }

        if (testId == null) {
            Toast.makeText(this, "שגיאה: לא נמצא מבחן", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTest(testId);

        btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                saveCurrentAnswer();
                currentQuestionIndex--;
                displayQuestion();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "אנא בחר תשובה", Toast.LENGTH_SHORT).show();
                return;
            }
            saveCurrentAnswer();
            if (currentQuestionIndex < test.getQuestions().size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            }
        });

        btnSubmitTest.setOnClickListener(v -> submitTest());
    }

    private void loadTest(String testId) {
        db.collection("tests").document(testId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        test = documentSnapshot.toObject(TestModel.class);
                        if (test != null && test.getQuestions() != null) {
                            userAnswers = new ArrayList<>();
                            for (int i = 0; i < test.getQuestions().size(); i++) {
                                userAnswers.add(-1); // -1 means not answered
                            }
                            displayQuestion();
                        } else {
                            Toast.makeText(this, "שגיאה בטעינת המבחן", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "מבחן לא נמצא", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayQuestion() {
        if (test == null || test.getQuestions() == null || currentQuestionIndex >= test.getQuestions().size()) {
            return;
        }

        TestModel.Question question = test.getQuestions().get(currentQuestionIndex);

        tvTestTitle.setText(test.getTitle());
        tvTestSubject.setText("נושא: " + test.getSubject());
        tvQuestionNumber.setText("שאלה " + (currentQuestionIndex + 1) + " מתוך " + test.getQuestions().size());
        tvQuestion.setText(question.getQuestionText());

        // Clear previous options
        radioGroupOptions.removeAllViews();

        // Add options
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText((char)('א' + i) + ". " + options.get(i));
            radioButton.setId(i);
            radioGroupOptions.addView(radioButton);
        }

        // Restore previous answer if exists
        int previousAnswer = userAnswers.get(currentQuestionIndex);
        if (previousAnswer >= 0) {
            radioGroupOptions.check(previousAnswer);
        } else {
            radioGroupOptions.clearCheck();
        }

        // Update buttons
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        if (currentQuestionIndex == test.getQuestions().size() - 1) {
            btnNext.setVisibility(View.GONE);
            btnSubmitTest.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnSubmitTest.setVisibility(View.GONE);
        }
    }

    private void saveCurrentAnswer() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId != -1) {
            userAnswers.set(currentQuestionIndex, selectedId);
        }
    }

    private void submitTest() {
        if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "אנא בחר תשובה", Toast.LENGTH_SHORT).show();
            return;
        }

        saveCurrentAnswer();

        // Check if all questions answered
        boolean allAnswered = true;
        for (int answer : userAnswers) {
            if (answer == -1) {
                allAnswered = false;
                break;
            }
        }

        if (!allAnswered) {
            new AlertDialog.Builder(this)
                    .setTitle("שים לב")
                    .setMessage("לא ענית על כל השאלות. האם אתה בטוח שברצונך להגיש?")
                    .setPositiveButton("כן, הגש", (dialog, which) -> calculateAndSaveScore())
                    .setNegativeButton("לא", null)
                    .show();
        } else {
            calculateAndSaveScore();
        }
    }

    private void calculateAndSaveScore() {
        int correctAnswers = 0;
        List<TestModel.Question> questions = test.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers.get(i) == questions.get(i).getCorrectAnswerIndex()) {
                correctAnswers++;
            }
        }

        // Make final copies for lambda
        final int finalCorrectAnswers = correctAnswers;
        final int finalTotalQuestions = questions.size();

        // Get student name
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_student_name, null);
        EditText etStudentName = dialogView.findViewById(R.id.et_student_name);

        new AlertDialog.Builder(this)
                .setTitle("הזן את שמך")
                .setView(dialogView)
                .setPositiveButton("שמור תוצאה", (dialog, which) -> {
                    String studentName = etStudentName.getText().toString().trim();
                    if (studentName.isEmpty()) {
                        studentName = "אנונימי";
                    }

                    saveResult(finalCorrectAnswers, finalTotalQuestions, studentName);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void saveResult(int score, int totalQuestions, String studentName) {
        String resultId = UUID.randomUUID().toString();
        TestResult result = new TestResult(resultId, test.getTestId(), studentName, score, totalQuestions);

        db.collection("testResults").document(resultId).set(result)
                .addOnSuccessListener(aVoid -> {
                    showResultDialog(score, totalQuestions);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בשמירת התוצאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showResultDialog(int score, int totalQuestions) {
        int percentage = (score * 100) / totalQuestions;
        String message = "סיימת את המבחן!\n\n" +
                "תוצאה: " + score + " מתוך " + totalQuestions + "\n" +
                "אחוז: " + percentage + "%";

        new AlertDialog.Builder(this)
                .setTitle("תוצאות המבחן")
                .setMessage(message)
                .setPositiveButton("סגור", (dialog, which) -> finish())
                .show();
    }
}