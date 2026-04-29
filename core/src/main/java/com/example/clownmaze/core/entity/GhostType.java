package com.example.clownmaze.core.entity;

public final class GhostType {

    private final int    id;
    private final Object region;
    private final float  animSpeed;

    GhostType(int id, Object region, float animSpeed) {
        this.id        = id;
        this.region    = region;
        this.animSpeed = animSpeed;
    }

    public int    getId()       { return id; }
    public Object getRegion()   { return region; }
    public float  getAnimSpeed(){ return animSpeed; }
}
