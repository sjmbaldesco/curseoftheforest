package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.stages.GameStage;

/** Shown when a stage is lost. Retry restarts that stage from its checkpoint. */
public class GameOverScreen extends ScreenAdapter {
    private final ForestGame game;
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
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        MenuText.begin(batch);
        Theme.rect(batch, Theme.COURAGE, 0, 0, ForestGame.WORLD_WIDTH, 8);
        MenuText.centered(batch, font, "Game Over", 400, 4f, Theme.WARNING);
        MenuText.centered(batch, font, failedStage.getName(), 330, 2f, Theme.PAPER);
        Theme.panel(batch, 330, 130, 300, 120);
        MenuText.centered(batch, font, "R  Retry stage", 214, 2f, Theme.INK);
        MenuText.centered(batch, font, "M  Main menu", 166, 2f, Theme.INK);
        batch.end();
    }

    public void retry() {
        game.retryStage(failedStage);
    }

    public void toMainMenu() {
        game.toMainMenu();
    }
}
