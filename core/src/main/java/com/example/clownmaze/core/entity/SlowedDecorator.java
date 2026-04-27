package com.example.clownmaze.core.entity;

public final class SlowedDecorator implements IInputHandler {

    private static final float SLOW_FACTOR = 0.45f;

    private final IInputHandler wrapped;
    private final Hero hero;

    public SlowedDecorator(IInputHandler wrapped, Hero hero) {
        this.wrapped = wrapped;
        this.hero    = hero;
    }

    @Override
    public void handleInput(float dt) {
        wrapped.handleInput(hero.isSlowed() ? dt * SLOW_FACTOR : dt);
    }
}
