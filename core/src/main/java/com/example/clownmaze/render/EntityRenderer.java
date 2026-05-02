package com.example.clownmaze.render;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import com.example.clownmaze.core.entity.Hero;
import com.example.clownmaze.core.entity.ai.ClownAI;

/**
 * Loads sprite animations and draws Hero and ClownAI each frame.
 * Animation selection is based on input state (hero) and AI state (clown).
 */
public final class EntityRenderer implements Disposable {

    // Hero animation speeds (seconds per frame)
    private static final float FPS_IDLE  = 0.25f;
    private static final float FPS_WALK  = 0.10f;
    private static final float FPS_RUN   = 0.07f;
    // Clown
    private static final float FPS_CLOWN_IDLE = 0.50f;
    private static final float FPS_CLOWN_WALK = 0.06f;

    private final Animation<TextureRegion> heroIdle;
    private final Animation<TextureRegion> heroWalk;
    private final Animation<TextureRegion> heroRun;
    private final Animation<TextureRegion> clownIdle;
    private final Animation<TextureRegion> clownWalk;

    // All loaded textures — disposed together
    private final List<Texture> textures = new ArrayList<>();

    private float heroTime  = 0f;
    private float clownTime = 0f;

    public EntityRenderer() {
        heroIdle  = load("sprites/hero/hero_idle/idle_",    3,  FPS_IDLE);
        heroWalk  = load("sprites/hero/hero_walk/walk_",    7,  FPS_WALK);
        heroRun   = load("sprites/hero/hero_run/run_",      8,  FPS_RUN);
        clownIdle = load("sprites/clown/clown_idle/idle_",  2,  FPS_CLOWN_IDLE);
        clownWalk = load("sprites/clown/clown_walk/walk_",  14, FPS_CLOWN_WALK);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update(float dt) {
        heroTime  += dt;
        clownTime += dt;
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void renderHero(SpriteBatch batch, Hero hero) {
        boolean moving  = isMoving();
        boolean running = moving && isRunning();

        Animation<TextureRegion> anim =
            running ? heroRun : moving ? heroWalk : heroIdle;

        TextureRegion frame = anim.getKeyFrame(heroTime, true);

        // Blueish tint when slowed by a spider
        if (hero.isSlowed()) {
            batch.setColor(0.6f, 0.7f, 1.0f, 1f);
        }

        batch.draw(frame,
            hero.getX(), hero.getY(),
            Hero.SPRITE_WIDTH * 2, Hero.SPRITE_HEIGHT * 2);

        batch.setColor(Color.WHITE); // restore default tint
    }

    public void renderClown(SpriteBatch batch, ClownAI clown) {
        Animation<TextureRegion> anim = clown.isIdle() ? clownIdle : clownWalk;
        TextureRegion frame = anim.getKeyFrame(clownTime, true);

        // Red tint when in KILL state
        if (clown.isKilling()) {
            batch.setColor(1f, 0.3f, 0.3f, 1f);
        }

        batch.draw(frame,
            clown.getX(), clown.getY(),
            Hero.SPRITE_WIDTH * 2, Hero.SPRITE_HEIGHT * 2);

        batch.setColor(Color.WHITE);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static boolean isMoving() {
        return Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.S)
            || Gdx.input.isKeyPressed(Input.Keys.D)
            || Gdx.input.isKeyPressed(Input.Keys.UP)
            || Gdx.input.isKeyPressed(Input.Keys.DOWN)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    private static boolean isRunning() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    /**
     * Loads {@code count} frames from {@code prefix + "01.png"} … {@code "NN.png"}.
     * All textures are registered for disposal.
     */
    private Animation<TextureRegion> load(String prefix, int count, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            String path = prefix + String.format("%02d", i + 1) + ".png";
            Texture tex = new Texture(Gdx.files.internal(path));
            textures.add(tex);
            frames[i] = new TextureRegion(tex);
        }
        return new Animation<>(frameDuration, frames);
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
    }
}
