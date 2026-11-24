package com.cupids.curseoftheforest.screens;

/**
 * Where the 960 by 540 game sits inside a window or screen of any size: as big as fits without
 * stretching, centred, with black bars filling the rest. Every screen draws in 960 by 540
 * coordinates, so pointing OpenGL at this rectangle scales the whole game, UI included.
 */
public final class Letterbox {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private Letterbox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * The largest area with the game's shape that fits the screen. When the screen is a whole
     * multiple of the game (1920 by 1080 is exactly 2x), every pixel is drawn the same size.
     */
    public static Letterbox fit(int screenWidth, int screenHeight, int gameWidth, int gameHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return new Letterbox(0, 0, Math.max(0, screenWidth), Math.max(0, screenHeight));
        }
        float scale = Math.min(screenWidth / (float) gameWidth, screenHeight / (float) gameHeight);
        int width = Math.round(gameWidth * scale);
        int height = Math.round(gameHeight * scale);
        return new Letterbox((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }
}
