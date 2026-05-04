package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {

    private final Game        game;
    private final GameScreen  gameScreen;
    private final SpriteBatch batch;
    private final Texture     gameOverTex;
    private final BitmapFont  font;
    private final GlyphLayout layout;

    public GameOverScreen(Game game, GameScreen gameScreen) {
        this.game        = game;
        this.gameScreen  = gameScreen;
        this.batch       = new SpriteBatch();
        this.gameOverTex = new Texture(Gdx.files.internal("ui/game_over.png"));
        this.font        = new BitmapFont();
        this.layout      = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();
        batch.draw(gameOverTex, 0, 0, w, h);

        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        layout.setText(font, "PRESS ENTER TO RETURN TO MENU");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.12f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameScreen.dispose();
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override public void show() {}

    @Override
    public void resize(int w, int h) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        gameOverTex.dispose();
        font.dispose();
    }
}
