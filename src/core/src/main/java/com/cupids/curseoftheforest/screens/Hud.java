package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.AssetLoader;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.stages.StageTimer;
import java.util.HashMap;
import java.util.Map;

/** Screen-space meters and inventory; borrows the game's font and batch. */
public class Hud {
    private final Player player;
    private final StageTimer timer;
    private final BitmapFont font;
    private final String stageName;
    /** Each potion kind's bubbling, glowing bottle (red for Damage, green for Heal, blue for Stun). */
    private static final Map<PotionType, String> POTION_SHEETS = Map.of(
            PotionType.DAMAGE, "images/animated/potion-red_glow.png",
            PotionType.HEAL, "images/animated/potion-green_glow.png",
            PotionType.STUN, "images/animated/potion-light-blue_glow.png");
    /** Item name to icon file; items without an icon are listed by name instead. */
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
    private final Map<PotionType, Animation<TextureRegion>> potionCache = new HashMap<>();
    /** Item slots in the bar; more items show as "+N". */
    static final int SLOTS = 8;
    private final Matrix4 worldProjection = new Matrix4();
    private final Matrix4 screen = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    public Hud(Player player, StageTimer timer, BitmapFont font, String stageName) {
        this.player = player;
        this.timer = timer;
        this.font = font;
        this.stageName = stageName;
    }

    /** The animated bottle sheet for a potion kind (64 px frames). Also used by the duel. */
    public static String potionSheet(PotionType type) {
        return POTION_SHEETS.get(type);
    }

    /** The icon file for an item name (images/icons/...), or null if it has none. Also used by the shop. */
    public static String iconPath(String itemName) {
        String file = ICON_FILES.get(itemName);
        return file == null ? null : "images/icons/" + file + ".png";
    }

    /** Lets the HUD draw item icons; without it the item list stays text-only. */
    public void setIcons(AssetLoader loader) {
        this.icons = loader;
    }

    /** The icon to draw for an item now: a potion's current animation frame, or a still icon; null for none. */
    private TextureRegion iconFrameFor(Item item) {
        if (item instanceof Potion potion && icons != null) {
            Animation<TextureRegion> bottle = potionCache.computeIfAbsent(potion.getType(),
                    type -> icons.loadStrip(POTION_SHEETS.get(type), 64, 0.12f));
            if (bottle != null) {
                return bottle.getKeyFrame(pulseTime);
            }
        }
        Texture icon = iconFor(item);
        return icon == null ? null : new TextureRegion(icon);
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
        batch.setProjectionMatrix(screen);
        batch.begin();
        drawTopBand(batch);
        drawItemBar(batch);
        batch.end();
        // Earlier stages draw their overlays next and expect screen coordinates.
    }

    /** Layout from the UI spec (mockup 01), converted from top-left to LibGDX's bottom-left origin. */
    private void drawTopBand(SpriteBatch batch) {
        float top = ForestGame.WORLD_HEIGHT;
        if (!UiSkin.image(batch, "hud-strip", 0, top - 62, ForestGame.WORLD_WIDTH, 62)) {
            Theme.shade(batch, 0, top - 62, ForestGame.WORLD_WIDTH, 62);
        }
        Theme.text(batch, Theme.Size.BODY, stageName, 16, top - 6, Theme.GOLD);

        float courageEnd = meter(batch, "icon-courage", "Courage", player.getCourage(), 16, "bar-courage", courageDraining);
        float sanityEnd = meter(batch, "icon-sanity", "Sanity", player.getSanity(), 380, "bar-sanity", sanityDraining);

        UiSkin.image(batch, "icon-coin", 700, top - 30);
        Theme.text(batch, Theme.Size.TALK, String.valueOf(player.getCoins()), 732, top - 8, Theme.PAPER);
        if (timer != null) {
            int seconds = (int) Math.ceil(timer.getRemaining());
            boolean hurry = seconds < 10;
            UiSkin.image(batch, "icon-timer", 780, top - 30);
            // Under 10 seconds the clock turns red and blinks twice a second.
            if (!hurry || (int) (pulseTime * 2) % 2 == 0) {
                Theme.text(batch, Theme.Size.TALK, String.format("%d:%02d", seconds / 60, seconds % 60), 812, top - 8,
                        hurry ? Theme.WARNING : Theme.PAPER);
            }
        }
        if (!goal.isEmpty()) {
            // The goal shares the second row with the meters: it gets the space right of them,
            // and drops to the small font (then trims) if it is long.
            float room = 944 - Math.max(courageEnd, sanityEnd) - 24;
            Theme.Size size = Theme.width(Theme.font(Theme.Size.BODY), goal) <= room ? Theme.Size.BODY : Theme.Size.SMALL;
            String shown = fit(Theme.font(size), goal, room);
            Theme.textRight(batch, size, shown, 944, top - 38, Theme.GOLD);
        }
    }

