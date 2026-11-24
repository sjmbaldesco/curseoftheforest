package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.AssetLoader;
import com.cupids.curseoftheforest.ForestGame;

/**
 * Stacked scenery layers that scroll at different speeds, which makes the forest look deep.
 * Far trees move the least, the near grass a bit less than the ground, and an optional foreground
 * layer (leaves in front of Dayo) moves faster than the ground. The speeds are fractions of the
 * ground's speed. Each layer is an image that repeats sideways; a missing image is skipped.
 *
 * <p>Far and mid are drawn behind the map, near between the map and the characters, and the
 * foreground on top of the characters, so stages only call {@link #renderBack},
 * {@link #renderFront} and {@link #renderForeground}.
 */
public class ParallaxBackground {
    static final float FAR_SPEED = 0.1f;
    static final float MID_SPEED = 0.35f;
    static final float NEAR_SPEED = 0.65f;
    /** Above 1: the foreground passes faster than the ground, as if it were nearer the viewer. */
    static final float FOREGROUND_SPEED = 1.3f;
    /** Layers move up and down less than sideways, so the horizon does not bob when Dayo jumps. */
    private static final float VERTICAL_SHARE = 0.5f;

    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    private final Texture far;
    private final Texture mid;
    private final Texture near;
    private final Texture foreground;

    /** @param folder asset folder holding far.png, mid.png, near.png and optionally front.png, e.g. "images/parallax/stage3-gubat" */
    public ParallaxBackground(AssetLoader assets, String folder) {
        far = load(assets, folder + "/far.png");
        mid = load(assets, folder + "/mid.png");
        near = load(assets, folder + "/near.png");
        foreground = load(assets, folder + "/front.png");
    }

    /** Picks the art set for a stage from the "Stage N: ..." name; null if the name has no number. */
    public static ParallaxBackground forStage(AssetLoader assets, String stageName) {
        String folder = switch (stageName.length() > 7 ? stageName.substring(0, 8) : "") {
            case "Stage 1:" -> "stage1-simula";
            case "Stage 2:" -> "stage2-palengke";
            case "Stage 3:" -> "stage3-gubat";
            case "Stage 4:" -> "stage4-tulay";
            case "Stage 5:" -> "stage5-mangkukulam";
            default -> null;
        };
        return folder == null ? null : new ParallaxBackground(assets, "images/parallax/" + folder);
    }

    private static Texture load(AssetLoader assets, String path) {
        if (!Gdx.files.internal(path).exists()) {
            return null;
        }
        Texture texture = assets.loadImage(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        return texture;
    }

    /** Draws the far and mid layers; call before the tile map. The batch must not be started. */
    public void renderBack(SpriteBatch batch, OrthographicCamera camera) {
        draw(batch, camera, far, FAR_SPEED);
        draw(batch, camera, mid, MID_SPEED);
    }

    /** Draws the near layer; call after the tile map and before the player. */
    public void renderFront(SpriteBatch batch, OrthographicCamera camera) {
        draw(batch, camera, near, NEAR_SPEED);
    }

    /** Draws the foreground layer over the characters; call after the player, before the HUD. */
    public void renderForeground(SpriteBatch batch, OrthographicCamera camera) {
        draw(batch, camera, foreground, FOREGROUND_SPEED);
    }

    /**
     * How far a layer has slid, in window pixels, when the camera's edge is at {@code cameraEdge}
     * world pixels. {@code pixelsPerWorldUnit} is the camera zoom (2 when the world is drawn twice
     * as big), so a layer with speed 1 moves exactly with the ground.
     */
    static float offset(float cameraEdge, float speed, float pixelsPerWorldUnit) {
        return cameraEdge * speed * pixelsPerWorldUnit;
    }

    private void draw(SpriteBatch batch, OrthographicCamera camera, Texture layer, float speed) {
        if (layer == null) {
            return;
        }
        float pixelsPerWorldUnit = ForestGame.WORLD_WIDTH / camera.viewportWidth;
        float left = camera.position.x - camera.viewportWidth / 2;
        float bottom = camera.position.y - camera.viewportHeight / 2;
        float u = offset(left, speed, pixelsPerWorldUnit) / layer.getWidth();
        float uWidth = (float) ForestGame.WORLD_WIDTH / layer.getWidth();
        // When the camera rises, the layer sinks a little (less than the ground does).
        float y = -offset(bottom, speed * VERTICAL_SHARE, pixelsPerWorldUnit);
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
        // Screen space: the layer is window-sized and only its texture window slides sideways.
        batch.draw(layer, 0, y, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT, u, 1f, u + uWidth, 0f);
        batch.end();
    }
}
