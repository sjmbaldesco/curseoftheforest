package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.Objects;

/** Dialogue state can be tested without graphics; drawing resources are allocated on show. */
public class CutScene extends ScreenAdapter {
    private final String[] lines;
    private int currentIndex;
    private final Texture background;
    private SpriteBatch batch;
    private BitmapFont font;

    public CutScene(String[] lines, Texture background) {
        this.lines = Objects.requireNonNull(lines, "lines").clone();
        for (String line : this.lines) {
            if (line == null || line.isBlank()) throw new IllegalArgumentException("Dialogue cannot be blank.");
        }
        this.background = background;
    }

    public void advance() { if (!isFinished()) currentIndex++; }
    public String getCurrentLine() { return isFinished() ? "" : lines[currentIndex]; }

    @Override
    public void show() {
        if (batch == null) { batch = new SpriteBatch(); font = new BitmapFont(); }
    }

    @Override
    public void render(float delta) {
        if (batch == null) show();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) advance();
        ScreenUtils.clear(0.035f, 0.055f, 0.07f, 1);
        MenuText.begin(batch);
        if (background != null) batch.draw(background, 0, 180, 960, 360);
        font.setColor(Color.WHITE);
        font.draw(batch, getCurrentLine(), 48, 145, 864, Align.left, true);
        font.draw(batch, isFinished() ? "Scene complete" : "Enter / Space: continue", 48, 40);
        batch.end();
    }

    public boolean isFinished() { return currentIndex >= lines.length; }

    @Override
    public void dispose() {
        if (batch != null) { batch.dispose(); font.dispose(); batch = null; font = null; }
        // Background is shared and disposed by AssetLoader.
    }

    /** Draft story text for team review, returned as fresh arrays. */
    public static String[] intro() {
        return new String[] {
            "Dayo: Tonight I will visit Luningning and tell her how I feel.",
            "Lola Pacita: The forest tests more than bravery. Listen, be respectful, and keep your wits.",
            "Dayo takes his guitar and follows the path into the forest."
        };
    }

    public static String[] flashbackOne() {
        return new String[] {
            "Dayo remembers Lola Pacita's advice: ask permission before crossing another's home.",
            "Dayo: Tabi-tabi po. I mean no harm."
        };
    }

    public static String[] flashbackTwo() {
        return new String[] {
            "Dayo remembers practicing his song beneath Luningning's window.",
            "Dayo: Courage is choosing to be honest, even when I am afraid."
        };
    }

    public static String[] serenade() {
        return serenade(false);
    }

    public static String[] serenade(boolean hasGuitarStrings) {
        if (hasGuitarStrings) {
            return new String[] {
                "Dayo lifts his guitar and sings the song he practiced.",
                "Dayo: Luningning, may I court you? Whatever your answer, I will respect it.",
                "Luningning: Your guitar sounds beautiful, Dayo. I can tell how much you practiced."
            };
        }
        return new String[] {
            "Dayo lifts his guitar and sings the song he practiced.",
            "Dayo: Luningning, may I court you? Whatever your answer, I will respect it."
        };
    }

    public static String[] ending() {
        return new String[] {
            "Luningning: Yes, Dayo. Let us get to know each other.",
            "The forest grows quiet. Dayo's journey ends with a promise to listen and care."
        };
    }
}
