package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.AssetLoader;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.stages.StageTimer;
import java.util.HashMap;
import java.util.Map;

/** Screen-space meters and inventory; borrows the game's font and batch. */
public class Hud {
    private final Player player;
    private final StageTimer timer;
    private final BitmapFont font;
    private final String stageName;
    /** Item name to icon file; items without an icon (potions) are listed by name instead. */
    private static final Map<String, String> ICON_FILES = Map.of(
            "Lumang Itak", "itak",
            "Lumang Punyal", "punyal",
            "Agimat ni Enteng", "agimat-ni-enteng",
            "Malunggay Juice", "malunggay-juice",
            "Kwerdas ng Gitara", "kwerdas",
            "Rosary", "rosary",
            "Lumang Gitara", "lumang-gitara");

    /** True while the stage is draining that stat, so its bar pulses. */
    private boolean courageDraining;
    private boolean sanityDraining;
    private float pulseTime;
    private String goal = "";
    private AssetLoader icons;
    private final Map<String, Texture> iconCache = new HashMap<>();
    private final Matrix4 worldProjection = new Matrix4();
    private final Matrix4 screen = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    public Hud(Player player, StageTimer timer, BitmapFont font, String stageName) {
        this.player = player;
        this.timer = timer;
        this.font = font;
        this.stageName = stageName;
    }

    /** Lets the HUD draw item icons; without it the item list stays text-only. */
    public void setIcons(AssetLoader loader) {
        this.icons = loader;
    }

    private Texture iconFor(Item item) {
        String file = ICON_FILES.get(item.getName());
        if (icons == null || file == null) {
            return null;
        }
        return iconCache.computeIfAbsent(file, f -> {
            String path = "images/icons/" + f + ".png";
            if (!Gdx.files.internal(path).exists()) {
                return null;
            }
            Texture texture = icons.loadImage(path);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return texture;
        });
    }

    /** Called each frame by the stage, so the player can see which stats the place is eating. */
    public void setDraining(boolean courage, boolean sanity) {
        courageDraining = courage;
        sanityDraining = sanity;
    }

    /** The current objective, shown at the right of the top bar. */
    public void setGoal(String goal) {
        this.goal = goal == null ? "" : goal;
    }

    public void render(SpriteBatch batch) {
        pulseTime = (pulseTime + Gdx.graphics.getDeltaTime()) % 1000f;
        worldProjection.set(batch.getProjectionMatrix());
        Color color = new Color(font.getColor());
        batch.setProjectionMatrix(screen);
        batch.begin();
        Theme.shade(batch, 0, 478, ForestGame.WORLD_WIDTH, 62);
        font.setColor(Theme.GOLD);
        font.draw(batch, stageName, 16, 534);
        if (!goal.isEmpty()) {
            font.setColor(Theme.PAPER);
            font.draw(batch, goal, 520, 534, 424, Align.right, false);
        }
        meter(batch, "Courage", player.getCourage(), 16, Theme.COURAGE, courageDraining);
        meter(batch, "Sanity", player.getSanity(), 300, Theme.SANITY, sanityDraining);
        font.setColor(Theme.PAPER);
        font.draw(batch, "Coins: " + player.getCoins(), 600, 500);
        if (timer != null) {
            int seconds = (int) Math.ceil(timer.getRemaining());
            font.setColor(seconds <= 10 ? Theme.WARNING : Theme.PAPER);
            font.draw(batch, String.format("Time %d:%02d", seconds / 60, seconds % 60), 760, 500);
        }
        Theme.shade(batch, 0, 0, ForestGame.WORLD_WIDTH, 56);
        font.setColor(Theme.PAPER);
        float x = 16;
        font.draw(batch, "Items:", x, 38);
        x += 76;
        StringBuilder names = new StringBuilder();
        int count = 0;
        for (Item item : player.getInventory().getAll()) {
            if (count++ == 8) {
                names.append(" +").append(player.getInventory().getAll().size() - 8).append(" more");
                break;
            }
            Texture icon = iconFor(item);
            if (icon != null) {
                batch.draw(icon, x, 4, 44, 44);
                x += 52;
            } else {
                if (names.length() > 0) names.append(", ");
                names.append(item.getName());
            }
        }
        if (count == 0) names.append("none");
        font.draw(batch, names.toString(), x + 8, 38, ForestGame.WORLD_WIDTH - x - 24, Align.left, false);
        batch.end();
        font.setColor(color);
        // Earlier stages draw their overlays next and expect screen coordinates.
    }

    /** Stages with creature sprites can resume the camera used before the HUD. */
    public void beginWorld(SpriteBatch batch) {
        batch.setProjectionMatrix(worldProjection);
        batch.begin();
    }

    /**
     * Filled bar (0 to 100) with its label; turns salmon-red text when low. While draining, the
     * border pulses red and the label says so.
     */
    private void meter(SpriteBatch batch, String label, int value, float x, Color fill, boolean draining) {
        int clamped = Math.max(0, Math.min(100, value));
        font.setColor(value <= 25 ? Theme.WARNING : Theme.PAPER);
        font.draw(batch, label + " " + value + (draining ? "  draining" : ""), x, 516);
        boolean pulseOn = draining && (int) (pulseTime * 3) % 2 == 0;
        Theme.rect(batch, pulseOn ? Theme.WARNING : Theme.INK, x - 2, 480, 204, 14);
        Theme.rect(batch, Theme.NIGHT, x, 482, 200, 10);
        Theme.rect(batch, value <= 25 ? Theme.WARNING : fill, x, 482, 2f * clamped, 10);
    }
}
