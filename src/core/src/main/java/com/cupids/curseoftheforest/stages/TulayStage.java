package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.ForestGame;

/**
 * Stage 4: Tulay ng Nuno sa Punso. Three timed riddles answered with food offerings; the Nuno gives three potions.
 *
 * <p>Placeholder: uses the shared test map and completes when Dayo reaches the exit.
 * TODO(Johndel): stage mechanics.
 */
public class TulayStage extends GameStage {

    public TulayStage(ForestGame game) {
        super(game, "Stage 4: Tulay ng Nuno sa Punso", "maps/test.tmx");
    }

    @Override
    protected void init() {
        timer = new StageTimer(180f); // TODO(Johndel): 300 s if Dayo has the Malunggay Juice.
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
