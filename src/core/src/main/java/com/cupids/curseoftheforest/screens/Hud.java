package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.stages.StageTimer;

/**
 * On-screen stats drawn on top of a stage. Plain text for now.
 * TODO(Phebe): owner of this class; Courage and Sanity meters, item icons.
 */
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
        batch.setProjectionMatrix(screen);
        batch.begin();
        float top = ForestGame.WORLD_HEIGHT - 12;
        font.draw(batch, stageName, 12, top);
        String stats = "Courage " + player.getCourage() + "   Sanity " + player.getSanity() + "   Coins " + player.getCoins();
        if (timer != null) {
            int seconds = (int) Math.ceil(timer.getRemaining());
            stats += String.format("   Time %d:%02d", seconds / 60, seconds % 60);
        }
        font.draw(batch, stats, 12, top - 22);
        batch.end();
    }
}
