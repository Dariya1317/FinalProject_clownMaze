package com.example.clownmaze.core.entity;

import java.util.function.Consumer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import com.example.clownmaze.core.EventBus;

public final class Hero {

    public static final float WALK_SPEED    = 64f;
    public static final float RUN_SPEED     = 112f;
    public static final float SLOW_DURATION = 3f;

    public static final int   SPRITE_WIDTH  = 16;
    public static final int   SPRITE_HEIGHT = 32;

    private static final float EDGE_PADDING = 4f;

    private final BaseSpeedProvider baseSpeed = new BaseSpeedProvider(WALK_SPEED, RUN_SPEED);

    private float x, y;
    private HeroState state = HeroState.NORMAL;

    private SpeedProvider   currentSpeed = baseSpeed;
    private SlowedDecorator slowed;
    private float           freezeTimer  = 0f;

    private Rectangle bounds;

    private final Consumer<EventBus.SpiderContactEvent> slowHandler;

    public Hero(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        slowHandler = e -> applySlow();
        EventBus.getInstance()
            .subscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
        clampToBounds();
    }

    public void update(float delta) {
        // Freeze countdown (takes priority over slow)
        if (freezeTimer > 0f) {
            freezeTimer -= delta;
            if (freezeTimer <= 0f) {
                freezeTimer = 0f;
                // Restore to SLOWED if slow is still active, otherwise NORMAL
                state = (slowed != null) ? HeroState.SLOWED : HeroState.NORMAL;
            }
        }
        if (slowed != null) {
            slowed.tick(delta);
            if (slowed.isExpired()) {
                currentSpeed = slowed.unwrap();
                slowed = null;
                if (state != HeroState.FROZEN) state = HeroState.NORMAL;
            }
        }
    }

    public void freeze(float duration) {
        freezeTimer = duration;
        state = HeroState.FROZEN;
    }

    public boolean isFrozen() { return state == HeroState.FROZEN; }

    public void attemptMove(float dx, float dy) {
        x += dx;
        y += dy;
        clampToBounds();
    }

    private void clampToBounds() {
        if (bounds == null) return;
        float minX = bounds.x + EDGE_PADDING;
        float maxX = bounds.x + bounds.width  - SPRITE_WIDTH  - EDGE_PADDING;
        float minY = bounds.y + EDGE_PADDING;
        float maxY = bounds.y + bounds.height - SPRITE_HEIGHT - EDGE_PADDING;
        if (maxX < minX) maxX = minX;
        if (maxY < minY) maxY = minY;
        x = MathUtils.clamp(x, minX, maxX);
        y = MathUtils.clamp(y, minY, maxY);
    }

    public float currentSpeed(boolean wantsRun) {
        return currentSpeed.speedFor(wantsRun);
    }

    public void applySlow() {
        if (slowed != null) {
            slowed = new SlowedDecorator(slowed.unwrap(), SLOW_DURATION);
        } else {
            slowed = new SlowedDecorator(baseSpeed, SLOW_DURATION);
        }
        currentSpeed = slowed;
        state = HeroState.SLOWED;
    }

    public void teleportTo(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        if (slowed != null) {
            currentSpeed = slowed.unwrap();
            slowed = null;
        }
        state = HeroState.NORMAL;
        clampToBounds();
    }

    public void dispose() {
        EventBus.getInstance()
            .unsubscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    public float     getX()         { return x; }
    public float     getY()         { return y; }
    public Rectangle getBounds()    { return bounds; }
    public HeroState getState()     { return state; }
    public boolean   isSlowed()     { return state == HeroState.SLOWED; }
    public float     getSlowTimer() { return slowed == null ? 0f : slowed.getRemaining(); }
}
