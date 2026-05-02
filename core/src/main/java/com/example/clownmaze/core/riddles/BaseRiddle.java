package com.example.clownmaze.core.riddles;

import com.example.clownmaze.core.GameStateManager;

public abstract class BaseRiddle {

    protected final int roomId;
    protected final int riddleIndex;
    protected final String question;

    private boolean solved;

    protected BaseRiddle(int roomId, int riddleIndex, String question) {
        this.roomId = roomId;
        this.riddleIndex = riddleIndex;
        this.question = question;
    }

    public void display() {}

    public boolean isAwaiting() {
        return !solved;
    }
    public final void submit(String playerAnswer) {
        if (solved) return;
        if (validate(playerAnswer)) {
            solved = true;
            onSuccess();
        } else {
            onFail();
        }
    }

    protected abstract boolean validate(String playerAnswer);
    protected void onSuccess() {
        GameStateManager.getInstance().markRiddleSolved(roomId, riddleIndex);
    }

    protected void onFail() {}
    public abstract RiddleType getType();

    public String getQuestion() { return question; }
    public int getRoomId() { return roomId; }
    public int getRiddleIndex() { return riddleIndex; }
    public boolean isSolved() { return solved; }

    public void reset() { solved = false; }
}
