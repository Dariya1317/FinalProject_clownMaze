package com.example.clownmaze.world.map;

public final class TileType {

    private final int          id;
    private final Object       region;
    private final TileCategory category;
    private final boolean      walkable;

    TileType(int id, Object region, TileCategory category) {
        this.id       = id;
        this.region   = region;
        this.category = category;
        this.walkable = (category != TileCategory.WALL);
    }

    public int getId()                { return id; }
    public Object getRegion()         { return region; }
    public TileCategory getCategory() { return category; }
    public boolean isWalkable()       { return walkable; }

    /**
     * Draws the tile. batch and region are Object to avoid libGDX
     * compile dependency in headless/test environments.
     */
    public void draw(Object batch, float x, float y, float tileSize) {
        // rendering handled by libGDX at runtime via RoomManager
    }

    @Override
    public String toString() {
        return "TileType{id=" + id + ", category=" + category + ", walkable=" + walkable + '}';
    }
}