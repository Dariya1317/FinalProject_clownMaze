package com.example.clownmaze.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.clownmaze.core.riddles.BaseRiddle;
import com.example.clownmaze.core.riddles.ChoiceRiddle;
import com.example.clownmaze.core.riddles.RiddleType;

public final class RiddleOverlay implements Disposable {

    private static final float FEEDBACK_DURATION = 0.5f;
    private static final float PANEL_W = 460f;

    private final Stage stage;
    private final Skin skin;

    private BaseRiddle currentRiddle;
    private boolean open;
    private float feedbackTimer;
    private boolean lastCorrect;

    private TextField    textField;
    private TextButton[] choiceButtons;

    public RiddleOverlay() {
        stage = new Stage(new ScreenViewport());
        skin = buildSkin();
    }

    public void show(BaseRiddle riddle) {
        if (riddle == null || riddle.isSolved()) return;
        currentRiddle = riddle;
        feedbackTimer = 0f;
        lastCorrect = false;
        open = true;
        riddle.display();
        rebuild();
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        open = false;
        currentRiddle = null;
        stage.clear();
        Gdx.input.setInputProcessor(null);
    }

    public void update(float dt) {
        if (!open) return;
        stage.act(dt);

        if (feedbackTimer > 0f) {
            feedbackTimer -= dt;
            if (feedbackTimer <= 0f) {
                if (lastCorrect) hide();
                else resetColors();
            }
            return;
        }

        if (currentRiddle != null
                && currentRiddle.getType() == RiddleType.TEXT
                && textField != null
                && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            submitText();
        }
    }

    public void render() {
        if (!open) return;
        stage.draw();
    }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    public boolean isOpen() { return open; }
    private void rebuild() {
        stage.clear();
        textField     = null;
        choiceButtons = null;

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("px", new Color(0f, 0f, 0f, 0.72f)));
        stage.addActor(root);

        Table panel = new Table(skin);
        panel.setBackground(skin.newDrawable("px", new Color(0.08f, 0.04f, 0.04f, 0.96f)));
        panel.pad(24f).defaults().space(12f);

        Label q = new Label(currentRiddle.getQuestion(), skin);
        q.setWrap(true);
        panel.add(q).width(PANEL_W - 48f).padBottom(8f).row();

        if (currentRiddle.getType() == RiddleType.TEXT) {
            addTextField(panel);
        } else {
            addChoices(panel);
        }

        root.add(panel).width(PANEL_W);
    }

    private void addTextField(Table panel) {
        textField = new TextField("", skin);
        textField.setMessageText("type answer here…");
        panel.add(textField).width(PANEL_W - 48f).height(38f).row();

        TextButton submit = new TextButton("SUBMIT [ENTER]", skin);
        submit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { submitText(); }
        });
        panel.add(submit).width(200f).height(40f);
        stage.setKeyboardFocus(textField);
    }

    private void addChoices(Table panel) {
        ChoiceRiddle cr = (ChoiceRiddle) currentRiddle;
        int size = cr.getChoices().size();
        choiceButtons = new TextButton[size];

        Table grid = new Table();
        grid.defaults().width(192f).height(46f).space(8f);

        for (int i = 0; i < size; i++) {
            final String choice = cr.getChoices().get(i);
            TextButton btn = new TextButton(choice, skin);
            choiceButtons[i] = btn;
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    submitChoice(choice, btn);
                }
            });
            grid.add(btn);
            if ((i + 1) % 2 == 0) grid.row();
        }
        panel.add(grid);
    }
    private void submitText() {
        if (feedbackTimer > 0f || textField == null) return;
        currentRiddle.submit(textField.getText());
        boolean correct = currentRiddle.isSolved();
        textField.setColor(correct ? Color.GREEN : Color.RED);
        scheduleFeedback(correct);
    }

    private void submitChoice(String choice, TextButton pressed) {
        if (feedbackTimer > 0f) return;
        currentRiddle.submit(choice);
        boolean correct = currentRiddle.isSolved();
        pressed.setColor(correct ? Color.GREEN : Color.RED);
        scheduleFeedback(correct);
    }

    private void scheduleFeedback(boolean correct) {
        feedbackTimer = FEEDBACK_DURATION;
        lastCorrect = correct;
    }

    private void resetColors() {
        if (textField != null) textField.setColor(Color.WHITE);
        if (choiceButtons != null) {
            for (TextButton b : choiceButtons) b.setColor(Color.WHITE);
        }
    }

    private static Skin buildSkin() {
        Skin skin = new Skin();
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("px", new Texture(px));
        px.dispose();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.15f);
        skin.add("default-font", font);
        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.fontColor = Color.WHITE;
        tbs.up = skin.newDrawable("px", new Color(0.28f, 0.10f, 0.10f, 1f));
        tbs.down = skin.newDrawable("px", new Color(0.45f, 0.20f, 0.20f, 1f));
        tbs.over = skin.newDrawable("px", new Color(0.38f, 0.15f, 0.15f, 1f));
        skin.add("default", tbs);
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = font;
        tfs.fontColor = Color.WHITE;
        tfs.focusedFontColor = Color.YELLOW;
        tfs.background = skin.newDrawable("px", new Color(0.15f, 0.06f, 0.06f, 1f));
        tfs.cursor = skin.newDrawable("px", Color.WHITE);
        tfs.selection = skin.newDrawable("px", new Color(0.5f, 0.3f, 0.3f, 0.5f));
        skin.add("default", tfs);

        return skin;
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
