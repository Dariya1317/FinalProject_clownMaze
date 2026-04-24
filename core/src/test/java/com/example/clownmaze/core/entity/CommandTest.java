package com.example.clownmaze.core.entity;

import com.example.clownmaze.core.EventBus;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private static final WalkabilityChecker ALL_OPEN = (x, y) -> true;
    private static final WalkabilityChecker ALL_WALL = (x, y) -> false;

    @BeforeEach
    void setUp() {
        EventBus.resetInstance();
    }

    @AfterEach
    void tearDown() {
        EventBus.resetInstance();
    }

    // ---- MoveCommand ----

    @Test
    @DisplayName("MoveCommand.execute() displaces hero by dx*delta, dy*delta")
    void moveCommand_execute_movesHero() {
        Hero hero = new Hero(100f, 100f, ALL_OPEN);
        new MoveCommand(hero, 1f, 0f, 10f).execute();
        assertEquals(110f, hero.getX(), 0.001f);
        assertEquals(100f, hero.getY(), 0.001f);
        hero.dispose();
    }

    @Test
    @DisplayName("MoveCommand.execute() is blocked by WALL tiles")
    void moveCommand_execute_blockedByWall() {
        Hero hero = new Hero(100f, 100f, ALL_WALL);
        new MoveCommand(hero, 1f, 1f, 10f).execute();
        assertEquals(100f, hero.getX(), 0.001f);
        assertEquals(100f, hero.getY(), 0.001f);
        hero.dispose();
    }

    @Test
    @DisplayName("MoveCommand.execute() is blocked when hero is TRAPPED")
    void moveCommand_execute_blockedWhenTrapped() {
        Hero hero = new Hero(100f, 100f, ALL_OPEN);
        hero.trap();
        new MoveCommand(hero, 1f, 1f, 10f).execute();
        assertEquals(100f, hero.getX(), 0.001f);
        assertEquals(100f, hero.getY(), 0.001f);
        hero.dispose();
    }

    @Test
    @DisplayName("MoveCommand with zero delta produces no movement")
    void moveCommand_zeroDelta_noMovement() {
        Hero hero = new Hero(50f, 50f, ALL_OPEN);
        new MoveCommand(hero, 5f, 5f, 0f).execute();
        assertEquals(50f, hero.getX(), 0.001f);
        assertEquals(50f, hero.getY(), 0.001f);
        hero.dispose();
    }

    @Test
    @DisplayName("MoveCommand allows Y movement when X is blocked (sliding)")
    void moveCommand_xBlocked_yStillMoves() {
        WalkabilityChecker blockX = (wx, wy) -> !(wx > 105f && Math.abs(wy - 100f) < 1f);
        Hero hero = new Hero(100f, 100f, blockX);
        new MoveCommand(hero, 1f, 1f, 10f).execute();
        assertEquals(110f, hero.getY(), 0.001f);
        hero.dispose();
    }

    // ---- MashCommand ----

    @Test
    @DisplayName("MashCommand.execute() increments mash count when trapped")
    void mashCommand_execute_incrementsMashCount() {
        Hero hero = new Hero(100f, 100f, ALL_OPEN);
        hero.trap();
        new MashCommand(hero).execute();
        assertEquals(1, hero.getMashCount());
        hero.dispose();
    }

    @Test
    @DisplayName("MashCommand.execute() is ignored when hero is NORMAL")
    void mashCommand_execute_ignoredWhenNormal() {
        Hero hero = new Hero(100f, 100f, ALL_OPEN);
        new MashCommand(hero).execute();
        assertEquals(0, hero.getMashCount());
        hero.dispose();
    }

    @Test
    @DisplayName("Enough MashCommands free the hero from trap")
    void mashCommand_enoughExecutions_freesHero() {
        Hero hero = new Hero(100f, 100f, ALL_OPEN);
        hero.trap();
        for (int i = 0; i < Hero.MASH_REQUIRED; i++) new MashCommand(hero).execute();
        assertEquals(HeroState.NORMAL, hero.getState());
        hero.dispose();
    }
}
