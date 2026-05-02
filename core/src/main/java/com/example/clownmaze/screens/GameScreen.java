package com.example.clownmaze.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;
import com.example.clownmaze.core.entity.Hero;
import com.example.clownmaze.core.entity.PlayerInputHandler;
import com.example.clownmaze.core.entity.ai.ClownAI;
import com.example.clownmaze.core.riddles.BaseRiddle;
import com.example.clownmaze.core.riddles.RiddleFactory;
import com.example.clownmaze.render.CameraController;
import com.example.clownmaze.render.EntityRenderer;
import com.example.clownmaze.render.LightingSystem;
import com.example.clownmaze.ui.hud.RiddleOverlay;
import com.example.clownmaze.ui.hud.ScreamerOverlay;
import com.example.clownmaze.ui.hud.TimerWidget;
import com.example.clownmaze.world.map.MapLoader;
import com.example.clownmaze.world.map.RoomDescriptor;
import com.example.clownmaze.world.map.RoomManager;
import com.example.clownmaze.world.map.TileFactory;

public class GameScreen implements Screen {

    private static final float CATCH_RADIUS = 20f;

    private final Game               game;
    private final SpriteBatch        batch;
    private final GameStateManager   gsm;
    private final Hero               hero;
    private final ClownAI            clown;
    private final PlayerInputHandler input;
    private final CameraController   camera;
    private final LightingSystem     lighting;
    private final RoomManager        roomManager;
    private final PauseScreen        pauseScreen;

    // Sprite rendering
    private final EntityRenderer entities;

    // HUD
    private final RiddleOverlay  riddleOverlay;
    private final TimerWidget    timerWidget;
    private final ScreamerOverlay screamer;

    // Riddles for the current room
    private List<BaseRiddle> riddles = new ArrayList<>();

    private boolean snapCameraNextFrame;
    private boolean winHandled;

    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter = e -> {
        snapCameraNextFrame = true;
        loadRiddles(e.roomId());
    };
    private final Consumer<EventBus.RoomResetEvent> onRoomReset =
        e -> resetRiddles();

    public GameScreen(Game game) {
        this.game = game;

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch    = new SpriteBatch();
        gsm      = GameStateManager.getInstance();
        camera   = new CameraController(w, h);
        lighting = new LightingSystem(w, h);

        MapLoader   mapLoader   = new MapLoader();
        TileFactory tileFactory = new TileFactory();
        roomManager = new RoomManager(tileFactory, mapLoader);

        hero  = new Hero(0f, 0f);
        clown = new ClownAI(0f, 0f, hero);
        roomManager.bind(hero, clown);

        roomManager.registerRoom(new RoomDescriptor(1, "maps/maze.tmx", 144f, 360f,  32f, 288f));
        roomManager.registerRoom(new RoomDescriptor(2, "maps/maze.tmx", 480f, 360f, 368f, 288f));
        roomManager.registerRoom(new RoomDescriptor(3, "maps/maze.tmx", 320f, 120f,  32f,  32f));

        input         = new PlayerInputHandler(hero);
        entities      = new EntityRenderer();
        riddleOverlay = new RiddleOverlay();
        timerWidget   = new TimerWidget();
        screamer      = new ScreamerOverlay();
        pauseScreen   = new PauseScreen(game, this);

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        bus.subscribe(EventBus.RoomResetEvent.class, onRoomReset);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        if (gsm.getScreen() == GameStateManager.Screen.PAUSED) {
            gsm.resume();
        } else {
            winHandled = false;
            gsm.startGame();   // → fires RoomEnterEvent(1) → onRoomEnter loads riddles
            tryLoadRoom(1);
        }
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 0.05f);

        // ESC → pause (only when no overlay is open)
        if (!riddleOverlay.isOpen()
                && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                && gsm.getScreen() == GameStateManager.Screen.PLAYING) {
            gsm.pause();
            game.setScreen(pauseScreen);
            return;
        }

