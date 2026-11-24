package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;

/** Title screen: New Game or Exit. */
public class MainMenuScreen extends ScreenAdapter {
    private final ForestGame game;
    private Texture backdrop;

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

        ScreenUtils.clear(Theme.NIGHT);
        if (backdrop == null) {
            backdrop = Theme.loadBackdrop(game.getAssetLoader(), "images/backgrounds/title.png");
        }
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        MenuText.begin(batch);
        Theme.drawBackdrop(batch, backdrop);
        Theme.rect(batch, Theme.LEAF, 0, 0, ForestGame.WORLD_WIDTH, 8);
        Theme.rect(batch, Theme.GOLD, 280, 322, 400, 4);
        BitmapFont title = game.getTitleFont();
        MenuText.centered(batch, title, "Curse of the Forest", 400, 4f, Theme.PAPER);
        MenuText.centered(batch, title, "Harana sa Dilim", 308, 2f, Theme.GOLD);
        Theme.panel(batch, 330, 130, 300, 120);
        font.setColor(Theme.INK);
        MenuText.centered(batch, font, "ENTER  New Game", 214, 2f);
        font.setColor(Theme.INK);
        MenuText.centered(batch, font, "ESC  Exit", 166, 2f);
        MenuText.centered(batch, font, "Move: A/D or arrows    Jump: SPACE/W/UP    Menu: ESC    Full screen: F11", 60, 1f, Theme.PAPER);
        batch.end();
    }

    public void newGame() {
        game.newGame();
    }

    public void exit() {
        Gdx.app.exit();
    }
}
