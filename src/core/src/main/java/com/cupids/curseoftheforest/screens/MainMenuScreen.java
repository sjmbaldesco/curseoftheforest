package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;

/** Title screen: New Game or Exit. */
public class MainMenuScreen extends ScreenAdapter {
    private final ForestGame game;

    public MainMenuScreen(ForestGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            newGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exit();
            return;
        }

        ScreenUtils.clear(0.05f, 0.12f, 0.08f, 1f);
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        MenuText.begin(batch);
        MenuText.centered(batch, font, "Curse of the Forest", 360, 3f);
        MenuText.centered(batch, font, "ENTER  New Game", 240, 1.5f);
        MenuText.centered(batch, font, "ESC  Exit", 200, 1.5f);
        MenuText.centered(batch, font, "Move: A/D or arrows    Jump: SPACE/W/UP    Menu: ESC", 60, 1f);
        batch.end();
    }

    public void newGame() {
        game.newGame();
    }

    public void exit() {
        Gdx.app.exit();
    }
}
