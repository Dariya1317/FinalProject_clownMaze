package com.example.clownmaze.lwjgl3.core;

import java.util.*;
import java.util.function.Consumer;

/**
 * EventBus — паттерн Observer (Поведенческий) + Singleton (Порождающий).
 *
 * Централизованная шина событий. Все компоненты общаются только через неё,
 * не зная друг о друге — слабая связанность (Loose Coupling).
 *
 * КАК ИСПОЛЬЗОВАТЬ:
 *   EventBus.getInstance().subscribe(SomeEvent.class, e -> doSomething(e));
 *   EventBus.getInstance().publish(new SomeEvent(...));
 */
public final class EventBus {

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static EventBus instance;

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    // ─── Хранилище подписчиков ────────────────────────────────────────────────
    private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();

    // ─── Публичный API ────────────────────────────────────────────────────────

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

    // ─── Сброс для тестов ────────────────────────────────────────────────────
    public static void resetInstance() {
        instance = null;
    }

    // =========================================================================
    //  ВСЕ ТИПЫ СОБЫТИЙ ИГРЫ (Java 17 records)
    // =========================================================================

    /** Герой вышел из комнаты в коридор → клоун переходит в CHASE */
    public record RoomExitEvent(float heroX, float heroY) {}

    /** Герой вошёл в комнату → клоун возвращается в PATROL */
    public record RoomEnterEvent(int roomId) {}

    /** Герой попал в капкан */
    public record TrapCaughtEvent(float trapX, float trapY) {}

    /** Герой не вырвался из капкана за 5 секунд → клоун бежит убивать */
    public record TrapFailEvent(float trapX, float trapY) {}

    /** Герой коснулся паука → скример + замедление */
    public record SpiderContactEvent(float spiderX, float spiderY) {}

    /** Загадка решена */
    public record RiddleSolvedEvent(int roomId, int riddleIndex) {}

    /** Таймер загадок истёк → клоун убивает */
    public record RiddleTimerExpiredEvent(int roomId) {}

    /** Герой подобрал ключ */
    public record KeyCollectedEvent(int keyId, int targetRoom) {}

    /** Дверь открыта */
    public record DoorUnlockedEvent(int roomId) {}

    /** Игра окончена */
    public record GameOverEvent(String reason) {}

    /** Победа — герой вышел через красную дверь */
    public record GameWinEvent(float elapsedSeconds) {}

    /** Запустить скример (звук + красный флеш) */
    public record ScreamerEvent() {}
}
