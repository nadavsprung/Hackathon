package com.nadavsprung.hackathon;

public class LeaderboardEntry {
    private String studentName;
    private String testName;
    private int score;

    public LeaderboardEntry(String studentName, String testName, int score) {
        this.studentName = studentName;
        this.testName = testName;
        this.score = score;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getTestName() {
        return testName;
    }

    public int getScore() {
        return score;
    }
}