package com.example.clownmaze.core.entity;

import com.example.clownmaze.core.EventBus;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Hero state machine, timers, movement, and EventBus integration.
 * All tests run headless — no libGDX GL context required.
 */
class HeroTest {

    /** Always-walkable world used by most tests. */
    private static final WalkabilityChecker ALL_OPEN = (x, y) -> true;

    /** Nothing-walkable world used for collision tests. */
    private static final WalkabilityChecker ALL_WALL = (x, y) -> false;

    private Hero hero;

    @BeforeEach
    void setUp() {
        EventBus.resetInstance();
        hero = new Hero(100f, 100f, ALL_OPEN);
    }

    @AfterEach
    void tearDown() {
        hero.dispose();
        EventBus.resetInstance();
    }

    // ------------------------------------------------------------------ //
    //  Initial state                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Hero starts at given position")
    void initialPosition() {
        assertEquals(100f, hero.getX(), 0.001f);
        assertEquals(100f, hero.getY(), 0.001f);
    }

    @Test
    @DisplayName("Hero starts in NORMAL state")
    void initialState_isNormal() {
        assertEquals(HeroState.NORMAL, hero.getState());
    }

    // ------------------------------------------------------------------ //
    //  State transitions                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("trap() → TRAPPED")
    void trap_transitionsToTrapped() {
        hero.trap();
        assertEquals(HeroState.TRAPPED, hero.getState());
    }

    @Test
    @DisplayName("slow() → SLOWED")
    void slow_transitionsToSlowed() {
        hero.slow();
        assertEquals(HeroState.SLOWED, hero.getState());
    }

    @Test
    @DisplayName("trap() overrides SLOWED — trap takes priority")
    void trap_overridesSlowed() {
        hero.slow();
        hero.trap();
        assertEquals(HeroState.TRAPPED, hero.getState());
    }

    @Test
    @DisplayName("slow() is ignored when already TRAPPED")
    void slow_ignoredWhenTrapped() {
        hero.trap();
        hero.slow();
        assertEquals(HeroState.TRAPPED, hero.getState());
    }

    @Test
    @DisplayName("trap() is idempotent — calling twice does not reset timer")
    void trap_idempotent() {
        hero.trap();
        float timerAfterFirst = hero.getTrapTimer();
        // advance slightly so timer decreases
        hero.update(0.1f);
        hero.trap(); // second call — must be ignored
        assertTrue(hero.getTrapTimer() < timerAfterFirst,
            "Timer should not reset on second trap() call");
    }

    // ------------------------------------------------------------------ //
    //  Mash mechanic                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Enough mashes escape the trap → NORMAL")
    void mash_enoughTimes_escapeTrap() {
        hero.trap();
        for (int i = 0; i < Hero.MASH_REQUIRED; i++) hero.mash();
        assertEquals(HeroState.NORMAL, hero.getState());
    }

    @Test
    @DisplayName("Insufficient mashes keep hero TRAPPED")
    void mash_notEnough_staysTrapped() {
        hero.trap();
        for (int i = 0; i < Hero.MASH_REQUIRED - 1; i++) hero.mash();
        assertEquals(HeroState.TRAPPED, hero.getState());
    }

    @Test
    @DisplayName("mash() is no-op when NORMAL")
    void mash_ignoredWhenNormal() {
        hero.mash();
        assertEquals(HeroState.NORMAL, hero.getState());
        assertEquals(0, hero.getMashCount());
    }

    @Test
    @DisplayName("Successful mash publishes TrapEscapeEvent")
    void mash_success_publishesTrapEscapeEvent() {
        AtomicBoolean fired = new AtomicBoolean(false);
        EventBus.getInstance().subscribe(EventBus.TrapEscapeEvent.class, e -> fired.set(true));

        hero.trap();
        for (int i = 0; i < Hero.MASH_REQUIRED; i++) hero.mash();

        assertTrue(fired.get(), "TrapEscapeEvent must be published on successful mash");
    }

    @Test
    @DisplayName("Mash counter resets after escape")
    void mash_counterResetAfterEscape() {
        hero.trap();
        for (int i = 0; i < Hero.MASH_REQUIRED; i++) hero.mash();
        assertEquals(0, hero.getMashCount());
    }

