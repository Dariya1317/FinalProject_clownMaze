package com.example.clownmaze.core.entity;

import java.util.HashMap;
import java.util.Map;

public final class GhostFactory {

    private final Map<Integer, GhostType> pool = new HashMap<>();

    public void register(int id, Object region, float animSpeed) {
        pool.putIfAbsent(id, new GhostType(id, region, animSpeed));
    }

    public GhostType getType(int id) {
        return pool.computeIfAbsent(id, k -> new GhostType(k, null, 1f));
    }

    public int  poolSize() { return pool.size(); }
    public void clear()    { pool.clear(); }
}
