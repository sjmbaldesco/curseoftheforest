package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Short notices ("+1 coin", "Wrong offering") that appear at the right under the HUD (or under the
 * talk box) and fade away on their own, so the player is told what happened without a panel
 * covering the level. Losses use the red warning frame.
 */
public class Toast {
    /** Fade in 0.2 s, hold 2 s, fade out 0.5 s (UI spec). */
    private static final float FADE_IN_SECONDS = 0.2f;
    private static final float FADE_SECONDS = 0.5f;
    /** Seconds a notice stays on screen, including its fades. */
    static final float SHOW_SECONDS = FADE_IN_SECONDS + 2f + FADE_SECONDS;
    private static final int MAX_SHOWN = 3;

    private final Deque<Notice> notices = new ArrayDeque<>();

    private static final class Notice {
        final String text;
        final boolean warning;
        float age;

        Notice(String text, boolean warning) {
            this.text = text;
            this.warning = warning;
        }
    }

    public void show(String text) {
        add(text, false);
    }

    /** A notice in the warning colour, for losses (Courage, Sanity, health). */
    public void warn(String text) {
        add(text, true);
    }

    private void add(String text, boolean warning) {
        if (text == null || text.isBlank()) {
            return;
        }
        // Repeating the newest notice just restarts it instead of stacking copies.
        if (!notices.isEmpty() && notices.peekLast().text.equals(text)) {
            notices.peekLast().age = 0;
            return;
        }
        notices.addLast(new Notice(text, warning));
        while (notices.size() > MAX_SHOWN) {
            notices.removeFirst();
        }
    }

    public void update(float delta) {
        for (Notice notice : notices) {
            notice.age += delta;
        }
        notices.removeIf(n -> n.age >= SHOW_SECONDS);
    }

    public int size() {
        return notices.size();
    }

    public String newest() {
        return notices.isEmpty() ? "" : notices.peekLast().text;
    }

    public void clear() {
        notices.clear();
    }

    /** How visible a notice is at an age: fades in, holds, fades out. */
    static float alpha(float age) {
        return Math.max(0f, Math.min(1f, Math.min(age / FADE_IN_SECONDS, (SHOW_SECONDS - age) / FADE_SECONDS)));
    }

    /**
     * Draws the notices right-aligned to x = 944, stacked downward from {@code topY} (window y of
     * the first notice's top edge). The batch must be started.
     */
    public void render(SpriteBatch batch, BitmapFont font, float topY) {
        float y = topY;
        for (Notice notice : notices) {
            float alpha = alpha(notice.age);
            float width = Math.round((Theme.width(Theme.font(Theme.Size.BODY), notice.text) + 28) / 2) * 2;
            float x = 944 - width;
            batch.setColor(1f, 1f, 1f, alpha);
            UiSkin.frame(batch, notice.warning ? UiSkin.Frame.TOAST_WARNING : UiSkin.Frame.TOAST, x, y - 32, width, 32);
            batch.setColor(Color.WHITE);
            Color ink = notice.warning ? Theme.WARNING : Theme.GOLD;
            BitmapFont body = Theme.font(Theme.Size.BODY);
            body.setColor(ink.r, ink.g, ink.b, alpha);
            body.draw(batch, notice.text, x + 14, y - 8);
            y -= 40;
        }
    }
}
