package com.cupids.curseoftheforest.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.cupids.curseoftheforest.ForestGame;

/** Desktop entry point. Opens the game window and hands control to {@link ForestGame}. */
public class Main {
    public static final String TITLE = "Curse of the Forest";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(TITLE);
        config.setWindowedMode(WIDTH, HEIGHT);
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new ForestGame(), config);
    }
}
