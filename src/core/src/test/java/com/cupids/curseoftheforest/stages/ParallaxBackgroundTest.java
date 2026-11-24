package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ParallaxBackgroundTest {

    @Test
    void newBlenderLayersAreUsedFirstAndTheOldArtIsTheFallback() {
        assertEquals("images/parallax-v2/stage3-gubat/far.png",
                ParallaxBackground.layerPath("stage3-gubat", "far.png", path -> true));
        assertEquals("images/parallax/stage3-gubat/far.png",
                ParallaxBackground.layerPath("stage3-gubat", "far.png", path -> false));
    }

    @Test
    void eachStageHasItsOwnArtSet() {
        assertEquals("stage2-palengke", ParallaxBackground.setFor("Stage 2: Ang Palengke ng Duwende"));
        assertEquals("stage5-mangkukulam", ParallaxBackground.setFor("Stage 5: Ang Tukso ng Mangkukulam"));
        assertNull(ParallaxBackground.setFor("Shop"));
        assertNull(ParallaxBackground.setFor(null));
    }
}
