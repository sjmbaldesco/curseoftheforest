package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;

/**
 * The talking box at the top of the screen: a portrait, the speaker's name and text that types
 * itself out. It replaces the old hint text that sat on top of the platforms.
 *
 * <p>Two kinds of use:
 * <ul>
 *   <li>{@link #say} plays a conversation. It is <em>blocking</em>: the stage pauses until the
 *       player has read every line (E, Enter or Space).</li>
 *   <li>{@link #ask} shows a question with numbered answers. It is not blocking: the game keeps
 *       running (timers, drains) and the stage reads the number keys itself.</li>
 * </ul>
 *
 * <p>All state can be tested without graphics; only {@link #render} needs a batch.
 */
public class DialogueBox {
    /** How fast the text types out, in characters per second. */
    static final float CHARS_PER_SECOND = 45f;

    // Layout in window pixels (960 by 540): under the HUD bar, above where Dayo walks.
    private static final float BOX_X = 16f;
    private static final float BOX_Y = 330f;
    private static final float BOX_W = 928f;
    private static final float BOX_H = 136f;
    private static final float PORTRAIT_SIZE = 128f;

    private final List<String> lines = new ArrayList<>();
    private final List<String> choices = new ArrayList<>();
    private String speaker = "";
    private TextureRegion portrait;
    private int index;
    private float shownChars;
    private boolean open;
    private boolean blocking;
    private float blinkTime;
    private Runnable onClose;

    /** Starts a conversation; the stage pauses until the last line is dismissed. */
    public void say(String speaker, TextureRegion portrait, String... lines) {
        if (lines == null || lines.length == 0) {
            throw new IllegalArgumentException("A conversation needs at least one line.");
        }
        start(speaker, portrait, true);
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("Dialogue cannot be blank.");
            }
            this.lines.add(line);
        }
    }

    /** Shows a question with numbered answers; the game keeps running while it is up. */
    public void ask(String speaker, TextureRegion portrait, String question, String... answers) {
        start(speaker, portrait, false);
        lines.add(question);
        for (String answer : answers) {
            choices.add(answer);
        }
    }

    private void start(String speaker, TextureRegion portrait, boolean blocking) {
        this.speaker = speaker == null ? "" : speaker;
        this.portrait = portrait;
        this.blocking = blocking;
        lines.clear();
        choices.clear();
        index = 0;
        shownChars = 0;
        open = true;
    }

    /** Runs once, when a conversation's last line is dismissed (or the box is closed). */
    public void setOnClose(Runnable action) {
        onClose = action;
    }

    public void update(float delta) {
        blinkTime = (blinkTime + delta) % 1000f;
        if (open) {
            shownChars = Math.min(shownChars + CHARS_PER_SECOND * delta, getCurrentLine().length());
        }
    }

    /**
     * The player pressed continue: finish typing the line, or go to the next line, or close the
     * box after the last line.
     */
    public void advance() {
        if (!open) {
            return;
        }
        if (isTyping()) {
            shownChars = getCurrentLine().length();
            return;
        }
        index++;
        shownChars = 0;
        if (index >= lines.size()) {
            close();
        }
    }

    public void close() {
        if (!open) {
            return;
        }
        open = false;
        lines.clear();
        choices.clear();
        Runnable action = onClose;
        onClose = null;
        if (action != null) {
            action.run();
        }
    }

    public boolean isOpen() {
        return open;
    }

    /** True while a conversation is up; the stage then pauses everything but this box. */
    public boolean isBlocking() {
        return open && blocking;
    }

    public boolean isTyping() {
        return open && shownChars < getCurrentLine().length();
    }

    public String getCurrentLine() {
        return open && index < lines.size() ? lines.get(index) : "";
    }

    public String getSpeaker() {
        return speaker;
    }

    /** The part of the current line typed out so far. */
    public String getVisibleText() {
        String line = getCurrentLine();
        return line.substring(0, Math.min(line.length(), (int) shownChars));
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!open) {
            return;
        }
        Color previous = new Color(font.getColor());
        Theme.panel(batch, BOX_X, BOX_Y, BOX_W, BOX_H);

        float textX = BOX_X + 20;
        if (portrait != null) {
            Theme.rect(batch, Theme.INK, BOX_X + 4, BOX_Y + 4, PORTRAIT_SIZE, PORTRAIT_SIZE);
            batch.draw(portrait, BOX_X + 4, BOX_Y + 4, PORTRAIT_SIZE, PORTRAIT_SIZE);
            textX = BOX_X + PORTRAIT_SIZE + 24;
        }
        float textWidth = BOX_X + BOX_W - 20 - textX;

        if (!speaker.isEmpty()) {
            // Name tag sitting on the box's top edge.
            float tagWidth = Math.max(120f, speaker.length() * 13f + 28f);
            Theme.rect(batch, Theme.INK, textX - 8, BOX_Y + BOX_H - 14, tagWidth, 30);
            font.setColor(Theme.GOLD);
            font.draw(batch, speaker, textX + 4, BOX_Y + BOX_H + 10);
        }

        font.setColor(Theme.INK);
        font.draw(batch, getVisibleText(), textX, BOX_Y + BOX_H - 26, textWidth, Align.left, true);

        if (!choices.isEmpty()) {
            float y = BOX_Y + 52;
            float x = textX;
            for (int i = 0; i < choices.size(); i++) {
                String label = (i + 1) + "  " + choices.get(i);
                KeyPrompt.keyCap(batch, font, String.valueOf(i + 1), x, y - 20);
                font.setColor(Theme.INK);
                font.draw(batch, choices.get(i), x + 30, y);
                x += Math.max(150f, label.length() * 12f + 40f);
            }
        } else if (blocking && !isTyping() && (int) (blinkTime * 2.5f) % 2 == 0) {
            font.setColor(Theme.LEAF);
            font.draw(batch, "E  continue", BOX_X + BOX_W - 150, BOX_Y + 28);
        }
        font.setColor(previous);
    }

    /**
     * A head-and-shoulders crop of the first frame of a 64-pixel sprite strip, for use as a
     * portrait (drawn 4x, so it stays crisp). A strip of another size is used whole.
     */
    public static TextureRegion portraitFrom(Texture strip, int frames) {
        if (strip == null) {
            return null;
        }
        if (strip.getHeight() == 64 && strip.getWidth() == 64 * frames) {
            return new TextureRegion(strip, 16, 6, 32, 32);
        }
        return new TextureRegion(strip);
    }
}
