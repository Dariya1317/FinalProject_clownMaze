package com.example.clownmaze.riddle;

import java.util.List;


public interface RiddleStrategy {

    String getQuestion();

    String getHint();

    boolean validate(String answer);

    int getDifficulty();

    default List<String> getChoices() { return List.of(); }
}

