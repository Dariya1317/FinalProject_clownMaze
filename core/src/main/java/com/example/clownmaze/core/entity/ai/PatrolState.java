package com.example.clownmaze.core.entity.ai;

public final class PatrolState implements ClownState {

    private final PatrolStrategy strategy;

    public PatrolState(PatrolStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void enter(ClownAI clown) {
        strategy.reset();
        float[] start = strategy.getFirstWaypoint();
        clown.setX(start[0]);
        clown.setY(start[1]);
    }
    @Override
    public void update(ClownAI clown, float delta) {
        strategy.move(clown, delta);
    }
    @Override
    public void exit(ClownAI clown) {}
}
