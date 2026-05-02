package com.example.clownmaze.screens;

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
import com.example.clownmaze.render.CameraController;
import com.example.clownmaze.render.LightingSystem;
import com.example.clownmaze.world.map.MapLoader;
import com.example.clownmaze.world.map.RoomDescriptor;
import com.example.clownmaze.world.map.RoomManager;
import com.example.clownmaze.world.map.TileFactory;

public class GameScreen implements Screen {

    private final Game             game;
    private final SpriteBatch      batch;
    private final GameStateManager gsm;
    private final Hero             hero;
    private final ClownAI          clown;
    private final PlayerInputHandler input;
    private final CameraController camera;
    private final LightingSystem   lighting;
    private final RoomManager      roomManager;
    private final PauseScreen      pauseScreen;

    private boolean snapCameraNextFrame;
    private boolean winHandled;

    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter =
        e -> snapCameraNextFrame = true;

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

        roomManager.registerRoom(new RoomDescriptor(1, "maps/room1.tmx", 100f, 100f, 32f, 32f));
        roomManager.registerRoom(new RoomDescriptor(2, "maps/room2.tmx", 100f, 100f, 32f, 32f));
        roomManager.registerRoom(new RoomDescriptor(3, "maps/room3.tmx", 100f, 100f, 32f, 32f));

        input       = new PlayerInputHandler(hero);
        pauseScreen = new PauseScreen(game, this);

        EventBus.getInstance().subscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
    }

    @Override
    public void show() {
        if (gsm.getScreen() == GameStateManager.Screen.PAUSED) {
            gsm.resume();
        } else {
            winHandled = false;
            gsm.startGame();
            tryLoadRoom(1);
        }
    }

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
        Rectangle b = roomManager.getCurrentBounds();
        if (b == null) return;
        camera.setBounds(b.x, b.y, b.x + b.width, b.y + b.height);
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 0.05f);

        // pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                && gsm.getScreen() == GameStateManager.Screen.PLAYING) {
            gsm.pause();
            game.setScreen(pauseScreen);
            return;
        }

        // win
        if (!winHandled && gsm.getScreen() == GameStateManager.Screen.WIN) {
            winHandled = true;
            game.setScreen(new WinScreen(game, this));
            return;
        }

        gsm.update(dt);

        if (gsm.getScreen() == GameStateManager.Screen.PLAYING && !gsm.isScreamerActive()) {
            input.handleInput(dt);
        }

        hero.update(dt);
        clown.update(dt);

        if (snapCameraNextFrame) {
            applyCameraBounds();
            camera.snapTo(hero.getX(), hero.getY());
            snapCameraNextFrame = false;
        }

        camera.update(hero.getX(), hero.getY(), dt);

        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f);

        batch.setProjectionMatrix(camera.getCamera().combined);
        batch.begin();
        roomManager.render(batch);
        batch.end();

        lighting.render(hero.getX(), hero.getY(), camera.getCamera());
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        lighting.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(EventBus.RoomEnterEvent.class, onRoomEnter);
        batch.dispose();
        lighting.dispose();
        roomManager.dispose();
        hero.dispose();
        clown.dispose();
        pauseScreen.dispose();
    }
}
