package com.cupids.curseoftheforest.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.cupids.curseoftheforest.DisplayToggle;
import org.lwjgl.glfw.GLFW;

/**
 * Borderless full screen: the window loses its title bar and grows to cover the whole monitor it
 * is on. Unlike exclusive fullscreen, Alt+Tab and screen recording work smoothly. Pressing F11
 * again puts the window back where it was, at its old size.
 */
public class BorderlessToggle implements DisplayToggle {
    private boolean borderless;
    private final int[] savedX = new int[1];
    private final int[] savedY = new int[1];
    private final int[] savedWidth = new int[1];
    private final int[] savedHeight = new int[1];

    @Override
    public void toggleFullscreen() {
        long window = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        if (!borderless) {
            GLFW.glfwGetWindowPos(window, savedX, savedY);
            GLFW.glfwGetWindowSize(window, savedWidth, savedHeight);
            Graphics.Monitor monitor = Gdx.graphics.getMonitor();
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode(monitor);
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowMonitor(window, 0, monitor.virtualX, monitor.virtualY,
                    mode.width, mode.height, GLFW.GLFW_DONT_CARE);
        } else {
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowMonitor(window, 0, savedX[0], savedY[0],
                    savedWidth[0], savedHeight[0], GLFW.GLFW_DONT_CARE);
        }
        borderless = !borderless;
    }
}
