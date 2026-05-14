package com.example.clownmaze.world.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

public final class BloodDecalSystem {

    private static final String ASSET          = "fx/blood_decal.png";
    private static final int    FLOOR_TILE_ID  = 1;
    private static final int    DECALS_MIN     = 1;
    private static final int    DECALS_MAX     = 3;
    private static final float  SIZE_BASE_MUL  = 1.2f;
    private static final float  SIZE_SCALE_MIN = 0.7f;
    private static final float  SIZE_SCALE_MAX = 1.5f;
    private static final float  CLUSTER_CHANCE = 0.4f;

    private Texture texture;
    private final List<BloodDecal> decals = new ArrayList<>();

    public void rebuild(List<Room> rooms, TiledMapTileLayer layer, int tileSize) {
        decals.clear();
        if (rooms == null || rooms.isEmpty() || layer == null || tileSize <= 0) return;

        Random rng = new Random();
        for (Room room : rooms) {
            spawnForRoom(room, layer, tileSize, rng);
        }
    }

    private void spawnForRoom(Room room, TiledMapTileLayer layer, int tileSize, Random rng) {
        if (room == null) return;

        List<int[]> floorCells = floorCellsInBounds(room.getBounds(), layer, tileSize);
        if (floorCells.isEmpty()) return;

        List<int[]> nearWall = wallAdjacentCells(floorCells, layer);
        int count = DECALS_MIN + rng.nextInt(DECALS_MAX - DECALS_MIN + 1);
        float base = tileSize * SIZE_BASE_MUL;

        for (int i = 0; i < count; i++) {
            boolean cluster   = !nearWall.isEmpty() && rng.nextFloat() < CLUSTER_CHANCE;
            List<int[]> pool  = cluster ? nearWall : floorCells;
            int[] cell        = pool.get(rng.nextInt(pool.size()));
            float cx          = (cell[0] + rng.nextFloat()) * tileSize;
            float cy          = (cell[1] + rng.nextFloat()) * tileSize;
            float size        = base * (SIZE_SCALE_MIN + rng.nextFloat() * (SIZE_SCALE_MAX - SIZE_SCALE_MIN));
            float rot         = rng.nextFloat() * 360f;
            decals.add(new BloodDecal(cx, cy, size, rot));
        }
    }

    public void render(SpriteBatch batch) {
        ensureTexture();
        if (texture == null || decals.isEmpty() || batch == null) return;

        for (BloodDecal d : decals) {
            float size = d.getSize();
            float half = size * 0.5f;
            batch.draw(texture,
                d.getCenterX() - half, d.getCenterY() - half,
                half, half,
                size, size,
                1f, 1f,
                d.getRotationDeg(),
                0, 0, texture.getWidth(), texture.getHeight(),
                false, false);
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        decals.clear();
    }

    private void ensureTexture() {
        if (texture != null) return;
        if (!Gdx.files.internal(ASSET).exists()) return;
        texture = new Texture(Gdx.files.internal(ASSET));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private static List<int[]> floorCellsInBounds(Rectangle bounds,
                                                  TiledMapTileLayer layer,
                                                  int tileSize) {
        List<int[]> out = new ArrayList<>();
        int colMin = Math.max(0, (int) (bounds.x / tileSize));
        int colMax = Math.min(layer.getWidth()  - 1, (int) ((bounds.x + bounds.width)  / tileSize));
        int rowMin = Math.max(0, (int) (bounds.y / tileSize));
        int rowMax = Math.min(layer.getHeight() - 1, (int) ((bounds.y + bounds.height) / tileSize));
        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                if (isFloor(layer, col, row)) {
                    out.add(new int[]{col, row});
                }
            }
        }
        return out;
    }

    private static List<int[]> wallAdjacentCells(List<int[]> floor, TiledMapTileLayer layer) {
        List<int[]> out = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = { 0, 0, -1, 1};
        for (int[] c : floor) {
            for (int k = 0; k < 4; k++) {
                if (!isFloor(layer, c[0] + dx[k], c[1] + dy[k])) {
                    out.add(c);
                    break;
                }
            }
        }
        return out;
    }

    private static boolean isFloor(TiledMapTileLayer layer, int col, int row) {
        if (col < 0 || row < 0 || col >= layer.getWidth() || row >= layer.getHeight()) return false;
        TiledMapTileLayer.Cell cell = layer.getCell(col, row);
        if (cell == null || cell.getTile() == null) return false;
        return cell.getTile().getId() == FLOOR_TILE_ID;
    }
}
