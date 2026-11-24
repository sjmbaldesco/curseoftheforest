package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.LolaPacita;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.Theme;

/** Stage 1: repeat the color sequence before leaving the map. */
public class SimulaStage extends GameStage {
    private static final int[] PATTERN = {1, 3, 2};
    private static final String[] COLORS = {"Red", "Green", "Blue"};
    private static final Color[] SWATCHES = {
            Color.valueOf("C83E46"), Color.valueOf("318255"), Color.valueOf("396EB8")
    };
    private static final float SECONDS_PER_COLOR = 1.2f;
    private final LolaPacita lolaPacita = new LolaPacita(0, 0);
    private TextureRegion lolaPortrait;
    private float previewTime;
    private int matched;
    private boolean solved;
    private String feedback;

    public SimulaStage(ForestGame game) {
        super(game, "Stage 1: Ang Simula", "maps/simula.tmx");
    }

    @Override
    protected void init() {
        previewTime = 0f;
        matched = 0;
        solved = false;
        feedback = "";
        setGoal("Repeat Lola's colours");
    }

    @Override
    public void update(float delta) {
        if (isPreviewing()) {
            previewTime += delta;
        }
        if (solved) {
            setGoal("Head to the exit");
        }
        if (solved && reachedExit()) {
            completed = true;
        }
    }

    @Override
    protected void handleInput() {
        if (solved || isPreviewing()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            init();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            checkColor(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            checkColor(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            checkColor(3);
        }
    }

    private void checkColor(int color) {
        if (color != PATTERN[matched]) {
            matched = 0;
            feedback = "Wrong color. Start again from the first color.";
            return;
        }

        matched++;
        solved = matched == PATTERN.length;
        if (solved) {
            lolaPacita.interact(player);
        }
        feedback = "";
    }

    private boolean isPreviewing() {
        return previewTime < PATTERN.length * SECONDS_PER_COLOR;
    }

    /**
     * Lola's lesson in the shared talk box (mockup 01): her portrait and name, the colour being
     * shown with its swatch, then the player's turn with the keys to press.
     */
    @Override
    protected void drawOverlay(SpriteBatch batch) {
        if (lolaPortrait == null) {
            lolaPortrait = DialogueBox.portraitFrom(game.getAssetLoader().loadImage("images/lola-pacita.png"), 5);
        }
        DialogueBox.drawFrame(batch, lolaPacita.getName(), lolaPortrait, 136);
        float x = 170;
        float top = ForestGame.WORLD_HEIGHT - 112;
        if (isPreviewing()) {
            int index = (int) (previewTime / SECONDS_PER_COLOR);
            int colorIndex = PATTERN[index] - 1;
            Theme.text(batch, Theme.Size.TALK, "Dayo, remember these colors.", x, top, Theme.PAPER);
            Theme.rect(batch, Theme.NIGHT, x, top - 54, 24, 24);
            Theme.rect(batch, SWATCHES[colorIndex], x + 2, top - 52, 20, 20);
            Theme.text(batch, Theme.Size.TALK, (index + 1) + "/" + PATTERN.length + "  " + COLORS[colorIndex], x + 36, top - 30, Theme.PAPER);
        } else if (solved) {
            Theme.text(batch, Theme.Size.TALK, "Well done, Dayo! Take this Rosary and Lumang Gitara.", x, top, Theme.PAPER);
            Theme.text(batch, Theme.Size.BODY, "Both are in your item bar. Head to the exit.", x, top - 34, Theme.GOLD);
        } else {
            Theme.text(batch, Theme.Size.TALK, "Your turn. Repeat the colors in order.", x, top, Theme.PAPER);
            float keyX = x;
            for (int i = 0; i < COLORS.length; i++) {
                Theme.text(batch, Theme.Size.TALK, "[" + (i + 1) + "]", keyX, top - 30, Theme.GOLD);
                Theme.rect(batch, Theme.NIGHT, keyX + 44, top - 52, 20, 20);
                Theme.rect(batch, SWATCHES[i], keyX + 46, top - 50, 16, 16);
                Theme.text(batch, Theme.Size.TALK, COLORS[i], keyX + 72, top - 30, Theme.PAPER);
                keyX += 170;
            }
            String status = matched + "/" + PATTERN.length + " correct. " + feedback;
            Theme.text(batch, Theme.Size.SMALL, status, x, top - 66, feedback.isEmpty() ? Theme.MUTED : Theme.WARNING);
            Theme.textRight(batch, Theme.Size.SMALL, "[E] Watch again", 924, top - 66, Theme.GOLD);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
