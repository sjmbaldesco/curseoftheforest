package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.ForestGame;

/** Small helper for drawing centered text on the menu and game-over screens. */
final class MenuText {
    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private MenuText() {
    }

    static void begin(SpriteBatch batch) {
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
    }

    static void centered(SpriteBatch batch, BitmapFont font, String text, float y, float scale) {
        font.getData().setScale(scale);
        LAYOUT.setText(font, text);
        font.draw(batch, LAYOUT, (ForestGame.WORLD_WIDTH - LAYOUT.width) / 2, y);
        font.getData().setScale(1f);
    }
}
