package com.example.clownmaze.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

import com.example.clownmaze.core.EventBus;

/** Singleton. Plays looping ambient per room + optional heartbeat at low timer. */
public final class AudioManager implements Disposable {

    private static AudioManager instance;

    private final Map<Integer, Music> ambients = new HashMap<>();
    private Music current;

    private Music   heartbeat;
    private boolean heartbeatPlaying = false;

    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter;

    private AudioManager() {
        for (int i = 1; i <= 3; i++) {
            Music m = Gdx.audio.newMusic(
                Gdx.files.internal("audio/ambient_room" + i + ".ogg"));
            m.setLooping(true);
            ambients.put(i, m);
        }

        // Heartbeat is optional — works only if the file exists
        try {
            heartbeat = Gdx.audio.newMusic(
                Gdx.files.internal("audio/heartbeat.ogg"));
            heartbeat.setLooping(true);
            heartbeat.setVolume(1.0f);
        } catch (Exception e) {
            heartbeat = null;
        }

        onRoomEnter = e -> playRoom(e.roomId());
        EventBus.getInstance().subscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
    }

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public void playRoom(int roomId) {
        if (current != null) current.stop();
        current = ambients.get(roomId);
        if (current != null) current.play();
    }

    /** Call every frame. Starts/stops the heartbeat loop based on tension flag. */
    public void updateHeartbeat(boolean active) {
        if (heartbeat == null) return;
        if (active && !heartbeatPlaying) {
            heartbeat.play();
            heartbeatPlaying = true;
        } else if (!active && heartbeatPlaying) {
            heartbeat.stop();
            heartbeatPlaying = false;
        }
    }

    public void stop() {
        if (current != null) {
            current.stop();
            current = null;
        }
        if (heartbeat != null && heartbeatPlaying) {
            heartbeat.stop();
            heartbeatPlaying = false;
        }
    }

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        stop();
        for (Music m : ambients.values()) m.dispose();
        ambients.clear();
        if (heartbeat != null) { heartbeat.dispose(); heartbeat = null; }
        instance = null;
    }
}
