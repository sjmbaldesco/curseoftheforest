package com.cupids.curseoftheforest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads images through LibGDX's AssetManager. A missing or broken file is logged and replaced
 * by a magenta placeholder so the game keeps running.
 * TODO(Phebe): owner of this class; minimal version so the baseline runs.
 */
public class AssetLoader implements Disposable {
    private final AssetManager assetManager = new AssetManager();
    private Texture missingTexture;

    public Texture loadImage(String path) {
        if (assetManager.isLoaded(path, Texture.class)) {
            return assetManager.get(path, Texture.class);
        }
        try {
            if (!Gdx.files.internal(path).exists()) {
                throw new GdxRuntimeException("File not found: " + path);
            }
            assetManager.load(path, Texture.class);
            assetManager.finishLoadingAsset(path);
            return assetManager.get(path, Texture.class);
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AssetLoader", "Could not load " + path + ", using a placeholder.", e);
            return getMissingTexture();
        }
    }

    private Texture getMissingTexture() {
        if (missingTexture == null) {
            Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.MAGENTA);
            pixmap.fill();
            missingTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return missingTexture;
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        if (missingTexture != null) {
            missingTexture.dispose();
        }
    }
}
