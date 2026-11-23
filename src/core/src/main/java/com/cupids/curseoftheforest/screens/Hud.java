package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.stages.StageTimer;

/** Screen-space meters and inventory; borrows the game's font and batch. */
public class Hud {
    private final Player player;
    private final StageTimer timer;
    private final BitmapFont font;
    private final String stageName;
    private final Matrix4 worldProjection = new Matrix4();
    private final Matrix4 screen = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    public Hud(Player player, StageTimer timer, BitmapFont font, String stageName) {
        this.player = player;
        this.timer = timer;
        this.font = font;
        this.stageName = stageName;
    }

    public void render(SpriteBatch batch) {
        worldProjection.set(batch.getProjectionMatrix());
        Color color = new Color(font.getColor());
        batch.setProjectionMatrix(screen);
        batch.begin();
        Theme.shade(batch, 0, 478, ForestGame.WORLD_WIDTH, 62);
        font.setColor(Theme.GOLD);
        font.draw(batch, stageName, 16, 534);
        meter(batch, "Courage", player.getCourage(), 16, Theme.COURAGE);
        meter(batch, "Sanity", player.getSanity(), 300, Theme.SANITY);
        font.setColor(Theme.PAPER);
        font.draw(batch, "Coins: " + player.getCoins(), 600, 500);
        if (timer != null) {
            int seconds = (int) Math.ceil(timer.getRemaining());
            font.setColor(seconds <= 10 ? Theme.WARNING : Theme.PAPER);
            font.draw(batch, String.format("Time %d:%02d", seconds / 60, seconds % 60), 760, 500);
        }
        StringBuilder items = new StringBuilder("Items: ");
        int count = 0;
        for (Item item : player.getInventory().getAll()) {
            if (count == 6) { items.append(" +").append(player.getInventory().getAll().size() - count).append(" more"); break; }
            if (count++ > 0) items.append(", ");
            items.append(item.getName());
        }
        if (count == 0) items.append("none");
        Theme.shade(batch, 0, 0, ForestGame.WORLD_WIDTH, 56);
        font.setColor(Theme.PAPER);
        font.draw(batch, items.toString(), 16, 40, 920, Align.left, true);
        batch.end();
        font.setColor(color);
        // Earlier stages draw their overlays next and expect screen coordinates.
    }

    /** Stages with creature sprites can resume the camera used before the HUD. */
    public void beginWorld(SpriteBatch batch) {
        batch.setProjectionMatrix(worldProjection);
        batch.begin();
    }

    /** Filled bar (0 to 100) with its label; turns salmon-red text when low. */
    private void meter(SpriteBatch batch, String label, int value, float x, Color fill) {
        int clamped = Math.max(0, Math.min(100, value));
        font.setColor(value <= 25 ? Theme.WARNING : Theme.PAPER);
        font.draw(batch, label + " " + value, x, 516);
        Theme.rect(batch, Theme.INK, x - 2, 480, 204, 14);
        Theme.rect(batch, Theme.NIGHT, x, 482, 200, 10);
        Theme.rect(batch, value <= 25 ? Theme.WARNING : fill, x, 482, 2f * clamped, 10);
    }
}
