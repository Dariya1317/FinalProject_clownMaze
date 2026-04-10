package com.example.clownmaze.core;

import java.util.*;
import java.util.function.Consumer;

public final class EventBus {

    private static EventBus instance;

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }
    
    private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();


    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        listeners
            .computeIfAbsent(eventType, k -> new ArrayList<>())
            .add((Consumer<Object>) handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<Object>> list = listeners.get(eventType);
        if (list != null) list.remove((Consumer<Object>) handler);
    }

    public <T> void publish(T event) {
        List<Consumer<Object>> list = listeners.get(event.getClass());
        if (list != null) new ArrayList<>(list).forEach(h -> h.accept(event));
    }

    public void clear() {
        listeners.clear();
    }

    public static void resetInstance() {
        instance = null;
    }

    public record RoomExitEvent(float heroX, float heroY) {}
    
    public record RoomEnterEvent(int roomId) {}

    public record TrapCaughtEvent(float trapX, float trapY) {}
    
    public record TrapFailEvent(float trapX, float trapY) {}

    public record SpiderContactEvent(float spiderX, float spiderY) {}

    public record RiddleSolvedEvent(int roomId, int riddleIndex) {}

    public record RiddleTimerExpiredEvent(int roomId) {}

    public record KeyCollectedEvent(int keyId, int targetRoom) {}

    public record DoorUnlockedEvent(int roomId) {}

    public record GameOverEvent(String reason) {}
    
    public record GameWinEvent(float elapsedSeconds) {}

    public record ScreamerEvent() {}
}