    // ------------------------------------------------------------------ //
    //  Timers                                                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Trap timer expiry → NORMAL + TrapFailEvent")
    void update_trapTimerExpires_firesFailEvent() {
        AtomicBoolean fired = new AtomicBoolean(false);
        EventBus.getInstance().subscribe(EventBus.TrapFailEvent.class, e -> fired.set(true));

        hero.trap();
        hero.update(Hero.TRAP_DURATION + 0.1f);

        assertEquals(HeroState.NORMAL, hero.getState());
        assertTrue(fired.get(), "TrapFailEvent must fire when trap timer expires");
    }

    @Test
    @DisplayName("Slow timer expiry → back to NORMAL")
    void update_slowTimerExpires_returnsToNormal() {
        hero.slow();
        hero.update(Hero.SLOW_DURATION + 0.1f);
        assertEquals(HeroState.NORMAL, hero.getState());
    }

    @Test
    @DisplayName("Slow timer does not expire early")
    void update_slowTimer_doesNotExpireEarly() {
        hero.slow();
        hero.update(Hero.SLOW_DURATION * 0.5f);
        assertEquals(HeroState.SLOWED, hero.getState());
    }

    // ------------------------------------------------------------------ //
    //  Movement                                                            //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("attemptMove displaces hero on walkable terrain")
    void attemptMove_walkable_movesHero() {
        hero.attemptMove(10f, 5f);
        assertEquals(110f, hero.getX(), 0.001f);
        assertEquals(105f, hero.getY(), 0.001f);
    }

    @Test
    @DisplayName("attemptMove is blocked when TRAPPED")
    void attemptMove_trapped_doesNotMove() {
        hero.trap();
        float startX = hero.getX(), startY = hero.getY();
        hero.attemptMove(10f, 10f);
        assertEquals(startX, hero.getX(), 0.001f);
        assertEquals(startY, hero.getY(), 0.001f);
    }

    @Test
    @DisplayName("attemptMove is blocked by non-walkable tiles")
    void attemptMove_wall_staysInPlace() {
        Hero wallHero = new Hero(100f, 100f, ALL_WALL);
        float startX = wallHero.getX(), startY = wallHero.getY();
        wallHero.attemptMove(10f, 10f);
        assertEquals(startX, wallHero.getX(), 0.001f);
        assertEquals(startY, wallHero.getY(), 0.001f);
        wallHero.dispose();
    }

    @Test
    @DisplayName("SLOWED hero can still move (walk speed, no run)")
    void attemptMove_slowed_canMove() {
        hero.slow();
        hero.attemptMove(10f, 0f);
        assertEquals(110f, hero.getX(), 0.001f);
    }

    @Test
    @DisplayName("Axis-independent sliding: blocked X still allows Y movement")
    void attemptMove_sliding_yAllowedWhenXBlocked() {
        // Block only the X-probe points; Y-probe points remain open
        WalkabilityChecker blockX = (wx, wy) -> {
            // Block right probe (cx + r, cy) when moving to x=110
            if (wx > 105f && Math.abs(wy - 100f) < 1f) return false;
            return true;
        };
        Hero slideHero = new Hero(100f, 100f, blockX);
        slideHero.attemptMove(10f, 10f); // X blocked, Y open
        // Y should still move
        assertEquals(110f, slideHero.getY(), 0.001f);
        slideHero.dispose();
    }

    // ------------------------------------------------------------------ //
    //  EventBus integration                                                //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("TrapCaughtEvent automatically traps the hero")
    void eventBus_trapCaughtEvent_trapsHero() {
        EventBus.getInstance().publish(new EventBus.TrapCaughtEvent(100f, 100f));
        assertEquals(HeroState.TRAPPED, hero.getState());
    }

    @Test
    @DisplayName("SpiderContactEvent automatically slows the hero")
    void eventBus_spiderContactEvent_slowsHero() {
        EventBus.getInstance().publish(new EventBus.SpiderContactEvent(100f, 100f));
        assertEquals(HeroState.SLOWED, hero.getState());
    }

    @Test
    @DisplayName("dispose() removes EventBus subscriptions — no stale listeners")
    void dispose_removesSubscriptions() {
        hero.dispose();
        // After dispose, publishing TrapCaughtEvent must not change state
        EventBus.getInstance().publish(new EventBus.TrapCaughtEvent(0f, 0f));
        assertEquals(HeroState.NORMAL, hero.getState());
    }
}
