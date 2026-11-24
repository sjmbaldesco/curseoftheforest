package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.AssetLoader;
import com.cupids.curseoftheforest.ForestGame;

/**
 * Three stacked scenery layers that scroll slower than the camera, which makes the forest look deep.
 * Far trees move the least and the near grass moves almost with the player. Each layer is an image
 * that repeats sideways; if an image is missing, that layer is simply skipped.
 *
 * <p>The far and mid layers are drawn behind the map and the near layer in front of it, so the
 * stage classes only call {@link #renderBack} and {@link #renderFront}.
 */
public class ParallaxBackground {
    private static final float FAR_SPEED = 0.2f;
    private static final float MID_SPEED = 0.5f;
    private static final float NEAR_SPEED = 0.9f;

    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    private final Texture far;
    private final Texture mid;
    private final Texture near;

    /** @param folder asset folder holding far.png, mid.png and near.png, e.g. "images/parallax/stage3-gubat" */
    public ParallaxBackground(AssetLoader assets, String folder) {
        far = load(assets, folder + "/far.png");
        mid = load(assets, folder + "/mid.png");
        near = load(assets, folder + "/near.png");
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

    private void draw(SpriteBatch batch, OrthographicCamera camera, Texture layer, float speed) {
        if (layer == null) {
            return;
        }
        float left = camera.position.x - camera.viewportWidth / 2;
        float scroll = left * speed;
        float u = scroll / layer.getWidth();
        float uWidth = (float) ForestGame.WORLD_WIDTH / layer.getWidth();
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
        // Screen space: the layer fills the window and only its texture window slides.
        batch.draw(layer, 0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT, u, 1f, u + uWidth, 0f);
        batch.end();
    }
}
