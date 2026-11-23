package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.cupids.curseoftheforest.ForestGame;

/** Small helper for drawing centered text on the menu and game-over screens. */
public final class MenuText {
    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private MenuText() {
    }

    public static void begin(SpriteBatch batch) {
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
    }

    public static void centered(SpriteBatch batch, BitmapFont font, String text, float y, float scale) {
        float previousX = font.getData().scaleX;
        float previousY = font.getData().scaleY;
        font.getData().setScale(scale);
        LAYOUT.setText(font, text);
        font.draw(batch, LAYOUT, (ForestGame.WORLD_WIDTH - LAYOUT.width) / 2, y);
        font.getData().setScale(previousX, previousY);
    }

    /** Same as {@link #centered} but in the given colour, with a dark drop shadow for readability. */
    public static void centered(SpriteBatch batch, BitmapFont font, String text, float y, float scale, Color color) {
        Color previous = new Color(font.getColor());
        font.setColor(Theme.INK);
        centeredAt(batch, font, text, y - 2 * scale, scale, 2 * scale);
        font.setColor(color);
        centered(batch, font, text, y, scale);
        font.setColor(previous);
    }

    private static void centeredAt(SpriteBatch batch, BitmapFont font, String text, float y, float scale, float dx) {
        float previousX = font.getData().scaleX;
        float previousY = font.getData().scaleY;
        font.getData().setScale(scale);
        LAYOUT.setText(font, text);
        font.draw(batch, LAYOUT, (ForestGame.WORLD_WIDTH - LAYOUT.width) / 2 + dx, y);
        font.getData().setScale(previousX, previousY);
    }
}
