package com.example.clownmaze.core.entity;

public final class MashCommand implements ICommand {

    private final Hero hero;

    public MashCommand(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void execute() {
        hero.mash();
    }
}
