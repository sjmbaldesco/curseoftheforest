package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.ForestGame;

/**
 * Stage 1: Ang Simula. Lola Pacita gives the Rosary and guitar, then the color-sequence pattern game.
 *
 * <p>Placeholder: uses the shared test map and completes when Dayo reaches the exit.
 * TODO(Johndel): stage mechanics.
 */
public class SimulaStage extends GameStage {

    public SimulaStage(ForestGame game) {
        super(game, "Stage 1: Ang Simula", "maps/test.tmx");
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
