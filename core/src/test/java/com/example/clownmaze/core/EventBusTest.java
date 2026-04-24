package com.example.clownmaze.core;

import org.junit.jupiter.api.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    @BeforeEach
    void setUp() {
        EventBus.resetInstance();
    }

    @AfterEach
    void tearDown() {
        EventBus.resetInstance();
    }

    @Test
    @DisplayName("publish() calls the subscribed handler")
    void publish_callsSubscribedHandler() {
        AtomicBoolean called = new AtomicBoolean(false);
        EventBus.getInstance().subscribe(EventBus.GameOverEvent.class, e -> called.set(true));
        EventBus.getInstance().publish(new EventBus.GameOverEvent("test"));
        assertTrue(called.get());
    }

    @Test
    @DisplayName("Multiple subscribers for the same event type are all notified")
    void publish_notifiesAllSubscribers() {
        AtomicInteger count = new AtomicInteger(0);
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> count.incrementAndGet());
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> count.incrementAndGet());
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> count.incrementAndGet());
        bus.publish(new EventBus.RoomEnterEvent(1));
        assertEquals(3, count.get());
    }

    @Test
    @DisplayName("unsubscribe() stops handler from receiving events")
    void unsubscribe_preventsHandlerFromFiring() {
        AtomicBoolean called = new AtomicBoolean(false);
        Consumer<EventBus.GameOverEvent> handler = e -> called.set(true);
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.GameOverEvent.class, handler);
        bus.unsubscribe(EventBus.GameOverEvent.class, handler);
        bus.publish(new EventBus.GameOverEvent("test"));
        assertFalse(called.get());
    }

    @Test
    @DisplayName("publish() with no listeners does not throw")
    void publish_noListeners_doesNotThrow() {
        assertDoesNotThrow(() ->
            EventBus.getInstance().publish(new EventBus.RoomEnterEvent(2)));
    }

    @Test
    @DisplayName("clear() removes all listeners — publish fires nothing")
    void clear_removesAllListeners() {
        AtomicBoolean called = new AtomicBoolean(false);
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> called.set(true));
        bus.clear();
        bus.publish(new EventBus.RoomEnterEvent(1));
        assertFalse(called.get());
    }

    @Test
    @DisplayName("Events are routed only to their correct subscriber type")
    void publish_routesEventToCorrectType() {
        AtomicBoolean roomFired = new AtomicBoolean(false);
        AtomicBoolean overFired = new AtomicBoolean(false);
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> roomFired.set(true));
        bus.subscribe(EventBus.GameOverEvent.class,  e -> overFired.set(true));
        bus.publish(new EventBus.RoomEnterEvent(1));
        assertTrue(roomFired.get());
        assertFalse(overFired.get(), "GameOverEvent handler must not fire for RoomEnterEvent");
    }

    @Test
    @DisplayName("Handler receives the correct event payload")
    void publish_handlerReceivesCorrectData() {
        AtomicReference<String> reason = new AtomicReference<>();
        EventBus.getInstance().subscribe(EventBus.GameOverEvent.class, e -> reason.set(e.reason()));
        EventBus.getInstance().publish(new EventBus.GameOverEvent("clown_killed"));
        assertEquals("clown_killed", reason.get());
    }

    @Test
    @DisplayName("getInstance() returns the same singleton instance")
    void getInstance_returnsSameInstance() {
        assertSame(EventBus.getInstance(), EventBus.getInstance());
    }

    @Test
    @DisplayName("resetInstance() gives a fresh bus with no listeners")
    void resetInstance_givesCleanBus() {
        AtomicBoolean called = new AtomicBoolean(false);
        EventBus.getInstance().subscribe(EventBus.RoomEnterEvent.class, e -> called.set(true));
        EventBus.resetInstance();
        EventBus.getInstance().publish(new EventBus.RoomEnterEvent(1));
        assertFalse(called.get(), "After resetInstance, old listeners must not fire");
    }

    @Test
    @DisplayName("Nested publish() does not throw ConcurrentModificationException")
    void publish_nestedPublish_doesNotThrow() {
        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class, e -> {
            if (e.roomId() == 1) bus.publish(new EventBus.RoomEnterEvent(2));
        });
        assertDoesNotThrow(() -> bus.publish(new EventBus.RoomEnterEvent(1)));
    }
}
