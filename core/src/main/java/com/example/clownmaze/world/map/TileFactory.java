package com.example.clownmaze.world.map;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class TileFactory {

    private final Map<Integer, TileType> pool = new HashMap<>();

    public TileType getTile(int tileId, TiledMapTileSet tileSet) {
        TileType cached = pool.get(tileId);
        if (cached != null) return cached;

        TextureRegion region = null;
        TileCategory  category = TileCategory.UNKNOWN;

        if (tileSet != null) {
            TiledMapTile raw = tileSet.getTile(tileId);
            if (raw != null) {
                region = raw.getTextureRegion();
                category = resolveCategory(raw);
            }
        }

        TileType created = new TileType(tileId, region, category);
        pool.put(tileId, created);
        return created;
    }

    private TileCategory resolveCategory(TiledMapTile raw) {
        Object explicit = raw.getProperties().get("category");
        if (explicit instanceof String s) {
            try { return TileCategory.valueOf(s.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        Boolean wall = raw.getProperties().get("wall", Boolean.class);
        if (wall != null && wall) return TileCategory.WALL;
        return TileCategory.FLOOR;
    }

    public int poolSize() {
        return pool.size();
    }

    public void clear() {
        pool.clear();
    }
}
