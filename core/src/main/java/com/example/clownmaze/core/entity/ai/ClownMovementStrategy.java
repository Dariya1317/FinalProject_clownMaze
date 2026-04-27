package com.example.clownmaze.core.entity.ai;

public interface ClownMovementStrategy {
    void move(ClownAI clown, float delta);
    void setTarget(float targetX, float targetY);
}
