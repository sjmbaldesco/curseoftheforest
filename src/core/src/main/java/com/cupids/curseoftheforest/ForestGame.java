package com.cupids.curseoftheforest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** Main game class. Owns the resources shared by every screen. */
public class ForestGame extends Game {
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.12f, 0.08f, 1f);
        super.render();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
    }
}
