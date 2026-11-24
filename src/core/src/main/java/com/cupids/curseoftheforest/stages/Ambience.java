package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.cupids.curseoftheforest.ForestGame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Small moving details that make a stage feel alive: fireflies that drift and blink, leaves that
 * fall and sway, dust floating in the cave, and slow bands of mist. None of it touches the
 * gameplay; it is drawn over the background and slides with the camera so it feels part of the world.
 *
 * <p>Fireflies and leaves use the animated Blender sprites when the stage hands them over with
 * {@link #setSprites}; without them (tests, or a missing file) they fall back to shapes drawn in code.
 * Dust is always drawn in code.
 *
 * <p>Particles live in window coordinates (960 by 540) and wrap around the edges, so a stage of
 * any length needs only a few dozen. The motion is plain maths, testable without graphics; only
 * {@link #renderBack} and {@link #renderFront} need a batch.
 */
public class Ambience implements Disposable {
    /** What one stage shows. Counts of 0 switch an effect off. */
    public static final class Settings {
        final int fireflies;
        final int leaves;
        final int dust;
        /** Mist colour, or null for no mist. */
        final Color mist;
        final Color leafColor;
        /** Which falling-leaf sprite the stage uses: "green", "brown" or "violet". */
        final String leafSprite;

        Settings(int fireflies, int leaves, int dust, Color mist, Color leafColor, String leafSprite) {
            this.fireflies = fireflies;
            this.leaves = leaves;
            this.dust = dust;
            this.mist = mist;
            this.leafColor = leafColor;
            this.leafSprite = leafSprite;
        }

        /** The same effects without the code-drawn mist (used when the stage has a fog image instead). */
        public Settings withoutMist() {
            return new Settings(fireflies, leaves, dust, null, leafColor, leafSprite);
        }

        /** The leaf sprite sheet for these settings, or null when the stage has no leaves. */
        public String leafSheet() {
            return leaves == 0 || leafSprite == null ? null : "images/animated/leaf_" + leafSprite + "_tumble.png";
        }
    }

    public static final String FIREFLY_SHEET = "images/animated/firefly.png";
    /** Sprite frames are 16 px; drawn twice as big so they sit on the same pixel grid as the world. */
    private static final float SPRITE_SIZE = 32f;

    /** Particles slide this much slower than the ground, like a layer between near and the tiles. */
    static final float DEPTH = 0.8f;
    /** Particles may sit this far past the window edges before wrapping, so they never pop in. */
    static final float MARGIN = 40f;
    private static final Color FIREFLY = new Color(1f, 0.88f, 0.42f, 1f);
    private static final Color DUST = new Color(0.75f, 0.8f, 1f, 1f);

    /** One drifting speck: a firefly, a leaf or a mote of dust. */
    static final class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float phase;
        float size;
        float spin;
    }

    private final Settings settings;
    final List<Particle> fireflies = new ArrayList<>();
    final List<Particle> leaves = new ArrayList<>();
    final List<Particle> dust = new ArrayList<>();
    private float time;
    private Texture glow;
    private Texture pixel;
    private Texture mist;
    private Animation<TextureRegion> fireflySprite;
    private Animation<TextureRegion> leafSprite;
    private final Matrix4 screen = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
    private final Color tint = new Color();

    public Ambience(Settings settings, long seed) {
        this.settings = settings;
        Random random = new Random(seed);
        for (int i = 0; i < settings.fireflies; i++) {
            Particle p = spawn(random, 70, 470);
            p.vx = (random.nextFloat() - 0.5f) * 16f;
            p.vy = (random.nextFloat() - 0.5f) * 8f;
            p.size = 14f + random.nextFloat() * 10f;
            fireflies.add(p);
        }
        for (int i = 0; i < settings.leaves; i++) {
            Particle p = spawn(random, 0, ForestGame.WORLD_HEIGHT);
            p.vx = -6f - random.nextFloat() * 10f;
            p.vy = -(18f + random.nextFloat() * 22f);
            p.size = 4f + random.nextFloat() * 3f;
            p.spin = 60f + random.nextFloat() * 120f;
            leaves.add(p);
        }
        for (int i = 0; i < settings.dust; i++) {
            Particle p = spawn(random, 40, 500);
            p.vx = (random.nextFloat() - 0.5f) * 4f;
            p.vy = 2f + random.nextFloat() * 4f;
            p.size = 2f;
            dust.add(p);
        }
    }

    private static Particle spawn(Random random, float minY, float maxY) {
        Particle p = new Particle();
        p.x = -MARGIN + random.nextFloat() * (ForestGame.WORLD_WIDTH + 2 * MARGIN);
        p.y = minY + random.nextFloat() * (maxY - minY);
        p.phase = random.nextFloat() * MathUtils.PI2;
        return p;
    }

    /** The effects for each stage, picked from its "Stage N:" name; null for none. */
    public static Settings settingsFor(String stageName) {
        String prefix = stageName != null && stageName.length() >= 8 ? stageName.substring(0, 8) : "";
        return switch (prefix) {
            case "Stage 1:" -> new Settings(10, 6, 0, null, Color.valueOf("8A5A2B"), "brown");
            case "Stage 2:" -> new Settings(0, 0, 36, null, null, null);
            case "Stage 3:" -> new Settings(22, 12, 0, new Color(0.55f, 0.75f, 0.6f, 0.16f), Color.valueOf("5E8C3A"), "green");
            case "Stage 4:" -> new Settings(16, 0, 0, new Color(0.6f, 0.7f, 0.9f, 0.14f), null, null);
            case "Stage 5:" -> new Settings(5, 10, 0, new Color(0.55f, 0.35f, 0.7f, 0.2f), Color.valueOf("7A4E6E"), "violet");
            default -> null;
        };
    }

    public void update(float delta) {
        if (delta <= 0) {
            return;
        }
        time = (time + delta) % 10_000f;
        for (Particle p : fireflies) {
            // A lazy wander: steady drift plus a slow loop.
            p.x += (p.vx + MathUtils.sin(time * 0.7f + p.phase) * 10f) * delta;
            p.y += (p.vy + MathUtils.cos(time * 0.9f + p.phase) * 8f) * delta;
            p.y = MathUtils.clamp(p.y, 60f, 480f);
            p.x = wrap(p.x);
        }
        for (Particle p : leaves) {
            p.x += (p.vx + MathUtils.sin(time * 1.6f + p.phase) * 24f) * delta;
            p.y += p.vy * delta;
            if (p.y < -MARGIN) {
                p.y += ForestGame.WORLD_HEIGHT + 2 * MARGIN;
            }
            p.x = wrap(p.x);
        }
        for (Particle p : dust) {
            p.x += (p.vx + MathUtils.sin(time * 0.5f + p.phase) * 3f) * delta;
            p.y += p.vy * delta;
            if (p.y > 500f) {
                p.y = 40f;
            }
            p.x = wrap(p.x);
        }
    }

    /** Hands over the animated sprites; either may be null to keep the code-drawn shape. */
    public void setSprites(Animation<TextureRegion> firefly, Animation<TextureRegion> leaf) {
        this.fireflySprite = firefly;
        this.leafSprite = leaf;
    }

    /** The frame a particle shows now: each one starts at its own point in the loop. */
    private TextureRegion frameOf(Animation<TextureRegion> sprite, Particle p) {
        return sprite.getKeyFrame(time + p.phase);
    }

    static float wrap(float x) {
        float span = ForestGame.WORLD_WIDTH + 2 * MARGIN;
        float shifted = (x + MARGIN) % span;
        if (shifted < 0) {
            shifted += span;
        }
        return shifted - MARGIN;
    }

    /** 0 to 1: how bright a firefly is now. Each one blinks on its own slow rhythm. */
    float fireflyBrightness(Particle p) {
        float wave = 0.5f + 0.5f * MathUtils.sin(time * 1.8f + p.phase * 3f);
        return wave * wave;
    }

    /** Mist, drawn behind the tiles. The batch must not be started. */
    public void renderBack(SpriteBatch batch, float cameraLeft) {
        if (settings.mist == null) {
            return;
        }
        ensureTextures();
        batch.setProjectionMatrix(screen);
        batch.begin();
        for (int band = 0; band < 2; band++) {
            float speed = band == 0 ? 8f : 14f;
            float shift = (time * speed + cameraLeft * (0.3f + 0.2f * band)) % ForestGame.WORLD_WIDTH;
            float y = band == 0 ? 60f : 150f;
            float alpha = settings.mist.a * (0.8f + 0.2f * MathUtils.sin(time * 0.3f + band));
            batch.setColor(settings.mist.r, settings.mist.g, settings.mist.b, alpha);
            for (int copy = -1; copy <= 1; copy++) {
                batch.draw(mist, copy * ForestGame.WORLD_WIDTH - shift, y, ForestGame.WORLD_WIDTH, 160f);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    /** Fireflies, leaves and dust, drawn over the world and under the HUD. The batch must not be started. */
    public void renderFront(SpriteBatch batch, float cameraLeft) {
        if (fireflies.isEmpty() && leaves.isEmpty() && dust.isEmpty()) {
            return;
        }
        ensureTextures();
        float slide = cameraLeft * DEPTH;
        batch.setProjectionMatrix(screen);
        batch.begin();
        for (Particle p : leaves) {
            if (leafSprite != null) {
                // The sprite tumbles on its own; it only drifts here.
                batch.setColor(1f, 1f, 1f, 0.9f);
                batch.draw(frameOf(leafSprite, p), wrap(p.x - slide) - SPRITE_SIZE / 2, p.y - SPRITE_SIZE / 2, SPRITE_SIZE, SPRITE_SIZE);
                continue;
            }
            Color c = settings.leafColor != null ? settings.leafColor : Color.OLIVE;
            batch.setColor(c.r, c.g, c.b, 0.85f);
            float angle = time * p.spin + p.phase * 57f;
            batch.draw(pixel, wrap(p.x - slide), p.y, p.size, p.size / 2, p.size * 2, p.size, 1, 1, angle, 0, 0, 1, 1, false, false);
        }
        for (Particle p : dust) {
            batch.setColor(DUST.r, DUST.g, DUST.b, 0.25f + 0.2f * MathUtils.sin(time + p.phase));
            batch.draw(pixel, wrap(p.x - slide * 0.6f), p.y, p.size, p.size);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // fireflies add light
        for (Particle p : fireflies) {
            float bright = fireflyBrightness(p);
            float x = wrap(p.x - slide);
            if (fireflySprite != null) {
                // The sprite has its own glow and blink; the slow brightness wave keeps them out of step.
                batch.setColor(1f, 1f, 1f, 0.55f + 0.45f * bright);
                batch.draw(frameOf(fireflySprite, p), x - SPRITE_SIZE / 2, p.y - SPRITE_SIZE / 2, SPRITE_SIZE, SPRITE_SIZE);
                continue;
            }
            tint.set(FIREFLY.r, FIREFLY.g, FIREFLY.b, 0.75f * bright);
            batch.setColor(tint);
            batch.draw(glow, x - p.size, p.y - p.size, p.size * 2, p.size * 2);
            batch.setColor(1f, 0.97f, 0.8f, 0.4f + 0.6f * bright);
            batch.draw(pixel, x - 1.5f, p.y - 1.5f, 3, 3);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void ensureTextures() {
        if (pixel != null) {
            return;
        }
        Pixmap one = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        one.setColor(Color.WHITE);
        one.fill();
        pixel = new Texture(one);
        one.dispose();

        int size = 32;
        Pixmap dot = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = Math.min(1f, (float) Math.hypot(x - size / 2f + 0.5f, y - size / 2f + 0.5f) / (size / 2f));
                float a = (1f - d) * (1f - d);
                dot.setColor(1f, 1f, 1f, a);
                dot.drawPixel(x, y);
            }
        }
        glow = new Texture(dot);
        glow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        dot.dispose();

        // A soft horizontal band: clear at the top and bottom edges, thicker in a few uneven puffs.
        int w = 256;
        int h = 32;
        Pixmap band = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float vertical = MathUtils.sin(MathUtils.PI * (y + 0.5f) / h);
            for (int x = 0; x < w; x++) {
                float puffs = 0.6f + 0.25f * MathUtils.sin(x * MathUtils.PI2 / w * 2) + 0.15f * MathUtils.sin(x * MathUtils.PI2 / w * 5 + 1f);
                band.setColor(1f, 1f, 1f, MathUtils.clamp(vertical * vertical * puffs, 0f, 1f));
                band.drawPixel(x, y);
            }
        }
        mist = new Texture(band);
        mist.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        band.dispose();
    }

    @Override
    public void dispose() {
        if (pixel != null) {
            pixel.dispose();
            glow.dispose();
            mist.dispose();
            pixel = null;
        }
    }
}
