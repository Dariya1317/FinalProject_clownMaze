package com.example.clownmaze.world.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class TileType {

    private final int           id;
    private final TextureRegion region;
    private final TileCategory  category;
    private final boolean       walkable;

    TileType(int id, TextureRegion region, TileCategory category) {
        this.id       = id;
        this.region   = region;
        this.category = category;
        this.walkable = (category != TileCategory.WALL);
    }

    public int getId()              { return id; }
    public TextureRegion getRegion(){ return region; }
    public TileCategory getCategory(){ return category; }
    public boolean isWalkable()     { return walkable; }

    public void draw(SpriteBatch batch, float x, float y, float tileSize) {
        if (region != null) {
            batch.draw(region, x, y, tileSize, tileSize);
        }
    }

    @Override
    public String toString() {
        return "TileType{id=" + id + ", category=" + category + ", walkable=" + walkable + '}';
    }
}
