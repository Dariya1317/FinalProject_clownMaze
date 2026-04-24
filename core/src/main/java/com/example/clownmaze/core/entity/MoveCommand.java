package com.example.clownmaze.core.entity;

/**
 * Command that moves the Hero by a pre-scaled velocity vector.
 *
 * <p>{@code dx} and {@code dy} are already multiplied by speed (pixels/sec);
 * this command multiplies by {@code delta} to get per-frame displacement.
 *
 * <p>Pattern: Command (GoF #10).
 */
public final class MoveCommand implements ICommand {

    private final Hero  hero;
    private final float dx;
    private final float dy;
    private final float delta;

    /**
     * @param hero  the hero to move
     * @param dx    horizontal velocity (pixels/sec), positive = right
     * @param dy    vertical velocity (pixels/sec), positive = up
     * @param delta frame time in seconds
     */
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
