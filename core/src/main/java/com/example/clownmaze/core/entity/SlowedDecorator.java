package com.example.clownmaze.core.entity;

public final class SlowedDecorator implements SpeedProvider {

    private final SpeedProvider wrapped;
    private float remaining;

    public SlowedDecorator(SpeedProvider wrapped, float duration) {
        this.wrapped   = wrapped;
        this.remaining = duration;
    }

    @Override
    public float speedFor(boolean wantsRun) {
        return wrapped.speedFor(false);
    }

    public void tick(float delta) {
        if (remaining > 0f) remaining = Math.max(0f, remaining - delta);
    }

    public boolean isExpired() {
        return remaining <= 0f;
    }

    public float getRemaining() {
        return remaining;
    }

    public SpeedProvider unwrap() {
        return wrapped;
    }
}
