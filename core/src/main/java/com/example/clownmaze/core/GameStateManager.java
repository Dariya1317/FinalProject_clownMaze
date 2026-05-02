package com.example.clownmaze.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    public enum Screen { MAIN_MENU, PLAYING, PAUSED, WIN }

    public static final int FIRST_ROOM = 1;
    public static final int LAST_ROOM  = 3;

    public static final Map<Integer, Float> ROOM_TIMERS;
    public static final Map<Integer, Integer> ROOM_RIDDLE_COUNT;

    static {
        Map<Integer, Float> timers = new LinkedHashMap<>();
        timers.put(1, 40f);
        timers.put(2, 90f);
        timers.put(3, 180f);
        ROOM_TIMERS = Map.copyOf(timers);

        Map<Integer, Integer> riddles = new LinkedHashMap<>();
        riddles.put(1, 2);
        riddles.put(2, 3);
        riddles.put(3, 4);
        ROOM_RIDDLE_COUNT = Map.copyOf(riddles);
    }

    private Screen currentScreen;
    private int    currentRoom;

    private final Map<Integer, Set<Integer>> solvedRiddles = new HashMap<>();
    private final Map<Integer, Float>        roomTimeLeft  = new HashMap<>();

    private boolean timerExpired;
    private boolean screamerActive;
    private float   screamerTimer;
    private float   totalElapsed;
    private int     deathCount;

    private void reset() {
        currentScreen = Screen.MAIN_MENU;
        currentRoom   = FIRST_ROOM;
        solvedRiddles.clear();
        roomTimeLeft.clear();
        for (Map.Entry<Integer, Float> e : ROOM_TIMERS.entrySet()) {
            solvedRiddles.put(e.getKey(), new HashSet<>());
            roomTimeLeft.put(e.getKey(), e.getValue());
        }
        timerExpired   = false;
        screamerActive = false;
        screamerTimer  = 0f;
        totalElapsed   = 0f;
    }

    public void startGame() {
        reset();
        currentScreen = Screen.PLAYING;
        EventBus.getInstance().publish(new EventBus.RoomEnterEvent(currentRoom));
    }

    public void pause()  { if (currentScreen == Screen.PLAYING) currentScreen = Screen.PAUSED; }
    public void resume() { if (currentScreen == Screen.PAUSED)  currentScreen = Screen.PLAYING; }

    public void update(float dt) {
        if (currentScreen != Screen.PLAYING) return;

        if (screamerActive) {
            screamerTimer -= dt;
            if (screamerTimer <= 0f) {
                screamerActive = false;
                EventBus.getInstance().publish(
                    new EventBus.ScreamerEndEvent(EventBus.ScreamerSource.CLOWN));
            }
            return;
        }

        totalElapsed += dt;

        if (!timerExpired) {
            float left = roomTimeLeft.getOrDefault(currentRoom, 0f);
            if (left > 0f) {
                left = Math.max(0f, left - dt);
                roomTimeLeft.put(currentRoom, left);
                if (left == 0f) {
                    timerExpired = true;
                    EventBus.getInstance().publish(
                        new EventBus.TimerExpiredEvent(currentRoom));
                }
            }
        }
    }

    public void markRiddleSolved(int roomId, int riddleIndex) {
        Set<Integer> set = solvedRiddles.computeIfAbsent(roomId, k -> new HashSet<>());
        if (!set.add(riddleIndex)) return;

        EventBus.getInstance().publish(new EventBus.RiddleSolvedEvent(roomId, riddleIndex));
        if (allRiddlesSolved(roomId)) {
            EventBus.getInstance().publish(new EventBus.RoomCompleteEvent(roomId));
        }
    }

    public boolean allRiddlesSolved(int roomId) {
        int solved = solvedRiddles.getOrDefault(roomId, Set.of()).size();
        int needed = ROOM_RIDDLE_COUNT.getOrDefault(roomId, 0);
        return needed > 0 && solved >= needed;
    }

    public int riddlesSolvedIn(int roomId) {
        return solvedRiddles.getOrDefault(roomId, Set.of()).size();
    }

    public void advanceToRoom(int nextRoomId) {
        currentRoom    = nextRoomId;
        timerExpired   = false;
        roomTimeLeft.put(nextRoomId, ROOM_TIMERS.getOrDefault(nextRoomId, 0f));
        solvedRiddles.computeIfAbsent(nextRoomId, k -> new HashSet<>()).clear();
        EventBus.getInstance().publish(new EventBus.RoomEnterEvent(nextRoomId));
    }

    public void heroCaught() {
        if (screamerActive) return;
        screamerActive = true;
        screamerTimer  = 3f;
        deathCount++;
        EventBus.getInstance().publish(new EventBus.HeroCaughtEvent());
        EventBus.getInstance().publish(
            new EventBus.ScreamerStartEvent(EventBus.ScreamerSource.CLOWN));
    }

    public void resetCurrentRoom() {
        timerExpired = false;
        roomTimeLeft.put(currentRoom, ROOM_TIMERS.getOrDefault(currentRoom, 0f));
        solvedRiddles.computeIfAbsent(currentRoom, k -> new HashSet<>()).clear();
        EventBus.getInstance().publish(new EventBus.RoomResetEvent(currentRoom));
    }

    public void triggerWin() {
        currentScreen = Screen.WIN;
        EventBus.getInstance().publish(new EventBus.GameWinEvent());
    }

    public Screen  getScreen()              { return currentScreen; }
    public int     getCurrentRoom()         { return currentRoom; }
    public int     getDeathCount()          { return deathCount; }
    public float   getTotalElapsed()        { return totalElapsed; }
    public float   getRoomTimeLeft(int id)  { return roomTimeLeft.getOrDefault(id, 0f); }
    public boolean isTimerExpired()         { return timerExpired; }
    public boolean isScreamerActive()       { return screamerActive; }
    public float   getScreamerTimer()       { return screamerTimer; }
}
