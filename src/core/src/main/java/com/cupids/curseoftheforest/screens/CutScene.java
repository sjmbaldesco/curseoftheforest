package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;

/**
 * Narration, dialogue, flashback and ending screens.
 * TODO(Phebe): stub from the UML.
 */
public class CutScene extends ScreenAdapter {
    private final String[] lines;
    private int currentIndex;
    private final Texture background;

    public CutScene(String[] lines, Texture background) {
        this.lines = lines;
        this.background = background;
    }

    public void advance() {
        // TODO(Phebe): move to the next line.
    }

    @Override
    public void render(float delta) {
        // TODO(Phebe): draw the background and the current line.
    }

    public boolean isFinished() {
        return currentIndex >= lines.length;
    }
}
