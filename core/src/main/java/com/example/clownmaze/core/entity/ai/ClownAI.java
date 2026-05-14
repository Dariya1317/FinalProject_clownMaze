package com.example.clownmaze.core.entity.ai;

import java.util.function.Consumer;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;
import com.example.clownmaze.core.entity.Hero;

public final class ClownAI {

    public static final float CHASE_SPEED = 95f;

    private final IdleState   idleState;
    private final ChaseState  chaseState;
    private final KillState   killState;
    private final PatrolState patrolState;

    private float x, y;
    private float heroX, heroY;
    private final Hero heroRef;

    private ClownState currentState;

    private final Consumer<EventBus.TimerExpiredEvent>  onTimerExpired;
    private final Consumer<EventBus.ScreamerEndEvent>   onScreamerEnd;
    private final Consumer<EventBus.RoomEnterEvent>     onRoomEnter;
    private final Consumer<EventBus.RoomResetEvent>     onRoomReset;

    public ClownAI(float startX, float startY, Hero heroRef) {
        this.x = startX;
        this.y = startY;
        this.heroRef = heroRef;
        if (heroRef != null) {
            this.heroX = heroRef.getX();
            this.heroY = heroRef.getY();
        }

        this.idleState   = new IdleState(startX, startY);
        this.chaseState  = new ChaseState(new ChaseStrategy());
        this.killState   = new KillState();
        this.patrolState = new PatrolState();

        this.currentState = idleState;

        onTimerExpired = e -> {
            if (currentState == idleState || currentState == patrolState)
                setState(chaseState);
        };
        onScreamerEnd = e -> {
            if (currentState == killState) setState(initialStateForLevel());
        };
        onRoomEnter = e -> setState(initialStateForLevel());
        onRoomReset = e -> setState(initialStateForLevel());

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.TimerExpiredEvent.class, onTimerExpired);
        bus.subscribe(EventBus.ScreamerEndEvent.class,  onScreamerEnd);
        bus.subscribe(EventBus.RoomEnterEvent.class,    onRoomEnter);
        bus.subscribe(EventBus.RoomResetEvent.class,    onRoomReset);
    }

    public void update(float delta) {
        if (heroRef != null) {
            heroX = heroRef.getX();
            heroY = heroRef.getY();
        }
        currentState.update(this, delta);
    }

    public void teleportTo(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        idleState.setAnchor(newX, newY);
    }

    public void toIdle() {
        setState(initialStateForLevel());
    }

    private ClownState initialStateForLevel() {
        return GameStateManager.getInstance().getCurrentLevel() == 3
            ? patrolState : idleState;
    }

    public void updateHeroPosition(float hx, float hy) {
        this.heroX = hx;
        this.heroY = hy;
    }

    void setState(ClownState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
    }

    void kill() {
        if (currentState == killState) return;
        setState(killState);
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.TimerExpiredEvent.class, onTimerExpired);
        bus.unsubscribe(EventBus.ScreamerEndEvent.class,  onScreamerEnd);
        bus.unsubscribe(EventBus.RoomEnterEvent.class,    onRoomEnter);
        bus.unsubscribe(EventBus.RoomResetEvent.class,    onRoomReset);
    }

    public float getX()           { return x; }
    public float getY()           { return y; }
    public void  setX(float x)    { this.x = x; }
    public void  setY(float y)    { this.y = y; }
    public float getHeroX()       { return heroX; }
    public float getHeroY()       { return heroY; }
    public float getChaseSpeed()  { return CHASE_SPEED; }

    public ClownState getCurrentState() { return currentState; }
    public boolean isIdle()             { return currentState == idleState || currentState == patrolState; }
    public boolean isChasing()          { return currentState == chaseState; }
    public boolean isKilling()          { return currentState == killState; }
}
