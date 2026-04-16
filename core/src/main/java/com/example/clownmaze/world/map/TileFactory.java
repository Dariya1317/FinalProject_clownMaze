package com.example.clownmaze.world.map;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;

public final class TileFactory {

    private static final String PROP_CATEGORY = "category";

    private final Map<Integer, TileType> pool = new HashMap<>();

    public TileType getTile(int tileId, TiledMapTileSet tileSet) {
        return pool.computeIfAbsent(tileId, id -> buildFromTileSet(id, tileSet));
    }

    public int poolSize() {
        return pool.size();
    }

    public void clear() {
        pool.clear();
    }

    void register(int tileId, TextureRegion region, TileCategory category) {
        pool.putIfAbsent(tileId, new TileType(tileId, region, category));
    }


    private TileType buildFromTileSet(int tileId, TiledMapTileSet tileSet) {
        TiledMapTile tile     = tileSet.getTile(tileId);
        TextureRegion region  = (tile != null) ? tile.getTextureRegion() : null;
        TileCategory category = resolveCategory(tile);
        return new TileType(tileId, region, category);
    }

    private TileCategory resolveCategory(TiledMapTile tile) {
        if (tile == null) return TileCategory.UNKNOWN;
        String raw = tile.getProperties().get(PROP_CATEGORY, String.class);
        if (raw == null) return TileCategory.FLOOR;
        try {
            return TileCategory.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TileCategory.UNKNOWN;
        }
    }
}
