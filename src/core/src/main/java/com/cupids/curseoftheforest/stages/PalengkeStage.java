package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Wakwak;
import com.cupids.curseoftheforest.player.Player;

/**
 * Stage 2: Ang Palengke ng Duwende. Wakwak cave coin run, then Duwende Tonyo's shop.
 *
 * The coin chase uses the shared test map.
 */
public class PalengkeStage extends GameStage {
    private static final int CATCH_COURAGE_LOSS = 10;
    private static final float CATCH_COOLDOWN = 1.5f;
    private Wakwak wakwak;
    private float catchCooldown;

    public PalengkeStage(ForestGame game) {
        super(game, "Stage 2: Ang Palengke ng Duwende", "maps/test.tmx");
    }

    @Override
    protected void init() {
        wakwak = new Wakwak(Math.max(0f, player.getX() - 56f), player.getY() + Player.HEIGHT + 16f);
        wakwak.interact(player);
        catchCooldown = 0f;
    }

    @Override
    public void update(float delta) {
        if (reachedExit()) {
            completed = true;
            return;
        }

        wakwak.update(delta);
        catchCooldown = Math.max(0f, catchCooldown - delta);
        if (catchCooldown == 0f && wakwak.hasCaughtDayo()) {
            player.changeCourage(-CATCH_COURAGE_LOSS);
            catchCooldown = CATCH_COOLDOWN;
        }
    }

    @Override
    protected void handleInput() {
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (game.getScreen() != this) {
            return;
        }

        // Match the shared camera offset to draw the marker in HUD coordinates.
        float halfWidth = ForestGame.WORLD_WIDTH / 2f;
        float halfHeight = ForestGame.WORLD_HEIGHT / 2f;
        float left = MathUtils.clamp(player.getX() + Player.WIDTH / 2 - halfWidth,
                0f, Math.max(0f, tileMap.getPixelWidth() - ForestGame.WORLD_WIDTH));
        float bottom = MathUtils.clamp(player.getY() - halfHeight,
                0f, Math.max(0f, tileMap.getPixelHeight() - ForestGame.WORLD_HEIGHT));

        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        batch.begin();
        font.draw(batch, "Collect coins and reach the exit. Wakwak is following you!",
                12, ForestGame.WORLD_HEIGHT - 60);
        if (catchCooldown > 0f) {
            font.draw(batch, "Wakwak caught you! -" + CATCH_COURAGE_LOSS + " Courage",
                    12, ForestGame.WORLD_HEIGHT - 82);
        }
        font.draw(batch, "< Wakwak >", wakwak.getX() - left - 50f,
                wakwak.getY() - bottom, 100f, Align.center, false);
        batch.end();
    }
}
