package com.example.clownmaze.world.map;

public final class BloodDecal {

    private final float centerX;
    private final float centerY;
    private final float size;
    private final float rotationDeg;

    public BloodDecal(float centerX, float centerY, float size, float rotationDeg) {
        this.centerX     = centerX;
        this.centerY     = centerY;
        this.size        = size;
        this.rotationDeg = rotationDeg;
    }

    public float getCenterX()     { return centerX; }
    public float getCenterY()     { return centerY; }
    public float getSize()        { return size; }
    public float getRotationDeg() { return rotationDeg; }
}
