package com.example.clownmaze.core.entity.ai;

public final class ChaseStrategy implements ClownMovementStrategy {

    private static final float ARRIVAL_THRESHOLD = 0.5f;

    private float targetX, targetY;

    @Override
    public void setTarget(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public void move(ClownAI clown, float delta) {
        float dx   = targetX - clown.getX();
        float dy   = targetY - clown.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < ARRIVAL_THRESHOLD) return;

        float speed = clown.getChaseSpeed();
        clown.setX(clown.getX() + (dx / dist) * speed * delta);
        clown.setY(clown.getY() + (dy / dist) * speed * delta);
    }
}
