package com.example.clownmaze.world.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.clownmaze.core.interact.InteractableObject;
import com.example.clownmaze.core.interact.InteractableObject.Kind;

/**
 * Static configuration of interactable objects for each (level, room) pair.
 * Coordinates are in world-pixel space matching the maze.tmx layout.
 *
 * Room 1 bounds ≈ x:16-272  y:288-464
 * Room 2 bounds ≈ x:352-608 y:288-464
 * Room 3 bounds ≈ x:16-608  y:16-224
 */
public final class LevelManager {

    private static final float R  = 30f; // default interaction radius (px)
    private static final float RR = 40f; // larger radius for rune/sequence objects

    private static final Map<String, List<InteractableObject>> CFG = new HashMap<>();

    static {
        // ── LEVEL 1 ── same riddles as before, wrapped as TASK objects ──────
        CFG.put("1.1", List.of(
            obj(Kind.TASK, 80,  350, 0),
            obj(Kind.TASK, 230, 420, 1)
        ));
        CFG.put("1.2", List.of(
            obj(Kind.TASK, 420, 350, 0),
            obj(Kind.TASK, 550, 420, 1),
            obj(Kind.TASK, 500, 310, 2)
        ));
        CFG.put("1.3", List.of(
            obj(Kind.TASK, 100,  80, 0),
            obj(Kind.TASK, 250, 160, 1),
            obj(Kind.TASK, 450,  80, 2),
            obj(Kind.TASK, 550, 160, 3)
        ));

        // ── LEVEL 2 ── mini-game tasks + fake traps ──────────────────────────
        // Room 1: 3 × "remember the color" + 2 fakes (all look identical)
        CFG.put("2.1", List.of(
            obj(Kind.MEMORY_TASK,  80, 350, 0),   // shows RED
            obj(Kind.MEMORY_TASK, 210, 420, 1),   // shows BLUE
            obj(Kind.MEMORY_TASK, 240, 310, 2),   // shows GREEN
            obj(Kind.FAKE,        145, 390, 0),
            obj(Kind.FAKE,        200, 355, 0)
        ));
        // Room 2: 3 × "repeat the sequence" + 2 fakes
        CFG.put("2.2", List.of(
            obj(Kind.SEQ_TASK, 420, 350, 0),      // sequence variant 0: B→C→A
            obj(Kind.SEQ_TASK, 510, 420, 1),      // sequence variant 1: C→A→B
            obj(Kind.SEQ_TASK, 475, 310, 2),      // sequence variant 2: A→C→B
            obj(Kind.FAKE,     455, 395, 0),
            obj(Kind.FAKE,     565, 355, 0)
        ));
        // Room 3: "find the correct object" — 3 correct + 2 wrong + 1 fake
        // All scrolls look identical; wrong/fake give penalties
        CFG.put("2.3", List.of(
            obj(Kind.FIND_CORRECT, 100,  80, 0),  // task slot 0
            obj(Kind.FIND_CORRECT, 350, 160, 1),  // task slot 1
            obj(Kind.FIND_CORRECT, 540,  80, 2),  // task slot 2
            obj(Kind.FIND_WRONG,   215, 125, 0),
            obj(Kind.FIND_WRONG,   460, 160, 0),
            obj(Kind.FAKE,         300,  80, 0)
        ));

        // ── LEVEL 3 ── hold / collect / rune-sequence ────────────────────────
        // Room 1: 3 × hold-E-for-3s tasks (object_l3_t1.png)
        CFG.put("3.1", List.of(
            obj(Kind.HOLD,  90, 390, 0),
            obj(Kind.HOLD, 210, 340, 1),
            obj(Kind.HOLD, 240, 430, 2)
        ));
        // Room 2: 1 trigger → spawns 3 collect items (object_l3_t2.png)
        CFG.put("3.2", List.of(
            obj(Kind.COLLECT_TRIGGER, 480, 380, 0)
        ));
        // Room 3: 3 runes (rune_a/b/c.png) — must activate in order A(0)→B(1)→C(2)
        // Use larger radius RR so runes are easier to reach
        CFG.put("3.3", List.of(
            obj(Kind.SEQUENCE_NODE, 520, 160, 0, RR),   // Rune A
            obj(Kind.SEQUENCE_NODE,  80,  50, 1, RR),   // Rune B
            obj(Kind.SEQUENCE_NODE, 300, 110, 2, RR)    // Rune C
        ));
    }

    private LevelManager() {}

    /** Returns a fresh (not shared) list of interactable objects for the given level+room. */
    public static List<InteractableObject> get(int level, int roomId) {
        List<InteractableObject> tmpl = CFG.getOrDefault(level + "." + roomId, List.of());
        List<InteractableObject> out  = new ArrayList<>(tmpl.size());
        for (InteractableObject o : tmpl) {
            out.add(new InteractableObject(o.getKind(), o.getX(), o.getY(), o.getRadius(), o.getPayload()));
        }
        return out;
    }

    private static InteractableObject obj(Kind kind, float x, float y, int payload) {
        return new InteractableObject(kind, x, y, R, payload);
    }

    private static InteractableObject obj(Kind kind, float x, float y, int payload, float radius) {
        return new InteractableObject(kind, x, y, radius, payload);
    }
}
