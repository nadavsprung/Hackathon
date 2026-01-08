package com.nadavsprung.hackathon;

import java.util.List;

public class TestModel {
    private String testId;
    private String title;
    private String subject;
    private String materialSummary;
    private List<Question> questions;
    private String creatorId;
    private long createdAt;
    private String shareLink;

    public TestModel() {
        // Default constructor for Firestore
    }

    public TestModel(String testId, String title, String subject, String materialSummary, 
                    List<Question> questions, String creatorId) {
        this.testId = testId;
        this.title = title;
        this.subject = subject;
        this.materialSummary = materialSummary;
        this.questions = questions;
        this.creatorId = creatorId;
        this.createdAt = System.currentTimeMillis();
    }

    public static class Question {
        private String questionText;
        private List<String> options;
        private int correctAnswerIndex;

        public Question() {}

        public Question(String questionText, List<String> options, int correctAnswerIndex) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public int getCorrectAnswerIndex() { return correctAnswerIndex; }
        public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
    }

    // Getters and setters
    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMaterialSummary() { return materialSummary; }
    public void setMaterialSummary(String materialSummary) { this.materialSummary = materialSummary; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getShareLink() { return shareLink; }
    public void setShareLink(String shareLink) { this.shareLink = shareLink; }
}