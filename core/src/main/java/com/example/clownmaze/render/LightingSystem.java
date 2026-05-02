package com.example.clownmaze.render;

import com.badlogic.gdx.Gdx;
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

public final class LightingSystem implements Disposable {

    public static final float TILE_PX        = 16f;
    public static final float TORCH_RADIUS   = TILE_PX * 12f;

    private static final float DARKNESS         = 0.75f;
    private static final float TORCH_R          = 1.0f;
    private static final float TORCH_G          = 0.85f;
    private static final float TORCH_B          = 0.45f;
    private static final int   FALLOFF_RINGS    = 16;

    private FrameBuffer fbo;
    private final ShapeRenderer shapes;
    private final SpriteBatch overlay;
    private final Matrix4 proj = new Matrix4();
    private final Vector3 tmp  = new Vector3();

    public LightingSystem(int screenW, int screenH) {
        fbo     = new FrameBuffer(Pixmap.Format.RGBA8888, screenW, screenH, false);
        shapes  = new ShapeRenderer();
        overlay = new SpriteBatch();
        proj.setToOrtho2D(0, 0, screenW, screenH);
    }

    public void render(float heroWorldX, float heroWorldY, OrthographicCamera worldCam) {
        tmp.set(heroWorldX, heroWorldY, 0f);
        worldCam.project(tmp);

        int w = fbo.getWidth();
        int h = fbo.getHeight();

        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapes.setProjectionMatrix(proj);

        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, DARKNESS);
        shapes.rect(0, 0, w, h);
        shapes.end();

        Gdx.gl.glBlendFuncSeparate(GL20.GL_ZERO, GL20.GL_ONE, GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = FALLOFF_RINGS; i >= 1; i--) {
            float t      = i / (float) FALLOFF_RINGS;
            float radius = TORCH_RADIUS * t;
            float alpha  = 1f - t;
            shapes.setColor(TORCH_R, TORCH_G, TORCH_B, alpha);
            shapes.circle(tmp.x, tmp.y, radius, 48);
        }
        shapes.end();

        fbo.end();

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Texture tex = fbo.getColorBufferTexture();
        overlay.setProjectionMatrix(proj);
        overlay.begin();
        overlay.draw(tex, 0, 0, w, h, 0, 0, w, h, false, true);
        overlay.end();
    }

    public void resize(int screenW, int screenH) {
        if (fbo != null) fbo.dispose();
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
