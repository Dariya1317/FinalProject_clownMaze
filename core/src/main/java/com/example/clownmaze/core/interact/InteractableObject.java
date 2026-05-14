package com.example.clownmaze.core.interact;

public final class InteractableObject {

    public enum Kind {
        TASK,           
        FAKE,          
        HOLD,           
        COLLECT,        
        SEQUENCE_NODE,  
        MEMORY_TASK,      
        SEQ_TASK,        
        FIND_CORRECT,     
        FIND_WRONG,     
        COLLECT_TRIGGER   
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
