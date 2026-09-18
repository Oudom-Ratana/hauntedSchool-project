package com.khmerspirit.admin.model;

import java.util.Objects;

/**
 * Model representing a quiz question in the Khmer Spirit Admin System.
 */
public class QuestionModel {

    private String id;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; // "A", "B", "C", "D"
    private String category;      // "Programming", "Networking", "CyberSecurity", "Hardware", "General"
    private String room;          // "entrance", "classroomA", "classroomB", "computer", "laboratory", etc.
    private String difficulty;    // "Easy", "Medium", "Hard"
    private String rewardType;    // "Item", "Key", "Health", "Buff", etc.
    private String rewardValue;   // e.g. "Flashlight", "Master Key", "First Aid Kit"
    private String explanation;   // Feedback or educational clue
    private boolean active;       // Whether question is available in active pool
    private String questionType;  // "MCQ", "True / False", "Short Answer"
    private int taskNumber;       // 1 to 5 (mapped to room tasks)

    public QuestionModel() {
        this.active = true;
        this.questionType = "MCQ";
        this.taskNumber = 1;
    }

    public QuestionModel(String id, String text, String optionA, String optionB, String optionC, String optionD,
                         String correctAnswer, String category, String room, String difficulty,
                         String rewardType, String rewardValue, String explanation, boolean active) {
        this(id, text, optionA, optionB, optionC, optionD, correctAnswer, category, room, difficulty,
             rewardType, rewardValue, explanation, active, "MCQ", 1);
    }

    public QuestionModel(String id, String text, String optionA, String optionB, String optionC, String optionD,
                         String correctAnswer, String category, String room, String difficulty,
                         String rewardType, String rewardValue, String explanation, boolean active,
                         String questionType, int taskNumber) {
        this.id = id;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.room = room;
        this.difficulty = difficulty;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.explanation = explanation;
        this.active = active;
        this.questionType = (questionType != null && !questionType.isBlank()) ? questionType : "MCQ";
        this.taskNumber = taskNumber > 0 ? taskNumber : 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(String rewardValue) {
        this.rewardValue = rewardValue;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getQuestionType() {
        return (questionType != null && !questionType.isBlank()) ? questionType : "MCQ";
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getTaskNumber() {
        return taskNumber > 0 ? taskNumber : 1;
    }

    public void setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber > 0 ? taskNumber : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionModel model = (QuestionModel) o;
        return Objects.equals(id, model.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
