package com.example.clownmaze.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import com.example.clownmaze.core.EventBus;

public final class AudioManager implements Disposable {

    private static AudioManager instance;

    private final Map<Integer, Music> ambients = new HashMap<>();
    private Music   current;
    private boolean heartbeatPlaying = false;

    private Music   heartbeat;
    private Sound   clownSting;

    private Music   menuMusic;
    private Music   successMusic;
    private Music   gameOverMusic;

    private final Consumer<EventBus.RoomEnterEvent>    onRoomEnter;
    private final Consumer<EventBus.ScreamerStartEvent> onScreamerStart;
    private final Consumer<EventBus.GameWinEvent>       onWin;
    private final Consumer<EventBus.GameOverEvent>      onGameOver;

    private AudioManager() {
        for (int i = 1; i <= 3; i++) {
            Music m = Gdx.audio.newMusic(Gdx.files.internal("audio/ambient_room" + i + ".ogg"));
            m.setLooping(true);
            m.setVolume(1.0f);
            ambients.put(i, m);
        }

        try {
            heartbeat = Gdx.audio.newMusic(Gdx.files.internal("audio/heartbeat.ogg"));
            heartbeat.setLooping(true);
            heartbeat.setVolume(1.0f);
        } catch (Exception e) {
            heartbeat = null;
        }

        try {
            clownSting = Gdx.audio.newSound(Gdx.files.internal("audio/screamer_sound.ogg"));
        } catch (Exception e) {
            clownSting = null;
        }

        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/main_menu_sound.ogg"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(1.0f);
        } catch (Exception e) {
            menuMusic = null;
        }

        try {
            successMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/success_sound.ogg"));
            successMusic.setLooping(false);
            successMusic.setVolume(1.0f);
        } catch (Exception e) {
            successMusic = null;
        }

        try {
            gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/game_over_sound.ogg"));
            gameOverMusic.setLooping(false);
            gameOverMusic.setVolume(1.0f);
        } catch (Exception e) {
            gameOverMusic = null;
        }

        onRoomEnter     = e -> playRoom(e.roomId());
        onScreamerStart = e -> { if (clownSting != null) clownSting.play(1.0f); };
        onWin           = e -> playSuccess();
        onGameOver      = e -> playGameOver();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class,    onRoomEnter);
        bus.subscribe(EventBus.ScreamerStartEvent.class, onScreamerStart);
        bus.subscribe(EventBus.GameWinEvent.class,       onWin);
        bus.subscribe(EventBus.GameOverEvent.class,      onGameOver);
    }

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public void playMainMenu() {
        stopAll();
        if (menuMusic != null) menuMusic.play();
    }

    public void playSuccess() {
        stopAll();
        if (successMusic != null) successMusic.play();
    }

    public void playGameOver() {
        stopAll();
        if (gameOverMusic != null) gameOverMusic.play();
    }

    public void playRoom(int roomId) {
        stopAll();
        current = ambients.get(roomId);
        if (current != null) current.play();
    }

    public void updateHeartbeat(boolean active) {
        if (heartbeat == null) return;
        if (active && !heartbeatPlaying) {
            heartbeat.play();
            heartbeatPlaying = true;
            if (current != null) current.setVolume(0.2f);
        } else if (!active && heartbeatPlaying) {
            heartbeat.stop();
            heartbeatPlaying = false;
            if (current != null) current.setVolume(1.0f);
        }
    }

    public void playClownSting() {
        if (clownSting != null) clownSting.play(0.55f);
    }

    private void stopAll() {
        if (current != null)      { current.setVolume(1.0f); current.stop(); current = null; }
        if (heartbeat != null && heartbeatPlaying) { heartbeat.stop(); heartbeatPlaying = false; }
        if (menuMusic     != null && menuMusic.isPlaying())     menuMusic.stop();
        if (successMusic  != null && successMusic.isPlaying())  successMusic.stop();
        if (gameOverMusic != null && gameOverMusic.isPlaying()) gameOverMusic.stop();
    }

    @Override
    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomEnterEvent.class,    onRoomEnter);
        bus.unsubscribe(EventBus.ScreamerStartEvent.class, onScreamerStart);
        bus.unsubscribe(EventBus.GameWinEvent.class,       onWin);
        bus.unsubscribe(EventBus.GameOverEvent.class,      onGameOver);

        stopAll();
        for (Music m : ambients.values()) m.dispose();
        ambients.clear();
        if (heartbeat     != null) { heartbeat.dispose();     heartbeat     = null; }
        if (clownSting    != null) { clownSting.dispose();    clownSting    = null; }
        if (menuMusic     != null) { menuMusic.dispose();      menuMusic     = null; }
        if (successMusic  != null) { successMusic.dispose();   successMusic  = null; }
        if (gameOverMusic != null) { gameOverMusic.dispose();  gameOverMusic = null; }
        instance = null;
    }
}

