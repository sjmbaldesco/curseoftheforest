package com.cupids.curseoftheforest.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.cupids.curseoftheforest.ForestGame;

/**
 * Desktop entry point. Opens the game window and hands control to {@link ForestGame}. The window
 * can be resized or maximized, and F11 (or Alt+Enter) switches to borderless full screen; the game
 * keeps its 16:9 shape with black bars where needed. Start with {@code --fullscreen} to begin
 * full screen.
 */
public class Main {
    public static final String TITLE = "Curse of the Forest";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(TITLE);
        config.setWindowedMode(WIDTH, HEIGHT);
        config.setResizable(true);
        config.setWindowSizeLimits(WIDTH / 2, HEIGHT / 2, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);
        boolean startFullscreen = java.util.Arrays.asList(args).contains("--fullscreen");
        BorderlessToggle toggle = new BorderlessToggle();
        new Lwjgl3Application(new ForestGame(toggle) {
            @Override
            public void create() {
                super.create();
                if (startFullscreen) {
                    toggle.toggleFullscreen();
                }
            }
        }, config);
    }
}
