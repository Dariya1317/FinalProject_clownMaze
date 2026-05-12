package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Full-screen level title card shown for DURATION seconds before gameplay begins.
 * Disposes itself automatically when hidden (single-use screen).
 */
public class LevelIntroScreen implements Screen {

    private static final float DURATION = 2.5f;

    private final Game        game;
    private final Texture     bg;
    private final Screen      nextScreen;
    private final SpriteBatch batch;
    private float             elapsed;

    /**
     * @param game        the libGDX Game instance used for screen switching
     * @param texturePath path relative to assets root, e.g. "ui/level1.png"
     * @param nextScreen  screen to transition to after DURATION seconds
     */
    public LevelIntroScreen(Game game, String texturePath, Screen nextScreen) {
        this.game       = game;
        this.bg         = new Texture(Gdx.files.internal(texturePath));
        this.nextScreen = nextScreen;
        this.batch      = new SpriteBatch();
    }

    @Override
    public void show() {
        elapsed = 0f;
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();
        batch.draw(bg, 0, 0, w, h);
        batch.end();

        if (elapsed >= DURATION) {
            game.setScreen(nextScreen);
        }
    }

    @Override
    public void resize(int w, int h) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    /** Auto-dispose when the framework hides this screen (it is single-use). */
    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        bg.dispose();
    }
}
