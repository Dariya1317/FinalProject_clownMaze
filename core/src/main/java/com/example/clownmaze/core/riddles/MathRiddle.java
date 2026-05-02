package com.example.clownmaze.core.riddles;

public final class MathRiddle extends BaseRiddle {

    private final String correctAnswer;
    public MathRiddle(int roomId, int riddleIndex, String question, String correctAnswer) {
        super(roomId, riddleIndex, question);
        this.correctAnswer = correctAnswer.trim().toLowerCase();
    }
    @Override
    protected boolean validate(String playerAnswer) {
        return playerAnswer != null
            && playerAnswer.trim().toLowerCase().equals(correctAnswer);
    }

    @Override
    public RiddleType getType() { return RiddleType.TEXT; }
    public String getCorrectAnswer() { return correctAnswer; }
}
