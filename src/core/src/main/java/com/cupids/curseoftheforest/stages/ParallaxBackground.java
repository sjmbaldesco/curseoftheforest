package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.AssetLoader;
import com.cupids.curseoftheforest.ForestGame;

/**
 * Stacked scenery layers that scroll at different speeds, which makes the forest look deep.
 * Far trees move the least, the near grass a bit less than the ground, and an optional foreground
 * layer (leaves in front of Dayo) moves faster than the ground. The speeds are fractions of the
 * ground's speed. Each layer is an image that repeats sideways; a missing image is skipped.
 *
 * <p>Each art set is a folder name such as "stage3-gubat". The richer Blender layers in
 * {@code images/parallax-v2/} are used first; any layer missing there falls back to the older
 * {@code images/parallax/} art. A set may also have a fog band ({@code images/animated/fog/}) that
 * drifts slowly over the near layer.
 *
 * <p>Far and mid are drawn behind the map, near and fog between the map and the characters, and
 * the foreground on top of the characters, so stages only call {@link #renderBack},
 * {@link #renderFront} and {@link #renderForeground}.
 */
public class ParallaxBackground {
    static final String V2_FOLDER = "images/parallax-v2/";
    static final String V1_FOLDER = "images/parallax/";
    static final String FOG_FOLDER = "images/animated/fog/";
    static final float FAR_SPEED = 0.1f;
    static final float MID_SPEED = 0.35f;
    static final float NEAR_SPEED = 0.65f;
    /** Above 1: the foreground passes faster than the ground, as if it were nearer the viewer. */
    static final float FOREGROUND_SPEED = 1.3f;
    /** Layers move up and down less than sideways, so the horizon does not bob when Dayo jumps. */
    private static final float VERTICAL_SHARE = 0.5f;
    /** The fog drifts on its own at this many window pixels a second, on top of the scrolling. */
    static final float FOG_DRIFT = 8f;
    static final float FOG_ALPHA = 0.7f;

    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    private final Texture far;
    private final Texture mid;
    private final Texture near;
    private final Texture foreground;
    private final Texture fog;
    /** Seconds since the background was first drawn, for the sway and the fog drift. */
    private float time;
    /** Another art set shown right of {@link #switchX} (Stage 2's Wakwak Cave), or null. */
    private ParallaxBackground beyond;
    private float switchX;

    /** @param set art set folder name, e.g. "stage3-gubat" */
    public ParallaxBackground(AssetLoader assets, String set) {
        far = layer(assets, set, "far.png");
        mid = layer(assets, set, "mid.png");
        near = layer(assets, set, "near.png");
        foreground = layer(assets, set, "front.png");
        fog = load(assets, FOG_FOLDER + "fog_" + set + ".png");
    }

    /** Picks the art set for a stage from the "Stage N: ..." name; null if the name has no number. */
    public static ParallaxBackground forStage(AssetLoader assets, String stageName) {
        String set = setFor(stageName);
        return set == null ? null : new ParallaxBackground(assets, set);
    }

    /** The art set folder for a "Stage N: ..." name, or null. */
    static String setFor(String stageName) {
        return switch (stageName != null && stageName.length() > 7 ? stageName.substring(0, 8) : "") {
            case "Stage 1:" -> "stage1-simula";
            case "Stage 2:" -> "stage2-palengke";
            case "Stage 3:" -> "stage3-gubat";
            case "Stage 4:" -> "stage4-tulay";
            case "Stage 5:" -> "stage5-mangkukulam";
            default -> null;
        };
    }

    /** The path a layer loads from: the v2 art if it exists, else the older art. */
    static String layerPath(String set, String file, java.util.function.Predicate<String> exists) {
        String v2 = V2_FOLDER + set + "/" + file;
        return exists.test(v2) ? v2 : V1_FOLDER + set + "/" + file;
    }

