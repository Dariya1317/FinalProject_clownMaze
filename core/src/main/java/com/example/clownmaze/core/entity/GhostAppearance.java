package com.example.clownmaze.core.entity;

import java.util.HashMap;
import java.util.Map;

public final class GhostAppearance {

    private static final Map<String, GhostAppearance> POOL = new HashMap<>();

    public static GhostAppearance shared(String spriteKey, float width, float height, float alpha) {
        return POOL.computeIfAbsent(spriteKey,
            k -> new GhostAppearance(k, width, height, alpha));
    }

    public static int poolSize() {
        return POOL.size();
    }

    public static void clearPool() {
        POOL.clear();
    }

    private final String spriteKey;
    private final float  width;
    private final float  height;
    private final float  alpha;

    private GhostAppearance(String spriteKey, float width, float height, float alpha) {
        this.spriteKey = spriteKey;
        this.width     = width;
        this.height    = height;
        this.alpha     = alpha;
    }

    public String getSpriteKey() { return spriteKey; }
    public float  getWidth()     { return width; }
    public float  getHeight()    { return height; }
    public float  getAlpha()     { return alpha; }
}
