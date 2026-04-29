package com.example.clownmaze.world.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;

public final class RoomManager {

    private static final String LAYER_TILES = "tiles";
    private static final String LAYER_ROOMS = "Rooms";
    private static final String PROP_ROOM_ID = "id";

    private final TileFactory tileFactory;
    private final List<Room>  rooms = new ArrayList<>();

    private TiledMap         map;
    private int              tileSize; // pixels

    public RoomManager(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
    }

    public void init(MapLoader mapLoader) {
        this.map      = mapLoader.getMap();
        this.tileSize = mapLoader.getTileWidthPx();
        tileFactory.clear();
        rooms.clear();
        parseRooms();
    }

    public void render(SpriteBatch batch) {
        if (map == null) return;

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(LAYER_TILES);
        if (layer == null) return;

        TiledMapTileSet tileSet = map.getTileSets().getTileSet(0);
        int cols = layer.getWidth();
        int rows = layer.getHeight();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if (cell == null || cell.getTile() == null) continue;

                int      tileId   = cell.getTile().getId();
                TileType tileType = tileFactory.getTile(tileId, tileSet);

                float worldX = col * tileSize;
                float worldY = row * tileSize;
                tileType.draw(batch, worldX, worldY, tileSize);
            }
        }
    }

    public Room getRoomAt(float worldX, float worldY) {
        for (Room r : rooms) {
            if (r.contains(worldX, worldY)) return r;
        }
        return null;
    }

    public Room getRoom(int id) {
        return rooms.stream()
                    .filter(r -> r.getId() == id)
                    .findFirst()
                    .orElse(null);
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public TileType getTileTypeAt(int col, int row) {
        if (map == null) return null;
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(LAYER_TILES);
        if (layer == null) return null;
        TiledMapTileLayer.Cell cell = layer.getCell(col, row);
        if (cell == null || cell.getTile() == null) return null;
        TiledMapTileSet tileSet = map.getTileSets().getTileSet(0);
        return tileFactory.getTile(cell.getTile().getId(), tileSet);
    }

    public int worldToCol(float worldX) { return (int) (worldX / tileSize); }

    public int worldToRow(float worldY) { return (int) (worldY / tileSize); }

    public boolean isWalkable(float worldX, float worldY) {
        TileType t = getTileTypeAt(worldToCol(worldX), worldToRow(worldY));
        return t != null && t.isWalkable();
    }


    private void parseRooms() {
        MapLayer objectLayer = map.getLayers().get(LAYER_ROOMS);
        if (objectLayer == null) return;

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) map.getLayers().get(LAYER_TILES);

        for (com.badlogic.gdx.maps.MapObject obj : objectLayer.getObjects()) {
            if (!(obj instanceof RectangleMapObject rectObj)) continue;

            MapProperties props  = obj.getProperties();
            int           roomId = props.get(PROP_ROOM_ID, 1, Integer.class);
            Rectangle     bounds = rectObj.getRectangle();

            List<int[]> coords = collectTileCoordsInBounds(tileLayer, bounds);
            rooms.add(new Room(roomId, bounds, coords));
        }

        rooms.sort(Comparator.comparingInt(Room::getId));
    }

    private List<int[]> collectTileCoordsInBounds(TiledMapTileLayer layer, Rectangle bounds) {
        List<int[]> result = new ArrayList<>();
        if (layer == null) return result;

        int colMin = Math.max(0, worldToCol(bounds.x));
        int colMax = Math.min(layer.getWidth() - 1, worldToCol(bounds.x + bounds.width));
        int rowMin = Math.max(0, worldToRow(bounds.y));
        int rowMax = Math.min(layer.getHeight() - 1, worldToRow(bounds.y + bounds.height));

        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                if (layer.getCell(col, row) != null) {
                    result.add(new int[]{col, row});
                }
            }
        }
        return result;
    }
}
