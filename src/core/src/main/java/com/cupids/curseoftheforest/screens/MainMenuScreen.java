package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;

/** Title screen: New Game or Exit (layout from mockup 06). */
public class MainMenuScreen extends ScreenAdapter {
    static final String CONTROLS = "[A/D] Move  [SPACE] Jump  [E] Interact  [1-5] Choose  [F11] Full screen";
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
        MenuText.begin(batch);
        Theme.drawBackdrop(batch, backdrop);
        // Fiesta flags (banderitas) strung along the top edge.
        int flagWidth = UiSkin.width("banderitas");
        for (int x = 0; flagWidth > 0 && x < ForestGame.WORLD_WIDTH; x += flagWidth) {
            UiSkin.image(batch, "banderitas", x, ForestGame.WORLD_HEIGHT - 28);
        }
        if (!UiSkin.image(batch, "logo", 260, ForestGame.WORLD_HEIGHT - 76 - 140)) {
            Theme.title(batch, Theme.Size.HUGE, "Curse of the Forest", 480, 440, Theme.GOLD);
        }
        MenuText.button(batch, "[ENTER] New Game", 272, 296, true);
        MenuText.button(batch, "[ESC] Exit", 338, 296, false);

        float footerWidth = Theme.width(Theme.font(Theme.Size.SMALL), CONTROLS) + 40;
        UiSkin.frame(batch, UiSkin.Frame.TOAST, (ForestGame.WORLD_WIDTH - footerWidth) / 2, 540 - 504, footerWidth, 32);
        Theme.textCentred(batch, Theme.Size.SMALL, CONTROLS, 480, 540 - 480, Theme.PAPER);
        Theme.textRight(batch, Theme.Size.SMALL, "CS-2103 | Team CUPIDS | BatStateU", 944, 540 - 518, Theme.MUTED);
        batch.end();
    }

    public void newGame() {
        game.newGame();
    }

    public void exit() {
        Gdx.app.exit();
    }
}
