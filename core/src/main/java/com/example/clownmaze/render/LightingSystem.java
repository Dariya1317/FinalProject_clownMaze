package com.example.clownmaze.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class LightingSystem implements Disposable {

    private static final float TILE_PX = 16f;
    private static final float TORCH_RADIUS = TILE_PX * 6f;
    private static final float AMBIENT = 0.93f;

    private FrameBuffer fbo;
    private final ShapeRenderer shapes;
    private final SpriteBatch overlay;
    private final Matrix4 proj = new Matrix4();
    private final Vector3 tmpVec = new Vector3();

    public LightingSystem(int screenW, int screenH) {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, screenW, screenH, false);
        shapes = new ShapeRenderer();
        overlay = new SpriteBatch();
        proj.setToOrtho2D(0, 0, screenW, screenH);
    }

    public void render(float heroWorldX, float heroWorldY, OrthographicCamera camera) {
        tmpVec.set(heroWorldX, heroWorldY, 0f);
        camera.project(tmpVec);

        int w = fbo.getWidth();
        int h = fbo.getHeight();

        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapes.setProjectionMatrix(proj);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
        shapes.setColor(0f, 0f, 0f, AMBIENT);
        shapes.rect(0, 0, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_ZERO, GL20.GL_ONE, GL20.GL_ZERO, GL20.GL_ZERO);
        shapes.setColor(Color.CLEAR);
        shapes.circle(tmpVec.x, tmpVec.y, TORCH_RADIUS, 64);
        shapes.end();

        fbo.end();

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Texture tex = fbo.getColorBufferTexture();
        overlay.setProjectionMatrix(proj);
        overlay.begin();
        overlay.draw(tex, 0, 0, w, h);
        overlay.end();
    }

    public void resize(int screenW, int screenH) {
        fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, screenW, screenH, false);
        proj.setToOrtho2D(0, 0, screenW, screenH);
    }

    @Override
    public void dispose() {
        fbo.dispose();
        shapes.dispose();
        overlay.dispose();
    }
}
