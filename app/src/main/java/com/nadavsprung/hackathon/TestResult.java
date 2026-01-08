package com.nadavsprung.hackathon;

public class TestResult {
    private String resultId;
    private String testId;
    private String studentName;
    private int score;
    private int totalQuestions;
    private long completedAt;

    public TestResult() {}

    public TestResult(String resultId, String testId, String studentName, int score, int totalQuestions) {
        this.resultId = resultId;
        this.testId = testId;
        this.studentName = studentName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = System.currentTimeMillis();
    }

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    
    public int getPercentage() {
        if (totalQuestions == 0) return 0;
        return (score * 100) / totalQuestions;
    }
}