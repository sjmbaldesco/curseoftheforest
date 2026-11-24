package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.stages.GameStage;

/** Shown when a stage is lost. Retry restarts that stage from its checkpoint (mockup 07). */
public class GameOverScreen extends ScreenAdapter {
    private final ForestGame game;
    private Texture backdrop;
    private final GameStage failedStage;

    public GameOverScreen(ForestGame game, GameStage failedStage) {
        this.game = game;
        this.failedStage = failedStage;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            retry();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            toMainMenu();
            return;
        }

        ScreenUtils.clear(0.12f, 0.03f, 0.04f, 1f);
        if (backdrop == null) {
            backdrop = Theme.loadBackdrop(game.getAssetLoader(), "images/backgrounds/game-over.png");
        }
        SpriteBatch batch = game.getBatch();
        MenuText.begin(batch);
        Theme.drawBackdrop(batch, backdrop);
        float top = ForestGame.WORLD_HEIGHT;
        UiSkin.frame(batch, UiSkin.Frame.PANEL, 224, top - 436, 512, 352);
        Theme.title(batch, Theme.Size.HUGE, "Game Over", 480, top - 108, Theme.WARNING);
        Theme.rect(batch, Theme.GOLD, 260, top - 192, 440, 4);
        Theme.textCentred(batch, Theme.Size.TALK, failedStage.getName(), 480, top - 212, Theme.GOLD);
        Theme.textCentred(batch, Theme.Size.BODY, failedStage.failureReason(), 480, top - 248, Theme.PAPER);
        Theme.textCentred(batch, Theme.Size.SMALL, "Coins and items are restored on retry.", 480, top - 272, Theme.MUTED);
        MenuText.button(batch, "[R] Retry stage", 302, 316, true);
        MenuText.button(batch, "[M] Main menu", 366, 316, false);
        batch.end();
    }

    public void retry() {
        game.retryStage(failedStage);
    }

    public void toMainMenu() {
        game.toMainMenu();
    }
}
