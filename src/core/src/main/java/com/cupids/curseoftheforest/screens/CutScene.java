package com.cupids.curseoftheforest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Dialogue state can be tested without graphics; drawing resources are allocated on show. */
public class CutScene extends ScreenAdapter {
    private final String[] lines;
    private final Map<String, TextureRegion> portraits = new LinkedHashMap<>();
    private final Map<String, Texture> characters = new LinkedHashMap<>();
    private int currentIndex;
    private final Texture background;
    private SpriteBatch batch;
    private BitmapFont font;
    private Runnable onFinished;
    private float blink;

    public CutScene(String[] lines, Texture background) {
        this.lines = Objects.requireNonNull(lines, "lines").clone();
        for (String line : this.lines) {
            if (line == null || line.isBlank()) throw new IllegalArgumentException("Dialogue cannot be blank.");
        }
        this.background = background;
    }

    public void advance() { if (!isFinished()) currentIndex++; }

    /**
     * Runs {@code action} once, on the frame the last line is dismissed. The scene draws nothing
     * after that, so the action may switch screens and dispose this scene.
     */
    public void setOnFinished(Runnable action) { onFinished = action; }
    /** Associates a speaker name with the portrait shown on that speaker's dialogue lines. */
    public void setPortrait(String speaker, TextureRegion portrait) {
        speaker = Objects.requireNonNull(speaker, "speaker");
        portrait = Objects.requireNonNull(portrait, "portrait");
        portraits.put(speaker, portrait);
        Texture texture = portrait.getTexture();
        if (texture.getHeight() == 64 && texture.getWidth() >= 64) {
            characters.put(speaker, texture);
        }
    }
    public String getCurrentLine() { return isFinished() ? "" : lines[currentIndex]; }

    @Override
    public void show() {
        if (batch == null) { batch = new SpriteBatch(); font = Theme.font(Theme.Size.TALK); }
    }

    @Override
    public void render(float delta) {
        if (batch == null) show();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            currentIndex = lines.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            advance();
        }
        if (isFinished() && onFinished != null) {
            Runnable action = onFinished;
            onFinished = null;
            action.run();
            return;
        }
        blink = (blink + delta) % 1000f;
        ScreenUtils.clear(Theme.NIGHT);
        MenuText.begin(batch);
        if (background != null) batch.draw(background, 0, 0, 960, 540);
        String line = getCurrentLine();
        drawCharacters(batch, speakerFor(line));
        // Caption in the talk-box frame along the bottom (mockup 11).
        UiSkin.frame(batch, UiSkin.Frame.TALKBOX, 40, 32, 880, 116);
        UiSkin.frame(batch, UiSkin.Frame.PANEL, 758, 494, 184, 30);
        Theme.textCentred(batch, Theme.Size.SMALL, "[ESC] Skip", 850, 514, Theme.GOLD);
        TextureRegion portrait = portraits.get(speakerFor(line));
        float textX = 72;
        float textWidth = 816;
        if (portrait != null) {
            UiSkin.frame(batch, UiSkin.Frame.PORTRAIT, 52, 50, 80, 80);
            batch.draw(portrait, 60, 58, 64, 64);
            textX = 152;
            textWidth = 736;
        }
        font.setColor(Theme.PAPER);
        font.draw(batch, line, textX, 130, textWidth, Align.left, true);
        Theme.textRight(batch, Theme.Size.BODY, "[ENTER] continue", 880, 58, Theme.GOLD);
        if ((int) (blink * 2) % 2 == 0) {
            UiSkin.triangle(batch, Theme.GOLD, 888, 52);
        }
        batch.end();
    }

    private void drawCharacters(SpriteBatch batch, String speaker) {
        Texture left = characters.get(speaker);
        String leftName = speaker;
        if (left == null && !characters.isEmpty()) {
            Map.Entry<String, Texture> first = characters.entrySet().iterator().next();
            leftName = first.getKey();
            left = first.getValue();
        }

        Texture right = null;
        for (Map.Entry<String, Texture> entry : characters.entrySet()) {
            if (!entry.getKey().equals(leftName)) {
                right = entry.getValue();
                break;
            }
        }
        if (left != null) {
            batch.draw(left, 0, 32, 320, 320, 0, 0, 64, 64, true, false);
        }
        if (right != null) {
            batch.draw(right, 640, 32, 320, 320, 0, 0, 64, 64, false, false);
        }
    }

    public boolean isFinished() { return currentIndex >= lines.length; }

    private static String speakerFor(String line) {
        int separator = line.indexOf(':');
        return separator > 0 ? line.substring(0, separator).trim() : "";
    }

    @Override
    public void dispose() {
        if (batch != null) { batch.dispose(); batch = null; font = null; } // the font belongs to Theme
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
