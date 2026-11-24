package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * The carved-wood UI pieces from Jai's mockups (images/ui/, made at 1x art scale). Every piece is
 * drawn twice as big with crisp pixels, so 1 art pixel is 2 window pixels, like the rest of the game.
 *
 * <p>Frames are nine-slice: the corners keep their size and the edges and middle stretch, so one
 * small image makes a box of any size. If an image is missing, a plain coloured box is drawn
 * instead, so the game never shows the magenta placeholder in its UI.
 */
public final class UiSkin {
    static final String FOLDER = "images/ui/";
    /** Window pixels per art pixel. */
    public static final int SCALE = 2;

    /** A nine-slice frame and the size of its corners in art pixels (all four the same). */
    public enum Frame {
        PANEL("panel", 8),
        TALKBOX("talkbox", 8),
        PORTRAIT("portrait-frame", 8),
        NAMETAG("nametag", 6),
        BAR("bar-frame", 3),
        KEYCAP("keycap", 4),
        TOAST("toast", 5),
        TOAST_WARNING("toast-warning", 5),
        BUTTON("button", 6),
        BUTTON_SELECTED("button-selected", 6),
        SLOT("slot", 6),
        SLOT_SELECTED("slot-selected", 6);

        final String file;
        final int corner;

        Frame(String file, int corner) {
            this.file = file;
            this.corner = corner;
        }
    }

    private static final Map<Frame, NinePatch> FRAMES = new EnumMap<>(Frame.class);
    private static final Map<String, Texture> IMAGES = new HashMap<>();
    /** Files already found missing, so the disk is not checked every frame. */
    private static final Map<String, Boolean> MISSING = new HashMap<>();

    private UiSkin() {
    }

    /** Draws a frame with its bottom-left at (x, y) in window pixels. The batch must be started. */
    public static void frame(SpriteBatch batch, Frame frame, float x, float y, float w, float h) {
        NinePatch patch = FRAMES.get(frame);
        if (patch == null && !FRAMES.containsKey(frame)) {
            Texture texture = image(frame.file);
            patch = texture == null ? null : new NinePatch(texture, frame.corner, frame.corner, frame.corner, frame.corner);
            if (patch != null) {
                patch.scale(SCALE, SCALE);
            }
            FRAMES.put(frame, patch);
        }
        if (patch != null) {
            patch.draw(batch, x, y, w, h);
        } else {
            Theme.rect(batch, Theme.GOLD, x, y, w, h);
            Theme.rect(batch, Theme.PANEL, x + 2, y + 2, w - 4, h - 4);
        }
    }

    /**
     * Draws a plain image (icon, bar fill, chest...) with its bottom-left at (x, y), stretched to
     * w by h. Returns false, drawing nothing, if the image is missing.
     */
    public static boolean image(SpriteBatch batch, String name, float x, float y, float w, float h) {
        Texture texture = image(name);
        if (texture == null) {
            return false;
        }
        batch.draw(texture, x, y, w, h);
        return true;
    }

    /** Draws an image at twice its own size. */
    public static boolean image(SpriteBatch batch, String name, float x, float y) {
        Texture texture = image(name);
        if (texture == null) {
            return false;
        }
        batch.draw(texture, x, y, texture.getWidth() * SCALE, texture.getHeight() * SCALE);
        return true;
    }

    /** Draws an image at twice its size, mirrored left to right (the right-hand menu pointer). */
    public static void imageFlipped(SpriteBatch batch, String name, float x, float y) {
        Texture texture = image(name);
        if (texture != null) {
            batch.draw(texture, x, y, texture.getWidth() * SCALE, texture.getHeight() * SCALE,
                    0, 0, texture.getWidth(), texture.getHeight(), true, false);
        }
    }

    /** One part of an image, in art pixels from its top-left (for the HP pips' full and empty halves). */
    public static TextureRegion region(String name, int x, int y, int w, int h) {
        Texture texture = image(name);
        return texture == null ? null : new TextureRegion(texture, x, y, w, h);
    }

    /** The image's width in window pixels (twice its art width), or 0 if it is missing. */
    public static int width(String name) {
        Texture texture = image(name);
        return texture == null ? 0 : texture.getWidth() * SCALE;
    }

    private static Texture image(String name) {
        if (MISSING.containsKey(name)) {
            return null;
        }
        Texture texture = IMAGES.get(name);
        if (texture == null) {
            String path = FOLDER + name + ".png";
            if (!Gdx.files.internal(path).exists()) {
                MISSING.put(name, true);
                return null;
            }
            texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            IMAGES.put(name, texture);
        }
        return texture;
    }

    /** A small downward triangle (the "continue" mark; the font has no ▼). Top-left at (x, y). */
    public static void triangle(SpriteBatch batch, Color color, float x, float y) {
        for (int row = 0; row < 3; row++) {
            Theme.rect(batch, color, x + row * 2, y - (row + 1) * 2, 12 - row * 4, 2);
        }
    }

    public static void dispose() {
        for (Texture texture : IMAGES.values()) {
            texture.dispose();
        }
        IMAGES.clear();
        FRAMES.clear();
        MISSING.clear();
    }
}
