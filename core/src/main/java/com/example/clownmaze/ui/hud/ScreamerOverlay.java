package com.example.clownmaze.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/** Full-screen red flash shown while the clown's screamer is active. */
public final class ScreamerOverlay implements Disposable {

    private static final float SCREAMER_DURATION = 3f;

    private final ShapeRenderer shapes = new ShapeRenderer();

    /** Call every frame; alpha fades from 0.85 → 0 as screamerTimer counts down. */
    public void render(boolean active, float screamerTimer) {
        if (!active || screamerTimer <= 0f) return;

        float alpha = 0.85f * (screamerTimer / SCREAMER_DURATION);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.7f, 0f, 0f, alpha);
        shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() { shapes.dispose(); }
}
