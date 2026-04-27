package com.example.clownmaze.core.entity.ai;

public interface ClownState {
    void enter(ClownAI clown);
    void update(ClownAI clown, float delta);
    void exit(ClownAI clown);
}
