package com.example.clownmaze.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public final class CameraController {

    /** Virtual resolution the game was designed for. GL stretches this to the OS window. */
    public static final float VIRTUAL_W = 640f;
    public static final float VIRTUAL_H = 480f;

    private static final float LERP = 5f;

    private final OrthographicCamera camera;
    private float   minX, minY, maxX, maxY;
    private boolean boundsSet;

    public CameraController(float viewportW, float viewportH) {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIRTUAL_W, VIRTUAL_H);
    }

    public void setBounds(float worldMinX, float worldMinY, float worldMaxX, float worldMaxY) {
        float halfW = camera.viewportWidth  / 2f;
        float halfH = camera.viewportHeight / 2f;
        minX = worldMinX + halfW;
        minY = worldMinY + halfH;
        maxX = worldMaxX - halfW;
        maxY = worldMaxY - halfH;
        if (maxX < minX) maxX = minX;
        if (maxY < minY) maxY = minY;
        boundsSet = true;
    }

    public void clearBounds() {
        boundsSet = false;
    }

    public void update(float heroX, float heroY, float dt) {
        float targetX = boundsSet ? MathUtils.clamp(heroX, minX, maxX) : heroX;
        float targetY = boundsSet ? MathUtils.clamp(heroY, minY, maxY) : heroY;
        camera.position.x += (targetX - camera.position.x) * LERP * dt;
        camera.position.y += (targetY - camera.position.y) * LERP * dt;
        camera.update();
    }

    public void snapTo(float worldX, float worldY) {
        float x = boundsSet ? MathUtils.clamp(worldX, minX, maxX) : worldX;
        float y = boundsSet ? MathUtils.clamp(worldY, minY, maxY) : worldY;
        camera.position.set(x, y, 0f);
        camera.update();
    }

    public void resize(float viewportW, float viewportH) {
        // Virtual resolution stays fixed; the GL viewport (set by the backend) stretches it.
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
