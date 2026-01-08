package com.nadavsprung.hackathon;

public class SummaryModel {
    private String summaryId;
    private String title;
    private String content;
    private String subject;
    private String userId;
    private String userName;
    private long createdAt;

    public SummaryModel() {
        // Default constructor for Firestore
    }

    public SummaryModel(String summaryId, String title, String content, String subject, String userId, String userName) {
        this.summaryId = summaryId;
        this.title = title;
        this.content = content;
        this.subject = subject;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSummaryId() { return summaryId; }
    public void setSummaryId(String summaryId) { this.summaryId = summaryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
