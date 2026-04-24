package com.example.clownmaze.world.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TileFactory flyweight pool.
 *
 * <p>All tests run headless (no libGDX GL context required) by using the
 * package-private {@code register()} method instead of the TiledMapTileSet overload.
 */
class TileFactoryTest {

    private TileFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TileFactory();
    }

    // ------------------------------------------------------------------ //
    //  Flyweight pooling                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Same tile ID returns the same TileType instance (flyweight reuse)")
    void getTile_sameId_returnsSameInstance() {
        factory.register(1, null, TileCategory.FLOOR);

        TileType first  = factory.getTile(1, null);
        TileType second = factory.getTile(1, null);

        assertSame(first, second, "Flyweight: identical IDs must share one object");
    }

    @Test
    @DisplayName("Different tile IDs return distinct TileType instances")
    void getTile_differentIds_returnDifferentInstances() {
        factory.register(1, null, TileCategory.FLOOR);
        factory.register(2, null, TileCategory.WALL);

        TileType floor = factory.getTile(1, null);
        TileType wall  = factory.getTile(2, null);

        assertNotSame(floor, wall);
    }

    // ------------------------------------------------------------------ //
    //  Pool size                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Pool size grows only for new IDs")
    void poolSize_growsOnlyForNewIds() {
        assertEquals(0, factory.poolSize());

        factory.register(10, null, TileCategory.FLOOR);
        assertEquals(1, factory.poolSize());

        factory.register(10, null, TileCategory.WALL); // duplicate — must not grow
        assertEquals(1, factory.poolSize());

        factory.register(20, null, TileCategory.CORRIDOR);
        assertEquals(2, factory.poolSize());
    }

    // ------------------------------------------------------------------ //
    //  clear()                                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("clear() empties the pool")
    void clear_emptiesPool() {
        factory.register(1, null, TileCategory.FLOOR);
        factory.register(2, null, TileCategory.WALL);
        assertEquals(2, factory.poolSize());

        factory.clear();

        assertEquals(0, factory.poolSize());
    }

    @Test
    @DisplayName("After clear(), the same ID is treated as new (fresh flyweight)")
    void clear_allowsReinsertion() {
        factory.register(5, null, TileCategory.FLOOR);
        TileType before = factory.getTile(5, null);

        factory.clear();
        factory.register(5, null, TileCategory.WALL);
        TileType after = factory.getTile(5, null);

        assertNotSame(before, after, "After clear(), a new flyweight object must be created");
        assertEquals(TileCategory.WALL, after.getCategory());
    }

    // ------------------------------------------------------------------ //
    //  TileType intrinsic state                                            //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("TileType stores id and category correctly")
    void tileType_storesIntrinsicState() {
        factory.register(42, null, TileCategory.TRAP_ZONE);
        TileType t = factory.getTile(42, null);

        assertEquals(42, t.getId());
        assertEquals(TileCategory.TRAP_ZONE, t.getCategory());
    }

    @Test
    @DisplayName("WALL tiles are not walkable; all others are")
    void tileType_walkability_wallIsBlocked() {
        factory.register(1, null, TileCategory.WALL);
        factory.register(2, null, TileCategory.FLOOR);
        factory.register(3, null, TileCategory.CORRIDOR);
        factory.register(4, null, TileCategory.DOOR);
        factory.register(5, null, TileCategory.TRAP_ZONE);
        factory.register(6, null, TileCategory.SPAWN_HERO);
        factory.register(7, null, TileCategory.SPAWN_CLOWN);
        factory.register(8, null, TileCategory.UNKNOWN);

        assertFalse(factory.getTile(1, null).isWalkable(), "WALL must not be walkable");
        assertTrue(factory.getTile(2, null).isWalkable(),  "FLOOR must be walkable");
        assertTrue(factory.getTile(3, null).isWalkable(),  "CORRIDOR must be walkable");
        assertTrue(factory.getTile(4, null).isWalkable(),  "DOOR must be walkable");
        assertTrue(factory.getTile(5, null).isWalkable(),  "TRAP_ZONE must be walkable");
        assertTrue(factory.getTile(6, null).isWalkable(),  "SPAWN_HERO must be walkable");
        assertTrue(factory.getTile(7, null).isWalkable(),  "SPAWN_CLOWN must be walkable");
        assertTrue(factory.getTile(8, null).isWalkable(),  "UNKNOWN must be walkable");
    }

    @Test
    @DisplayName("register() respects putIfAbsent — first category wins")
    void register_putIfAbsent_firstCategoryWins() {
        factory.register(99, null, TileCategory.FLOOR);
        factory.register(99, null, TileCategory.WALL); // must be ignored

        assertEquals(TileCategory.FLOOR, factory.getTile(99, null).getCategory());
    }
}
