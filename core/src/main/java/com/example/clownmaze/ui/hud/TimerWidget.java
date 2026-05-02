package com.example.clownmaze.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.clownmaze.core.GameStateManager;

public final class TimerWidget implements Disposable {

    private static final float WARN_YELLOW = 20f;
    private static final float WARN_RED    = 10f;
    private final Stage stage;
    private final Skin  skin;
    private final Label roomLabel;
    private final Label timeLabel;
    private final Label riddleLabel;

    public TimerWidget() {
        stage = new Stage(new ScreenViewport());
        skin = buildSkin();

        roomLabel = new Label("ROOM 1",  skin, "title");
        timeLabel = new Label("0:40",    skin, "timer");
        riddleLabel = new Label("0 / 2",   skin, "small");
        Table panel = new Table(skin);
        panel.setBackground(skin.newDrawable("px", new Color(0f, 0f, 0f, 0.55f)));
        panel.pad(8f, 14f, 8f, 14f).defaults().align(Align.right);

        panel.add(roomLabel).row();
        panel.add(timeLabel).row();
        panel.add(riddleLabel);

        Table root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.add(panel).pad(12f);

        stage.addActor(root);
    }

    public void update() {
        GameStateManager gsm = GameStateManager.getInstance();
        int roomId = gsm.getCurrentRoom();
        float left = gsm.getRoomTimeLeft(roomId);
        int solved = gsm.riddlesSolvedIn(roomId);
        int total  = GameStateManager.ROOM_RIDDLE_COUNT
                                                  .getOrDefault(roomId, 0);

        roomLabel.setText("ROOM " + roomId);
        timeLabel.setText(formatTime(left));
        riddleLabel.setText(solved + " / " + total + " riddles");

        if (left <= WARN_RED) {
            timeLabel.setColor(Color.RED);
        } else if (left <= WARN_YELLOW) {
            timeLabel.setColor(Color.YELLOW);
        } else {
            timeLabel.setColor(Color.WHITE);
        }

        stage.act();
    }

    public void render() {
        stage.draw();
    }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    private static String formatTime(float seconds) {
        int total = (int) Math.ceil(seconds);
        if (total < 0) total = 0;
        return String.format("%d:%02d", total / 60, total % 60);
    }

    private static Skin buildSkin() {
        Skin skin = new Skin();

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("px", new Texture(px));
        px.dispose();

        BitmapFont title = new BitmapFont();
        title.getData().setScale(1.1f);
        skin.add("title-font", title);
        skin.add("title", new Label.LabelStyle(title, Color.LIGHT_GRAY));

        BitmapFont timer = new BitmapFont();
        timer.getData().setScale(2.0f);
        skin.add("timer-font", timer);
        skin.add("timer", new Label.LabelStyle(timer, Color.WHITE));

        BitmapFont small = new BitmapFont();
        small.getData().setScale(0.9f);
        skin.add("small-font", small);
        skin.add("small", new Label.LabelStyle(small, new Color(0.8f, 0.8f, 0.8f, 1f)));

        return skin;
    }
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
