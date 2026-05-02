package com.example.clownmaze.core.entity;

import java.util.Random;

public final class Ghost implements Cloneable {

    private static final float SPEED          = 18f;
    private static final float DIR_CHANGE_MIN = 1.2f;
    private static final float DIR_CHANGE_MAX = 3.0f;

    private final GhostAppearance appearance;
    private final Random rng;

    private float x, y;
    private float roamMinX, roamMinY, roamMaxX, roamMaxY;
    private float dirX, dirY;
    private float dirTimer;

    public Ghost(GhostAppearance appearance) {
        this(appearance, new Random());
    }

    public Ghost(GhostAppearance appearance, Random rng) {
        this.appearance = appearance;
        this.rng        = rng;
        pickDirection();
    }

    public void placeAt(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setRoamBounds(float minX, float minY, float maxX, float maxY) {
        this.roamMinX = minX;
        this.roamMinY = minY;
        this.roamMaxX = maxX;
        this.roamMaxY = maxY;
    }

    public void update(float delta) {
        dirTimer -= delta;
        if (dirTimer <= 0f) pickDirection();

        x += dirX * SPEED * delta;
        y += dirY * SPEED * delta;

        if (roamMaxX > roamMinX) {
            if (x < roamMinX) { x = roamMinX; dirX = -dirX; }
            if (x > roamMaxX) { x = roamMaxX; dirX = -dirX; }
        }
        if (roamMaxY > roamMinY) {
            if (y < roamMinY) { y = roamMinY; dirY = -dirY; }
            if (y > roamMaxY) { y = roamMaxY; dirY = -dirY; }
        }
    }

    private void pickDirection() {
        double angle = rng.nextDouble() * Math.PI * 2;
        dirX = (float) Math.cos(angle);
        dirY = (float) Math.sin(angle);
        dirTimer = DIR_CHANGE_MIN + rng.nextFloat() * (DIR_CHANGE_MAX - DIR_CHANGE_MIN);
    }

    @Override
    public Ghost clone() {
        Ghost copy = new Ghost(appearance, rng);
        copy.x = this.x;
        copy.y = this.y;
        copy.roamMinX = this.roamMinX;
        copy.roamMinY = this.roamMinY;
        copy.roamMaxX = this.roamMaxX;
        copy.roamMaxY = this.roamMaxY;
        return copy;
    }

    public GhostAppearance getAppearance() { return appearance; }
    public float getX()                    { return x; }
    public float getY()                    { return y; }
}
