package com.nadavsprung.hackathon;

public class QAModel {
    private String qaId;
    private String question;
    private String answer;
    private String subject;
    private String userId;
    private String userName;
    private long createdAt;

    public QAModel() {
        // Default constructor for Firestore
    }

    public QAModel(String qaId, String question, String answer, String subject, String userId, String userName) {
        this.qaId = qaId;
        this.question = question;
        this.answer = answer;
        this.subject = subject;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = System.currentTimeMillis();
    }

    public String getQaId() { return qaId; }
    public void setQaId(String qaId) { this.qaId = qaId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
