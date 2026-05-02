package com.example.clownmaze.world.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;
import com.example.clownmaze.core.entity.Hero;
import com.example.clownmaze.core.entity.ai.ClownAI;

public final class RoomManager {

    private static final String LAYER_TILES  = "tiles";
    private static final String LAYER_ROOMS  = "Rooms";
    private static final String PROP_ROOM_ID = "id";

    private final TileFactory tileFactory;
    private final MapLoader   mapLoader;
    private final Map<Integer, RoomDescriptor> descriptors = new LinkedHashMap<>();
    private final List<Room>  rooms = new ArrayList<>();

    private TiledMap map;
    private int      tileSize;
    private Room     currentRoom;

    private Hero    hero;
    private ClownAI clown;

    private final Consumer<EventBus.RoomCompleteEvent> onRoomComplete;
    private final Consumer<EventBus.ScreamerEndEvent>  onScreamerEnd;

    public RoomManager(TileFactory tileFactory, MapLoader mapLoader) {
        this.tileFactory = tileFactory;
        this.mapLoader   = mapLoader;

        onRoomComplete = e -> handleRoomComplete(e.roomId());
        onScreamerEnd  = e -> handleScreamerEnd();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomCompleteEvent.class, onRoomComplete);
        bus.subscribe(EventBus.ScreamerEndEvent.class,  onScreamerEnd);
    }

    public void registerRoom(RoomDescriptor descriptor) {
        descriptors.put(descriptor.id(), descriptor);
    }

    public void bind(Hero hero, ClownAI clown) {
        this.hero  = hero;
        this.clown = clown;
    }

    public void loadRoom(int roomId) {
        RoomDescriptor desc = descriptors.get(roomId);
        if (desc == null) return;

        mapLoader.load(desc.tmxPath());
        this.map      = mapLoader.getMap();
        this.tileSize = mapLoader.getTileWidthPx();
        tileFactory.clear();
        rooms.clear();

        parseRooms();

        currentRoom = rooms.stream()
                           .filter(r -> r.getId() == roomId)
                           .findFirst()
                           .orElseGet(() -> buildFallbackRoom(roomId, desc));

        applySpawns(currentRoom);
    }

    private Room buildFallbackRoom(int roomId, RoomDescriptor desc) {
        Rectangle bounds = new Rectangle(0, 0,
            mapLoader.getMapWidthTiles()  * (float) tileSize,
            mapLoader.getMapHeightTiles() * (float) tileSize);
        return new Room(roomId, bounds, List.of(),
            desc.heroSpawnX(), desc.heroSpawnY(),
            desc.clownSpawnX(), desc.clownSpawnY());
    }

    private void applySpawns(Room room) {
        if (hero != null) {
            hero.setBounds(room.getBounds());
            hero.teleportTo(room.getHeroSpawnX(), room.getHeroSpawnY());
        }
        if (clown != null) {
            clown.teleportTo(room.getClownSpawnX(), room.getClownSpawnY());
            clown.toIdle();
            clown.updateHeroPosition(room.getHeroSpawnX(), room.getHeroSpawnY());
        }
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

                float worldX = col * (float) tileSize;
                float worldY = row * (float) tileSize;
                tileType.draw(batch, worldX, worldY, tileSize);
            }
        }
    }

    private void handleRoomComplete(int completedRoomId) {
        if (completedRoomId == GameStateManager.LAST_ROOM) {
            GameStateManager.getInstance().triggerWin();
            return;
        }

        int next = completedRoomId + 1;
        if (!descriptors.containsKey(next)) return;

        GameStateManager.getInstance().advanceToRoom(next);
        loadRoom(next);
    }

    private void handleScreamerEnd() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.resetCurrentRoom();
        if (currentRoom != null) applySpawns(currentRoom);
    }

    public Room      getCurrentRoom()   { return currentRoom; }
    public Rectangle getCurrentBounds() { return currentRoom == null ? null : currentRoom.getBounds(); }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public int getTileSize() { return tileSize; }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomCompleteEvent.class, onRoomComplete);
        bus.unsubscribe(EventBus.ScreamerEndEvent.class,  onScreamerEnd);
        mapLoader.dispose();
    }

    private void parseRooms() {
        MapLayer objectLayer = map.getLayers().get(LAYER_ROOMS);
        if (objectLayer == null) return;

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) map.getLayers().get(LAYER_TILES);

        for (MapObject obj : objectLayer.getObjects()) {
            if (!(obj instanceof RectangleMapObject rectObj)) continue;

            MapProperties props  = obj.getProperties();
            int           roomId = props.get(PROP_ROOM_ID, 1, Integer.class);
            Rectangle     bounds = rectObj.getRectangle();

            float clownX = bounds.x + tileSize * 0.5f;
            float clownY = bounds.y + tileSize * 0.5f;
            float heroX  = bounds.x + bounds.width  * 0.5f;
            float heroY  = bounds.y + bounds.height * 0.5f;

            Float pHX = props.get("heroSpawnX", Float.class);
            Float pHY = props.get("heroSpawnY", Float.class);
            Float pCX = props.get("clownSpawnX", Float.class);
            Float pCY = props.get("clownSpawnY", Float.class);
            if (pHX != null) heroX  = pHX;
            if (pHY != null) heroY  = pHY;
            if (pCX != null) clownX = pCX;
            if (pCY != null) clownY = pCY;

            List<int[]> coords = collectTileCoordsInBounds(tileLayer, bounds);
            rooms.add(new Room(roomId, bounds, coords, heroX, heroY, clownX, clownY));
        }
    }

    private List<int[]> collectTileCoordsInBounds(TiledMapTileLayer layer, Rectangle bounds) {
        List<int[]> result = new ArrayList<>();
        if (layer == null) return result;

        int colMin = Math.max(0, (int) (bounds.x / tileSize));
        int colMax = Math.min(layer.getWidth()  - 1, (int) ((bounds.x + bounds.width)  / tileSize));
        int rowMin = Math.max(0, (int) (bounds.y / tileSize));
        int rowMax = Math.min(layer.getHeight() - 1, (int) ((bounds.y + bounds.height) / tileSize));

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
