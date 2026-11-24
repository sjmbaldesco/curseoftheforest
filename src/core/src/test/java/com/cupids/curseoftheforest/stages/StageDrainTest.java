package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;

class StageDrainTest {

    /** A stage that only overrides the drain rates, to test the shared drain logic. */
    private static GameStage stageDraining(float courage, float sanity) {
        GameStage stage = new GameStage(null, "Test", "") {
            @Override protected void init() { }
            @Override public void update(float delta) { }
            @Override protected void handleInput() { }
            @Override protected float courageDrainPerSecond() { return courage; }
            @Override protected float sanityDrainPerSecond() { return sanity; }
        };
        stage.player = new Player(0, 0);
        return stage;
    }

    @Test
    void drainBuildsUpAcrossFramesInWholePoints() {
        GameStage stage = stageDraining(0.5f, 2f);
        for (int frame = 0; frame < 8; frame++) {
            stage.applyDrain(0.25f); // two seconds in total
        }
        assertEquals(99, stage.player.getCourage());
        assertEquals(96, stage.player.getSanity());
    }

    @Test
    void stagesDrainNothingByDefault() {
        GameStage stage = new GameStage(null, "Safe", "") {
            @Override protected void init() { }
            @Override public void update(float delta) { }
            @Override protected void handleInput() { }
        };
        stage.player = new Player(0, 0);
        stage.applyDrain(10f);
        assertEquals(100, stage.player.getCourage());
        assertEquals(100, stage.player.getSanity());
    }

    @Test
    void gameOverSaysWhyTheStageWasLost() {
        GameStage stage = stageDraining(0f, 0f);
        assertEquals("Dayo was beaten.", stage.failureReason());
        stage.player.changeSanity(-100);
        assertEquals("Dayo's mind gave way.", stage.failureReason());
        stage.player.changeCourage(-100);
        assertEquals("Dayo's Courage ran out.", stage.failureReason());
    }
}
