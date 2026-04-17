package com.example.clownmaze.world.map;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;

public final class Room {

    private final int         id;
    private final Rectangle   bounds;    
    private final List<int[]> tileCoords;  

    Room(int id, Rectangle bounds, List<int[]> tileCoords) {
        this.id          = id;
        this.bounds      = new Rectangle(bounds);                   
        this.tileCoords  = Collections.unmodifiableList(tileCoords);
    }

    public int getId() { return id; }

    public Rectangle getBounds() { return bounds; }

    public List<int[]> getTileCoords() { return tileCoords; }

    public boolean contains(float worldX, float worldY) {
        return bounds.contains(worldX, worldY);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", bounds=" + bounds + ", tiles=" + tileCoords.size() + '}';
    }
}
