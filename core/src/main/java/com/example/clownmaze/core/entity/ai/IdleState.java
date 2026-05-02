package com.example.clownmaze.core.entity.ai;

public final class IdleState implements ClownState {

    private float anchorX;
    private float anchorY;

    public IdleState(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public void setAnchor(float x, float y) {
        this.anchorX = x;
        this.anchorY = y;
    }

    @Override
    public void enter(ClownAI clown) {
        clown.setX(anchorX);
        clown.setY(anchorY);
    }

    @Override
    public void update(ClownAI clown, float delta) {
    }

    @Override
    public void exit(ClownAI clown) {
    }
}
