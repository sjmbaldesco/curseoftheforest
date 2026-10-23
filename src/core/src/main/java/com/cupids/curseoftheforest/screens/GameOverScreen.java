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

        ScreenUtils.clear(0.15f, 0.02f, 0.02f, 1f);
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        MenuText.begin(batch);
        MenuText.centered(batch, font, "Game Over", 340, 3f);
        MenuText.centered(batch, font, failedStage.getName(), 280, 1.2f);
        MenuText.centered(batch, font, "R  Retry stage", 210, 1.5f);
        MenuText.centered(batch, font, "M  Main menu", 170, 1.5f);
        batch.end();
    }

    public void retry() {
        game.retryStage(failedStage);
    }

    public void toMainMenu() {
        game.toMainMenu();
    }
}
