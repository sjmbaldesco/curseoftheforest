package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Swaying grass tufts and ferns standing on the ground tiles of a map. They are decoration only:
 * Dayo walks in front of them and they never block him.
 *
 * <p>Plants grow on "surface" tiles (a solid tile with open air above it). Which surfaces get a plant
 * comes from the tile's position, not from a random roll, so the same map always looks the same and
 * the choice can be tested without graphics.
 */
public class Foliage {
    public static final String GRASS_SHEET = "images/animated/grass_tuft_sway.png";
    public static final String FERN_SHEET = "images/animated/fern_sway.png";
    /** Roughly one surface tile in this many gets a grass tuft. */
    static final int GRASS_EVERY = 3;
    /** Roughly one surface tile in this many gets a fern (ferns win over grass). */
    static final int FERN_EVERY = 8;

    enum Kind { GRASS, FERN }

    /** One plant: where its bottom-left sits in the world, and which sprite it uses. */
    record Plant(Kind kind, float x, float y, float phase) { }

    final List<Plant> plants = new ArrayList<>();
    private final Animation<TextureRegion> grass;
    private final Animation<TextureRegion> fern;
    private float time;
    private final Color tint = new Color(Color.WHITE);

    /**
     * @param growsAt says whether plants may grow at a world x (Stage 2 keeps its cave bare)
     * @param grass   the grass sheet, or null for no grass
     * @param fern    the fern sheet, or null for no ferns
     */
    public Foliage(TileMap map, Predicate<Float> growsAt, Animation<TextureRegion> grass, Animation<TextureRegion> fern) {
        this.grass = grass;
        this.fern = fern;
        int size = map.getTileSize();
        int rows = Math.round(map.getPixelHeight() / Math.max(1, size));
        for (int col = 0; col < map.getWidthInTiles(); col++) {
            for (int row = 0; row < rows; row++) {
                if (!map.isSolid(col, row) || map.isSolid(col, row + 1)) {
                    continue;
                }
                float x = col * size;
                if (!growsAt.test(x)) {
                    continue;
                }
                Kind kind = kindFor(col, row);
                if (kind == Kind.FERN && fern != null) {
                    // A 48 px fern, centred on its 32 px tile.
                    plants.add(new Plant(kind, x - 8, (row + 1) * size, phaseFor(col, row)));
                } else if (kind != null && grass != null) {
                    plants.add(new Plant(Kind.GRASS, x, (row + 1) * size, phaseFor(col, row)));
                }
            }
        }
    }

    /** Which plant, if any, grows on the surface tile at (col, row). Spread by a simple hash. */
    static Kind kindFor(int col, int row) {
        int h = hash(col, row);
        if (h % FERN_EVERY == 0) {
            return Kind.FERN;
        }
        return h % GRASS_EVERY == 0 ? Kind.GRASS : null;
    }

    private static float phaseFor(int col, int row) {
        return (hash(row, col) % 100) / 100f;
    }

    private static int hash(int a, int b) {
        int h = a * 73_856_093 ^ b * 19_349_663;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }

    /**
     * The sprites are bright forest green; the night stages shade them to match their colours.
     * Returns white (no change) for stages that keep the plain green.
     */
    public static Color tintFor(String stageName) {
        String prefix = stageName != null && stageName.length() >= 8 ? stageName.substring(0, 8) : "";
        return switch (prefix) {
            case "Stage 2:" -> new Color(0.85f, 0.78f, 0.95f, 1f);
            case "Stage 4:" -> new Color(0.72f, 0.82f, 1f, 1f);
            case "Stage 5:" -> new Color(0.78f, 0.62f, 0.88f, 1f);
            default -> new Color(Color.WHITE);
        };
    }

    public void setTint(Color color) {
        tint.set(color);
    }

    public void update(float delta) {
        time = (time + delta) % 10_000f;
    }

    /** Draws every plant in world coordinates; the batch is already started. */
    public void render(SpriteBatch batch) {
        Color previous = batch.getColor().cpy();
        batch.setColor(tint);
        for (Plant plant : plants) {
            Animation<TextureRegion> sheet = plant.kind() == Kind.FERN ? fern : grass;
            TextureRegion frame = sheet.getKeyFrame(time + plant.phase() * sheet.getAnimationDuration());
            batch.draw(frame, plant.x(), plant.y(), frame.getRegionWidth(), frame.getRegionHeight());
        }
        batch.setColor(previous);
    }
}
