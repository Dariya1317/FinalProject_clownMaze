package com.example.clownmaze.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Overlay for Level-2 mini-games: "remember the color" and "repeat the sequence".
 * Call showMemory() or showSequence(), then update(dt) + render() each frame.
 */
public final class MiniGameOverlay implements Disposable {

    public interface ResultHandler {
        void onSolved();
        void onPenalty();
    }

    private enum TaskType { MEMORY, SEQUENCE }

    // Sequence variants: the order in which buttons 0/1/2 (A/B/C) must be clicked
    private static final int[][] SEQ_VARIANTS = {
        {1, 2, 0},   // B >C >A
        {2, 0, 1},   // C >A >B
        {0, 2, 1}    // A >C >B
    };

    private static final Color[]  COLORS      = {
        new Color(0.85f, 0.08f, 0.08f, 1f),   // red
        new Color(0.08f, 0.12f, 0.88f, 1f),   // blue
        new Color(0.08f, 0.65f, 0.08f, 1f)    // green
    };
    private static final String[] COLOR_NAMES    = { "RED", "BLUE", "GREEN" };
    // Labels deliberately mismatched to confuse — RED bg says "BLUE", etc.
    private static final String[] MISLEAD_NAMES = { "BLUE", "GREEN", "RED" };

    private static final float FLASH_MEMORY   = 1.0f;
    private static final float FLASH_SEQUENCE = 3.0f;
    private static final float FEEDBACK_DELAY = 0.5f;

    private final Stage stage;
    private final Skin  skin;

    private boolean       open;
    private TaskType      taskType;
    private boolean       flashPhase;
    private float         flashTimer;
    private int           correctIndex;   // correct button for MEMORY
    private int[]         seqOrder;       // required click order for SEQUENCE
    private int           seqProgress;
    private ResultHandler handler;
    private TextButton[]  buttons;
    private float         feedbackTimer;
    private boolean       feedbackCorrect;

    public MiniGameOverlay() {
        stage = new Stage(new ScreenViewport());
        skin  = buildSkin();
    }

    // ── Open methods ─────────────────────────────────────────────────────────

    /** Show "remember the color" task. correctColor: 0=RED, 1=BLUE, 2=GREEN. */
    public void showMemory(int correctColor, ResultHandler h) {
        taskType      = TaskType.MEMORY;
        correctIndex  = correctColor;
        handler       = h;
        flashTimer    = FLASH_MEMORY;
        flashPhase    = true;
        feedbackTimer = 0f;
        open          = true;
        buildFlashMemory(correctColor);
        Gdx.input.setInputProcessor(stage);
    }

