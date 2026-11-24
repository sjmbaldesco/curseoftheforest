package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cupids.curseoftheforest.ForestGame;
import org.junit.jupiter.api.Test;

class AmbienceTest {

    @Test
    void eachStageGetsItsOwnEffects() {
        assertEquals(22, new Ambience(Ambience.settingsFor("Stage 3: Ang Gubat ng Tikbalang"), 1).fireflies.size());
        assertEquals(12, new Ambience(Ambience.settingsFor("Stage 3: Ang Gubat ng Tikbalang"), 1).leaves.size());
        Ambience cave = new Ambience(Ambience.settingsFor("Stage 2: Ang Palengke ng Duwende"), 1);
        assertTrue(cave.fireflies.isEmpty(), "no fireflies in the Wakwak Cave");
        assertEquals(36, cave.dust.size());
        assertNotNull(Ambience.settingsFor("Stage 5: Ang Tukso ng Mangkukulam"));
        assertNull(Ambience.settingsFor("Shop"));
    }

    @Test
    void particlesStayOnScreenOverAMinute() {
        Ambience forest = new Ambience(Ambience.settingsFor("Stage 3: Ang Gubat ng Tikbalang"), 7);
        for (int frame = 0; frame < 3600; frame++) {
            forest.update(1 / 60f);
        }
        float left = -Ambience.MARGIN;
        float right = ForestGame.WORLD_WIDTH + Ambience.MARGIN;
        for (Ambience.Particle p : forest.fireflies) {
            assertTrue(p.x >= left && p.x <= right, "firefly x " + p.x);
            assertTrue(p.y >= 60 && p.y <= 480, "firefly y " + p.y);
            float bright = forest.fireflyBrightness(p);
            assertTrue(bright >= 0 && bright <= 1);
        }
        for (Ambience.Particle p : forest.leaves) {
            assertTrue(p.x >= left && p.x <= right, "leaf x " + p.x);
            assertTrue(p.y >= -Ambience.MARGIN && p.y <= ForestGame.WORLD_HEIGHT + Ambience.MARGIN, "leaf y " + p.y);
        }
    }

    @Test
    void wrappingKeepsParticlesInsideTheBand() {
        assertEquals(100f, Ambience.wrap(100f), 1e-3);
        assertEquals(-Ambience.MARGIN + 10f, Ambience.wrap(ForestGame.WORLD_WIDTH + Ambience.MARGIN + 10f), 1e-3);
        assertEquals(ForestGame.WORLD_WIDTH + Ambience.MARGIN - 10f, Ambience.wrap(-Ambience.MARGIN - 10f), 1e-3);
    }
}
