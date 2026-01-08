package com.nadavsprung.hackathon;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIService {
    private static final String TAG = "OpenAIService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // TODO: Add your OpenAI API key here
    private static final String API_KEY = ""; // Keep empty for now as requested
    
    private OkHttpClient client;
    private Gson gson;

    public OpenAIService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public interface AICallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void generateTestPrep(String material, String subject, AICallback callback) {
        if (API_KEY.isEmpty()) {
            // Return mock response when API key is empty
            callback.onSuccess(generateMockTestPrep(material, subject));
            return;
        }

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String prompt = "You are a helpful tutor. Based on the following material about " + subject + 
                            ", create:\n\n1. A concise summary (2-3 paragraphs)\n\n2. 5 multiple choice questions to help the student prepare for a test.\n\n" +
                            "Format your response as JSON:\n" +
                            "{\n" +
                            "  \"summary\": \"...\",\n" +
                            "  \"questions\": [\n" +
                            "    {\n" +
                            "      \"question\": \"...\",\n" +
                            "      \"options\": [\"...\", \"...\", \"...\", \"...\"],\n" +
                            "      \"correct\": 0\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}\n\nMaterial:\n" + material;

                    JsonObject requestBody = new JsonObject();
                    requestBody.addProperty("model", "gpt-3.5-turbo");
                    
                    JsonArray messages = new JsonArray();
                    JsonObject message = new JsonObject();
                    message.addProperty("role", "user");
                    message.addProperty("content", prompt);
                    messages.add(message);
                    
                    requestBody.add("messages", messages);
                    requestBody.addProperty("temperature", 0.7);
                    requestBody.addProperty("max_tokens", 1500);

                    RequestBody body = RequestBody.create(requestBody.toString(), JSON);
                    Request request = new Request.Builder()
                            .url(API_URL)
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        JsonArray choices = jsonResponse.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject choice = choices.get(0).getAsJsonObject();
                            JsonObject messageObj = choice.getAsJsonObject("message");
                            return messageObj.get("content").getAsString();
                        }
                    }
                    return "Error: " + response.code();
                } catch (IOException e) {
                    Log.e(TAG, "Error calling OpenAI API", e);
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.startsWith("Error")) {
                    callback.onError(result);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    private String generateMockTestPrep(String material, String subject) {
        // Mock JSON response when API key is empty
        return "{\n" +
                "  \"summary\": \"זה סיכום לדוגמה של החומר על " + subject + ". החומר כולל את הנושאים העיקריים שצריך לדעת למבחן. חשוב לזכור את הנקודות המרכזיות ולהבין את הקשרים ביניהן.\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"מהו הנושא המרכזי בחומר?\",\n" +
                "      \"options\": [\"תשובה א\", \"תשובה ב\", \"תשובה ג\", \"תשובה ד\"],\n" +
                "      \"correct\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"question\": \"מה חשוב לזכור?\",\n" +
                "      \"options\": [\"נקודה א\", \"נקודה ב\", \"נקודה ג\", \"נקודה ד\"],\n" +
                "      \"correct\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public void generateTest(String material, String subject, String title, int numQuestions, AICallback callback) {
        if (API_KEY.isEmpty()) {
            callback.onSuccess(generateMockTest(material, subject, title, numQuestions));
            return;
        }

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String prompt = "Create a multiple choice test about " + subject + 
                            " based on this material. Generate exactly " + numQuestions + 
                            " questions with 4 options each.\n\n" +
                            "Format as JSON:\n" +
                            "{\n" +
                            "  \"summary\": \"Brief summary of the material\",\n" +
                            "  \"questions\": [\n" +
                            "    {\n" +
                            "      \"question\": \"...\",\n" +
                            "      \"options\": [\"...\", \"...\", \"...\", \"...\"],\n" +
                            "      \"correct\": 0\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}\n\nMaterial:\n" + material;

                    JsonObject requestBody = new JsonObject();
                    requestBody.addProperty("model", "gpt-3.5-turbo");
                    
                    JsonArray messages = new JsonArray();
                    JsonObject message = new JsonObject();
                    message.addProperty("role", "user");
                    message.addProperty("content", prompt);
                    messages.add(message);
                    
                    requestBody.add("messages", messages);
                    requestBody.addProperty("temperature", 0.7);
                    requestBody.addProperty("max_tokens", 2000);

                    RequestBody body = RequestBody.create(requestBody.toString(), JSON);
                    Request request = new Request.Builder()
                            .url(API_URL)
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        JsonArray choices = jsonResponse.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject choice = choices.get(0).getAsJsonObject();
                            JsonObject messageObj = choice.getAsJsonObject("message");
                            return messageObj.get("content").getAsString();
                        }
                    }
                    return "Error: " + response.code();
                } catch (IOException e) {
                    Log.e(TAG, "Error calling OpenAI API", e);
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.startsWith("Error")) {
                    callback.onError(result);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    private String generateMockTest(String material, String subject, String title, int numQuestions) {
        StringBuilder mockJson = new StringBuilder();
        mockJson.append("{\n");
        mockJson.append("  \"summary\": \"סיכום של החומר על ").append(subject).append("\",\n");
        mockJson.append("  \"questions\": [\n");
        
        for (int i = 0; i < numQuestions; i++) {
            mockJson.append("    {\n");
            mockJson.append("      \"question\": \"שאלה ").append(i + 1).append(" על ").append(subject).append("?\",\n");
            mockJson.append("      \"options\": [\"תשובה א\", \"תשובה ב\", \"תשובה ג\", \"תשובה ד\"],\n");
            mockJson.append("      \"correct\": ").append(i % 4).append("\n");
            mockJson.append("    }");
            if (i < numQuestions - 1) mockJson.append(",");
            mockJson.append("\n");
        }
        
        mockJson.append("  ]\n");
        mockJson.append("}");
        return mockJson.toString();
    }
}