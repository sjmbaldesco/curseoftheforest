package com.cupids.curseoftheforest.minigames;

/**
 * One of the Nuno's riddles and the food that answers it.
 * Answers ignore case and surrounding whitespace.
 */
public class Riddle {
    private final String question;
    private final String answer;

    public Riddle(String question, String answer) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("A riddle needs a question and answer.");
        }
        this.question = question;
        this.answer = answer;
    }

    public boolean isCorrect(String guess) {
        return guess != null && answer.strip().equalsIgnoreCase(guess.strip());
    }

    public String getQuestion() {
        return question;
    }
}
