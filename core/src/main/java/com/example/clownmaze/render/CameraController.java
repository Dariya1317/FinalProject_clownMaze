package com.example.clownmaze.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class CameraController {

    private static final float LERP = 5f;

    private final OrthographicCamera camera;
    private float   minX, minY, maxX, maxY;
    private boolean boundsSet;

    public CameraController(float viewportW, float viewportH) {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportW, viewportH);
    }

    public void setBounds(float worldMinX, float worldMinY, float worldMaxX, float worldMaxY) {
        minX = worldMinX + camera.viewportWidth  / 2f;
        minY = worldMinY + camera.viewportHeight / 2f;
        maxX = worldMaxX - camera.viewportWidth  / 2f;
        maxY = worldMaxY - camera.viewportHeight / 2f;
        boundsSet = true;
    }

    public void update(float heroX, float heroY, float dt) {
        float targetX = boundsSet ? MathUtils.clamp(heroX, minX, maxX) : heroX;
        float targetY = boundsSet ? MathUtils.clamp(heroY, minY, maxY) : heroY;
        camera.position.x += (targetX - camera.position.x) * LERP * dt;
        camera.position.y += (targetY - camera.position.y) * LERP * dt;
        camera.update();
    }

    public void snapTo(float x, float y) {
        camera.position.set(x, y, 0);
        camera.update();
    }

    public void resize(float viewportW, float viewportH) {
        camera.setToOrtho(false, viewportW, viewportH);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
