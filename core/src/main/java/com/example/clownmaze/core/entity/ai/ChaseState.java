package com.example.clownmaze.core.entity.ai;

public final class ChaseState implements ClownState {
    static final float KILL_RANGE = 8f;
    private final ChaseStrategy strategy;

    public ChaseState(ChaseStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void enter(ClownAI clown) {
        strategy.setTarget(clown.getHeroX(), clown.getHeroY());
    }

    @Override
    public void update(ClownAI clown, float delta) {
        strategy.setTarget(clown.getHeroX(), clown.getHeroY());
        strategy.move(clown, delta);

        float dx = clown.getX() - clown.getHeroX();
        float dy = clown.getY() - clown.getHeroY();
        if (dx * dx + dy * dy <= KILL_RANGE * KILL_RANGE) {
            clown.kill("clown_caught");
        }
    }
    @Override
    public void exit(ClownAI clown) {}
}
