package com.cupids.curseoftheforest.minigames;

/**
 * One of the Nuno's riddles and the food that answers it.
 */
public class Riddle {
    private final String question;
    private final String answer;

    public Riddle(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public boolean isCorrect(String guess) {
        return answer.equals(guess);
    }

    public String getQuestion() {
        return question;
    }
}