        // Win → WinScreen (once)
        if (!winHandled && gsm.getScreen() == GameStateManager.Screen.WIN) {
            winHandled = true;
            game.setScreen(new WinScreen(game, this));
            return;
        }

        // E → open riddle overlay
        if (!riddleOverlay.isOpen()
                && !gsm.isScreamerActive()
                && gsm.getScreen() == GameStateManager.Screen.PLAYING
                && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            BaseRiddle active = getActiveRiddle();
            if (active != null) riddleOverlay.show(active);
        }

        // Game logic update
        gsm.update(dt);

        // Hero movement — frozen while riddle overlay or screamer is shown
        if (gsm.getScreen() == GameStateManager.Screen.PLAYING
                && !gsm.isScreamerActive()
                && !riddleOverlay.isOpen()) {
            input.handleInput(dt);
        }

        hero.update(dt);
        clown.update(dt);

        // Clown-hero collision
        if (gsm.getScreen() == GameStateManager.Screen.PLAYING
                && !gsm.isScreamerActive()
                && clown.isChasing()) {
            checkClownCollision();
        }

        // Camera
        if (snapCameraNextFrame) {
            applyCameraBounds();
            camera.snapTo(hero.getX(), hero.getY());
            snapCameraNextFrame = false;
        }
        camera.update(hero.getX(), hero.getY(), dt);

        // ── Render ────────────────────────────────────────────────────────────
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f);

        entities.update(dt);

        batch.setProjectionMatrix(camera.getCamera().combined);
        batch.begin();
        roomManager.render(batch);
        entities.renderClown(batch, clown);
        entities.renderHero(batch, hero);
        batch.end();

        lighting.render(hero.getX(), hero.getY(), camera.getCamera());

        // HUD — drawn in screen coords (ScreenViewport)
        timerWidget.update();
        timerWidget.render();

        riddleOverlay.update(dt);
        riddleOverlay.render();

        screamer.render(gsm.isScreamerActive(), gsm.getScreamerTimer());
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        lighting.resize(width, height);
        riddleOverlay.resize(width, height);
        timerWidget.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        bus.unsubscribe(EventBus.RoomResetEvent.class, onRoomReset);
        batch.dispose();
        lighting.dispose();
        roomManager.dispose();
        hero.dispose();
        clown.dispose();
        entities.dispose();
        riddleOverlay.dispose();
        timerWidget.dispose();
        screamer.dispose();
        pauseScreen.dispose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void tryLoadRoom(int roomId) {
        try {
            roomManager.loadRoom(roomId);
            applyCameraBounds();
            camera.snapTo(hero.getX(), hero.getY());
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Room " + roomId + " not loaded: " + e.getMessage());
        }
    }

    private void applyCameraBounds() {
        Rectangle b = roomManager.getMapBounds();
        if (b == null) return;
        camera.setBounds(b.x, b.y, b.x + b.width, b.y + b.height);
    }

    private void loadRiddles(int roomId) {
        try {
            riddles = RiddleFactory.loadRoom(roomId);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Riddles for room " + roomId + " not loaded: " + e.getMessage());
            riddles = new ArrayList<>();
        }
    }

    private void resetRiddles() {
        for (BaseRiddle r : riddles) r.reset();
        if (riddleOverlay.isOpen()) riddleOverlay.hide();
    }

    /** Returns the first unsolved riddle, or null if all are done. */
    private BaseRiddle getActiveRiddle() {
        for (BaseRiddle r : riddles) {
            if (!r.isSolved()) return r;
        }
        return null;
    }

    private void checkClownCollision() {
        float cx = clown.getX() + Hero.SPRITE_WIDTH  / 2f;
        float cy = clown.getY() + Hero.SPRITE_HEIGHT / 2f;
        float hx = hero.getX()  + Hero.SPRITE_WIDTH  / 2f;
        float hy = hero.getY()  + Hero.SPRITE_HEIGHT / 2f;
        double dist = Math.sqrt((cx - hx) * (cx - hx) + (cy - hy) * (cy - hy));
        if (dist < CATCH_RADIUS) {
            gsm.heroCaught();
        }
    }
}
