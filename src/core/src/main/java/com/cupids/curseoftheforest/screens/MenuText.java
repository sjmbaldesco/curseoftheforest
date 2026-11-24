package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.TimeUtils;
import com.cupids.curseoftheforest.ForestGame;

/** Small helpers shared by the menu-style screens (main menu, game over, the end). */
public final class MenuText {
    private static final Matrix4 SCREEN = new Matrix4().setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);

    private MenuText() {
    }

    /** Starts the batch in window coordinates (960 by 540). */
    public static void begin(SpriteBatch batch) {
        batch.setProjectionMatrix(SCREEN);
        batch.begin();
    }

    /**
     * A wooden menu button centred on the window, with its top edge {@code top} px from the top
     * of the window. The selected one has a gold frame and a pointer either side that bobs
     * 2 px every 0.4 s.
     */
    public static void button(SpriteBatch batch, String label, float top, float width, boolean selected) {
        float height = 48;
        float x = (ForestGame.WORLD_WIDTH - width) / 2;
        float y = ForestGame.WORLD_HEIGHT - top - height;
        UiSkin.frame(batch, selected ? UiSkin.Frame.BUTTON_SELECTED : UiSkin.Frame.BUTTON, x, y, width, height);
        Theme.textCentred(batch, Theme.Size.TALK, label, ForestGame.WORLD_WIDTH / 2f, y + 34,
                selected ? Theme.GOLD_LIGHT : Theme.PAPER);
        if (selected) {
            int bob = (TimeUtils.millis() / 400) % 2 == 0 ? 0 : 2;
            float pointerY = y + height / 2 - 7;
            UiSkin.image(batch, "pointer", x - 30 - 12 + bob, pointerY);
            UiSkin.imageFlipped(batch, "pointer", x + width + 30 - bob, pointerY);
        }
    }
}
