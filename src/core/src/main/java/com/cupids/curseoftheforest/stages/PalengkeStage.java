package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.ForestGame;

/**
 * Stage 2: Ang Palengke ng Duwende. Wakwak cave coin run, then Duwende Tonyo's shop.
 *
 * <p>Placeholder: uses the shared test map and completes when Dayo reaches the exit.
 * TODO(Johndel): stage mechanics.
 */
public class PalengkeStage extends GameStage {

    public PalengkeStage(ForestGame game) {
        super(game, "Stage 2: Ang Palengke ng Duwende", "maps/test.tmx");
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
