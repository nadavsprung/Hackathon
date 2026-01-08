package com.nadavsprung.hackathon;

import java.util.Date;

public class ChatMessage {
    private String text;
    private boolean isFromUser;
    private Date timestamp;
    private String fileUri; // For uploaded files

    public ChatMessage(String text, boolean isFromUser) {
        this.text = text;
        this.isFromUser = isFromUser;
        this.timestamp = new Date();
    }

    public ChatMessage(String text, boolean isFromUser, String fileUri) {
        this.text = text;
        this.isFromUser = isFromUser;
        this.timestamp = new Date();
        this.fileUri = fileUri;
    }

    public String getText() {
        return text;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getFileUri() {
        return fileUri;
    }
}