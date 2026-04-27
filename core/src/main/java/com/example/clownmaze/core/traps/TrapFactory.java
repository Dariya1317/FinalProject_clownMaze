package com.example.clownmaze.core.traps;

public final class TrapFactory {

    private TrapFactory() {}

    public static Trap createTrap(float worldX, float worldY) {
        return new Trap(worldX, worldY);
    }
}
