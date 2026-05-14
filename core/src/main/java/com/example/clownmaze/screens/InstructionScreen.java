package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public final class InstructionScreen implements Screen {

    private static final String[] PAGES = {
        "=== LEVEL 1: THE TRIAL ===\n\n"
        + "ENVIRONMENT: A dark room with stone walls.\n"
        + "Your only light source is a torch.\n\n"
        + "CLOWN: Stands in the bottom-left corner and waits for the\n"
        + "timer to run out (40 seconds). Once time expires, it rushes\n"
        + "directly toward you. A single touch takes one life (3 total).\n\n"
        + "SPIDERS: On contact - a brief jumpscare (0.5 sec) and a\n"
        + "3-second slow effect. Also reduce the timer by 5 seconds.\n\n"
        + "GHOSTS: Float around the room. Getting close triggers a slight\n"
        + "screen darkening (vignette), making it harder to see.\n\n"
        + "TASKS: Find the ancient scroll on a desk and interact (press E).\n"
        + "Solve math problems and riddles by typing your answer.\n\n"
        + "GOAL: Solve all tasks before time runs out while avoiding\n"
        + "the clown and other hazards.",

        "=== LEVEL 2: THE TRAP ===\n\n"
        + "ENVIRONMENT: A gloomier room with cracked walls and\n"
        + "flickering torches.\n\n"
        + "CLOWN: No longer stationary! It slowly patrols the room.\n"
        + "After the timer (90 sec) expires, it switches to aggressive\n"
        + "chase mode.\n\n"
        + "SPIDERS: Same as Level 1 (jumpscare + slow + -5 sec).\n\n"
        + "GHOSTS: On contact they FREEZE you for 5 seconds.\n"
        + "You cannot move but can still interact with objects.\n"
        + "The ghost then disappears.\n\n"
        + "TASKS: Find the old computer terminal. Answer Java syntax\n"
        + "questions with multiple-choice answers.\n\n"
        + "GOAL: Survive by answering questions while dodging\n"
        + "the patrolling clown.",

        "=== LEVEL 3: THE NIGHTMARE ===\n\n"
        + "ENVIRONMENT: An arena with chains and an eerie yellow glow.\n"
        + "Full immersion in horror.\n\n"
        + "CLOWN: Patrols from the very start, even more aggressive.\n"
        + "The timer (180 sec) adds constant pressure.\n\n"
        + "GHOSTS: BEHAVIOR CHANGED! On contact they take away one\n"
        + "life (heart) and disappear.\n\n"
        + "SPECIAL TASKS (activated through objects):\n"
        + "  HOLD - Activate the ritual orb and hold E for 3 seconds.\n"
        + "         Releasing resets the progress.\n"
        + "  COLLECT - After activation, 3 glowing skull-keys appear.\n"
        + "            Collect them all.\n"
        + "  SEQUENCE - Three runes must be activated in the correct\n"
        + "             order (briefly shown at the start).\n\n"
        + "EFFECTS: If the timer drops below 10 seconds, a tense\n"
        + "heartbeat sound kicks in.\n\n"
        + "GOAL: Overcome all trials without losing your remaining\n"
        + "lives, and defeat the clown."
    };

    private static final int PAGE_COUNT = PAGES.length;

    private final Game        game;
    private final Screen      returnTo;
    private final SpriteBatch bgBatch;
    private final Texture[]   bgTex = new Texture[PAGE_COUNT];

    private final Stage      stage;
    private final Skin       skin;
    private final Label      pageIndicator;
    private final Label      contentLabel;
    private final TextButton prevBtn;
    private final TextButton nextBtn;

    private int     page     = 0;
    private boolean disposed = false;

    public InstructionScreen(Game game, Screen returnTo) {
        this.game     = game;
        this.returnTo = returnTo;
        bgBatch = new SpriteBatch();

        String[] bgPaths = {"ui/level1.png", "ui/level2.png", "ui/level3.png"};
        for (int i = 0; i < PAGE_COUNT; i++) {
            if (Gdx.files.internal(bgPaths[i]).exists())
                bgTex[i] = new Texture(Gdx.files.internal(bgPaths[i]));
        }

        stage = new Stage(new ScreenViewport());
        skin  = buildSkin();

        pageIndicator = new Label("1 / 3", skin, "indicator");
        contentLabel  = new Label(PAGES[0], skin, "body");
        contentLabel.setWrap(true);

        prevBtn                    = new TextButton("< PREV",       skin, "nav");
        nextBtn                    = new TextButton("NEXT >",       skin, "nav");
        TextButton backBtn         = new TextButton("BACK TO MENU", skin, "nav");

        prevBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { changePage(-1); }
        });
        nextBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { changePage(+1); }
        });
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                Gdx.app.postRunnable(() -> game.setScreen(returnTo));
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.bottom();

        Table panel = new Table(skin);
        panel.setBackground(skin.newDrawable("px", new Color(0f, 0f, 0f, 0.82f)));
        panel.pad(16f, 32f, 12f, 32f);

        panel.add(pageIndicator).center().padBottom(8f).row();

        ScrollPane scroll = new ScrollPane(contentLabel, skin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        panel.add(scroll).growX().height(230f).row();

        Table nav = new Table();
        nav.defaults().height(40f).pad(5f, 10f, 5f, 10f);
        nav.add(prevBtn).width(150f);
        nav.add(backBtn).width(210f);
        nav.add(nextBtn).width(150f);
        panel.add(nav).center().padTop(10f);

        root.add(panel).growX().padLeft(40f).padRight(40f).padBottom(20f);
        stage.addActor(root);

        updatePage();
    }

    private static Skin buildSkin() {
        Skin skin = new Skin();

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("px", new Texture(px));
        px.dispose();

        BitmapFont bodyFont = new BitmapFont();
        bodyFont.getData().setScale(0.92f);
        skin.add("body-font", bodyFont);
        skin.add("body", new Label.LabelStyle(bodyFont,
            new Color(0.95f, 0.90f, 0.85f, 1f)));

        BitmapFont indicFont = new BitmapFont();
        indicFont.getData().setScale(1.15f);
        skin.add("indic-font", indicFont);
        skin.add("indicator", new Label.LabelStyle(indicFont,
            new Color(0.85f, 0.25f, 0.25f, 1f)));

        BitmapFont navFont = new BitmapFont();
        navFont.getData().setScale(0.88f);
        skin.add("nav-font", navFont);

        TextButton.TextButtonStyle nav = new TextButton.TextButtonStyle();
        nav.font             = navFont;
        nav.fontColor        = new Color(0.90f, 0.90f, 0.90f, 1f);
        nav.overFontColor    = Color.WHITE;
        nav.downFontColor    = new Color(0.55f, 0.15f, 0.15f, 1f);
        nav.disabledFontColor = new Color(0.38f, 0.38f, 0.38f, 1f);
        nav.up       = skin.newDrawable("px", new Color(0.14f, 0.04f, 0.04f, 0.92f));
        nav.over     = skin.newDrawable("px", new Color(0.32f, 0.08f, 0.08f, 0.96f));
        nav.down     = skin.newDrawable("px", new Color(0.22f, 0.06f, 0.06f, 1.00f));
        nav.disabled = skin.newDrawable("px", new Color(0.08f, 0.04f, 0.04f, 0.70f));
        skin.add("nav", nav);

        skin.add("default", new ScrollPane.ScrollPaneStyle());

        return skin;
    }

    private void changePage(int delta) {
        int next = page + delta;
        if (next < 0 || next >= PAGE_COUNT) return;
        page = next;
        updatePage();
    }

    private void updatePage() {
        contentLabel.setText(PAGES[page]);
        pageIndicator.setText("LEVEL " + (page + 1) + " / " + PAGE_COUNT);
        prevBtn.setDisabled(page == 0);
        nextBtn.setDisabled(page == PAGE_COUNT - 1);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        page = 0;
        updatePage();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        bgBatch.begin();
        if (bgTex[page] != null) bgBatch.draw(bgTex[page], 0, 0, w, h);
        bgBatch.end();

        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.postRunnable(() -> game.setScreen(returnTo));
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)  ||
            Gdx.input.isKeyJustPressed(Input.Keys.A))     changePage(-1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.D))     changePage(+1);
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
        bgBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
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
        bgBatch.dispose();
        for (Texture t : bgTex) { if (t != null) t.dispose(); }
        stage.dispose();
        skin.dispose();
    }
}

