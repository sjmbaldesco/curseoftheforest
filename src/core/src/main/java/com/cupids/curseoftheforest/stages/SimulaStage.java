package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.ForestGame;

/** Stage 1: repeat the color sequence before leaving the map. */
public class SimulaStage extends GameStage {
    private static final int[] PATTERN = {1, 3, 2};
    private static final String[] COLORS = {"Red", "Green", "Blue"};
    private static final Color[] SWATCHES = {
            Color.valueOf("C83E46"), Color.valueOf("318255"), Color.valueOf("396EB8")
    };
    private static final Color BUBBLE_INK = Color.valueOf("23382E");
    private static final Color BUBBLE_PAPER = Color.valueOf("FFF3D8");
    private static final float SECONDS_PER_COLOR = 1.2f;
    private final Color previousFontColor = new Color();
    private Texture bubblePixel;
    private float previewTime;
    private int matched;
    private boolean solved;
    private String feedback;

    public SimulaStage(ForestGame game) {
        super(game, "Stage 1: Ang Simula", "maps/test.tmx");
    }

    @Override
    protected void init() {
        previewTime = 0f;
        matched = 0;
        solved = false;
        feedback = "";
    }

    @Override
    public void update(float delta) {
        if (isPreviewing()) {
            previewTime += delta;
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
        feedback = "";
    }

    private boolean isPreviewing() {
        return previewTime < PATTERN.length * SECONDS_PER_COLOR;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (game.getScreen() != this) {
            return;
        }

        // The shared HUD leaves the batch set to screen coordinates.
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        if (bubblePixel == null) {
            Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            try {
                pixel.setColor(Color.WHITE);
                pixel.fill();
                bubblePixel = new Texture(pixel);
            } finally {
                pixel.dispose();
            }
        }

        float left = 20f;
        float top = ForestGame.WORLD_HEIGHT - 62f;
        float bottom = top - 116f;
        float width = 520f;
        float previousBatchColor = batch.getPackedColor();
        previousFontColor.set(font.getColor());
        float scaleX = font.getData().scaleX;
        float scaleY = font.getData().scaleY;
        batch.begin();
        batch.setColor(BUBBLE_INK);
        batch.draw(bubblePixel, left + 24, bottom - 9, 10, 10, 20, 20, 1, 1, 45, 0, 0, 1, 1, false, false);
        batch.draw(bubblePixel, left, bottom, width, 116);
        batch.setColor(BUBBLE_PAPER);
        batch.draw(bubblePixel, left + 26, bottom - 7, 8, 8, 16, 16, 1, 1, 45, 0, 0, 1, 1, false, false);
        batch.draw(bubblePixel, left + 2, bottom + 2, width - 4, 112);
        batch.setColor(Color.WHITE);

        float textLeft = left + 16;
        font.setColor(BUBBLE_INK);
        font.getData().setScale(1.4f);
        font.draw(batch, "Lola Pacita", textLeft, top - 12);
        font.getData().setScale(1.15f);
        if (isPreviewing()) {
            int index = (int) (previewTime / SECONDS_PER_COLOR);
            int colorIndex = PATTERN[index] - 1;
            font.draw(batch, "Dayo, remember these colors.", textLeft, top - 40);
            batch.setColor(SWATCHES[colorIndex]);
            batch.draw(bubblePixel, textLeft, top - 88, 23, 23);
            batch.setColor(Color.WHITE);
            font.draw(batch, (index + 1) + "/" + PATTERN.length + "  "
                    + COLORS[colorIndex], textLeft + 33, top - 67);
        } else if (solved) {
            font.draw(batch, "Well done, Dayo! You may head to the exit.", textLeft, top - 42);
        } else {
            font.draw(batch, "Your turn. Repeat the colors in order.", textLeft, top - 39);
            font.draw(batch, "1 Red     2 Green     3 Blue     E Replay", textLeft, top - 63);
            font.draw(batch, matched + "/" + PATTERN.length + " correct. " + feedback, textLeft, top - 87);
        }
        batch.end();
        batch.setPackedColor(previousBatchColor);
        font.getData().setScale(scaleX, scaleY);
        font.setColor(previousFontColor);
    }

    @Override
    public void dispose() {
        if (bubblePixel != null) {
            bubblePixel.dispose();
        }
        super.dispose();
    }
}
