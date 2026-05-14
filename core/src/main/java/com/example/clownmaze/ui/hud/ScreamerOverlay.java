package com.example.clownmaze.ui.hud;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import com.example.clownmaze.core.EventBus;

public final class ScreamerOverlay implements Disposable {

    private static final float CLOWN_DURATION  = 3f;
    private static final float SPIDER_DURATION = 0.5f;

    private final Texture     clownTex;
    private final Texture     spiderTex;
    private final SpriteBatch batch;

    private EventBus.ScreamerSource activeSource;
    private float timer;

    private final Consumer<EventBus.ScreamerStartEvent> onStart;

    public ScreamerOverlay() {
        clownTex  = new Texture(Gdx.files.internal("screamer/screamer_clown.png"));
        spiderTex = new Texture(Gdx.files.internal("screamer/screamer_spider.png"));
        batch     = new SpriteBatch();

        onStart = e -> {
            activeSource = e.source();
            timer = (e.source() == EventBus.ScreamerSource.CLOWN)
                    ? CLOWN_DURATION : SPIDER_DURATION;
        };
        EventBus.getInstance().subscribe(EventBus.ScreamerStartEvent.class, onStart);
    }

    public void update(float dt) {
        if (timer > 0f) timer = Math.max(0f, timer - dt);
    }

    public void render() {
        if (timer <= 0f || activeSource == null) return;

        Texture tex = (activeSource == EventBus.ScreamerSource.CLOWN) ? clownTex : spiderTex;
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();
        batch.draw(tex, 0, 0, w, h);
        batch.end();
    }

    public void resize(int w, int h) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(EventBus.ScreamerStartEvent.class, onStart);
        clownTex.dispose();
        spiderTex.dispose();
        batch.dispose();
    }
}

