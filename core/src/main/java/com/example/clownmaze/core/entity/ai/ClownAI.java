package com.example.clownmaze.core.entity.ai;

import java.util.function.Consumer;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.entity.WalkabilityChecker;

public final class ClownAI {

    public static final float PATROL_SPEED = 40f;
    public static final float CHASE_SPEED = 90f;

    private float x, y;
    private float heroX, heroY;

    private final PatrolState patrolState;
    private final ChaseState chaseState;
    private final KillState killState;
    private ClownState currentState;

    private final Consumer<EventBus.RoomExitEvent> onRoomExit;
    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter;
    private final Consumer<EventBus.TrapFailEvent> onTrapFail;
    private final Consumer<EventBus.RiddleTimerExpiredEvent> onTimerExpired;

    public ClownAI(float startX, float startY,
                   float[][] waypoints,
                   WalkabilityChecker walkabilityChecker,
                   int tileSize) {
        this.x = startX;
        this.y = startY;

        PatrolStrategy patrol = new PatrolStrategy(waypoints);
        ChaseStrategy  chase  = new ChaseStrategy(walkabilityChecker, tileSize);

        patrolState = new PatrolState(patrol);
        chaseState  = new ChaseState(chase);
        killState   = new KillState();

        currentState = patrolState;
        onRoomExit = e -> {
            heroX = e.heroX();
            heroY = e.heroY();
            if (currentState != chaseState && currentState != killState)
                setState(chaseState);
        };

        onRoomEnter = e -> {
            if (currentState == chaseState)
                setState(patrolState);
        };

        onTrapFail = e -> {
            heroX = e.trapX();
            heroY = e.trapY();
            if (currentState != killState)
                setState(chaseState);
        };
        onTimerExpired = e -> kill("timer_expired");
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomExitEvent.class, onRoomExit);
        bus.subscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        bus.subscribe(EventBus.TrapFailEvent.class, onTrapFail);
        bus.subscribe(EventBus.RiddleTimerExpiredEvent.class, onTimerExpired);
    }

    public void update(float delta) {
        currentState.update(this, delta);
    }

    public void updateHeroPosition(float hx, float hy) {
        heroX = hx;
        heroY = hy;
    }

    void setState(ClownState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
    }

    void kill(String reason) {
        if (currentState == killState) return;
        setState(killState);
        EventBus.getInstance().publish(new EventBus.GameOverEvent(reason));
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomExitEvent.class, onRoomExit);
        bus.unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        bus.unsubscribe(EventBus.TrapFailEvent.class, onTrapFail);
        bus.unsubscribe(EventBus.RiddleTimerExpiredEvent.class, onTimerExpired);
    }

    public float getX() { 
        return x; 
    }
    public float getY() { 
        return y; 
    }
    public void  setX(float x) { 
        this.x = x; 
    }
    public void  setY(float y) { 
        this.y = y; 
    }

    public float getHeroX() { 
        return heroX; 
    }
    public float getHeroY() { 
        return heroY; 
    }
    public float getPatrolSpeed() { 
        return PATROL_SPEED; 
    }
    public float getChaseSpeed() { 
        return CHASE_SPEED; 
    }

    public ClownState getCurrentState() { return currentState; }
    KillState getKillState()    { return killState; }
}
