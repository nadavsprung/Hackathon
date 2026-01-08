package com.nadavsprung.hackathon;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

public class SubjectChatFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText etMessageInput;
    private Button btnSend;
    private Button btnUploadImage;
    private Button btnUploadFile;

    private ChatAdapter chatAdapter;
    private String selectedSubject = "";
    private String uploadedMaterial = "";
    
    private OpenAIService openAIService;

    private ActivityResultLauncher<String> imagePicker;
    private ActivityResultLauncher<String> filePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openAIService = new OpenAIService();

        if (getArguments() != null) {
            selectedSubject = getArguments().getString("subject", "");
        }

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
        View view = inflater.inflate(R.layout.fragment_subject_chat, container, false);

        recyclerChat = view.findViewById(R.id.recycler_chat);
        etMessageInput = view.findViewById(R.id.et_message_input);
        btnSend = view.findViewById(R.id.btn_send);
        btnUploadImage = view.findViewById(R.id.btn_upload_image);
        btnUploadFile = view.findViewById(R.id.btn_upload_file);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        // Add welcome message
        addAIMessage("שלום! אני עוזר AI שלך עבור " + selectedSubject + ". שאל אותי כל שאלה או העלה חומר ואני אעזור לך.");

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

        // Get AI response
        simulateAIResponse(messageText);

        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    private void simulateAIResponse(String userMessage) {
        // Use OpenAI service for chat response
        addAIMessage("מעבד את השאלה שלך...");
        
        String prompt = "You are a helpful tutor for " + selectedSubject + ". Answer the following question in Hebrew: " + userMessage;
        
        openAIService.generateChatResponse(prompt, new OpenAIService.AICallback() {
            @Override
            public void onSuccess(String response) {
                getActivity().runOnUiThread(() -> {
                    // Remove "processing" message and add actual response
                    int itemCount = chatAdapter.getItemCount();
                    if (itemCount > 0) {
                        chatAdapter.removeLastMessage();
                    }
                    addAIMessage(response);
                });
            }

            @Override
            public void onError(String error) {
                getActivity().runOnUiThread(() -> {
                    int itemCount = chatAdapter.getItemCount();
                    if (itemCount > 0) {
                        chatAdapter.removeLastMessage();
                    }
                    addAIMessage("תודה על השאלה שלך. זה תשובה לדוגמה. " +
                            "בגרסה המלאה, כאן תהיה תשובה מ-AI.");
                });
            }
        });
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

        // Add message about uploaded file
        String fileTypeText = type.equals("image") ? "תמונה" : "קובץ PDF";
        String uploadMessage = "העליתי " + fileTypeText + ": " + fileName;
        ChatMessage uploadMsg = new ChatMessage(uploadMessage, true, uri.toString());
        chatAdapter.addMessage(uploadMsg);

        // For now, just notify - in real app, extract text from PDF/image
        uploadedMaterial += "\n[קובץ: " + fileName + "]";
        
        recyclerChat.postDelayed(() -> {
            addAIMessage("קיבלתי את ה" + fileTypeText + " שלך. אני יכול לעזור לך עם החומר.");
        }, 1500);

        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }
}
