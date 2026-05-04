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

public class MainMenuScreen implements Screen {

    private final Game        game;
    private final SpriteBatch batch;
    private final BitmapFont  font;
    private final GlyphLayout layout;
    private final Texture     bgTex;
    private final Texture     logoTex;

    public MainMenuScreen(Game game) {
        this.game    = game;
        this.batch   = new SpriteBatch();
        this.font    = new BitmapFont();
        this.layout  = new GlyphLayout();
        this.bgTex   = new Texture(Gdx.files.internal("ui/main_menu_bg.png"));
        this.logoTex = new Texture(Gdx.files.internal("ui/logo_title.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();

        // background
        batch.draw(bgTex, 0, 0, w, h);

        // logo — centred, occupies top 35% of screen
        float logoH = h * 0.35f;
        float logoW = logoH * ((float) logoTex.getWidth() / logoTex.getHeight());
        batch.draw(logoTex, (w - logoW) / 2f, h * 0.55f, logoW, logoH);

        // instructions
        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "PRESS ENTER TO START");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.38f);

        layout.setText(font, "PRESS ESC TO QUIT");
        font.draw(batch, layout, (w - layout.width) / 2f, h * 0.28f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
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
        font.dispose();
        bgTex.dispose();
        logoTex.dispose();
    }
}
