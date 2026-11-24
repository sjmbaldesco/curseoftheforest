package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * A small "[E] Talk" tag that floats above whatever Dayo can use, in place of a sentence of
 * instructions at the bottom of the screen. The stage says where (in window pixels) each frame.
 * It bobs 2 px up and down every 0.6 s so the eye finds it.
 */
public final class KeyPrompt {
    private static final float KEY_SIZE = 28f;
    private static final float LABEL_HEIGHT = 24f;

    private KeyPrompt() {
    }

    /** Draws the tag centred on x, with its bottom at y. The batch must be started. */
    public static void draw(SpriteBatch batch, BitmapFont font, String key, String action, float x, float y) {
        float labelWidth = Theme.width(Theme.font(Theme.Size.BODY), action) + 20;
        float width = KEY_SIZE + 4 + labelWidth;
        float left = Math.round((x - width / 2) / 2) * 2;
        float bottom = Math.round(y / 2) * 2 + bob(TimeUtils.millis());
        keyCap(batch, key, left, bottom);
        UiSkin.frame(batch, UiSkin.Frame.TOAST, left + KEY_SIZE + 4, bottom + 2, labelWidth, LABEL_HEIGHT);
        Theme.text(batch, Theme.Size.BODY, action, left + KEY_SIZE + 14, bottom + 20, Theme.PAPER);
    }

    /** 0 or 2: the tag's lift, switching every 0.6 s. */
    static int bob(long millis) {
        return (millis / 600) % 2 == 0 ? 0 : 2;
    }

    /** A keyboard key: the keycap frame with a dark letter; (x, y) is its bottom-left. */
    public static void keyCap(SpriteBatch batch, String key, float x, float y) {
        float width = Math.max(KEY_SIZE, Theme.width(Theme.font(Theme.Size.BODY), key) + 14);
        UiSkin.frame(batch, UiSkin.Frame.KEYCAP, x, y, width, KEY_SIZE);
        BitmapFont font = Theme.font(Theme.Size.BODY);
        font.setColor(Theme.NIGHT);
        font.draw(batch, key, Math.round(x + width / 2 - Theme.width(font, key) / 2), y + 22);
    }
}
