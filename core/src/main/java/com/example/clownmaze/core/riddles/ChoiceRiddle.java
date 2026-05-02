package com.example.clownmaze.core.riddles;

import java.util.Collections;
import java.util.List;

public abstract class ChoiceRiddle extends BaseRiddle {
    private final List<String> choices;
    private final String correctAnswer;

    protected ChoiceRiddle(int roomId, int riddleIndex,
                           String question, String correctAnswer,
                           List<String> choices) {
        super(roomId, riddleIndex, question);
        this.correctAnswer = correctAnswer.trim().toLowerCase();
        this.choices = Collections.unmodifiableList(choices);
    }

    @Override
    protected boolean validate(String playerAnswer) {
        return playerAnswer != null
            && playerAnswer.trim().toLowerCase().equals(correctAnswer);
    }

    @Override
    public RiddleType getType() { return RiddleType.CHOICE; }

    public List<String> getChoices() { return choices; }
    public String getCorrectAnswer() { return correctAnswer; }
}
