package com.example.clownmaze.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

import com.example.clownmaze.audio.AudioManager;

public class MainMenuScreen implements Screen {

    private static final float GLITCH_DURATION = 0.3f;

    // ── Instruction popup pages ───────────────────────────────────────────────

    private static final String[] POPUP_PAGES = {
        "=== LEVEL 1: THE TRIAL ===\n\n"
        + "ENVIRONMENT: A dark room with stone walls. Your only light source is a torch.\n\n"
        + "CLOWN: Waits in the corner for 40 seconds. Then rushes straight at you.\n"
        + "One touch = one life lost (you have 3).\n\n"
        + "SPIDERS: Contact triggers a jumpscare (0.5 sec), 3-sec slow, and -5 sec.\n\n"
        + "GHOSTS: Getting close darkens the screen edges (vignette).\n\n"
        + "TASKS: Find the ancient scroll on a desk. Press E and solve math riddles.\n\n"
        + "GOAL: Solve all tasks before time runs out.",

        "=== LEVEL 2: THE TRAP ===\n\n"
        + "ENVIRONMENT: Gloomier room, cracked walls, flickering torches.\n\n"
        + "CLOWN: Now patrols the room. After 90 seconds switches to aggressive chase.\n\n"
        + "SPIDERS: Same as Level 1 (jumpscare + slow + -5 sec).\n\n"
        + "GHOSTS: On contact they FREEZE you for 5 seconds. You cannot move,\n"
        + "but can still interact. The ghost then disappears.\n\n"
        + "TASKS: Find the old computer terminal. Answer Java syntax questions\n"
        + "with multiple-choice answers.\n\n"
        + "GOAL: Answer all questions while dodging the patrolling clown.",

        "=== LEVEL 3: THE NIGHTMARE ===\n\n"
        + "ENVIRONMENT: An arena with chains and eerie yellow glow.\n\n"
        + "CLOWN: Patrols from the start, most aggressive. Timer: 180 sec.\n\n"
        + "GHOSTS: On contact they take ONE life and disappear.\n\n"
        + "SPECIAL TASKS:\n"
        + "  HOLD     - Hold E on the ritual orb for 3 seconds.\n"
        + "  COLLECT  - Gather 3 skull-keys that appear in the room.\n"
        + "  SEQUENCE - Activate three runes in the correct order.\n\n"
        + "EFFECTS: Heartbeat sound when timer drops below 10 seconds.\n\n"
        + "GOAL: Complete all tasks without losing all three lives."
    };

    private static final int POPUP_PAGE_COUNT = POPUP_PAGES.length;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final Game        game;
    private final SpriteBatch batch;
    private final Texture     bgTex;
    private final Texture     logoTex;
    private final Texture     pixelTex;

    // ── Button textures ───────────────────────────────────────────────────────
    private final Texture startDefaultTex;
    private final Texture startHoverTex;
    private final Texture instructDefaultTex;
    private final Texture instructHoverTex;
    private final Texture exitDefaultTex;
    private final Texture exitHoverTex;

    // ── Button hitboxes ───────────────────────────────────────────────────────
    private final Rectangle startBounds    = new Rectangle();
    private final Rectangle instructBounds = new Rectangle();
    private final Rectangle exitBounds     = new Rectangle();

    // ── Menu state ────────────────────────────────────────────────────────────
    private boolean  startHovered;
    private boolean  instructHovered;
    private boolean  exitHovered;
    private boolean  glitchActive;
    private float    glitchTimer;

    // ── Click flash (all buttons glow briefly after any click) ────────────────
    private static final float CLICK_FLASH  = 0.18f;
    private float              clickFlashTimer = 0f;
    private Runnable           clickAction     = null;

    // ── Popup state ───────────────────────────────────────────────────────────
    private final Texture    popupScrollTex;
    private final BitmapFont popupFont;
    private final BitmapFont popupNavFont;
    private boolean          popupOpen = false;
    private int              popupPage = 0;

    // Popup navigation hitboxes (recomputed each frame)
    private final Rectangle popupPrevBounds  = new Rectangle();
    private final Rectangle popupNextBounds  = new Rectangle();
    private final Rectangle popupCloseBounds = new Rectangle();

    // ─────────────────────────────────────────────────────────────────────────

