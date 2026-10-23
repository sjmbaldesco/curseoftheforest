package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.ForestGame;

/**
 * Stage 3: Ang Gubat ng Tikbalang. Timed search of glowing bushes for the clue pieces.
 *
 * <p>Placeholder: uses the shared test map and completes when Dayo reaches the exit.
 * TODO(Johndel): stage mechanics.
 */
public class GubatStage extends GameStage {

    public GubatStage(ForestGame game) {
        super(game, "Stage 3: Ang Gubat ng Tikbalang", "maps/test.tmx");
    }

    @Override
    protected void init() {
        timer = new StageTimer(90f); // TODO(Johndel): 180 s if Dayo has the Agimat.
    }

    @Override
    public void update(float delta) {
        if (reachedExit()) {
            completed = true;
        }
    }

    @Override
    protected void handleInput() {
    }
}
