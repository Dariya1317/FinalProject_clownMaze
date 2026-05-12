package com.example.clownmaze.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Screen-space vignette: four dark semi-transparent bars along screen edges.
 * Used on Level 1 when a ghost is within proximity of the hero.
 * Call render() between batch.end() and the next batch.begin().
 */
public final class VignetteEffect implements Disposable {

    private static final float EDGE_FRAC = 0.14f; // fraction of screen per edge
    private static final float ALPHA     = 0.55f;

    private final ShapeRenderer shapes = new ShapeRenderer();

    public void render(int screenW, int screenH) {
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float ex = screenW * EDGE_FRAC;
        float ey = screenH * EDGE_FRAC;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, ALPHA);
        shapes.rect(0f,          0f,          ex,                screenH);          // left
        shapes.rect(screenW - ex, 0f,          ex,                screenH);          // right
        shapes.rect(ex,           0f,          screenW - 2f * ex, ey);               // bottom
        shapes.rect(ex,           screenH - ey, screenW - 2f * ex, ey);              // top
        shapes.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
