package com.example.clownmaze.world.map;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;

public final class Room {

    private final int id;
    private final Rectangle bounds;
    private final List<int[]> tileCoords;
    private final float heroSpawnX;
    private final float heroSpawnY;
    private final float clownSpawnX;
    private final float clownSpawnY;

    Room(int id,
         Rectangle bounds,
         List<int[]> tileCoords,
         float heroSpawnX,
         float heroSpawnY,
         float clownSpawnX,
         float clownSpawnY) {
        this.id          = id;
        this.bounds      = new Rectangle(bounds);
        this.tileCoords  = Collections.unmodifiableList(tileCoords);
        this.heroSpawnX  = heroSpawnX;
        this.heroSpawnY  = heroSpawnY;
        this.clownSpawnX = clownSpawnX;
        this.clownSpawnY = clownSpawnY;
    }

    public int getId()                 { return id; }
    public Rectangle getBounds()       { return bounds; }
    public List<int[]> getTileCoords() { return tileCoords; }
    public float getHeroSpawnX()       { return heroSpawnX; }
    public float getHeroSpawnY()       { return heroSpawnY; }
    public float getClownSpawnX()      { return clownSpawnX; }
    public float getClownSpawnY()      { return clownSpawnY; }

    public boolean contains(float worldX, float worldY) {
        return bounds.contains(worldX, worldY);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", bounds=" + bounds + ", tiles=" + tileCoords.size() + '}';
    }
}
