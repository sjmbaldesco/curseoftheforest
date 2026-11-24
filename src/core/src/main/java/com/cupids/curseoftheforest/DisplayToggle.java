package com.cupids.curseoftheforest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

/**
 * Switches the game between a window and full screen (F11). The desktop launcher passes in a
 * borderless version; {@link #standard()} is the plain libGDX fullscreen, used when none is given.
 */
public interface DisplayToggle {
    void toggleFullscreen();

    /** libGDX's own fullscreen at the monitor's current resolution, and back to a 960 by 540 window. */
    static DisplayToggle standard() {
        return () -> {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
            } else {
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode(Gdx.graphics.getMonitor());
                Gdx.graphics.setFullscreenMode(mode);
            }
        };
    }
}
