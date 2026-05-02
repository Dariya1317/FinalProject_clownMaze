package com.example.clownmaze.core.riddles;

import java.util.List;

public final class PatternRiddle extends ChoiceRiddle {

    public PatternRiddle(int roomId, int riddleIndex,
                         String question, String correctAnswer,
                         List<String> choices) {
        super(roomId, riddleIndex, question, correctAnswer, choices);
    }
}
