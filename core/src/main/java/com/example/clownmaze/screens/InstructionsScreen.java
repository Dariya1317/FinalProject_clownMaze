package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Full-screen instruction carousel.
 * Four pages: General → Level 1 → Level 2 → Level 3.
 * Navigation is entirely mouse-driven (PREV / NEXT / BACK TO MENU).
 */
public final class InstructionsScreen implements Screen {

    private static final int      PAGE_COUNT  = 4;
    private static final String[] IMAGE_PATHS = {
        "images/instruction_general.png",
        "images/instruction_level1.png",
        "images/instruction_level2.png",
        "images/instruction_level3.png"
    };

    // ── Resources ─────────────────────────────────────────────────────────────
    private final Game      game;
    private final Texture[] textures = new Texture[PAGE_COUNT];
    private final Stage     stage;
    private final Skin      skin;

    // ── Scene2D widgets ───────────────────────────────────────────────────────
    private final Image      bgImage;
    private final TextButton prevBtn;
    private final TextButton nextBtn;

    // ── State ─────────────────────────────────────────────────────────────────
    private int     currentPage = 0;
    private boolean disposed    = false;

    // ─────────────────────────────────────────────────────────────────────────

    public InstructionsScreen(Game game) {
        this.game = game; // stored for goBack()

        // Load all page textures (null-safe: missing files are logged, not crashed)
        for (int i = 0; i < PAGE_COUNT; i++) {
            if (Gdx.files.internal(IMAGE_PATHS[i]).exists()) {
                textures[i] = new Texture(Gdx.files.internal(IMAGE_PATHS[i]));
            } else {
                Gdx.app.error("InstructionsScreen",
                    "Missing asset: assets/" + IMAGE_PATHS[i]);
            }
        }

        stage = new Stage(new ScreenViewport());
        skin  = buildSkin();

        // ── Background image — full screen, fills its cell ────────────────────
        bgImage = new Image();
        bgImage.setScaling(Scaling.fill);

        // ── Navigation bar ────────────────────────────────────────────────────
        prevBtn             = new TextButton("<- Previous", skin, "nav");
        nextBtn             = new TextButton("Next ->",     skin, "nav");
        TextButton backBtn  = new TextButton("Back to Menu",    skin, "nav");

        prevBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (currentPage > 0) { currentPage--; updatePage(); }
            }
        });
        nextBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (currentPage < PAGE_COUNT - 1) { currentPage++; updatePage(); }
            }
        });
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                // postRunnable defers setScreen until after stage.act() finishes
                Gdx.app.postRunnable(InstructionsScreen.this::goBack);
            }
        });

        Table navBar = new Table(skin);
        navBar.setBackground(skin.newDrawable("pixel", new Color(0f, 0f, 0f, 0.55f)));
        navBar.pad(10f, 24f, 10f, 24f);
        navBar.add(prevBtn).width(160f).height(46f).padRight(18f);
        navBar.add(backBtn).width(200f).height(46f).padRight(18f);
        navBar.add(nextBtn).width(160f).height(46f);

        // ── Layout ────────────────────────────────────────────────────────────
        // bgWrapper fills the whole screen with the current page image
        Table bgWrapper = new Table();
        bgWrapper.setFillParent(true);
        bgWrapper.add(bgImage).grow();

        // navWrapper sits on top at the bottom edge
        Table navWrapper = new Table();
        navWrapper.setFillParent(true);
        navWrapper.bottom();
        navWrapper.add(navBar).growX();

        stage.addActor(bgWrapper);
        stage.addActor(navWrapper);

        updatePage();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void goBack() {
        game.setScreen(new MainMenuScreen(game));
    }

    /** Syncs the background image and button disabled-state with currentPage. */
    private void updatePage() {
        if (textures[currentPage] != null) {
            bgImage.setDrawable(
                new TextureRegionDrawable(new TextureRegion(textures[currentPage])));
        } else {
            bgImage.setDrawable(null);
        }
        prevBtn.setDisabled(currentPage == 0);
        nextBtn.setDisabled(currentPage == PAGE_COUNT - 1);
    }

    /** Builds a minimal Skin containing the nav-button style. */
    private static Skin buildSkin() {
        Skin skin = new Skin();

        // 1×1 white pixel used as a tintable background drawable
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("pixel", new Texture(px));
        px.dispose();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.05f);
        skin.add("default-font", font);

        TextButton.TextButtonStyle nav = new TextButton.TextButtonStyle();
        nav.font             = font;
        nav.fontColor        = new Color(0.95f, 0.90f, 0.82f, 1f);
        nav.overFontColor    = Color.WHITE;
        nav.downFontColor    = new Color(0.60f, 0.22f, 0.22f, 1f);
        nav.disabledFontColor = new Color(0.38f, 0.36f, 0.34f, 1f);
        nav.up       = skin.newDrawable("pixel", new Color(0.12f, 0.04f, 0.04f, 0.88f));
        nav.over     = skin.newDrawable("pixel", new Color(0.30f, 0.08f, 0.08f, 0.94f));
        nav.down     = skin.newDrawable("pixel", new Color(0.22f, 0.06f, 0.06f, 1.00f));
        nav.disabled = skin.newDrawable("pixel", new Color(0.07f, 0.05f, 0.05f, 0.60f));
        skin.add("nav", nav);

        return skin;
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        currentPage = 0;
        updatePage();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        for (Texture t : textures) { if (t != null) t.dispose(); }
        stage.dispose();
        skin.dispose();
    }
}
