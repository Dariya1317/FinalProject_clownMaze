package com.example.clownmaze.core.entity;

/**
 * Functional interface that abstracts tile-based collision queries.
 *
 * <p>In production pass {@code roomManager::isWalkable}.
 * In unit tests pass a lambda: {@code (x, y) -> true}.
 *
 * <p>Principle: Dependency Inversion — Hero depends on this abstraction,
 * not on the concrete {@link com.example.clownmaze.world.map.RoomManager}.
 */
@FunctionalInterface
public interface WalkabilityChecker {
    /**
     * Returns {@code true} when the given world-space point sits on a walkable tile.
     *
     * @param worldX X coordinate in pixels
     * @param worldY Y coordinate in pixels
     */
    boolean isWalkable(float worldX, float worldY);
}