    /** Trims text with "..." until it fits the width. */
    static String fit(BitmapFont font, String text, float width) {
        if (Theme.width(font, text) <= width) {
            return text;
        }
        String trimmed = text;
        while (trimmed.length() > 1 && Theme.width(font, trimmed + "...") > width) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.trim() + "...";
    }

    /** The item bar: eight slots centred along the bottom, with the slot number in each corner. */
    private void drawItemBar(SpriteBatch batch) {
        if (!UiSkin.image(batch, "itembar-strip", 0, 0, ForestGame.WORLD_WIDTH, 56)) {
            Theme.shade(batch, 0, 0, ForestGame.WORLD_WIDTH, 56);
        }
        Theme.text(batch, Theme.Size.BODY, "ITEMS", 16, 540 - 508, Theme.GOLD);
        java.util.List<Item> items = player.getInventory().getAll();
        for (int i = 0; i < SLOTS; i++) {
            float x = 256 + i * 56;
            UiSkin.frame(batch, UiSkin.Frame.SLOT, x, 4, 48, 48);
            if (i < items.size()) {
                Item item = items.get(i);
                TextureRegion icon = iconFrameFor(item);
                if (icon != null) {
                    batch.draw(icon, x, 4, 48, 48);
                } else {
                    // No art yet: the item's first letter keeps the slot readable.
                    Theme.textCentred(batch, Theme.Size.TALK, item.getName().substring(0, 1), x + 24, 38, Theme.PAPER);
                }
            }
            String number = String.valueOf(i + 1);
            Theme.title(batch, Theme.Size.SMALL, number, x + 42, 20, Theme.PAPER);
        }
        if (items.size() > SLOTS) {
            Theme.text(batch, Theme.Size.BODY, "+" + (items.size() - SLOTS), 256 + SLOTS * 56, 36, Theme.PAPER);
        }
    }

    /** Stages with creature sprites can resume the camera used before the HUD. */
    public void beginWorld(SpriteBatch batch) {
        batch.setProjectionMatrix(worldProjection);
        batch.begin();
    }

    /**
     * Icon, label, a framed bar and the value, on the HUD's second row (y = 36 from the top).
     * While the stat drains, the fill flashes to the drain colour every 0.25 s and a drain mark
     * shows after the value. Returns the x where the value ends.
     */
    private float meter(SpriteBatch batch, String icon, String label, int value, float x, String fill, boolean draining) {
        float rowTop = ForestGame.WORLD_HEIGHT - 36;
        int clamped = Math.max(0, Math.min(100, value));
        UiSkin.image(batch, icon, x, rowTop - 22);
        Theme.text(batch, Theme.Size.BODY, label, x + 28, rowTop - 2, Theme.PAPER);
        float frameX = x + 28 + Theme.width(Theme.font(Theme.Size.BODY), label) + 10;
        frameX = Math.round(frameX / 2) * 2; // stay on the 2x pixel grid
        UiSkin.frame(batch, UiSkin.Frame.BAR, frameX, rowTop - 20, 150, 20);
        boolean flash = draining && (int) (pulseTime * 4) % 2 == 0;
        float width = 138f * clamped / 100f;
        if (width > 0 && !UiSkin.image(batch, flash ? "bar-drain" : fill, frameX + 6, rowTop - 14, width, 8)) {
            Theme.rect(batch, flash ? Theme.WARNING : Theme.COURAGE, frameX + 6, rowTop - 14, width, 8);
        }
        String shown = String.valueOf(value);
        float valueX = frameX + 158;
        Theme.text(batch, Theme.Size.BODY, shown, valueX, rowTop - 2, value <= 25 ? Theme.WARNING : Theme.PAPER);
        float end = valueX + Theme.width(Theme.font(Theme.Size.BODY), shown);
        if (draining) {
            UiSkin.image(batch, "icon-drain", end + 8, rowTop - 16);
            end += 24;
        }
        return end;
    }
}
