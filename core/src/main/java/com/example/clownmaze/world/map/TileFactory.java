package com.example.clownmaze.world.map;

import java.util.HashMap;
import java.util.Map;

public final class TileFactory {

    private final Map<Integer, TileType> pool = new HashMap<>();

    public TileType getTile(int tileId, Object tileSet) {
        return pool.computeIfAbsent(tileId, id -> new TileType(id, null, TileCategory.UNKNOWN));
    }

    public int poolSize() {
        return pool.size();
    }

    public void clear() {
        pool.clear();
    }

    void register(int tileId, Object region, TileCategory category) {
        pool.putIfAbsent(tileId, new TileType(tileId, null, category));
    }
}