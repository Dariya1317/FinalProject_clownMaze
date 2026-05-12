package com.example.clownmaze.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class EventBus {

    private static EventBus instance;

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    public static void resetInstance() {
        instance = null;
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

    public record TimerExpiredEvent(int roomId) {}

    public record RoomCompleteEvent(int roomId) {}

    public record RoomEnterEvent(int roomId) {}

    public record RoomResetEvent(int roomId) {}

    public record ScreamerStartEvent(ScreamerSource source) {}

    public record ScreamerEndEvent(ScreamerSource source) {}

    public record HeroCaughtEvent() {}

    public record SpiderContactEvent(int spiderId, float spiderX, float spiderY) {}

    public record RiddleSolvedEvent(int roomId, int riddleIndex) {}

    public record GameWinEvent() {}

    public record HeroInteractEvent(float heroX, float heroY) {}

    public record PlayerHurtEvent(int remainingHp) {}

    public record GameOverEvent() {}

    public record LevelEnterEvent(int level) {}

    public record FreezeEvent(float duration) {}

    public enum ScreamerSource { CLOWN, SPIDER }
}
