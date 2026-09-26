package com.khmerspirit.education;

import java.util.List;

public class Question {

    private final String text;
    private final List<String> options;
    private final int answerIndex; // 0-based
    private final String category;
    private final String difficulty;

    public Question(String text, List<String> options, int answerIndex) {
        this(text, options, answerIndex, "General Knowledge", "Medium");
    }

    public Question(String text, List<String> options, int answerIndex, String category, String difficulty) {
        this.text = text;
        this.options = List.copyOf(options);
        this.answerIndex = answerIndex;
        this.category = (category != null && !category.isBlank()) ? category : "General Knowledge";
        this.difficulty = (difficulty != null && !difficulty.isBlank()) ? difficulty : "Medium";
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }
}