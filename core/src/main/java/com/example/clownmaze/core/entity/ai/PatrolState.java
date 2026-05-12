package com.example.clownmaze.core.entity.ai;

import com.badlogic.gdx.math.MathUtils;

/**
 * Level-3 clown behaviour before the timer expires.
 * The clown wanders slowly around its spawn anchor instead of standing still.
 */
public final class PatrolState implements ClownState {

    private static final float SPEED      = 32f;   // much slower than ChaseState (95)
    private static final float WANDER     = 70f;   // radius around anchor
    private static final float DIR_MIN    = 1.0f;
    private static final float DIR_MAX    = 2.8f;

    private float anchorX, anchorY;
    private float velX, velY;
    private float dirTimer;

    public void setAnchor(float x, float y) {
        anchorX = x;
        anchorY = y;
    }

    @Override
    public void enter(ClownAI clown) {
        anchorX = clown.getX();
        anchorY = clown.getY();
        pickDirection();
    }

    @Override
    public void update(ClownAI clown, float delta) {
        dirTimer -= delta;
        if (dirTimer <= 0f) pickDirection();

        float nx = clown.getX() + velX * delta;
        float ny = clown.getY() + velY * delta;

        if (nx < anchorX - WANDER) { nx = anchorX - WANDER; velX =  Math.abs(velX); }
        if (nx > anchorX + WANDER) { nx = anchorX + WANDER; velX = -Math.abs(velX); }
        if (ny < anchorY - WANDER) { ny = anchorY - WANDER; velY =  Math.abs(velY); }
        if (ny > anchorY + WANDER) { ny = anchorY + WANDER; velY = -Math.abs(velY); }

        clown.setX(nx);
        clown.setY(ny);
    }

    @Override
    public void exit(ClownAI clown) {}

    private void pickDirection() {
        float angle = MathUtils.random(MathUtils.PI2);
        velX     = MathUtils.cos(angle) * SPEED;
        velY     = MathUtils.sin(angle) * SPEED;
        dirTimer = MathUtils.random(DIR_MIN, DIR_MAX);
    }
}
