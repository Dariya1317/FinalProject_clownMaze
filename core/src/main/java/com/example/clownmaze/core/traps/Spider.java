package com.example.clownmaze.core.traps;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.entity.Hero;

public final class Spider {

    private static final float CONTACT_RADIUS = 10f;

    private final float x, y;
    private boolean cooldown;

    public Spider(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean tryContact(Hero hero) {
        if (cooldown) return false;
        float dx = hero.getX() - x;
        float dy = hero.getY() - y;
        if (dx * dx + dy * dy > CONTACT_RADIUS * CONTACT_RADIUS) return false;
        cooldown = true;
        EventBus.getInstance().publish(new EventBus.SpiderContactEvent(x, y));
        EventBus.getInstance().publish(new EventBus.ScreamerEvent());
        return true;
    }

    public float getX()        { return x; }
    public float getY()        { return y; }
    public boolean isCooldown(){ return cooldown; }
}
