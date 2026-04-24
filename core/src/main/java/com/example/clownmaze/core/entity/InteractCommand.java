package com.example.clownmaze.core.entity;

import com.example.clownmaze.core.EventBus;

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
