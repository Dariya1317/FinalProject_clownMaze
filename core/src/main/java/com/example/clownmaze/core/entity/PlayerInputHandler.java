package com.example.clownmaze.core.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public final class PlayerInputHandler implements IInputHandler {

    private static final int[] MASH_KEYS = {
        Keys.SPACE, Keys.E,
        Keys.W, Keys.A, Keys.S, Keys.D,
        Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT
    };

    private final Hero hero;

    public PlayerInputHandler(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void handleInput(float delta) {
        handleMash();
        handleMovement(delta);
        handleInteraction();
    }

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
