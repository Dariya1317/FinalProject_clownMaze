package com.example.clownmaze.core.entity;

public final class BaseSpeedProvider implements SpeedProvider {

    private final float walk;
    private final float run;

    public BaseSpeedProvider(float walk, float run) {
        this.walk = walk;
        this.run  = run;
    }

    @Override
    public float speedFor(boolean wantsRun) {
        return wantsRun ? run : walk;
    }
}
