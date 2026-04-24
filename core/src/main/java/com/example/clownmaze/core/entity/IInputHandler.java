package com.example.clownmaze.core.entity;

/**
 * Input handler abstraction.
 *
 * <p>GameScreen depends on this interface, not on the concrete
 * {@link PlayerInputHandler} — satisfying the Dependency Inversion Principle.
 *
 * <p>Pattern: Strategy / Command (GoF #8 / #10).
 */
public interface IInputHandler {
    /**
     * Reads device input and dispatches the appropriate {@link ICommand}s to the Hero.
     *
     * @param delta elapsed time in seconds since the last frame
     */
    void handleInput(float delta);
}
