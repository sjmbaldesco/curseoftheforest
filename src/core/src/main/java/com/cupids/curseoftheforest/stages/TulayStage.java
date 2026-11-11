package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Nuno;

/**
 * Stage 4: Tulay ng Nuno sa Punso. Three timed riddles answered with food offerings; the Nuno gives three potions.
 * Nuno waits at the exit marker on the shared test map.
 */
public class TulayStage extends GameStage {
    private static final float BASE_SECONDS = 180f;
    private static final float JUICE_SECONDS = 300f;
    private Nuno nuno;
    private String feedback;

    public TulayStage(ForestGame game) {
        super(game, "Stage 4: Tulay ng Nuno sa Punso", "maps/test.tmx");
    }

    @Override
    protected void init() {
        boolean hasJuice = player.getInventory().has("Malunggay Juice");
        timer = new StageTimer(hasJuice ? JUICE_SECONDS : BASE_SECONDS);
        Rectangle exit = tileMap.getExit();
        nuno = new Nuno(exit.x, exit.y);
        feedback = "";
    }

    @Override
    public void update(float delta) {
        completed = nuno.isComplete() && !isFailed();
    }

    @Override
    protected void handleInput() {
        if (!reachedExit() || isFailed()) {
            return;
        }
        if (!nuno.hasStarted()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                nuno.interact(player);
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            offer("Atis");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            offer("Sampalok");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            offer("Kalabasa");
        }
    }

    private void offer(String food) {
        feedback = nuno.offer(player, food)
                ? "Correct! You received a potion."
                : "That offering is incorrect. Try again.";
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
        float top = ForestGame.WORLD_HEIGHT - 64;
        batch.begin();
        if (!reachedExit()) {
            font.draw(batch, "Find Nuno at the exit marker.", 12, top);
        } else if (!nuno.hasStarted()) {
            font.draw(batch, "Press E to speak to Nuno.", 12, top);
        } else {
            font.draw(batch, "Nuno: " + nuno.getQuestion(), 12, top);
            font.draw(batch, "Offer food: 1 Atis    2 Sampalok    3 Kalabasa", 12, top - 22);
            font.draw(batch, feedback, 12, top - 44);
        }
        batch.end();
    }
}