    public MainMenuScreen(Game game) {
        this.game = game;
        batch   = new SpriteBatch();
        bgTex   = new Texture(Gdx.files.internal("ui/main_menu_bg.png"));
        logoTex = new Texture(Gdx.files.internal("ui/logo_title.png"));

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixelTex = new Texture(pm);
        pm.dispose();

        startDefaultTex    = loadTexture("images/button_start_default.png");
        startHoverTex      = loadTexture("images/button_start_hover.png");
        instructDefaultTex = loadTexture("images/button_instruction_default.png");
        instructHoverTex   = loadTexture("images/button_instruction_hover.png");
        exitDefaultTex     = loadTexture("images/button_exit_default.png");
        exitHoverTex       = loadTexture("images/button_exit_hover.png");

        popupScrollTex = loadTexture("images/popup_scroll_bg.png");
        popupFont    = new BitmapFont();
        popupFont.getData().setScale(0.82f);
        popupFont.setColor(new Color(0.15f, 0.08f, 0.04f, 1f)); // dark ink colour
        popupNavFont = new BitmapFont();
        popupNavFont.getData().setScale(0.95f);

        computeButtonBounds(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // ── Image loading ─────────────────────────────────────────────────────────

    private static Texture loadTexture(String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("MainMenuScreen",
                "Файл не найден: <проект>/assets/" + path + "  ←  положи PNG сюда");
            return null;
        }
        return new Texture(Gdx.files.internal(path));
    }

    // ── Button layout ─────────────────────────────────────────────────────────

    private static float[] scaledSize(Texture tex, float maxW, float maxH) {
        if (tex == null) return new float[]{0f, 0f};
        float scale = Math.min(1f, Math.min(maxW / tex.getWidth(), maxH / tex.getHeight()));
        return new float[]{tex.getWidth() * scale, tex.getHeight() * scale};
    }

    private void computeButtonBounds(int w, int h) {
        // Horizontal row: START | INSTRUCTION | EXIT
        // Each button capped at 30 % of screen width and 35 % of screen height
        float maxBW = w * 0.30f;
        float maxBH = h * 0.35f;

        float[] sSize = scaledSize(startDefaultTex,    maxBW, maxBH);
        float[] iSize = scaledSize(instructDefaultTex, maxBW, maxBH);
        float[] eSize = scaledSize(exitDefaultTex,     maxBW, maxBH);

        float gap    = 25f;
        float totalW = sSize[0] + iSize[0] + eSize[0] + 2f * gap;

        // Row vertically centred between logo bottom (≈ h * 0.55) and screen bottom
        float rowCenterY = h * 0.30f;

        float sx = (w - totalW) / 2f;
        float ix = sx + sSize[0] + gap;
        float ex = ix + iSize[0] + gap;

        startBounds  .set(sx, rowCenterY - sSize[1] / 2f, sSize[0], sSize[1]);
        instructBounds.set(ix, rowCenterY - iSize[1] / 2f, iSize[0], iSize[1]);
        exitBounds   .set(ex, rowCenterY - eSize[1] / 2f, eSize[0], eSize[1]);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        AudioManager.getInstance().playMainMenu();
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        // ── Glitch countdown ──────────────────────────────────────────────────
        if (glitchActive) {
            glitchTimer -= delta;
            if (glitchTimer <= 0f) { Gdx.app.exit(); return; }
        }

        // ── Click flash: count down → fire stored action when done ───────────
        if (clickFlashTimer > 0f) {
            clickFlashTimer -= delta;
            if (clickFlashTimer <= 0f && clickAction != null) {
                Runnable action = clickAction;
                clickAction = null;
                action.run(); // may switch screen — render below still runs once
            }
        }

        // ── Hover update (frozen during flash so glow doesn't flicker) ────────
        if (!glitchActive && !popupOpen && clickFlashTimer <= 0f) {
            startHovered    = startBounds.contains(mx, my);
            instructHovered = instructBounds.contains(mx, my);
            exitHovered     = exitBounds.contains(mx, my);
        }

        // ── Click → start flash, store action (ignored during active flash) ───
        if (!glitchActive && !popupOpen && clickFlashTimer <= 0f
                && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (startHovered)    { clickFlashTimer = CLICK_FLASH; clickAction = this::launchGame; }
            if (instructHovered) { clickFlashTimer = CLICK_FLASH; clickAction = () -> game.setScreen(new InstructionsScreen(game)); }
            if (exitHovered)     { clickFlashTimer = CLICK_FLASH; clickAction = this::triggerGlitch; }
        }

        // ── Popup + keyboard (unchanged) ─────────────────────────────────────
        if (!glitchActive && popupOpen)  handlePopupInput(mx, my);
        if (!glitchActive && !popupOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))  { launchGame();    return; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { triggerGlitch(); }
        }

        // ── Render ────────────────────────────────────────────────────────────
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();

        batch.draw(bgTex, 0, 0, w, h);

        float logoH = h * 0.35f;
        float logoW = logoH * ((float) logoTex.getWidth() / logoTex.getHeight());
        batch.draw(logoTex, (w - logoW) / 2f, h * 0.55f, logoW, logoH);

        // All buttons glow together for the entire flash window
        boolean glow = clickFlashTimer > 0f && !popupOpen;

        if (startDefaultTex != null) {
            Texture t = (glow && startHoverTex != null) ? startHoverTex : startDefaultTex;
            batch.draw(t, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        }
        if (instructDefaultTex != null) {
            Texture t = (glow && instructHoverTex != null) ? instructHoverTex : instructDefaultTex;
            batch.draw(t, instructBounds.x, instructBounds.y, instructBounds.width, instructBounds.height);
        }
        if (exitDefaultTex != null) {
            Texture t = (glow && exitHoverTex != null) ? exitHoverTex : exitDefaultTex;
            batch.draw(t, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
        }

        if (glitchActive) renderGlitch(w, h);
        if (popupOpen)    renderPopup(w, h);

        batch.end();
    }

    // ── Popup ─────────────────────────────────────────────────────────────────

    private void openPopup() {
        popupOpen = true;
        popupPage = 0;
    }

    private void closePopup() {
        popupOpen = false;
    }

    private void handlePopupInput(float mx, float my) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { closePopup(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)  ||
            Gdx.input.isKeyJustPressed(Input.Keys.A))     { if (popupPage > 0) popupPage--; return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.D))     { if (popupPage < POPUP_PAGE_COUNT - 1) popupPage++; return; }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (popupCloseBounds.contains(mx, my)) { closePopup(); return; }
            if (popupPrevBounds.contains(mx, my) && popupPage > 0)                    popupPage--;
            if (popupNextBounds.contains(mx, my) && popupPage < POPUP_PAGE_COUNT - 1) popupPage++;
        }
    }

    private void renderPopup(int w, int h) {
        // ── Dim the menu behind the popup ─────────────────────────────────────
        batch.setColor(0f, 0f, 0f, 0.65f);
        batch.draw(pixelTex, 0f, 0f, w, h);
        batch.setColor(Color.WHITE);

        if (popupScrollTex == null) return;

        // ── Scale scroll to fit the screen (max 72 % wide, 82 % tall) ────────
        float scrollW = Math.min(w * 0.72f, popupScrollTex.getWidth());
        float scrollH = scrollW * ((float) popupScrollTex.getHeight() / popupScrollTex.getWidth());
        if (scrollH > h * 0.82f) {
            scrollH = h * 0.82f;
            scrollW = scrollH * ((float) popupScrollTex.getWidth() / popupScrollTex.getHeight());
        }
        float scrollX = (w - scrollW) / 2f;
        float scrollY = (h - scrollH) / 2f;

        batch.draw(popupScrollTex, scrollX, scrollY, scrollW, scrollH);

        // ── Text area (inside scroll borders) ────────────────────────────────
        // Horizontal: 11 % margin each side; vertical: 14 % top, 16 % bottom
        float textX = scrollX + scrollW * 0.11f;
        float textW = scrollW * 0.78f;
        float textTop = scrollY + scrollH * 0.86f; // below scroll top border

        // Page indicator (e.g.  "< 2 / 3 >")
        String indicator = (popupPage > 0 ? "< " : "  ")
                         + "LEVEL " + (popupPage + 1) + " / " + POPUP_PAGE_COUNT
                         + (popupPage < POPUP_PAGE_COUNT - 1 ? " >" : "  ");
        GlyphLayout indicLayout = new GlyphLayout(popupNavFont, indicator);
        popupNavFont.setColor(new Color(0.55f, 0.18f, 0.06f, 1f));
        popupNavFont.draw(batch, indicator,
            scrollX + (scrollW - indicLayout.width) / 2f,
            textTop + indicLayout.height + 6f);

        // Main content
        popupFont.draw(batch, POPUP_PAGES[popupPage],
            textX, textTop, textW, Align.left, true);

        // ── Navigation buttons ────────────────────────────────────────────────
        float navY   = scrollY + scrollH * 0.10f;
        float navBtnW = 110f;
        float navBtnH = 28f;

        float prevX  = scrollX + scrollW * 0.12f;
        float closeX = scrollX + (scrollW - navBtnW) / 2f;
        float nextX  = scrollX + scrollW * 0.88f - navBtnW;

        popupPrevBounds .set(prevX,  navY, navBtnW, navBtnH);
        popupCloseBounds.set(closeX, navY, navBtnW, navBtnH);
        popupNextBounds .set(nextX,  navY, navBtnW, navBtnH);

        // Draw button backgrounds
        Color btnBg = new Color(0.20f, 0.07f, 0.03f, 0.85f);
        drawNavBtn(prevX,  navY, navBtnW, navBtnH,
            popupPage > 0 ? "< PREV" : "",                      btnBg, popupPage > 0);
        drawNavBtn(closeX, navY, navBtnW, navBtnH,
            "CLOSE [ESC]",                                        btnBg, true);
        drawNavBtn(nextX,  navY, navBtnW, navBtnH,
            popupPage < POPUP_PAGE_COUNT - 1 ? "NEXT >" : "",   btnBg,
            popupPage < POPUP_PAGE_COUNT - 1);
    }

    /** Renders a single nav button: dark background + centred label. */
    private void drawNavBtn(float x, float y, float bw, float bh,
                            String label, Color bg, boolean active) {
        if (label.isEmpty()) return;

        batch.setColor(bg);
        batch.draw(pixelTex, x, y, bw, bh);
        batch.setColor(Color.WHITE);

        Color fc = active
            ? new Color(0.95f, 0.85f, 0.65f, 1f)
            : new Color(0.45f, 0.40f, 0.35f, 1f);
        GlyphLayout gl = new GlyphLayout(popupNavFont, label);
        popupNavFont.setColor(fc);
        popupNavFont.draw(batch, label, x + (bw - gl.width) / 2f, y + (bh + gl.height) / 2f);
        popupNavFont.setColor(Color.WHITE);
    }

    // ── Glitch effect ─────────────────────────────────────────────────────────

    private void renderGlitch(int w, int h) {
        batch.setColor(0f, 0f, 0f, 0.72f);
        batch.draw(pixelTex, 0f, 0f, w, h);

        int count = 20 + MathUtils.random(25);
        for (int i = 0; i < count; i++) {
            float y    = MathUtils.random(0f, h);
            float barH = MathUtils.random(1f, 20f);
            float x    = MathUtils.random(-40f, 0f);
            float barW = w + MathUtils.random(0f, 80f);
            boolean isRed = MathUtils.randomBoolean(0.45f);
            float r = isRed ? MathUtils.random(0.75f, 1f)  : MathUtils.random(0.88f, 1f);
            float g = isRed ? MathUtils.random(0f, 0.05f)  : MathUtils.random(0.88f, 1f);
            float b = isRed ? MathUtils.random(0f, 0.05f)  : MathUtils.random(0.88f, 1f);
            batch.setColor(r, g, b, MathUtils.random(0.55f, 1f));
            batch.draw(pixelTex, x, y, barW, barH);
        }
        batch.setColor(Color.WHITE);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void launchGame() {
        game.setScreen(new LevelIntroScreen(game, "ui/level1.png", new GameScreen(game)));
    }

    private void triggerGlitch() {
        if (!glitchActive) { glitchActive = true; glitchTimer = GLITCH_DURATION; }
    }

    // ── Screen boilerplate ────────────────────────────────────────────────────

    @Override
    public void resize(int w, int h) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        computeButtonBounds(w, h);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        bgTex.dispose();
        logoTex.dispose();
        pixelTex.dispose();
        popupFont.dispose();
        popupNavFont.dispose();
        if (startDefaultTex    != null) startDefaultTex.dispose();
        if (startHoverTex      != null) startHoverTex.dispose();
        if (instructDefaultTex != null) instructDefaultTex.dispose();
        if (instructHoverTex   != null) instructHoverTex.dispose();
        if (exitDefaultTex     != null) exitDefaultTex.dispose();
        if (exitHoverTex       != null) exitHoverTex.dispose();
        if (popupScrollTex     != null) popupScrollTex.dispose();
    }
}
