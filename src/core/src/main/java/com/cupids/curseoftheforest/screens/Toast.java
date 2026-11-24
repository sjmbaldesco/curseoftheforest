package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.ForestGame;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Short notices ("+1 coin", "Wrong offering") that appear under the HUD and fade away on their
 * own, so the player is told what happened without a panel covering the level.
 */
public class Toast {
    /** Seconds a notice stays on screen, including its fade. */
    static final float SHOW_SECONDS = 2.6f;
    private static final float FADE_SECONDS = 0.6f;
    private static final int MAX_SHOWN = 3;
    private static final GlyphLayout LAYOUT = new GlyphLayout();

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

    /** Draws the notices stacked from {@code topY} downward, centred. The batch must be started. */
    public void render(SpriteBatch batch, BitmapFont font, float topY) {
        Color previous = new Color(font.getColor());
        float y = topY;
        for (Notice notice : notices) {
            float alpha = Math.min(1f, (SHOW_SECONDS - notice.age) / FADE_SECONDS);
            LAYOUT.setText(font, notice.text);
            float width = LAYOUT.width + 28;
            float x = (ForestGame.WORLD_WIDTH - width) / 2;
            Theme.rect(batch, new Color(0f, 0f, 0f, 0.6f * alpha), x, y - 30, width, 30);
            Color ink = notice.warning ? Theme.WARNING : Theme.PAPER;
            font.setColor(ink.r, ink.g, ink.b, alpha);
            font.draw(batch, notice.text, x + 14, y - 8);
            y -= 34;
        }
        font.setColor(previous);
    }
}
