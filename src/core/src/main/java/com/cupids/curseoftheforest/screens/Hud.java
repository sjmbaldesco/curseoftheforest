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
    private final Matrix4 screen = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    public Hud(Player player, StageTimer timer, BitmapFont font, String stageName) {
        this.player = player;
        this.timer = timer;
        this.font = font;
        this.stageName = stageName;
    }

    public void render(SpriteBatch batch) {
        Matrix4 previous = new Matrix4(batch.getProjectionMatrix());
        Color color = new Color(font.getColor());
        batch.setProjectionMatrix(screen);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, stageName, 16, 524);
        meter(batch, "Courage", player.getCourage(), 16);
        meter(batch, "Sanity", player.getSanity(), 330);
        font.setColor(Color.WHITE);
        String stats = "Coins: " + player.getCoins();
        if (timer != null) {
            int seconds = (int) Math.ceil(timer.getRemaining());
            stats += String.format("    Time %d:%02d", seconds / 60, seconds % 60);
        }
        font.draw(batch, stats, 650, 500);
        StringBuilder items = new StringBuilder("Items: ");
        int count = 0;
        for (Item item : player.getInventory().getAll()) {
            if (count == 6) { items.append(" +").append(player.getInventory().getAll().size() - count).append(" more"); break; }
            if (count++ > 0) items.append(", ");
            items.append(item.getName());
        }
        if (count == 0) items.append("none");
        font.draw(batch, items.toString(), 16, 475, 920, Align.left, true);
        batch.end();
        font.setColor(color);
        batch.setProjectionMatrix(previous);
    }

    private void meter(SpriteBatch batch, String label, int value, float x) {
        int filled = Math.max(0, Math.min(20, value / 5));
        font.setColor(value <= 25 ? Color.SALMON : Color.WHITE);
        font.draw(batch, label + " [" + "|".repeat(filled) + ".".repeat(20 - filled) + "] " + value, x, 500);
    }
}
