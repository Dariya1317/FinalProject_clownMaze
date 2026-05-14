package com.example.clownmaze.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.example.clownmaze.audio.AudioManager;
import com.example.clownmaze.core.EventBus;
import com.example.clownmaze.core.GameStateManager;
import com.example.clownmaze.core.entity.Ghost;
import com.example.clownmaze.core.entity.GhostAppearance;
import com.example.clownmaze.core.entity.Hero;
import com.example.clownmaze.core.entity.PlayerInputHandler;
import com.example.clownmaze.core.entity.Spider;
import com.example.clownmaze.core.entity.ai.ClownAI;
import com.example.clownmaze.core.interact.FakeEffect;
import com.example.clownmaze.core.interact.InteractableObject;
import com.example.clownmaze.core.riddles.BaseRiddle;
import com.example.clownmaze.core.riddles.RiddleFactory;
import com.example.clownmaze.render.CameraController;
import com.example.clownmaze.render.EntityRenderer;
import com.example.clownmaze.render.LightingSystem;
import com.example.clownmaze.ui.hud.MiniGameOverlay;
import com.example.clownmaze.ui.hud.RiddleOverlay;
import com.example.clownmaze.ui.hud.ScreamerOverlay;
import com.example.clownmaze.ui.hud.TimerWidget;
import com.example.clownmaze.ui.hud.VignetteEffect;
import com.example.clownmaze.world.map.LevelManager;
import com.example.clownmaze.world.map.MapLoader;
import com.example.clownmaze.world.map.RoomDescriptor;
import com.example.clownmaze.world.map.RoomManager;
import com.example.clownmaze.world.map.TileFactory;

public class GameScreen implements Screen {

    private static final float CATCH_RADIUS   = 20f;
    private static final float HOLD_DURATION  = 3f;
    private static final int   COLLECT_TOTAL  = 3;
    private static final float[][] COLLECT_SPAWN = {
        {420f, 360f}, {540f, 420f}, {490f, 310f}
    };

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

    private final EntityRenderer entities;

    private final RiddleOverlay   riddleOverlay;
    private final MiniGameOverlay miniGameOverlay;
    private final TimerWidget     timerWidget;
    private final ScreamerOverlay screamer;
    private final AudioManager    audio;

    private final List<Ghost>       ghosts;
    private final ArrayList<Spider> spiders;
    private final int               initialSpiderCount;

    private List<BaseRiddle>         riddles = new ArrayList<>();
    private List<InteractableObject> objects = new ArrayList<>();

    private float  holdTimer        = 0f;
    private int    sequenceProgress = 0;
    private int    collectedCount   = 0;
    private float  seqHintTimer     = 0f;
    private String seqHintMsg       = "";

    private final Texture    objMarkerTex;
    private final BitmapFont objHintFont;
    private final SpriteBatch hudBatch;
    private final Texture     taskTex;
    private final Texture     holdTaskTex;
    private final Texture     collectItemTex;
    private final Texture     runeATex;
    private final Texture     runeBTex;
    private final Texture     runeCTex;
    private final Vector3     projVec = new Vector3();

    private boolean snapCameraNextFrame;
    private boolean winHandled;
    private boolean gameOverHandled;

    private static final float       GHOST_DMG_FLASH   = 0.3f;
    private final  VignetteEffect     vignetteEffect    = new VignetteEffect();
    private final  Rectangle          heroHitbox        = new Rectangle();
    private        boolean            vignetteActive;
    private        float              ghostHitCooldown;
    private        float              ghostDmgFlashTimer;
    private final  Consumer<EventBus.FreezeEvent> onFreeze;

    private final Consumer<EventBus.LevelEnterEvent> onLevelEnter;

