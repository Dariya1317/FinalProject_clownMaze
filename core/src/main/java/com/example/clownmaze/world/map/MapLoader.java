package com.example.clownmaze.world.map;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public final class MapLoader {

    private TiledMap map;
    private int mapWidthTiles;
    private int mapHeightTiles;
    private int tileWidthPx;
    private int tileHeightPx;

    public void load(String tmxPath) {
        dispose();
        map            = new TmxMapLoader().load(tmxPath);
        mapWidthTiles  = map.getProperties().get("width",      Integer.class);
        mapHeightTiles = map.getProperties().get("height",     Integer.class);
        tileWidthPx    = map.getProperties().get("tilewidth",  Integer.class);
        tileHeightPx   = map.getProperties().get("tileheight", Integer.class);
    }

    public void dispose() {
        if (map != null) {
            map.dispose();
            map = null;
        }
    }

    public TiledMap getMap()          { return map; }
    public int getMapWidthTiles()     { return mapWidthTiles; }
    public int getMapHeightTiles()    { return mapHeightTiles; }
    public int getTileWidthPx()       { return tileWidthPx; }
    public int getTileHeightPx()      { return tileHeightPx; }
    public boolean isLoaded()         { return map != null; }
}
