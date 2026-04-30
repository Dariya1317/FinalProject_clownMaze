package com.example.clownmaze.core.entity;

public final class MoveCommand implements ICommand {

    private final Hero  hero;
    private final float dx;
    private final float dy;
    private final float delta;

    public MoveCommand(Hero hero, float dx, float dy, float delta) {
        this.hero  = hero;
        this.dx    = dx;
        this.dy    = dy;
        this.delta = delta;
    }

    @Override
    public void execute() {
        hero.attemptMove(dx * delta, dy * delta);
    }
}