    private final Consumer<EventBus.RoomEnterEvent> onRoomEnter = e -> {
        snapCameraNextFrame = true;
        loadRiddles(e.roomId());
        objects          = LevelManager.get(GameStateManager.getInstance().getCurrentLevel(), e.roomId());
        holdTimer        = 0f;
        collectedCount   = 0;
        sequenceProgress = 0;
        if (GameStateManager.getInstance().getCurrentLevel() == 3 && e.roomId() == 3) {
            seqHintTimer = 4f;
            seqHintMsg   = "Activate runes in order:  A > B > C";
        } else {
            seqHintTimer = 0f;
            seqHintMsg   = "";
        }
    };
    private final Consumer<EventBus.RoomResetEvent> onRoomReset = e -> {
        resetRiddles();
        objects          = LevelManager.get(GameStateManager.getInstance().getCurrentLevel(), e.roomId());
        holdTimer        = 0f;
        collectedCount   = 0;
        sequenceProgress = 0;
        if (GameStateManager.getInstance().getCurrentLevel() == 3 && e.roomId() == 3) {
            seqHintTimer = 4f;
            seqHintMsg   = "Activate runes in order:  A > B > C";
        } else {
            seqHintTimer = 0f;
            seqHintMsg   = "";
        }
    };

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

        input           = new PlayerInputHandler(hero);
        entities        = new EntityRenderer();
        ghosts          = buildGhosts();
        spiders         = buildSpiders();
        initialSpiderCount = spiders.size();
        riddleOverlay   = new RiddleOverlay();
        miniGameOverlay = new MiniGameOverlay();
        timerWidget     = new TimerWidget();
        screamer        = new ScreamerOverlay();
        pauseScreen     = new PauseScreen(game, this);
        audio           = AudioManager.getInstance();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        objMarkerTex = new Texture(pm);
        pm.dispose();

        objHintFont = new BitmapFont();
        objHintFont.getData().setScale(1.1f);

