package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.ForestGame;

/**
 * Stage 5: Ang Tukso ng Mangkukulam. Flashback, potion duel against the Mangkukulam, serenade.
 *
 * <p>Placeholder: uses the shared test map and completes when Dayo reaches the exit.
 * TODO(Johndel): stage mechanics.
 */
public class MangkukulamStage extends GameStage {

    public MangkukulamStage(ForestGame game) {
        super(game, "Stage 5: Ang Tukso ng Mangkukulam", "maps/test.tmx");
    }

    @Override
    protected void init() {
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
