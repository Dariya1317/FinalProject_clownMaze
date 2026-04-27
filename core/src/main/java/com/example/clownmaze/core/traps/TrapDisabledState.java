package com.example.clownmaze.core.traps;

public final class TrapDisabledState implements TrapState {

    @Override
    public void enter(Trap trap) {}

    @Override
    public void update(Trap trap, float delta, float heroX, float heroY) {}

    @Override
    public void exit(Trap trap) {}
}
