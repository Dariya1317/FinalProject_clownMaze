package com.example.clownmaze.core.entity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.example.clownmaze.core.EventBus;

public final class Spider {

    public static final float CONTACT_RADIUS  = 10f;
    public static final float SCREAMER_LENGTH = 0.5f;

    private static final AtomicInteger ID_SEQ = new AtomicInteger();

    private final int id;
    private final float x, y;

    private boolean triggered;
    private float   screamerLeft;

    private final Consumer<EventBus.RoomResetEvent> onReset;
    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter;

    public Spider(float x, float y) {
        this.id = ID_SEQ.incrementAndGet();
        this.x  = x;
        this.y  = y;

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

        float dx = heroX - x;
        float dy = heroY - y;
        if (dx * dx + dy * dy <= CONTACT_RADIUS * CONTACT_RADIUS) {
            trigger();
        }
    }

    private void trigger() {
        triggered    = true;
        screamerLeft = SCREAMER_LENGTH;
        EventBus bus = EventBus.getInstance();
        bus.publish(new EventBus.ScreamerStartEvent(EventBus.ScreamerSource.SPIDER));
        bus.publish(new EventBus.SpiderContactEvent(id, x, y));
    }

    private void rearm() {
        triggered    = false;
        screamerLeft = 0f;
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomResetEvent.class, onReset);
        bus.unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
    }

    public int     getId()           { return id; }
    public float   getX()            { return x; }
    public float   getY()            { return y; }
    public boolean isTriggered()     { return triggered; }
    public boolean isScreamerActive(){ return screamerLeft > 0f; }
    public float   getScreamerLeft() { return screamerLeft; }
}
