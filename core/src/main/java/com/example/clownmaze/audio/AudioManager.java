package com.example.clownmaze.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import com.example.clownmaze.core.EventBus;

/**
 * Singleton audio manager.
 * Handles ambient room music, heartbeat tension, screamer sounds,
 * and dedicated tracks for main menu, win, and game-over screens.
 */
public final class AudioManager implements Disposable {

    private static AudioManager instance;

    // ── Room ambients ─────────────────────────────────────────────────────────
    private final Map<Integer, Music> ambients = new HashMap<>();
    private Music   current;             // currently playing ambient
    private boolean heartbeatPlaying = false;

    // ── One-shot sound effect ─────────────────────────────────────────────────
    private Music   heartbeat;
    private Sound   clownSting;          // screamer hit + fake-object sting

    // ── Screen music ──────────────────────────────────────────────────────────
    private Music   menuMusic;
    private Music   successMusic;
    private Music   gameOverMusic;

    // ── Event listeners ───────────────────────────────────────────────────────
    private final Consumer<EventBus.RoomEnterEvent>    onRoomEnter;
    private final Consumer<EventBus.ScreamerStartEvent> onScreamerStart;
    private final Consumer<EventBus.GameWinEvent>       onWin;
    private final Consumer<EventBus.GameOverEvent>      onGameOver;

    private AudioManager() {
        // Room ambients
        for (int i = 1; i <= 3; i++) {
            Music m = Gdx.audio.newMusic(Gdx.files.internal("audio/ambient_room" + i + ".ogg"));
            m.setLooping(true);
            m.setVolume(1.0f);
            ambients.put(i, m);
        }

        // Heartbeat (looping tension track)
        try {
            heartbeat = Gdx.audio.newMusic(Gdx.files.internal("audio/heartbeat.ogg"));
            heartbeat.setLooping(true);
            heartbeat.setVolume(1.0f);
        } catch (Exception e) {
            heartbeat = null;
        }

        // Screamer / clown sting (reused for both)
        try {
            clownSting = Gdx.audio.newSound(Gdx.files.internal("audio/screamer_sound.ogg"));
        } catch (Exception e) {
            clownSting = null;
        }

        // Main-menu looping music
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/main_menu_sound.ogg"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(1.0f);
        } catch (Exception e) {
            menuMusic = null;
        }

        // Win / success jingle (plays once)
        try {
            successMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/success_sound.ogg"));
            successMusic.setLooping(false);
            successMusic.setVolume(1.0f);
        } catch (Exception e) {
            successMusic = null;
        }

        // Game-over music (plays once)
        try {
            gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/game_over_sound.ogg"));
            gameOverMusic.setLooping(false);
            gameOverMusic.setVolume(1.0f);
        } catch (Exception e) {
            gameOverMusic = null;
        }

        // Wire up events
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

    // ── Public API ────────────────────────────────────────────────────────────

    /** Start main-menu looping music (stops everything else first). */
    public void playMainMenu() {
        stopAll();
        if (menuMusic != null) menuMusic.play();
    }

    /** Play the win jingle (stops everything else first). */
    public void playSuccess() {
        stopAll();
        if (successMusic != null) successMusic.play();
    }

    /** Play the game-over track (stops everything else first). */
    public void playGameOver() {
        stopAll();
        if (gameOverMusic != null) gameOverMusic.play();
    }

    /** Switch to room ambient. Called automatically on RoomEnterEvent. */
    public void playRoom(int roomId) {
        stopAll();
        current = ambients.get(roomId);
        if (current != null) current.play();
    }

    /**
     * Call every frame while playing.
     * Starts heartbeat + ducks ambient at <= 5 s; restores when inactive.
     */
    public void updateHeartbeat(boolean active) {
        if (heartbeat == null) return;
        if (active && !heartbeatPlaying) {
            heartbeat.play();
            heartbeatPlaying = true;
            // Duck ambient so heartbeat is clearly audible
            if (current != null) current.setVolume(0.2f);
        } else if (!active && heartbeatPlaying) {
            heartbeat.stop();
            heartbeatPlaying = false;
            // Restore ambient volume
            if (current != null) current.setVolume(1.0f);
        }
    }

    /** Short clown laugh — used by fake objects and wrong-rune feedback. */
    public void playClownSting() {
        if (clownSting != null) clownSting.play(0.55f);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /** Stop all currently playing music tracks and reset state. */
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
