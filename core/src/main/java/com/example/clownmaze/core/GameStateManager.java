package com.example.clownmaze.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameStateManager {

    private static GameStateManager instance;

    private GameStateManager() { reset(); }

    public static GameStateManager getInstance() {
        if (instance == null) instance = new GameStateManager();
        return instance;
    }

    public static void resetInstance() { instance = null; }
 
    public enum Screen { MAIN_MENU, PLAYING, PAUSED, GAME_OVER, WIN }
 
    public static final Map<Integer, Float> ROOM_TIMERS = Map.of(
        1, 67f,
        2, 300f,
        3, 360f
    );

    private Screen  currentScreen;
    private int     currentRoom;

    private final Set<Integer>                collectedKeys  = new HashSet<>();
    private final Map<Integer, List<Integer>> solvedRiddles  = new HashMap<>();
    private final Map<Integer, Float>         roomTimeLeft   = new HashMap<>();

    private boolean isTrapped;
    private float   trapTimer;
    private boolean isSlowed;
    private float   slowTimer;

    private float   totalElapsed;
    private int     deathCount;

    private void reset() {
        currentScreen = Screen.MAIN_MENU;
        currentRoom  = 1;
        collectedKeys.clear();
        solvedRiddles.clear();
        roomTimeLeft.clear();

        for (Map.Entry<Integer, Float> e : ROOM_TIMERS.entrySet()) {
            solvedRiddles.put(e.getKey(), new ArrayList<>());
            roomTimeLeft.put(e.getKey(), e.getValue());
        }

        isTrapped = false;
        trapTimer = 0f;
        isSlowed  = false;
        slowTimer = 0f;
        totalElapsed = 0f;
    }

    public Screen getScreen()  { return currentScreen; }

    public void startGame() {
        reset();
        currentScreen = Screen.PLAYING;
    }

    public void pause()  { if (currentScreen == Screen.PLAYING) currentScreen = Screen.PAUSED; }
    public void resume() { if (currentScreen == Screen.PAUSED)  currentScreen = Screen.PLAYING; }

    public void triggerGameOver(String reason) {
        currentScreen = Screen.GAME_OVER;
        deathCount++;
        EventBus.getInstance().publish(new EventBus.GameOverEvent(reason));
    }

    public void triggerWin() {
        currentScreen = Screen.WIN;
        EventBus.getInstance().publish(new EventBus.GameWinEvent(totalElapsed));
    }

    public void restart() {
        reset();
        currentScreen = Screen.PLAYING;
    }

    public int   getCurrentRoom()           { return currentRoom; }
    public void  setCurrentRoom(int id)     { currentRoom = id; }
    public int   getDeathCount()            { return deathCount; }
    public float getTotalElapsed()          { return totalElapsed; }

    public boolean hasKeyFor(int roomId)    { return collectedKeys.contains(roomId); }
    public void    giveKey(int roomId)      { collectedKeys.add(roomId); }

    public void markRiddleSolved(int roomId, int riddleIndex) {
        solvedRiddles.computeIfAbsent(roomId, k -> new ArrayList<>()).add(riddleIndex);
    }

    public boolean allRiddlesSolved(int roomId) {
        Map<Integer, Integer> required = Map.of(1, 3, 2, 2, 3, 1);
        int solved = solvedRiddles.getOrDefault(roomId, List.of()).size();
        int needed = required.getOrDefault(roomId, 0);
        return solved >= needed;
    }

    public float   getRoomTimeLeft(int roomId)    { return roomTimeLeft.getOrDefault(roomId, 0f); }
    public boolean isRoomTimerExpired(int roomId) { return getRoomTimeLeft(roomId) <= 0f; }

    public void update(float dt) {
        if (currentScreen != Screen.PLAYING) return;

        totalElapsed += dt;
        float left = roomTimeLeft.getOrDefault(currentRoom, 0f);
        if (left > 0f) {
            left = Math.max(0f, left - dt);
            roomTimeLeft.put(currentRoom, left);
            if (left == 0f) {
                EventBus.getInstance().publish(new EventBus.RiddleTimerExpiredEvent(currentRoom));
            }
        }

        if (isTrapped) {
            trapTimer -= dt;
            if (trapTimer <= 0f) {
                isTrapped = false;
                EventBus.getInstance().publish(new EventBus.TrapFailEvent(0, 0));
            }
        }

        if (isSlowed) {
            slowTimer -= dt;
            if (slowTimer <= 0f) isSlowed = false;
        }
    }

    public void trapHero(float duration) {
        isTrapped = true;  trapTimer = duration; 
    }
    public void freeTrap() {
        isTrapped = false; trapTimer = 0f; 
    }
    public boolean isTrapped() {
        return isTrapped; 
    }
    public float getTrapTimer() { 
        return trapTimer; 
    }
    public void slowHero(float duration) {
        isSlowed = true;  slowTimer = duration; 
    }
    public boolean isSlowed() { 
        return isSlowed; 
    }
}
