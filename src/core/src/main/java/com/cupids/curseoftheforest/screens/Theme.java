package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Shared look for menus, HUD and cutscenes: one palette, two fonts (VCR OSD Mono for reading,
 * Pixelify Sans for titles), and flat-colour panels.
 * The palette comes from Lola Pacita's speech bubble (cream paper, dark green ink).
 */
public final class Theme {
    public static final Color NIGHT = Color.valueOf("0B1A12");
    public static final Color INK = Color.valueOf("23382E");
    public static final Color PAPER = Color.valueOf("FFF3D8");
    public static final Color GOLD = Color.valueOf("E8B83A");
    public static final Color COURAGE = Color.valueOf("C83E46");
    public static final Color SANITY = Color.valueOf("396EB8");
    public static final Color LEAF = Color.valueOf("318255");
    public static final Color WARNING = Color.valueOf("FF8A6B");
    public static final Color SHADE = new Color(0f, 0f, 0f, 0.55f);

    /** Body text. Its atlas is rendered at 2x (tools/make_bitmap_font.py) so it stays sharp full screen. */
    private static final String FONT_PATH = "fonts/vcr.fnt";
    private static final float BODY_FONT_SCALE = 0.5f;
    /** Titles: the chunky pixel font, drawn large. */
    private static final String TITLE_FONT_PATH = "fonts/pixelify.fnt";
    /** Each loaded font's normal scale, so {@link #setScale} can scale relative to it. */
    private static final Map<BitmapFont, Float> BASE_SCALES = new IdentityHashMap<>();
    private static Texture pixel;

    private Theme() {
    }

    /** The body font (VCR OSD Mono), about 16 px tall in the 960 by 540 window. */
    public static BitmapFont loadFont() {
        return load(FONT_PATH, BODY_FONT_SCALE, Texture.TextureFilter.Linear, false);
    }

    /** The title font (Pixelify Sans), for big headings such as the game's name and "Game Over". */
    public static BitmapFont loadTitleFont() {
        return load(TITLE_FONT_PATH, 1f, Texture.TextureFilter.Nearest, true);
    }

    /** Falls back to LibGDX's default font if the file is missing, so the game never crashes. */
    private static BitmapFont load(String path, float scale, Texture.TextureFilter filter, boolean integerPositions) {
        BitmapFont font;
        try {
            font = new BitmapFont(Gdx.files.internal(path));
            font.getRegion().getTexture().setFilter(filter, filter);
            font.getData().setScale(scale);
        } catch (RuntimeException e) {
            Gdx.app.error("Theme", "Font missing, using default: " + e.getMessage());
            font = new BitmapFont();
            scale = 1f;
        }
        font.setUseIntegerPositions(integerPositions);
        BASE_SCALES.put(font, scale);
        return font;
    }

    /** Sets a font's size relative to its normal size: 1 is normal, 2 is twice as big. */
    public static void setScale(BitmapFont font, float scale) {
        font.getData().setScale(scale * BASE_SCALES.getOrDefault(font, 1f));
    }

    public static void setScale(BitmapFont font, float scaleX, float scaleY) {
        float base = BASE_SCALES.getOrDefault(font, 1f);
        font.getData().setScale(scaleX * base, scaleY * base);
    }

    /** Draws a solid rectangle; the batch must already be started. */
    public static void rect(SpriteBatch batch, Color color, float x, float y, float w, float h) {
        if (pixel == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            pixel = new Texture(pm);
            pm.dispose();
        }
        Color previous = new Color(batch.getColor());
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(previous);
    }

    /** Cream paper panel with a dark ink border, like Lola Pacita's speech bubble. */
    public static void panel(SpriteBatch batch, float x, float y, float w, float h) {
        rect(batch, INK, x - 3, y - 3, w + 6, h + 6);
        rect(batch, PAPER, x, y, w, h);
    }

    /** Dark translucent panel for text that sits over a painted background. */
    public static void shade(SpriteBatch batch, float x, float y, float w, float h) {
        rect(batch, SHADE, x, y, w, h);
    }

    /** Loads a full-screen painting for a menu; returns null if the file is missing so the screen still draws. */
    public static Texture loadBackdrop(com.cupids.curseoftheforest.AssetLoader assets, String path) {
        return Gdx.files.internal(path).exists() ? assets.loadImage(path) : null;
    }

    /** Draws the painting over the whole screen, then a light shade so text stays readable. */
    public static void drawBackdrop(SpriteBatch batch, Texture backdrop) {
        if (backdrop == null) {
            return;
        }
        batch.draw(backdrop, 0, 0, 960, 540);
        rect(batch, new Color(0f, 0f, 0f, 0.25f), 0, 0, 960, 540);
    }

    public static void dispose() {
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
    }
}
