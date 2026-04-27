package com.example.clownmaze.core.traps;

import java.util.function.Consumer;

import com.example.clownmaze.core.EventBus;

public final class Trap {

    public static final float TRIGGER_RADIUS = 8f;
    private final float x, y;
    private final TrapIdleState idleState;
    private final TrapActiveState activeState;
    private final TrapDisabledState disabledState;
    private TrapState currentState;

    private final Consumer<EventBus.TrapEscapeEvent> onEscape;
    private final Consumer<EventBus.TrapFailEvent> onFail;

    public Trap(float x, float y) {
        this.x = x;
        this.y = y;

        idleState = new TrapIdleState();
        activeState = new TrapActiveState();
        disabledState = new TrapDisabledState();
        currentState = idleState;

        onEscape = e -> { if (currentState == activeState) setState(disabledState); };
        onFail = e -> { if (currentState == activeState) setState(disabledState); };

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.TrapEscapeEvent.class, onEscape);
        bus.subscribe(EventBus.TrapFailEvent.class,   onFail);
    }

    public void update(float delta, float heroX, float heroY) {
        currentState.update(this, delta, heroX, heroY);
    }

    void trigger() {
        setState(activeState);
        EventBus.getInstance().publish(new EventBus.TrapCaughtEvent(x, y));
    }

    void setState(TrapState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.TrapEscapeEvent.class, onEscape);
        bus.unsubscribe(EventBus.TrapFailEvent.class, onFail);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isIdle() { return currentState == idleState; }
    public boolean isActive() { return currentState == activeState; }
    public boolean isDisabled() { return currentState == disabledState; }
    public TrapState getCurrentState() { return currentState; }
}
