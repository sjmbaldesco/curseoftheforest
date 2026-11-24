package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.ForestGame;
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
    static final float CHARS_PER_SECOND = 40f;

    // Layout from the UI spec (mockups 01 and 10), in window pixels from the top-left: under the
    // HUD bar, above where Dayo walks.
    static final float BOX_X = 16f;
    static final float BOX_TOP = 84f;
    static final float BOX_W = 928f;
    static final float BOX_H = 136f;
    /** Taller when answer buttons sit inside it. */
    static final float BOX_H_WITH_ANSWERS = 176f;
    static final float TEXT_X = 170f;
    private static final float BUTTON_W = 180f;
    private static final float BUTTON_H = 48f;
    private static final float BUTTON_GAP = 14f;

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

    /** Window y (from the bottom) of the box's lower edge, for whatever stacks under it. */
    public float bottomY() {
        return ForestGame.WORLD_HEIGHT - BOX_TOP - (choices.isEmpty() ? BOX_H : BOX_H_WITH_ANSWERS);
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!open) {
            return;
        }
        float height = choices.isEmpty() ? BOX_H : BOX_H_WITH_ANSWERS;
        drawFrame(batch, speaker, portrait, height);
        float top = ForestGame.WORLD_HEIGHT;
        BitmapFont talk = Theme.font(Theme.Size.TALK);
        talk.setColor(Theme.PAPER);
        talk.draw(batch, getVisibleText(), TEXT_X, top - 112, BOX_X + BOX_W - 24 - TEXT_X, Align.left, true);

        float boxBottom = top - BOX_TOP - height;
        if (!choices.isEmpty()) {
            float x = TEXT_X;
            float y = top - BOX_TOP - 100 - BUTTON_H;
            for (int i = 0; i < choices.size(); i++) {
                String label = (i + 1) + "  " + choices.get(i);
                float w = Math.max(BUTTON_W, Theme.width(talk, label) + 40);
                UiSkin.frame(batch, UiSkin.Frame.BUTTON, x, y, w, BUTTON_H);
                Theme.textCentred(batch, Theme.Size.TALK, label, x + w / 2, y + 34, Theme.PAPER);
                x += w + BUTTON_GAP;
            }
            Theme.textRight(batch, Theme.Size.SMALL, "[1-" + choices.size() + "] Answer", BOX_X + BOX_W - 20, boxBottom + 22, Theme.GOLD);
        } else if (blocking && !isTyping()) {
            Theme.textRight(batch, Theme.Size.BODY, "[E] continue", BOX_X + BOX_W - 40, boxBottom + 28, Theme.GOLD);
            if ((int) (blinkTime * 2) % 2 == 0) {
                UiSkin.triangle(batch, Theme.GOLD, BOX_X + BOX_W - 34, boxBottom + 24);
            }
        }
        font.setColor(Theme.PAPER);
    }

    /**
     * The empty talk box: wooden frame, portrait in its own frame and the speaker's name tag on
     * the top edge. Shared with stages that write their own text inside it (Lola's colours).
     * {@code height} is the box height in window pixels; it hangs from 84 px below the top.
     */
    public static void drawFrame(SpriteBatch batch, String speaker, TextureRegion portrait, float height) {
        float top = ForestGame.WORLD_HEIGHT;
        UiSkin.frame(batch, UiSkin.Frame.TALKBOX, BOX_X, top - BOX_TOP - height, BOX_W, height);
        if (portrait != null) {
            UiSkin.frame(batch, UiSkin.Frame.PORTRAIT, 26, top - 92 - 120, 120, 120);
            batch.draw(portrait, 38, top - 92 - 108, 96, 96);
        }
        if (speaker != null && !speaker.isEmpty()) {
            float width = Theme.width(Theme.font(Theme.Size.TALK), speaker) + 40;
            UiSkin.frame(batch, UiSkin.Frame.NAMETAG, 176, top - 66 - 36, width, 36);
            BitmapFont talk = Theme.font(Theme.Size.TALK);
            talk.setColor(Theme.NIGHT);
            talk.draw(batch, speaker, 196, top - 74);
        }
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
