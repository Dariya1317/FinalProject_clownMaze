package com.example.clownmaze.core.entity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.badlogic.gdx.math.MathUtils;
import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;

public final class Spider {

    public static final float CONTACT_RADIUS  = 12f;
    public static final float SCREAMER_LENGTH = 0.5f;

    private static final float SPEED          = 35f;
    private static final float WANDER_RADIUS  = 90f;
    private static final float DIR_MIN        = 1.0f;
    private static final float DIR_MAX        = 2.5f;

    private static final AtomicInteger ID_SEQ = new AtomicInteger();

    private final int   id;
    private final float spawnX, spawnY;

    private float x, y;
    private float velX, velY;
    private float dirTimer;

    private boolean triggered;
    private float   screamerLeft;

    private final Consumer<EventBus.RoomResetEvent> onReset;
    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter;

    public Spider(float x, float y) {
        this.id     = ID_SEQ.incrementAndGet();
        this.spawnX = x;
        this.spawnY = y;
        this.x      = x;
        this.y      = y;

        pickDirection();

        onReset     = e -> rearm();
        onRoomEnter = e -> rearm();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomResetEvent.class, onReset);
        bus.subscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
    }

    public void update(float delta, float heroX, float heroY) {
        if (screamerLeft > 0f) {
            screamerLeft = Math.max(0f, screamerLeft - delta);
            if (screamerLeft == 0f) {
                EventBus.getInstance().publish(
                    new EventBus.ScreamerEndEvent(EventBus.ScreamerSource.SPIDER));
            }
        }

        if (triggered) return;

        dirTimer -= delta;
        if (dirTimer <= 0f) pickDirection();

        x += velX * delta;
        y += velY * delta;

        float left   = spawnX - WANDER_RADIUS;
        float right  = spawnX + WANDER_RADIUS;
        float bottom = spawnY - WANDER_RADIUS;
        float top    = spawnY + WANDER_RADIUS;

        if (x < left)  { x = left;   velX = Math.abs(velX); }
        if (x > right) { x = right;  velX = -Math.abs(velX); }
        if (y < bottom){ y = bottom; velY = Math.abs(velY); }
        if (y > top)   { y = top;    velY = -Math.abs(velY); }

        float dx = heroX - x;
        float dy = heroY - y;
        if (dx * dx + dy * dy <= CONTACT_RADIUS * CONTACT_RADIUS) {
            trigger();
        }
    }

    private void pickDirection() {
        float angle = MathUtils.random(MathUtils.PI2);
        velX = MathUtils.cos(angle) * SPEED;
        velY = MathUtils.sin(angle) * SPEED;
        dirTimer = MathUtils.random(DIR_MIN, DIR_MAX);
    }

    private void trigger() {
        triggered    = true;
        screamerLeft = SCREAMER_LENGTH;
        GameStateManager.getInstance().reduceTimer(5f);
        EventBus bus = EventBus.getInstance();
        bus.publish(new EventBus.ScreamerStartEvent(EventBus.ScreamerSource.SPIDER));
        bus.publish(new EventBus.SpiderContactEvent(id, x, y));
    }

    private void rearm() {
        triggered    = false;
        screamerLeft = 0f;
        x = spawnX;
        y = spawnY;
        pickDirection();
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomResetEvent.class, onReset);
        bus.unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
    }

    public int     getId()            { return id; }
    public float   getX()             { return x; }
    public float   getY()             { return y; }
    public boolean isTriggered()      { return triggered; }
    public boolean isScreamerActive() { return screamerLeft > 0f; }
    public float   getScreamerLeft()  { return screamerLeft; }
}
