package com.example.clownmaze.core.entity.ai;

public final class PatrolStrategy implements ClownMovementStrategy {
    private static final float ARRIVAL_THRESHOLD = 4f;
    private final float[][] waypoints;
    private int currentIndex;

    public PatrolStrategy(float[][] waypoints) {
        if (waypoints == null || waypoints.length == 0)
            throw new IllegalArgumentException("PatrolStrategy requires at least one waypoint");
        this.waypoints = waypoints;
    }

    @Override
    public void move(ClownAI clown, float delta) {
        float tx = waypoints[currentIndex][0];
        float ty = waypoints[currentIndex][1];
        float dx = tx - clown.getX();
        float dy = ty - clown.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < ARRIVAL_THRESHOLD) {
            currentIndex = (currentIndex + 1) % waypoints.length;
            return;
        }

        float speed = clown.getPatrolSpeed();
        clown.setX(clown.getX() + (dx / dist) * speed * delta);
        clown.setY(clown.getY() + (dy / dist) * speed * delta);
    }

    @Override
    public void setTarget(float targetX, float targetY) {
    }

    public void reset() {
        currentIndex = 0;
    }

    public float[] getFirstWaypoint() {
        return waypoints[0];
    }
}
