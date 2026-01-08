package com.nadavsprung.hackathon;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SecondFragment extends Fragment {

    private Button btnUploadSummary;
    private Button btnUploadQA;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> subjects;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        subjects = Arrays.asList(
                "מתמטיקה", "פיזיקה", "כימיה", "ביולוגיה",
                "מדעי המחשב", "אלקטרוניקה", "אנגלית", "ספרות",
                "לשון והבעה עברית", "היסטוריה", "תנך",
                "מדעים", "גיאוגרפיה", "אזרחות", "מחשבת ישראל", "מקצועות נוספים"
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        btnUploadSummary = view.findViewById(R.id.btn_upload_summary);
        btnUploadQA = view.findViewById(R.id.btn_upload_qa);

        btnUploadSummary.setOnClickListener(v -> showUploadSummaryDialog());
        btnUploadQA.setOnClickListener(v -> showUploadQADialog());

        return view;
    }

    private void showUploadSummaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upload_summary, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_summary_title);
        EditText etContent = dialogView.findViewById(R.id.et_summary_content);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinner_subject);
        Button btnUpload = dialogView.findViewById(R.id.btn_upload);

        // Setup subject spinner
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnUpload.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String subject = spinnerSubject.getSelectedItem().toString();

            if (title.isEmpty()) {
                etTitle.setError("אנא הכנס כותרת");
                return;
            }

            if (content.isEmpty()) {
                etContent.setError("אנא הכנס תוכן");
                return;
            }

            uploadSummary(title, content, subject, dialog);
        });
    }

    private void uploadSummary(String title, String content, String subject, AlertDialog dialog) {
        String summaryId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        String userName = auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null 
                ? auth.getCurrentUser().getDisplayName() 
                : auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "משתמש";

        SummaryModel summary = new SummaryModel(summaryId, title, content, subject, userId, userName);

        db.collection("summaries").document(summaryId).set(summary)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "סיכום הועלה בהצלחה!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בהעלאת הסיכום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUploadQADialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upload_qa, null);
        builder.setView(dialogView);

        EditText etQuestion = dialogView.findViewById(R.id.et_question);
        EditText etAnswer = dialogView.findViewById(R.id.et_answer);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinner_subject);
        Button btnUpload = dialogView.findViewById(R.id.btn_upload);

        // Setup subject spinner
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnUpload.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();
            String subject = spinnerSubject.getSelectedItem().toString();

            if (question.isEmpty()) {
                etQuestion.setError("אנא הכנס שאלה");
                return;
            }

            if (answer.isEmpty()) {
                etAnswer.setError("אנא הכנס תשובה");
                return;
            }

            uploadQA(question, answer, subject, dialog);
        });
    }

    private void uploadQA(String question, String answer, String subject, AlertDialog dialog) {
        String qaId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        String userName = auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null 
                ? auth.getCurrentUser().getDisplayName() 
                : auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "משתמש";

        QAModel qa = new QAModel(qaId, question, answer, subject, userId, userName);

        db.collection("qa").document(qaId).set(qa)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "שאלה ותשובה הועלו בהצלחה!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בהעלאת השאלה והתשובה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
