package com.example.clownmaze.core.entity.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.clownmaze.core.entity.WalkabilityChecker;

public final class ChaseStrategy implements ClownMovementStrategy {

    static final float PATH_UPDATE_INTERVAL = 0.2f;
    private static final float ARRIVAL_THRESHOLD = 4f;
    private static final int   MAX_ITERATIONS = 500;

    private final WalkabilityChecker walkabilityChecker;
    private final int tileSize;

    private float targetX, targetY;
    private List<float[]> path = new ArrayList<>();
    private int pathIndex;
    private float pathTimer = PATH_UPDATE_INTERVAL; 

    public ChaseStrategy(WalkabilityChecker walkabilityChecker, int tileSize) {
        this.walkabilityChecker = walkabilityChecker;
        this.tileSize = tileSize;
    }

    @Override
    public void setTarget(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public void move(ClownAI clown, float delta) {
        pathTimer += delta;
        if (pathTimer >= PATH_UPDATE_INTERVAL || path.isEmpty()) {
            recalculate(clown.getX(), clown.getY());
            pathTimer = 0f;
        }
        if (pathIndex >= path.size()) return;
        float[] next = path.get(pathIndex);
        float dx   = next[0] - clown.getX();
        float dy   = next[1] - clown.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < ARRIVAL_THRESHOLD) {
            pathIndex++;
            return;
        }

        float speed = clown.getChaseSpeed();
        clown.setX(clown.getX() + (dx / dist) * speed * delta);
        clown.setY(clown.getY() + (dy / dist) * speed * delta);
    }

    private void recalculate(float fromX, float fromY) {
        int sc = worldToCol(fromX),  sr = worldToRow(fromY);
        int gc = worldToCol(targetX), gr = worldToRow(targetY);
        List<int[]> tilePath = aStar(sc, sr, gc, gr);
        path = new ArrayList<>(tilePath.size());
        for (int[] tile : tilePath) {
            path.add(new float[]{
                tile[0] * tileSize + tileSize * 0.5f,
                tile[1] * tileSize + tileSize * 0.5f
            });
        }
        pathIndex = 0;
    }
    List<int[]> aStar(int startCol, int startRow, int goalCol, int goalRow) {
        Map<String, String>  cameFrom = new HashMap<>();
        Map<String, Integer> gScore   = new HashMap<>();
        Set<String> closed   = new HashSet<>();

        PriorityQueue<int[]> open = new PriorityQueue<>(
            Comparator.comparingInt(n -> n[2])
        );

        String startKey = key(startCol, startRow);
        gScore.put(startKey, 0);
        open.add(new int[]{startCol, startRow, h(startCol, startRow, goalCol, goalRow)});

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        int limit = MAX_ITERATIONS;

        while (!open.isEmpty() && limit-- > 0) {
            int[] curr = open.poll();
            int col = curr[0], row = curr[1];
            String currKey = key(col, row);
            if (closed.contains(currKey)) continue;
            closed.add(currKey);

            if (col == goalCol && row == goalRow)
                return reconstruct(cameFrom, currKey, startKey);

            int g = gScore.get(currKey);
            for (int[] d : dirs) {
                int nc = col + d[0], nr = row + d[1];
                String nKey = key(nc, nr);
                if (closed.contains(nKey)) continue;

                float cx = nc * tileSize + tileSize * 0.5f;
                float cy = nr * tileSize + tileSize * 0.5f;
                if (!walkabilityChecker.isWalkable(cx, cy)) continue;

                int ng = g + 1;
                if (ng < gScore.getOrDefault(nKey, Integer.MAX_VALUE)) {
                    gScore.put(nKey, ng);
                    cameFrom.put(nKey, currKey);
                    open.add(new int[]{nc, nr, ng + h(nc, nr, goalCol, goalRow)});
                }
            }
        }
        return List.of();
    }

    private List<int[]> reconstruct(Map<String, String> cameFrom, String end, String start) {
        LinkedList<int[]> result = new LinkedList<>();
        String cur = end;
        while (!cur.equals(start) && cameFrom.containsKey(cur)) {
            result.addFirst(parseKey(cur));
            cur = cameFrom.get(cur);
        }
        return result;
    }

    private int h(int c, int r, int gc, int gr){
        return Math.abs(c - gc) + Math.abs(r - gr); 
    }
    private int worldToCol(float wx){ 
        return (int) (wx / tileSize); 
    }
    private int worldToRow(float wy){ 
        return (int) (wy / tileSize); 
    }
    private String key(int col, int row){ 
        return col + "," + row; 
    }

    private int[] parseKey(String k) {
        int comma = k.indexOf(',');
        return new int[]{
            Integer.parseInt(k.substring(0, comma)),
            Integer.parseInt(k.substring(comma + 1))
        };
    }
}
