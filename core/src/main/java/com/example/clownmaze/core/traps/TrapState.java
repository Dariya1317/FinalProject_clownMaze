package com.example.clownmaze.core.traps;

public interface TrapState {
    void enter(Trap trap);
    void update(Trap trap, float delta, float heroX, float heroY);
    void exit(Trap trap);
}
