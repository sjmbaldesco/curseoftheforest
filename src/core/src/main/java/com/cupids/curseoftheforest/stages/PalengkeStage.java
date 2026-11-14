package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.DuwendeTonyo;
import com.cupids.curseoftheforest.creatures.Wakwak;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.minigames.InsufficientCoinsException;
import com.cupids.curseoftheforest.player.Player;
import java.util.List;

/**
 * Stage 2: Ang Palengke ng Duwende. Wakwak cave coin run, then Duwende Tonyo's shop.
 */
public class PalengkeStage extends GameStage {
    private static final int CATCH_COURAGE_LOSS = 10;
    private static final float CATCH_COOLDOWN = 1.5f;
    private static final int[] SHOP_KEYS = {
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5
    };
    private final Matrix4 shopProjection = new Matrix4()
            .setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
    private Wakwak wakwak;
    private DuwendeTonyo tonyo;
    private float catchCooldown;
    private boolean reachedShop;
    private String shopMessage;

    public PalengkeStage(ForestGame game) {
        super(game, "Stage 2: Ang Palengke ng Duwende", "maps/palengke.tmx");
    }

    @Override
    protected void init() {
        wakwak = new Wakwak(Math.max(0f, player.getX() - 56f), player.getY() + Player.HEIGHT + 16f);
        wakwak.interact(player);
        tonyo = new DuwendeTonyo(tileMap.getExit().x, tileMap.getExit().y);
        catchCooldown = 0f;
        reachedShop = false;
        shopMessage = "";
    }

    @Override
    public void update(float delta) {
        if (reachedExit()) {
            reachedShop = !isFailed();
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
        if (!reachedShop) {
            return;
        }
        if (!tonyo.isShopOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                tonyo.interact(player);
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            completed = true;
            return;
        }

        List<Item> stock = tonyo.getShop().listStock();
        for (int i = 0; i < stock.size(); i++) {
            if (Gdx.input.isKeyJustPressed(SHOP_KEYS[i])) {
                buy(stock.get(i));
                return;
            }
        }
    }

    private void buy(Item item) {
        if (player.getInventory().has(item)) {
            shopMessage = "You already bought " + item.getName() + ".";
            return;
        }
        try {
            tonyo.getShop().purchase(player, item);
            shopMessage = "Bought " + item.getName() + ".";
        } catch (InsufficientCoinsException exception) {
            shopMessage = exception.getMessage();
        }
    }

    @Override
    public void render(float delta) {
        if (reachedShop) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.toMainMenu();
                return;
            }
            handleInput();
            if (isComplete()) {
                game.onStageComplete(this);
                return;
            }
            drawShop();
            return;
        }

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

    private void drawShop() {
        ScreenUtils.clear(0.09f, 0.16f, 0.12f, 1f);
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        float left = 48f;
        float top = ForestGame.WORLD_HEIGHT - 48f;
        batch.setProjectionMatrix(shopProjection);
        batch.begin();
        font.draw(batch, tonyo.getName() + "'s Shop", left, top);
        font.draw(batch, "Dayo's coins: " + player.getCoins(), left, top - 32);
        if (!tonyo.isShopOpen()) {
            font.draw(batch, "Press E to talk to Duwende Tonyo.", left, top - 72);
        } else {
            font.draw(batch, "Press 1-5 to buy an item. Press Enter to continue to Stage 3.", left, top - 72);
            List<Item> stock = tonyo.getShop().listStock();
            for (int i = 0; i < stock.size(); i++) {
                Item item = stock.get(i);
                String price = player.getInventory().has(item) ? "Bought" : item.getPrice() + " coins";
                font.draw(batch, (i + 1) + "   " + item.getName() + "   " + price, left, top - 112 - i * 30);
            }
            font.draw(batch, shopMessage, left, top - 280);
        }
        batch.end();
    }
}
