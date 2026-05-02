package com.example.clownmaze.core.entity;

import java.util.Random;

public final class GhostPrototype {

    private final Ghost master;

    public GhostPrototype(GhostAppearance appearance) {
        this.master = new Ghost(appearance, new Random());
    }

    public Ghost spawnAt(float x, float y) {
        Ghost copy = master.clone();
        copy.placeAt(x, y);
        return copy;
    }

    public Ghost spawnIn(float minX, float minY, float maxX, float maxY, Random rng) {
        Ghost copy = master.clone();
        float x = minX + rng.nextFloat() * (maxX - minX);
        float y = minY + rng.nextFloat() * (maxY - minY);
        copy.placeAt(x, y);
        copy.setRoamBounds(minX, minY, maxX, maxY);
        return copy;
    }

    public GhostAppearance getAppearance() {
        return master.getAppearance();
    }
}
