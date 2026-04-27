package com.example.clownmaze.core.entity;

import java.util.function.Consumer;

import com.example.clownmaze.core.EventBus;


public final class Hero {

    public static final float WALK_SPEED = 64f;

    public static final float RUN_SPEED  = 112f;

    public static final float TRAP_DURATION = 5f;

    public static final float SLOW_DURATION = 3f;

    public static final int MASH_REQUIRED = 8;

    public static final int SPRITE_WIDTH  = 16;

    public static final int SPRITE_HEIGHT = 32;

    private static final float COLLISION_RADIUS = 6f;

    private float x, y;
    private HeroState state = HeroState.NORMAL;

    private float trapTimer;
    private float slowTimer;
    private int mashCount;

    private final WalkabilityChecker collisionCheck;

    private final Consumer<EventBus.TrapCaughtEvent> trapHandler;
    private final Consumer<EventBus.SpiderContactEvent> slowHandler;

    public Hero(float startX, float startY, WalkabilityChecker collisionCheck) {
        this.x = startX;
        this.y = startY;
        this.collisionCheck = collisionCheck;

        trapHandler = e -> trap();
        slowHandler = e -> slow();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.TrapCaughtEvent.class, trapHandler);
        bus.subscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    public void update(float delta) {
        switch (state) {
            case TRAPPED -> tickTrapped(delta);
            case SLOWED  -> tickSlowed(delta);
            default -> {}
        }
    }

    private void tickTrapped(float delta) {
        trapTimer -= delta;
        if (trapTimer <= 0f) {
            state = HeroState.NORMAL;
            mashCount = 0;
            EventBus.getInstance().publish(new EventBus.TrapFailEvent(x, y));
        }
    }

    private void tickSlowed(float delta) {
        slowTimer -= delta;
        if (slowTimer <= 0f) {
            state = HeroState.NORMAL;
        }
    }

    public void attemptMove(float dx, float dy) {
        if (state == HeroState.TRAPPED) return;
        if (canMoveTo(x + dx, y)) x += dx;
        if (canMoveTo(x, y + dy)) y += dy;
    }

    private boolean canMoveTo(float cx, float cy) {
        float r = COLLISION_RADIUS;
        return collisionCheck.isWalkable(cx - r, cy)
            && collisionCheck.isWalkable(cx + r, cy)
            && collisionCheck.isWalkable(cx, cy - r)
            && collisionCheck.isWalkable(cx, cy + r);
    }

    public void trap() {
        if (state == HeroState.TRAPPED) return;
        state = HeroState.TRAPPED;
        trapTimer = TRAP_DURATION;
        mashCount = 0;
    }

    public void slow() {
        if (state == HeroState.TRAPPED) return;
        state = HeroState.SLOWED;
        slowTimer = SLOW_DURATION;
    }

    public void mash() {
        if (state != HeroState.TRAPPED) return;
        mashCount++;
        if (mashCount >= MASH_REQUIRED) {
            state = HeroState.NORMAL;
            mashCount = 0;
            trapTimer = 0f;
            EventBus.getInstance().publish(new EventBus.TrapEscapeEvent(x, y));
        }
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.TrapCaughtEvent.class, trapHandler);
        bus.unsubscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public HeroState getState() { return state; }
    public float getTrapTimer() { return trapTimer; }
    public float getSlowTimer() { return slowTimer; }
    public int getMashCount() { return mashCount; }
    public boolean isTrapped() { return state == HeroState.TRAPPED; }
    public boolean isSlowed() { return state == HeroState.SLOWED; }
}