        hudBatch = new SpriteBatch();
        hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);

        taskTex        = new Texture(Gdx.files.internal("sprites/object/task.png"));
        holdTaskTex    = new Texture(Gdx.files.internal("sprites/object/object_l3_t1.png"));
        collectItemTex = new Texture(Gdx.files.internal("sprites/object/object_l3_t2.png"));
        runeATex       = new Texture(Gdx.files.internal("sprites/object/rune_a.png"));
        runeBTex       = new Texture(Gdx.files.internal("sprites/object/rune_b.png"));
        runeCTex       = new Texture(Gdx.files.internal("sprites/object/rune_c.png"));

        onLevelEnter = e -> {
            gsm.pause();
            ghosts.forEach(Ghost::reset);
            vignetteActive = false;
            game.setScreen(new LevelIntroScreen(game, "ui/level" + e.level() + ".png", this));
        };

        onFreeze = e -> hero.freeze(e.duration());

        EventBus bus = EventBus.getInstance();
        bus.subscribe(EventBus.LevelEnterEvent.class, onLevelEnter);
        bus.subscribe(EventBus.FreezeEvent.class,     onFreeze);
        bus.subscribe(EventBus.RoomEnterEvent.class,  onRoomEnter);
        bus.subscribe(EventBus.RoomResetEvent.class,  onRoomReset);
    }

    @Override
    public void show() {
        if (gsm.getScreen() == GameStateManager.Screen.PAUSED) {
            gsm.resume();
        } else {
            winHandled      = false;
            gameOverHandled = false;
            gsm.startGame();
            tryLoadRoom(1);
        }
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 0.05f);

        boolean anyOverlayOpen = riddleOverlay.isOpen() || miniGameOverlay.isOpen();

        if (!anyOverlayOpen
                && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                && gsm.getScreen() == GameStateManager.Screen.PLAYING) {
            gsm.pause();
            game.setScreen(pauseScreen);
            return;
        }

        if (!winHandled && gsm.getScreen() == GameStateManager.Screen.WIN) {
            winHandled = true;
            game.setScreen(new WinScreen(game, this));
            return;
        }

        if (!gameOverHandled && gsm.getScreen() == GameStateManager.Screen.GAME_OVER) {
            gameOverHandled = true;
            game.setScreen(new GameOverScreen(game, this));
            return;
        }

        InteractableObject nearbyObj = getNearbyObject();

        boolean playing = gsm.getScreen() == GameStateManager.Screen.PLAYING
                          && !gsm.isScreamerActive();

        boolean holdingNow = !anyOverlayOpen && playing
                && nearbyObj != null
                && nearbyObj.getKind() == InteractableObject.Kind.HOLD
                && Gdx.input.isKeyPressed(Input.Keys.E);
        if (holdingNow) {
            holdTimer += dt;
            if (holdTimer >= HOLD_DURATION) {
                holdTimer = 0f;
                nearbyObj.markUsed();
                gsm.markRiddleSolved(gsm.getCurrentRoom(), nearbyObj.getPayload());
            }
        } else {
            holdTimer = 0f;
        }

        if (!anyOverlayOpen && playing
                && Gdx.input.isKeyJustPressed(Input.Keys.E)
                && nearbyObj != null
                && nearbyObj.getKind() != InteractableObject.Kind.HOLD) {
            handleInteract(nearbyObj);
        }

        gsm.update(dt);
        audio.updateHeartbeat(gsm.isHeartbeatActive());

        if (playing && !anyOverlayOpen) {
            input.handleInput(dt);
        }

        hero.update(dt);
        clown.update(dt);
        for (Ghost  g : ghosts)  g.update(dt);
        for (Spider s : spiders) s.update(dt, hero.getX(), hero.getY());

        vignetteActive = false;
        ghostHitCooldown = Math.max(0f, ghostHitCooldown   - dt);
        ghostDmgFlashTimer = Math.max(0f, ghostDmgFlashTimer - dt);
        if (playing) checkGhostInteractions();

        if (gsm.getCurrentLevel() == 3 && playing) {
            checkAutoCollect();
            if (seqHintTimer > 0f) seqHintTimer = Math.max(0f, seqHintTimer - dt);
        }

        if (playing && clown.isChasing()) checkClownCollision();

        if (snapCameraNextFrame) {
            applyCameraBounds();
            camera.snapTo(hero.getX(), hero.getY());
            snapCameraNextFrame = false;
        }
        camera.update(hero.getX(), hero.getY(), dt);

        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f);

        entities.update(dt);

        batch.setProjectionMatrix(camera.getCamera().combined);
        batch.begin();
        roomManager.render(batch);
        roomManager.renderDecals(batch);
        for (InteractableObject o : objects) {
            if (isScrollVisible(o))
                batch.draw(textureFor(o), o.getX() - 12f, o.getY() - 12f, 24f, 24f);
        }
        for (Spider s : spiders) entities.renderSpider(batch, s);
        for (Ghost  g : ghosts)  entities.renderGhost(batch, g);
        entities.renderClown(batch, clown);
        entities.renderHero(batch, hero);
        batch.end();

        lighting.render(hero.getX(), hero.getY(), camera.getCamera());

        if (vignetteActive) {
            vignetteEffect.render(
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (ghostDmgFlashTimer > 0f) {
            float alpha = (ghostDmgFlashTimer / GHOST_DMG_FLASH) * 0.40f;
            hudBatch.begin();
            hudBatch.setColor(1f, 0f, 0f, alpha);
            hudBatch.draw(objMarkerTex, 0f, 0f,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            hudBatch.setColor(1f, 1f, 1f, 1f);
            hudBatch.end();
        }

        timerWidget.update();
        timerWidget.render();

        if (nearbyObj != null && !anyOverlayOpen) {
            String hint;
            if (nearbyObj.getKind() == InteractableObject.Kind.HOLD) {
                hint = holdTimer > 0f
                    ? "[E] " + (int)(holdTimer / HOLD_DURATION * 100f) + "%"
                    : "[E] Hold E...";
            } else {
                hint = hintTextFor(nearbyObj);
            }
            projVec.set(nearbyObj.getX(), nearbyObj.getY() + 22f, 0f);
            camera.getCamera().project(projVec);
            hudBatch.begin();
            objHintFont.draw(hudBatch, hint, projVec.x - 30f, projVec.y);
            hudBatch.end();
        }

        if (seqHintTimer > 0f && !seqHintMsg.isEmpty()) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            hudBatch.begin();
            objHintFont.draw(hudBatch, seqHintMsg, sw / 2f - 115f, sh * 0.68f);
            hudBatch.end();
        }

        riddleOverlay.update(dt);
        riddleOverlay.render();

        miniGameOverlay.update(dt);
        miniGameOverlay.render();

        screamer.update(dt);
        screamer.render();
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        lighting.resize(width, height);
        riddleOverlay.resize(width, height);
        miniGameOverlay.resize(width, height);
        timerWidget.resize(width, height);
        screamer.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(EventBus.LevelEnterEvent.class, onLevelEnter);
        bus.unsubscribe(EventBus.FreezeEvent.class,     onFreeze);
        bus.unsubscribe(EventBus.RoomEnterEvent.class,  onRoomEnter);
        bus.unsubscribe(EventBus.RoomResetEvent.class,  onRoomReset);
        batch.dispose();
        lighting.dispose();
        roomManager.dispose();
        hero.dispose();
        clown.dispose();
        entities.dispose();
        riddleOverlay.dispose();
        miniGameOverlay.dispose();
        timerWidget.dispose();
        screamer.dispose();
        pauseScreen.dispose();
        for (Spider s : spiders) s.dispose();
        audio.dispose();
        taskTex.dispose();
        holdTaskTex.dispose();
        collectItemTex.dispose();
        runeATex.dispose();
        runeBTex.dispose();
        runeCTex.dispose();
        objMarkerTex.dispose();
        objHintFont.dispose();
        hudBatch.dispose();
        vignetteEffect.dispose();
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

    private void checkAutoCollect() {
        float hx = hero.getX() + Hero.SPRITE_WIDTH  / 2f;
        float hy = hero.getY() + Hero.SPRITE_HEIGHT / 2f;
        for (InteractableObject o : objects) {
            if (!o.isUsed() && o.getKind() == InteractableObject.Kind.COLLECT && o.isInRange(hx, hy)) {
                o.markUsed();
                collectedCount++;
                if (collectedCount >= COLLECT_TOTAL) {
                    collectedCount = 0;
                    gsm.markRiddleSolved(gsm.getCurrentRoom(), 0);
                }
            }
        }
    }

    private void spawnCollectItems() {
        for (float[] pos : COLLECT_SPAWN) {
            objects.add(new InteractableObject(InteractableObject.Kind.COLLECT, pos[0], pos[1], 20f, 0));
        }
    }

    private Texture textureFor(InteractableObject o) {
        return switch (o.getKind()) {
            case HOLD                      -> holdTaskTex;
            case COLLECT, COLLECT_TRIGGER  -> collectItemTex;
            case SEQUENCE_NODE             -> runeTexFor(o.getPayload());
            default                        -> taskTex;
        };
    }

    private Texture runeTexFor(int payload) {
        return switch (payload) {
            case 0  -> runeATex;
            case 1  -> runeBTex;
            default -> runeCTex;
        };
    }

    private boolean isScrollVisible(InteractableObject o) {
        if (o.isUsed()) return false;
        if (o.getKind() == InteractableObject.Kind.TASK) {
            int idx = o.getPayload();
            return idx >= 0 && idx < riddles.size() && !riddles.get(idx).isSolved();
        }
        return true;
    }

    private InteractableObject getNearbyObject() {
        float hx = hero.getX() + Hero.SPRITE_WIDTH  / 2f;
        float hy = hero.getY() + Hero.SPRITE_HEIGHT / 2f;
        for (InteractableObject o : objects) {
            if (!isScrollVisible(o)) continue;
            if (o.getKind() == InteractableObject.Kind.COLLECT) continue;
            if (o.isInRange(hx, hy)) return o;
        }
        return null;
    }

    private void handleInteract(InteractableObject obj) {
        switch (obj.getKind()) {
            case TASK -> {
                int idx = obj.getPayload();
                if (idx >= 0 && idx < riddles.size() && !riddles.get(idx).isSolved()) {
                    riddleOverlay.show(riddles.get(idx));
                }
            }
            case FAKE -> {
                FakeEffect.applyRandom(hero, spiders);
                obj.markUsed();
            }
            case MEMORY_TASK -> {
                miniGameOverlay.showMemory(obj.getPayload(), new MiniGameOverlay.ResultHandler() {
                    @Override public void onSolved() {
                        obj.markUsed();
                        gsm.markRiddleSolved(gsm.getCurrentRoom(), obj.getPayload());
                    }
                    @Override public void onPenalty() { gsm.reduceTimer(5f); }
                });
            }
            case SEQ_TASK -> {
                miniGameOverlay.showSequence(obj.getPayload(), new MiniGameOverlay.ResultHandler() {
                    @Override public void onSolved() {
                        obj.markUsed();
                        gsm.markRiddleSolved(gsm.getCurrentRoom(), obj.getPayload());
                    }
                    @Override public void onPenalty() { gsm.reduceTimer(5f); }
                });
            }
            case FIND_CORRECT -> {
                obj.markUsed();
                gsm.markRiddleSolved(gsm.getCurrentRoom(), obj.getPayload());
            }
            case FIND_WRONG -> {
                obj.markUsed();
                if (com.badlogic.gdx.math.MathUtils.randomBoolean())
                    gsm.reduceTimer(5f);
                else
                    audio.playClownSting();
            }
            case COLLECT_TRIGGER -> {
                collectedCount = 0;
                obj.markUsed();
                spawnCollectItems();
            }
            case SEQUENCE_NODE -> {
                int step = obj.getPayload();
                if (step == sequenceProgress) {
                    sequenceProgress++;
                    if (sequenceProgress >= 3) {
                        sequenceProgress = 0;
                        seqHintTimer = 0f;
                        seqHintMsg   = "";
                        for (InteractableObject o : objects) {
                            if (o.getKind() == InteractableObject.Kind.SEQUENCE_NODE) o.markUsed();
                        }
                        gsm.markRiddleSolved(gsm.getCurrentRoom(), 0);
                    } else {
                        seqHintMsg   = "Good! Progress: " + sequenceProgress + " / 3";
                        seqHintTimer = 2f;
                    }
                } else {
                    sequenceProgress = 0;
                    gsm.reduceTimer(5f);
                    audio.playClownSting();
                    seqHintMsg   = "Wrong order! Reset  (-5 sec)";
                    seqHintTimer = 2f;
                }
            }
            default -> {}
        }
    }

    private String hintTextFor(InteractableObject o) {
        return switch (o.getKind()) {
            case MEMORY_TASK              -> "[E] Memory task";
            case SEQ_TASK                 -> "[E] Sequence task";
            case FIND_CORRECT, FIND_WRONG -> "[E] Examine";
            case COLLECT_TRIGGER          -> "[E] Activate";
            case SEQUENCE_NODE            -> "[E] Activate rune";
            default                       -> "[E] Get task";
        };
    }

    private List<Ghost> buildGhosts() {
        GhostAppearance look = GhostAppearance.shared("ghost", 32, 32, 0.75f);
        Ghost g1 = new Ghost(look); g1.placeAt( 80, 360); g1.setRoamBounds( 16, 288, 272, 464);
        Ghost g2 = new Ghost(look); g2.placeAt(400, 360); g2.setRoamBounds(352, 288, 608, 464);
        Ghost g3 = new Ghost(look); g3.placeAt(100, 100); g3.setRoamBounds( 16,  16, 608, 224);
        Ghost g4 = new Ghost(look); g4.placeAt(480, 150); g4.setRoamBounds( 16,  16, 608, 224);
        return List.of(g1, g2, g3, g4);
    }

    private ArrayList<Spider> buildSpiders() {
        ArrayList<Spider> list = new ArrayList<>();
        list.add(new Spider(150, 300));
        list.add(new Spider(500, 300));
        list.add(new Spider(200,  50));
        list.add(new Spider(420,  50));
        return list;
    }

    private void checkGhostInteractions() {
        int level = gsm.getCurrentLevel();
        heroHitbox.set(hero.getX(), hero.getY(),
                       Hero.SPRITE_WIDTH * 2f, Hero.SPRITE_HEIGHT * 2f);

        for (Ghost g : ghosts) {
            if (!g.isActive()) continue;

            if (level == 1) {
                float hcx = hero.getX() + Hero.SPRITE_WIDTH;
                float hcy = hero.getY() + Hero.SPRITE_HEIGHT;
                float gcx = g.getX()    + g.getAppearance().getWidth()  / 2f;
                float gcy = g.getY()    + g.getAppearance().getHeight() / 2f;
                float dx  = hcx - gcx;
                float dy  = hcy - gcy;
                if (dx * dx + dy * dy < 48f * 48f) vignetteActive = true;

            } else if (level == 2) {
                if (!hero.isFrozen() && heroHitbox.overlaps(g.getCollisionRect())) {
                    EventBus.getInstance().publish(new EventBus.FreezeEvent(5f));
                    g.deactivate(5f);
                }

            } else if (level == 3) {
                if (ghostHitCooldown <= 0f && heroHitbox.overlaps(g.getCollisionRect())) {
                    gsm.ghostHit();
                    g.deactivate(3f);
                    ghostHitCooldown   = 2f;
                    ghostDmgFlashTimer = GHOST_DMG_FLASH;
                }
            }
        }
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

