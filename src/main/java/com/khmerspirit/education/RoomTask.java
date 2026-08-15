package com.khmerspirit.education;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RoomTask {

    private final String roomId;
    private final List<Question> questions;
    private int currentIndex = 0;
    private int correctCount = 0;

    public RoomTask(String roomId, List<Question> allQuestions, Random rng) {
        this.roomId = roomId;
        List<Question> copy = new ArrayList<>(allQuestions);
        Collections.shuffle(copy, rng);
        if (copy.size() > 5) {
            copy = copy.subList(0, 5);
        }
        this.questions = List.copyOf(copy);
    }

    public String getRoomId() {
        return roomId;
    }

    public Question getCurrentQuestion() {
        if (currentIndex < questions.size()) return questions.get(currentIndex);
        return null;
    }

    public boolean submitAnswer(int choiceIndex) {
        Question q = getCurrentQuestion();
        if (q == null) return false;
        boolean correct = (choiceIndex == q.getAnswerIndex());
        if (correct) {
            correctCount++;
            currentIndex++;
        } else {
            // wrong answer - don't advance
        }
        return correct;
    }

    public boolean isCompleted() {
        return correctCount >= 5 || currentIndex >= questions.size();
    }

    public int getCorrectCount() {
        return correctCount;
    }
}