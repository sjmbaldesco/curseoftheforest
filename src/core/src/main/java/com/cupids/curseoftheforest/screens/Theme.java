package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Shared look for menus, HUD and cutscenes: the UI spec's palette (design/ui-revamp/UI-SPEC.md)
 * and VCR OSD Mono at five sizes. The wooden frames themselves are drawn by {@link UiSkin}.
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

    /** Secondary text (descriptions) and dimmed text (owned items), from the UI spec. */
    public static final Color MUTED = Color.valueOf("B9C7BD");
    public static final Color DIM = Color.valueOf("7E8F84");
    /** Owned / success, and the selected-button text. */
    public static final Color SUCCESS = Color.valueOf("52B07C");
    public static final Color GOLD_LIGHT = Color.valueOf("F7D977");
    /** Fill inside the wooden frames. */
    public static final Color PANEL = Color.valueOf("14241B");

    /**
     * The text sizes in the UI spec, in window pixels. Each is its own VCR OSD Mono bitmap font
     * (tools/make_bitmap_font.py) drawn at scale 1, with hard pixel edges.
     */
    public enum Size {
        /** Hints and descriptions. */
        SMALL(14),
        /** HUD and labels; the game's default font. */
        BODY(16),
        /** Dialogue and buttons (30 px between lines). */
        TALK(20),
        /** Titles. */
        HEADING(28),
        /** "Game Over" and "The End". */
        HUGE(56);

        public final int pixels;

        Size(int pixels) {
            this.pixels = pixels;
        }

        String path() {
            return "fonts/vcr-" + pixels + ".fnt";
        }
    }

    private static final Map<Size, BitmapFont> FONTS = new EnumMap<>(Size.class);
    /** Each loaded font's normal scale, so {@link #setScale} can scale relative to it. */
    private static final Map<BitmapFont, Float> BASE_SCALES = new IdentityHashMap<>();
    private static final GlyphLayout LAYOUT = new GlyphLayout();
    private static Texture pixel;

    private Theme() {
    }

    /** The font for a size, loaded the first time it is asked for. */
    public static BitmapFont font(Size size) {
        return FONTS.computeIfAbsent(size, s -> load(s.path()));
    }

    /** The body font (16 px). */
    public static BitmapFont loadFont() {
        return font(Size.BODY);
    }

    /** The heading font (28 px). */
    public static BitmapFont loadTitleFont() {
        return font(Size.HEADING);
    }

    /** Falls back to LibGDX's default font if the file is missing, so the game never crashes. */
    private static BitmapFont load(String path) {
        BitmapFont font;
        try {
            font = new BitmapFont(Gdx.files.internal(path));
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        } catch (RuntimeException e) {
            Gdx.app.error("Theme", "Font missing, using default: " + e.getMessage());
            font = new BitmapFont();
        }
        font.setUseIntegerPositions(true);
        BASE_SCALES.put(font, 1f);
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

    /** How wide a line of text is in a font, in window pixels. */
    public static float width(BitmapFont font, String text) {
        LAYOUT.setText(font, text);
        return LAYOUT.width;
    }

    /**
     * Draws text with the spec's 2 px night shadow down and to the right; (x, y) is the top-left
     * of the text. The batch must be started.
     */
    public static void text(SpriteBatch batch, Size size, String text, float x, float y, Color color) {
        BitmapFont font = font(size);
        font.setColor(NIGHT);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    /** Like {@link #text} but centred on {@code centreX}. */
    public static void textCentred(SpriteBatch batch, Size size, String text, float centreX, float y, Color color) {
        text(batch, size, text, Math.round(centreX - width(font(size), text) / 2), y, color);
    }

    /** Like {@link #text} but with its right edge at {@code rightX}. */
    public static void textRight(SpriteBatch batch, Size size, String text, float rightX, float y, Color color) {
        text(batch, size, text, Math.round(rightX - width(font(size), text)), y, color);
    }

    /** Title text with the spec's 2 px night outline all round. */
    public static void title(SpriteBatch batch, Size size, String text, float centreX, float y, Color color) {
        BitmapFont font = font(size);
        float x = Math.round(centreX - width(font, text) / 2);
        font.setColor(NIGHT);
        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dy = -2; dy <= 2; dy += 2) {
                if (dx != 0 || dy != 0) {
                    font.draw(batch, text, x + dx, y + dy);
                }
            }
        }
        font.setColor(color);
        font.draw(batch, text, x, y);
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
        for (BitmapFont font : FONTS.values()) {
            BASE_SCALES.remove(font);
            font.dispose();
        }
        FONTS.clear();
        UiSkin.dispose();
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
    }
}
