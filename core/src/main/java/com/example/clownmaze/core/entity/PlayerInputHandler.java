package com.example.clownmaze.core.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public final class PlayerInputHandler implements IInputHandler {

    private final Hero hero;

    public PlayerInputHandler(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void handleInput(float delta) {
        handleMovement(delta);
        handleInteraction();
    }

    private void handleMovement(float delta) {
        float dx = 0f, dy = 0f;

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    dy += 1f;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  dy -= 1f;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  dx -= 1f;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) dx += 1f;

        if (dx == 0f && dy == 0f) return;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        dx /= len;
        dy /= len;

        boolean wantsRun = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
                        || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);

        float speed = hero.currentSpeed(wantsRun);
        new MoveCommand(hero, dx * speed, dy * speed, delta).execute();
    }

    private void handleInteraction() {
        if (Gdx.input.isKeyJustPressed(Keys.E)) {
            new InteractCommand(hero).execute();
        }
    }
}
