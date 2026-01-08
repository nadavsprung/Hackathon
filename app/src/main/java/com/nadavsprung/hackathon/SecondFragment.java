package com.nadavsprung.hackathon;

import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText etMessageInput;
    private Button btnSend;
    private Button btnUploadImage;
    private Button btnUploadFile;
    private TextView tvSubjectName;
    private TextView tvUploadedFiles;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private List<String> uploadedFiles = new ArrayList<>();
    private String selectedSubject = "";
    private String uploadedMaterial = "";
    
    private OpenAIService openAIService;

    private ActivityResultLauncher<String> imagePicker;
    private ActivityResultLauncher<String> filePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openAIService = new OpenAIService();

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleFileUpload(uri, "image");
                    }
                }
        );

        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleFileUpload(uri, "file");
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        recyclerChat = view.findViewById(R.id.recycler_chat);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSend = view.findViewById(R.id.btn_send);
        btnUploadImage = view.findViewById(R.id.btn_upload_image);
        btnUploadFile = view.findViewById(R.id.btn_upload_file);
        tvSubjectName = view.findViewById(R.id.tv_subject_name);
        tvUploadedFiles = view.findViewById(R.id.tv_uploaded_files);

        // Get subject from arguments if passed, or from MainActivity
        if (getArguments() != null) {
            selectedSubject = getArguments().getString("subject", "");
        }
        if (selectedSubject.isEmpty() && getActivity() instanceof MainActivity) {
            selectedSubject = MainActivity.selectedSubject;
        }
        if (!selectedSubject.isEmpty()) {
            tvSubjectName.setText("הכנה למבחן - " + selectedSubject);
        }

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        // Add welcome message
        addAIMessage("שלום! אני עוזר AI שלך להכנה למבחן. העלה חומר (טקסט, תמונה או PDF) ואני אכין לך סיכום ושאלות תרגול.");

        // Setup button listeners
        btnSend.setOnClickListener(v -> sendMessage());
        btnUploadImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnUploadFile.setOnClickListener(v -> filePicker.launch("application/pdf"));

        return view;
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user message
        ChatMessage userMessage = new ChatMessage(messageText, true);
        chatAdapter.addMessage(userMessage);
        etMessageInput.setText("");

        // If user asks for test prep, generate it
        if (messageText.toLowerCase().contains("מבחן") || messageText.toLowerCase().contains("תרגול") || 
            messageText.toLowerCase().contains("שאלות")) {
            generateTestPrep();
        } else {
            // Regular chat response
            simulateAIResponse(messageText);
        }

        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    private void generateTestPrep() {
        if (uploadedMaterial.isEmpty()) {
            addAIMessage("אנא העלה חומר לימוד (טקסט, תמונה או PDF) לפני שאני יכול ליצור סיכום ושאלות תרגול.");
            return;
        }

        addAIMessage("מכין לך סיכום ושאלות תרגול...");
        
        openAIService.generateTestPrep(uploadedMaterial, selectedSubject, new OpenAIService.AICallback() {
            @Override
            public void onSuccess(String response) {
                parseAndDisplayTestPrep(response);
            }

            @Override
            public void onError(String error) {
                getActivity().runOnUiThread(() -> {
                    addAIMessage("שגיאה ביצירת התוכן. אנא נסה שוב.");
                });
            }
        });
    }

    private void parseAndDisplayTestPrep(String jsonResponse) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            
            String summary = json.has("summary") ? json.get("summary").getAsString() : "";
            JsonArray questions = json.has("questions") ? json.getAsJsonArray("questions") : null;
            
            getActivity().runOnUiThread(() -> {
                if (!summary.isEmpty()) {
                    addAIMessage("📝 סיכום החומר:\n\n" + summary);
                }
                
                if (questions != null && questions.size() > 0) {
                    StringBuilder questionsText = new StringBuilder("❓ שאלות תרגול:\n\n");
                    for (int i = 0; i < questions.size(); i++) {
                        JsonObject q = questions.get(i).getAsJsonObject();
                        questionsText.append((i + 1)).append(". ").append(q.get("question").getAsString()).append("\n");
                        JsonArray options = q.getAsJsonArray("options");
                        for (int j = 0; j < options.size(); j++) {
                            questionsText.append("   ").append((char)('א' + j)).append(". ").append(options.get(j).getAsString()).append("\n");
                        }
                        questionsText.append("\n");
                    }
                    addAIMessage(questionsText.toString());
                }
            });
        } catch (Exception e) {
            getActivity().runOnUiThread(() -> {
                addAIMessage("תגובה מ-AI:\n" + jsonResponse);
            });
        }
    }

    private void simulateAIResponse(String userMessage) {
        String response = "תודה על השאלה שלך. זה תשובה לדוגמה. " +
                         "בגרסה המלאה, כאן תהיה תשובה מ-AI שמנתח את החומר שהעלית.";
        
        recyclerChat.postDelayed(() -> {
            addAIMessage(response);
        }, 1000);
    }

    private void addAIMessage(String text) {
        ChatMessage aiMessage = new ChatMessage(text, false);
        chatAdapter.addMessage(aiMessage);
        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    private void handleFileUpload(Uri uri, String type) {
        String fileName = uri.getLastPathSegment();
        if (fileName == null) {
            fileName = "קובץ";
        }

        uploadedFiles.add(fileName);
        updateUploadedFilesDisplay();

        // Add message about uploaded file
        String fileTypeText = type.equals("image") ? "תמונה" : "קובץ PDF";
        String uploadMessage = "העליתי " + fileTypeText + ": " + fileName;
        ChatMessage uploadMsg = new ChatMessage(uploadMessage, true, uri.toString());
        chatAdapter.addMessage(uploadMsg);

        // For now, just notify - in real app, extract text from PDF/image
        uploadedMaterial += "\n[קובץ: " + fileName + "]";
        
        recyclerChat.postDelayed(() -> {
            addAIMessage("קיבלתי את ה" + fileTypeText + " שלך. אתה יכול לבקש ממני ליצור סיכום ושאלות תרגול.");
        }, 1500);

        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    private void updateUploadedFilesDisplay() {
        if (uploadedFiles.isEmpty()) {
            tvUploadedFiles.setVisibility(View.GONE);
        } else {
            tvUploadedFiles.setVisibility(View.VISIBLE);
            tvUploadedFiles.setText("קבצים שהועלו: " + String.join(", ", uploadedFiles));
        }
    }

    public void setSubject(String subject) {
        this.selectedSubject = subject;
        if (tvSubjectName != null) {
            tvSubjectName.setText("הכנה למבחן - " + subject);
        }
    }
}