    private static Texture layer(AssetLoader assets, String set, String file) {
        return load(assets, layerPath(set, file, path -> Gdx.files.internal(path).exists()));
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

    /** True if this set has its own fog band (then the code-drawn mist is not needed). */
    public boolean hasFog() {
        return fog != null;
    }

    /**
     * From world x {@code worldX} rightwards, shows {@code other}'s layers instead of these, so a
     * stage can walk from a forest into a cave. The seam scrolls with the ground.
     */
    public void switchAt(float worldX, ParallaxBackground other) {
        this.switchX = worldX;
        this.beyond = other;
    }

    /** Draws the far and mid layers; call before the tile map. The batch must not be started. */
    public void renderBack(SpriteBatch batch, OrthographicCamera camera) {
        time = (time + Math.min(Gdx.graphics.getDeltaTime(), 0.1f)) % 10_000f;
        begin(batch);
        drawLayer(batch, camera, Layer.FAR);
        drawLayer(batch, camera, Layer.MID);
        batch.end();
    }

    /** Draws the near layer and the fog; call after the tile map and before the player. */
    public void renderFront(SpriteBatch batch, OrthographicCamera camera) {
        begin(batch);
        drawLayer(batch, camera, Layer.NEAR);
        drawLayer(batch, camera, Layer.FOG);
        batch.end();
    }

    /** Draws the foreground layer over the characters; call after the player, before the HUD. */
    public void renderForeground(SpriteBatch batch, OrthographicCamera camera) {
        begin(batch);
        drawLayer(batch, camera, Layer.FOREGROUND);
        batch.end();
    }

    private static void begin(SpriteBatch batch) {
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
    }

    private enum Layer { FAR, MID, NEAR, FOG, FOREGROUND }

    /** Draws one layer of this set, and of the set beyond the switch point if there is one. */
    private void drawLayer(SpriteBatch batch, OrthographicCamera camera, Layer which) {
        float seam = ForestGame.WORLD_WIDTH;
        if (beyond != null) {
            float pixelsPerWorldUnit = ForestGame.WORLD_WIDTH / camera.viewportWidth;
            float left = camera.position.x - camera.viewportWidth / 2;
            seam = MathUtils.clamp(Math.round((switchX - left) * pixelsPerWorldUnit), 0, ForestGame.WORLD_WIDTH);
            beyond.time = time;
            beyond.drawPart(batch, camera, which, seam, ForestGame.WORLD_WIDTH);
        }
        drawPart(batch, camera, which, 0, seam);
    }

    private void drawPart(SpriteBatch batch, OrthographicCamera camera, Layer which, float fromX, float toX) {
        switch (which) {
            case FAR -> draw(batch, camera, far, FAR_SPEED, 0f, fromX, toX);
            case MID -> draw(batch, camera, mid, MID_SPEED, 0f, fromX, toX);
            // Near foliage breathes a little in the wind.
            case NEAR -> draw(batch, camera, near, NEAR_SPEED, sway(time, 1.5f, 0.6f), fromX, toX);
            case FOG -> {
                batch.setColor(1f, 1f, 1f, FOG_ALPHA);
                draw(batch, camera, fog, NEAR_SPEED, time * FOG_DRIFT, fromX, toX);
                batch.setColor(Color.WHITE);
            }
            case FOREGROUND -> draw(batch, camera, foreground, FOREGROUND_SPEED, sway(time, 4f, 0.45f), fromX, toX);
        }
    }

    /** A slow, uneven side-to-side drift of up to {@code pixels} window pixels, like leaves in a breeze. */
    static float sway(float time, float pixels, float speed) {
        return pixels * (0.7f * MathUtils.sin(time * speed * MathUtils.PI2) + 0.3f * MathUtils.sin(time * speed * 2.3f * MathUtils.PI2 + 1f));
    }

    /**
     * How far a layer has slid, in window pixels, when the camera's edge is at {@code cameraEdge}
     * world pixels. {@code pixelsPerWorldUnit} is the camera zoom (2 when the world is drawn twice
     * as big), so a layer with speed 1 moves exactly with the ground.
     */
    static float offset(float cameraEdge, float speed, float pixelsPerWorldUnit) {
        return cameraEdge * speed * pixelsPerWorldUnit;
    }

    /** Draws the window strip from {@code fromX} to {@code toX} of a layer. The batch is started. */
    private void draw(SpriteBatch batch, OrthographicCamera camera, Texture layer, float speed, float shiftPixels,
            float fromX, float toX) {
        if (layer == null || toX <= fromX) {
            return;
        }
        float pixelsPerWorldUnit = ForestGame.WORLD_WIDTH / camera.viewportWidth;
        float left = camera.position.x - camera.viewportWidth / 2;
        float bottom = camera.position.y - camera.viewportHeight / 2;
        float u = (offset(left, speed, pixelsPerWorldUnit) + Math.round(shiftPixels) + fromX) / layer.getWidth();
        float uWidth = (toX - fromX) / layer.getWidth();
        // When the camera rises, the layer sinks a little (less than the ground does).
        float y = -offset(bottom, speed * VERTICAL_SHARE, pixelsPerWorldUnit);
        // Screen space: the layer keeps its own height (the fog is a low band) and only its texture
        // window slides sideways.
        batch.draw(layer, fromX, y, toX - fromX, layer.getHeight(), u, 1f, u + uWidth, 0f);
    }
}