    /** Show "repeat the sequence" task. variant: 0-2 selects the sequence. */
    public void showSequence(int variant, ResultHandler h) {
        seqOrder      = SEQ_VARIANTS[Math.abs(variant) % SEQ_VARIANTS.length];
        seqProgress   = 0;
        taskType      = TaskType.SEQUENCE;
        handler       = h;
        flashTimer    = FLASH_SEQUENCE;
        flashPhase    = true;
        feedbackTimer = 0f;
        open          = true;
        buildFlashSequence(seqOrder);
        Gdx.input.setInputProcessor(stage);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void update(float dt) {
        if (!open) return;
        stage.act(dt);

        if (feedbackTimer > 0f) {
            feedbackTimer -= dt;
            if (feedbackTimer <= 0f) {
                if (feedbackCorrect) {
                    hide();
                    handler.onSolved();
                } else {
                    resetButtonColors();
                    handler.onPenalty();
                }
            }
            return;
        }

        if (flashPhase) {
            flashTimer -= dt;
            if (flashTimer <= 0f) {
                flashPhase = false;
                buildInputPhase();
            }
        }
    }

    public void render() {
        if (!open) return;
        stage.draw();
    }

    public void hide() {
        open = false;
        stage.clear();
        Gdx.input.setInputProcessor(null);
    }

    public boolean isOpen() { return open; }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    // ── Stage builders ────────────────────────────────────────────────────────

    private void buildFlashMemory(int colorIdx) {
        stage.clear();
        Table root  = makeRoot();
        Table panel = makePanel();
        root.add(panel).width(360f);

        panel.add(lbl("Remember this color!", "default")).padBottom(18f).row();

        Table block = new Table();
        block.setBackground(skin.newDrawable("px", COLORS[colorIdx]));
        block.add(lbl(COLOR_NAMES[colorIdx], "big")).pad(14f);
        panel.add(block).width(220f).height(100f).row();
    }

    private void buildFlashSequence(int[] seq) {
        stage.clear();
        Table root  = makeRoot();
        Table panel = makePanel();
        root.add(panel).width(420f);

        panel.add(lbl("Remember the sequence!", "default")).padBottom(18f).row();
        String seqText = letter(seq[0]) + " > " + letter(seq[1]) + " > " + letter(seq[2]);
        panel.add(lbl(seqText, "big")).padBottom(10f).row();
        panel.add(lbl("Then repeat it in order", "small")).row();
    }

    private void buildInputPhase() {
        if (taskType == TaskType.MEMORY) buildInputMemory();
        else                             buildInputSequence();
    }

    private void buildInputMemory() {
        stage.clear();
        Table root  = makeRoot();
        Table panel = makePanel();
        root.add(panel).width(500f);

        panel.add(lbl("Which color was it?", "default")).padBottom(18f).row();

        Table grid = new Table();
        buttons = new TextButton[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextButton btn = new TextButton(MISLEAD_NAMES[i], skin, "col" + i);
            buttons[i] = btn;
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (feedbackTimer > 0f) return;
                    btn.setColor(idx == correctIndex ? Color.GREEN : Color.RED);
                    feedbackCorrect = (idx == correctIndex);
                    feedbackTimer   = FEEDBACK_DELAY;
                }
            });
            grid.add(btn).width(142f).height(72f).pad(7f);
        }
        panel.add(grid).row();
    }

    private void buildInputSequence() {
        stage.clear();
        Table root  = makeRoot();
        Table panel = makePanel();
        root.add(panel).width(500f);

        panel.add(lbl("Click the buttons in the correct order!", "small")).padBottom(14f).row();

        Table grid = new Table();
        buttons = new TextButton[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextButton btn = new TextButton(letter(i), skin);
            buttons[i] = btn;
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (feedbackTimer > 0f) return;
                    onSeqClick(idx);
                }
            });
            grid.add(btn).width(120f).height(72f).pad(7f);
        }
        panel.add(grid).row();
    }

    private void onSeqClick(int idx) {
        if (seqOrder[seqProgress] == idx) {
            buttons[idx].setColor(Color.GREEN);
            seqProgress++;
            if (seqProgress >= seqOrder.length) {
                feedbackCorrect = true;
                feedbackTimer   = FEEDBACK_DELAY;
            }
        } else {
            seqProgress = 0;
            buttons[idx].setColor(Color.RED);
            feedbackCorrect = false;
            feedbackTimer   = FEEDBACK_DELAY;
        }
    }

    private void resetButtonColors() {
        if (buttons == null) return;
        for (TextButton b : buttons) b.setColor(Color.WHITE);
        if (taskType == TaskType.SEQUENCE) seqProgress = 0;
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private Table makeRoot() {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("px", new Color(0f, 0f, 0f, 0.78f)));
        stage.addActor(root);
        return root;
    }

    private Table makePanel() {
        Table panel = new Table(skin);
        panel.setBackground(skin.newDrawable("px", new Color(0.06f, 0.03f, 0.09f, 0.97f)));
        panel.pad(30f).defaults().space(10f);
        return panel;
    }

    private Label lbl(String text, String style) {
        return new Label(text, skin, style);
    }

    private static String letter(int idx) {
        return String.valueOf((char) ('A' + idx));
    }

    // ── Skin ──────────────────────────────────────────────────────────────────

    private static Skin buildSkin() {
        Skin skin = new Skin();

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("px", new Texture(px));
        px.dispose();

        BitmapFont normal = new BitmapFont();
        normal.getData().setScale(1.15f);
        skin.add("nfont", normal);
        skin.add("default", new Label.LabelStyle(normal, Color.WHITE));

        BitmapFont big = new BitmapFont();
        big.getData().setScale(1.9f);
        skin.add("bfont", big);
        skin.add("big", new Label.LabelStyle(big, Color.YELLOW));

        BitmapFont small = new BitmapFont();
        small.getData().setScale(0.9f);
        skin.add("sfont", small);
        skin.add("small", new Label.LabelStyle(small, new Color(0.8f, 0.8f, 0.8f, 1f)));

        // Default button
        TextButton.TextButtonStyle def = new TextButton.TextButtonStyle();
        def.font      = normal;
        def.fontColor = Color.WHITE;
        def.up   = skin.newDrawable("px", new Color(0.30f, 0.10f, 0.10f, 1f));
        def.down = skin.newDrawable("px", new Color(0.50f, 0.22f, 0.22f, 1f));
        def.over = skin.newDrawable("px", new Color(0.40f, 0.16f, 0.16f, 1f));
        skin.add("default", def);

        // Coloured styles for memory task buttons (col0=red, col1=blue, col2=green)
        Color[] bases  = {
            new Color(0.55f, 0.06f, 0.06f, 1f),
            new Color(0.06f, 0.06f, 0.60f, 1f),
            new Color(0.06f, 0.46f, 0.06f, 1f)
        };
        Color[] hovers = {
            new Color(0.75f, 0.14f, 0.14f, 1f),
            new Color(0.14f, 0.14f, 0.80f, 1f),
            new Color(0.14f, 0.62f, 0.14f, 1f)
        };
        for (int i = 0; i < 3; i++) {
            TextButton.TextButtonStyle cs = new TextButton.TextButtonStyle();
            cs.font      = normal;
            cs.fontColor = Color.WHITE;
            cs.up   = skin.newDrawable("px", bases[i]);
            cs.down = skin.newDrawable("px", hovers[i]);
            cs.over = skin.newDrawable("px", hovers[i]);
            skin.add("col" + i, cs);
        }
        return skin;
    }
}
