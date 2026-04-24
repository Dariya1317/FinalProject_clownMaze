package com.example.clownmaze.core.entity;

import com.example.clownmaze.core.EventBus;

/**
 * Command fired when the player presses E.
 * Publishes {@link EventBus.HeroInteractEvent} so that door and riddle systems
 * (Sprint 3) can react without knowing about the input layer.
 *
 * <p>Pattern: Command (GoF #10).
 */
public final class InteractCommand implements ICommand {

    private final Hero hero;

    public InteractCommand(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void execute() {
        EventBus.getInstance().publish(
            new EventBus.HeroInteractEvent(hero.getX(), hero.getY())
        );
    }
}
