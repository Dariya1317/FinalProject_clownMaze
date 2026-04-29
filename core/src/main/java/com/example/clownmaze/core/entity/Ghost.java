package com.example.clownmaze.core.entity;

import java.util.Random;

public final class Ghost {

    private static final float SPEED           = 24f;
    private static final float DIR_CHANGE_MIN  = 1.5f;
    private static final float DIR_CHANGE_MAX  = 3.5f;

    private final GhostType   type;
    private final Random      rng;

    private float x, y;
    private float vx, vy;
    private float dirTimer;

    private final float boundsMinX, boundsMinY, boundsMaxX, boundsMaxY;

    public Ghost(GhostType type, float x, float y,
                 float boundsMinX, float boundsMinY,
                 float boundsMaxX, float boundsMaxY,
                 Random rng) {
        this.type       = type;
        this.x          = x;
        this.y          = y;
        this.boundsMinX = boundsMinX;
        this.boundsMinY = boundsMinY;
        this.boundsMaxX = boundsMaxX;
        this.boundsMaxY = boundsMaxY;
        this.rng        = rng;
        randomizeDirection();
    }

    public void update(float dt) {
        x += vx * dt;
        y += vy * dt;

        if (x < boundsMinX || x > boundsMaxX) { vx = -vx; x = Math.max(boundsMinX, Math.min(boundsMaxX, x)); }
        if (y < boundsMinY || y > boundsMaxY) { vy = -vy; y = Math.max(boundsMinY, Math.min(boundsMaxY, y)); }

        dirTimer -= dt;
        if (dirTimer <= 0f) randomizeDirection();
    }

    private void randomizeDirection() {
        double angle = rng.nextDouble() * 2 * Math.PI;
        vx = (float) Math.cos(angle) * SPEED;
        vy = (float) Math.sin(angle) * SPEED;
        dirTimer = DIR_CHANGE_MIN + rng.nextFloat() * (DIR_CHANGE_MAX - DIR_CHANGE_MIN);
    }

    public GhostType getType() { return type; }
    public float     getX()    { return x; }
    public float     getY()    { return y; }
}
