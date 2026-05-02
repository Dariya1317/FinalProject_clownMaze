package com.example.clownmaze.core.entity.ai;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;

public final class KillState implements ClownState {

    @Override
    public void enter(ClownAI clown) {
        GameStateManager.getInstance().heroCaught();
    }

    @Override
    public void update(ClownAI clown, float delta) {
    }

    @Override
    public void exit(ClownAI clown) {
    }

    void onScreamerEnd(ClownAI clown, EventBus.ScreamerEndEvent event) {
    }
}
