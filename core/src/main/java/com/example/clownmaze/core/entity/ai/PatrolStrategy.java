package com.example.clownmaze.core.entity.ai;

public final class PatrolStrategy implements ClownMovementStrategy {

    private final float[][] waypoints;
    private int currentIndex;
    private float t;

    public PatrolStrategy(float[][] waypoints) {
        if (waypoints == null || waypoints.length == 0)
            throw new IllegalArgumentException("PatrolStrategy requires at least one waypoint");
        this.waypoints = waypoints;
    }

    @Override
    public void move(ClownAI clown, float delta) {
        if (waypoints.length == 1) return;

        int   nextIndex = (currentIndex + 1) % waypoints.length;
        float ax = waypoints[currentIndex][0], ay = waypoints[currentIndex][1];
        float bx = waypoints[nextIndex][0], by = waypoints[nextIndex][1];

        float dx = bx - ax;
        float dy = by - ay;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 0.001f) {
            currentIndex = nextIndex;
            t = 0f;
            return;
        }

        t += clown.getPatrolSpeed() * delta / dist;

        while (t >= 1f) {
            float excess = (t - 1f) * dist;
            currentIndex = (currentIndex + 1) % waypoints.length;
            nextIndex  = (currentIndex + 1) % waypoints.length;
            ax = waypoints[currentIndex][0]; ay = waypoints[currentIndex][1];
            bx = waypoints[nextIndex][0]; by = waypoints[nextIndex][1];
            dx = bx - ax;
            dy = by - ay;
            dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 0.001f) { t = 0f; break; }
            t = excess / dist;
        }

        clown.setX(ax + (bx - ax) * t);
        clown.setY(ay + (by - ay) * t);
    }

    @Override
    public void setTarget(float targetX, float targetY) {}

    public void reset() {
        currentIndex = 0;
        t = 0f;
    }

    public float[] getFirstWaypoint() {
        return waypoints[0];
    }
}
