package com.example.clownmaze.core.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Reads keyboard input each frame and dispatches {@link ICommand}s to the {@link Hero}.
 *
 * <h3>Responsibilities (SRP)</h3>
 * Only translates raw key events into commands — contains no game logic.
 *
 * <h3>Controls</h3>
 * <ul>
 *   <li>WASD / Arrow keys — movement</li>
 *   <li>Left/Right Shift + WASD — run (ignored when SLOWED)</li>
 *   <li>E — interact (pick up key, answer riddle, open door)</li>
 *   <li>Any key while TRAPPED — mash to escape</li>
 * </ul>
 *
 * <p>Patterns: Command (GoF #10) — each action becomes an {@link ICommand} object.
 */
public final class PlayerInputHandler implements IInputHandler {

    /**
     * Keys that count as a mash press while the hero is trapped.
     * Using a fixed set avoids polling every key code each frame.
     */
    private static final int[] MASH_KEYS = {
        Keys.SPACE, Keys.E,
        Keys.W, Keys.A, Keys.S, Keys.D,
        Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT
    };

    private final Hero hero;

    public PlayerInputHandler(Hero hero) {
        this.hero = hero;
    }

    // ------------------------------------------------------------------ //
    //  IInputHandler                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Must be called once per frame from GameScreen, before rendering.
     *
     * @param delta elapsed time in seconds since the last frame
     */
    @Override
    public void handleInput(float delta) {
        handleMash();
        handleMovement(delta);
        handleInteraction();
    }

    // ------------------------------------------------------------------ //
    //  Private handlers                                                    //
    // ------------------------------------------------------------------ //

    /** One mash press per frame maximum; breaks after first detected keypress. */
    private void handleMash() {
        if (!hero.isTrapped()) return;
        for (int key : MASH_KEYS) {
            if (Gdx.input.isKeyJustPressed(key)) {
                new MashCommand(hero).execute();
                break;
            }
        }
    }

    private void handleMovement(float delta) {
        if (hero.isTrapped()) return;

        float dx = 0f, dy = 0f;

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    dy += 1f;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  dy -= 1f;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  dx -= 1f;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) dx += 1f;

        if (dx == 0f && dy == 0f) return;

        // Normalise so diagonal movement isn't faster than cardinal
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        dx /= len;
        dy /= len;

        boolean running = !hero.isSlowed()
            && (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
             || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT));

        float speed = running ? Hero.RUN_SPEED : Hero.WALK_SPEED;
        new MoveCommand(hero, dx * speed, dy * speed, delta).execute();
    }

    private void handleInteraction() {
        if (Gdx.input.isKeyJustPressed(Keys.E)) {
            new InteractCommand(hero).execute();
        }
    }
}
