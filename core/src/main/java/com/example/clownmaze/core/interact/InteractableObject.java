package com.example.clownmaze.core.interact;

public final class InteractableObject {

    public enum Kind {
        TASK,           // opens a riddle (payload = riddleIndex in the riddles list)
        FAKE,           // random negative effect — looks identical to TASK
        HOLD,           // hold E for 3 seconds (payload = riddle index to mark solved)
        COLLECT,        // pick-up item; all items in room must be collected (payload = group id)
        SEQUENCE_NODE,  // must be activated in payload order 0→1→2
        // ── Level 2 ──────────────────────────────────────────────────────────
        MEMORY_TASK,      // show color for 2.5s → player picks it  (payload = task slot 0-2)
        SEQ_TASK,         // show sequence for 3s → player repeats  (payload = task slot 0-2)
        FIND_CORRECT,     // correct object in "find the item" group (payload = task slot 0-2)
        FIND_WRONG,       // wrong decoy — penalty only              (payload unused)
        // ── Level 3 ──────────────────────────────────────────────────────────
        COLLECT_TRIGGER   // press E → spawns COLLECT items for Task 2 (payload = task slot)
    }

    private final Kind  kind;
    private final float x, y;
    private final float radius;
    private final int   payload;
    private boolean used = false;

    public InteractableObject(Kind kind, float x, float y, float radius, int payload) {
        this.kind    = kind;
        this.x       = x;
        this.y       = y;
        this.radius  = radius;
        this.payload = payload;
    }

    public boolean isInRange(float heroX, float heroY) {
        float dx = heroX - x;
        float dy = heroY - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    public Kind    getKind()    { return kind; }
    public float   getX()       { return x; }
    public float   getY()       { return y; }
    public float   getRadius()  { return radius; }
    public int     getPayload() { return payload; }
    public boolean isUsed()     { return used; }
    public void    markUsed()   { used = true; }
    public void    reset()      { used = false; }
}
