package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A small "[E] Talk" tag that floats above whatever Dayo can use, in place of a sentence of
 * instructions at the bottom of the screen. The stage says where (in window pixels) each frame.
 */
public final class KeyPrompt {
    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private KeyPrompt() {
    }

    /** Draws the tag centred on (x, y), with its bottom at y. The batch must be started. */
    public static void draw(SpriteBatch batch, BitmapFont font, String key, String action, float x, float y) {
        LAYOUT.setText(font, action);
        float width = 30 + 8 + LAYOUT.width + 12;
        float left = x - width / 2;
        Theme.rect(batch, Theme.SHADE, left, y, width, 30);
        keyCap(batch, font, key, left + 4, y + 3);
        Color previous = new Color(font.getColor());
        font.setColor(Theme.PAPER);
        font.draw(batch, action, left + 38, y + 22);
        font.setColor(previous);
    }

    /** A keyboard key drawn as a cream square with a dark letter; (x, y) is its bottom-left. */
    public static void keyCap(SpriteBatch batch, BitmapFont font, String key, float x, float y) {
        Theme.rect(batch, Theme.INK, x, y, 24, 24);
        Theme.rect(batch, Theme.PAPER, x + 2, y + 4, 20, 18);
        Color previous = new Color(font.getColor());
        font.setColor(Theme.INK);
        LAYOUT.setText(font, key);
        font.draw(batch, key, x + 12 - LAYOUT.width / 2, y + 19);
        font.setColor(previous);
    }
}
