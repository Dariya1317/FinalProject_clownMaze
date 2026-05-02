package com.example.clownmaze.core.riddles;

import java.util.List;

public final class JavaSyntaxRiddle extends ChoiceRiddle {

    public JavaSyntaxRiddle(int roomId, int riddleIndex,
                            String question, String correctAnswer,
                            List<String> choices) {
        super(roomId, riddleIndex, question, correctAnswer, choices);
    }
}
