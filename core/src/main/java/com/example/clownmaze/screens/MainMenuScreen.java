package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {

    private final Game        game;
    private final SpriteBatch batch;
    private final BitmapFont  font;
    private final GlyphLayout layout;

    public MainMenuScreen(Game game) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.font   = new BitmapFont();
        this.layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.02f, 0.02f, 1f);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        batch.begin();

        font.getData().setScale(3f);
        font.setColor(Color.RED);
        layout.setText(font, "CLOWN MAZE");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.65f);

        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "PRESS ENTER TO START");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.40f);

        layout.setText(font, "PRESS ESC TO QUIT");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.30f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void show()               {}
    @Override public void resize(int w, int h) {}
    @Override public void pause()              {}
    @Override public void resume()             {}
    @Override public void hide()               {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
