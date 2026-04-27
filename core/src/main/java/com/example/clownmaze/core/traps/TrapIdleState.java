package com.example.clownmaze.core.traps;

public final class TrapIdleState implements TrapState {

    @Override
    public void enter(Trap trap) {}

    @Override
    public void update(Trap trap, float delta, float heroX, float heroY) {
        float dx = heroX - trap.getX();
        float dy = heroY - trap.getY();
        if (dx * dx + dy * dy <= Trap.TRIGGER_RADIUS * Trap.TRIGGER_RADIUS) {
            trap.trigger();
        }
    }

    @Override
    public void exit(Trap trap) {}
}
