package com.example.clownmaze.core.entity;

import com.example.clownmaze.core.EventBus;

import java.util.function.Consumer;

/**
 * The player-controlled hero entity.
 *
 * <h3>Responsibilities (SRP)</h3>
 * <ul>
 *   <li>World position and tile-based collision.</li>
 *   <li>HeroState finite-state machine (NORMAL / SLOWED / TRAPPED).</li>
 *   <li>Trap timer and mash-escape counter.</li>
 *   <li>Slow timer.</li>
 * </ul>
 * Hero does <b>not</b> read input — that is {@link PlayerInputHandler}'s job.
 * Hero does <b>not</b> render — that is GameScreen's job.
 *
 * <p>Collision is delegated to a {@link WalkabilityChecker} lambda so the class
 * stays independent of {@code RoomManager} (Dependency Inversion Principle).
 *
 * <p>Patterns: State (GoF #7), Observer via EventBus (GoF #9).
 */
public final class Hero {

    // ------------------------------------------------------------------ //
    //  Constants                                                           //
    // ------------------------------------------------------------------ //

    /** Walking speed in pixels per second (4 tiles/sec at 16 px/tile). */
    public static final float WALK_SPEED = 64f;

    /** Running speed in pixels per second (7 tiles/sec). Disabled when SLOWED. */
    public static final float RUN_SPEED  = 112f;

    /** How long (seconds) the hero stays TRAPPED before {@link EventBus.TrapFailEvent}. */
    public static final float TRAP_DURATION = 5f;

    /** How long (seconds) the SLOWED debuff lasts after spider contact. */
    public static final float SLOW_DURATION = 3f;

    /** Number of mash keypresses required to escape a trap. */
    public static final int   MASH_REQUIRED = 8;

    /** Sprite width in pixels (16 × 32 px sprite sheet). */
    public static final int SPRITE_WIDTH  = 16;

    /** Sprite height in pixels. */
    public static final int SPRITE_HEIGHT = 32;

    /**
     * Collision footprint radius in pixels.
     * Four cardinal probe points are checked at this distance from the hero's centre.
     * Value chosen so the hero fits inside a 16 px corridor with 2 px clearance each side.
     */
    private static final float COLLISION_RADIUS = 6f;

    // ------------------------------------------------------------------ //
    //  State                                                               //
    // ------------------------------------------------------------------ //

    private float     x, y;
    private HeroState state = HeroState.NORMAL;

    private float trapTimer;
    private float slowTimer;
    private int   mashCount;

    private final WalkabilityChecker collisionCheck;

    // EventBus handler references kept for clean unsubscription in dispose()
    private final Consumer<EventBus.TrapCaughtEvent>    trapHandler;
    private final Consumer<EventBus.SpiderContactEvent> slowHandler;

    // ------------------------------------------------------------------ //
    //  Construction                                                        //
    // ------------------------------------------------------------------ //

    /**
     * Creates a hero at the given world-space position.
     *
     * @param startX        initial X in pixels (hero centre)
     * @param startY        initial Y in pixels (hero centre)
     * @param collisionCheck walkability query — pass {@code roomManager::isWalkable}
     *                       in production or a lambda in tests
     */
    public Hero(float startX, float startY, WalkabilityChecker collisionCheck) {
        this.x              = startX;
        this.y              = startY;
        this.collisionCheck = collisionCheck;

        trapHandler = e -> trap();
        slowHandler = e -> slow();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.TrapCaughtEvent.class,    trapHandler);
        bus.subscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    // ------------------------------------------------------------------ //
    //  Game-loop update                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Advances state timers. Call once per frame from GameScreen.
     *
     * @param delta elapsed time in seconds
     */
    public void update(float delta) {
        switch (state) {
            case TRAPPED -> tickTrapped(delta);
            case SLOWED  -> tickSlowed(delta);
            default      -> { /* NORMAL — nothing to tick */ }
        }
    }

    private void tickTrapped(float delta) {
        trapTimer -= delta;
        if (trapTimer <= 0f) {
            state     = HeroState.NORMAL;
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

    // ------------------------------------------------------------------ //
    //  Movement + collision                                                //
    // ------------------------------------------------------------------ //

    /**
     * Attempts to displace the hero by {@code (dx, dy)} pixels.
     * Does nothing when {@link HeroState#TRAPPED}.
     *
     * <p>Axes are tested independently so the hero slides along walls
     * instead of stopping dead on diagonal contact.
     *
     * @param dx horizontal displacement (pixels)
     * @param dy vertical displacement (pixels)
     */
    public void attemptMove(float dx, float dy) {
        if (state == HeroState.TRAPPED) return;
        if (canMoveTo(x + dx, y)) x += dx;
        if (canMoveTo(x, y + dy)) y += dy;
    }

    /**
     * Checks whether the hero's collision footprint fits at {@code (cx, cy)}.
     * Four cardinal probe points are tested; all must be on walkable tiles.
     */
    private boolean canMoveTo(float cx, float cy) {
        float r = COLLISION_RADIUS;
        return collisionCheck.isWalkable(cx - r, cy)
            && collisionCheck.isWalkable(cx + r, cy)
            && collisionCheck.isWalkable(cx, cy - r)
            && collisionCheck.isWalkable(cx, cy + r);
    }

    // ------------------------------------------------------------------ //
    //  State transitions                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Transitions the hero to {@link HeroState#TRAPPED}.
     * Called automatically via the subscribed {@link EventBus.TrapCaughtEvent} handler.
     * Idempotent if already TRAPPED.
     */
    public void trap() {
        if (state == HeroState.TRAPPED) return;
        state     = HeroState.TRAPPED;
        trapTimer = TRAP_DURATION;
        mashCount = 0;
    }

    /**
     * Transitions the hero to {@link HeroState#SLOWED}.
     * Called automatically via the subscribed {@link EventBus.SpiderContactEvent} handler.
     * Ignored when TRAPPED (trap takes priority).
     */
    public void slow() {
        if (state == HeroState.TRAPPED) return;
        state     = HeroState.SLOWED;
        slowTimer = SLOW_DURATION;
    }

    /**
     * Registers one mash keypress. When {@link #MASH_REQUIRED} presses accumulate
     * the hero escapes the trap and a {@link EventBus.TrapEscapeEvent} is published.
     * No-op when not {@link HeroState#TRAPPED}.
     */
    public void mash() {
        if (state != HeroState.TRAPPED) return;
        mashCount++;
        if (mashCount >= MASH_REQUIRED) {
            state     = HeroState.NORMAL;
            mashCount = 0;
            trapTimer = 0f;
            EventBus.getInstance().publish(new EventBus.TrapEscapeEvent(x, y));
        }
    }

    // ------------------------------------------------------------------ //
    //  Cleanup                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Unsubscribes from EventBus. Call when the game screen is disposed
     * to prevent stale listeners on the next play-through.
     */
    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.TrapCaughtEvent.class,    trapHandler);
        bus.unsubscribe(EventBus.SpiderContactEvent.class, slowHandler);
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                           //
    // ------------------------------------------------------------------ //

    public float     getX()          { return x; }
    public float     getY()          { return y; }
    public HeroState getState()      { return state; }
    public float     getTrapTimer()  { return trapTimer; }
    public float     getSlowTimer()  { return slowTimer; }
    public int       getMashCount()  { return mashCount; }
    public boolean   isTrapped()     { return state == HeroState.TRAPPED; }
    public boolean   isSlowed()      { return state == HeroState.SLOWED; }
}
