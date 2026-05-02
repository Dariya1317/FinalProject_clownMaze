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

public class WinScreen implements Screen {

    private final Game        game;
    private final GameScreen  gameScreen;
    private final SpriteBatch batch;
    private final BitmapFont  font;
    private final GlyphLayout layout;

    public WinScreen(Game game, GameScreen gameScreen) {
        this.game       = game;
        this.gameScreen = gameScreen;
        this.batch      = new SpriteBatch();
        this.font       = new BitmapFont();
        this.layout     = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0.05f, 0f, 1f);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        batch.begin();

        font.getData().setScale(3f);
        font.setColor(Color.GREEN);
        layout.setText(font, "YOU ESCAPED!");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.65f);

        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "PRESS ENTER TO RETURN TO MENU");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.40f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameScreen.dispose();
            game.setScreen(new MainMenuScreen(game));
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
