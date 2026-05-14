package com.example.clownmaze.world.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.clownmaze.core.interact.InteractableObject;
import com.example.clownmaze.core.interact.InteractableObject.Kind;

public final class LevelManager {

    private static final float R  = 30f;
    private static final float RR = 40f;

    private static final Map<String, List<InteractableObject>> CFG = new HashMap<>();

    static {
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

        CFG.put("2.1", List.of(
            obj(Kind.MEMORY_TASK,  80, 350, 0),
            obj(Kind.MEMORY_TASK, 210, 420, 1),
            obj(Kind.MEMORY_TASK, 240, 310, 2),
            obj(Kind.FAKE,        145, 390, 0),
            obj(Kind.FAKE,        200, 355, 0)
        ));
        CFG.put("2.2", List.of(
            obj(Kind.SEQ_TASK, 420, 350, 0),
            obj(Kind.SEQ_TASK, 510, 420, 1),
            obj(Kind.SEQ_TASK, 475, 310, 2),
            obj(Kind.FAKE,     455, 395, 0),
            obj(Kind.FAKE,     565, 355, 0)
        ));
        CFG.put("2.3", List.of(
            obj(Kind.FIND_CORRECT, 100,  80, 0),
            obj(Kind.FIND_CORRECT, 350, 160, 1),
            obj(Kind.FIND_CORRECT, 540,  80, 2),
            obj(Kind.FIND_WRONG,   215, 125, 0),
            obj(Kind.FIND_WRONG,   460, 160, 0),
            obj(Kind.FAKE,         300,  80, 0)
        ));

        CFG.put("3.1", List.of(
            obj(Kind.HOLD,  90, 390, 0),
            obj(Kind.HOLD, 210, 340, 1),
            obj(Kind.HOLD, 240, 430, 2)
        ));
        CFG.put("3.2", List.of(
            obj(Kind.COLLECT_TRIGGER, 480, 380, 0)
        ));
        CFG.put("3.3", List.of(
            obj(Kind.SEQUENCE_NODE, 520, 160, 0, RR),
            obj(Kind.SEQUENCE_NODE,  80,  50, 1, RR),
            obj(Kind.SEQUENCE_NODE, 300, 110, 2, RR)
        ));
    }

    private LevelManager() {}

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

