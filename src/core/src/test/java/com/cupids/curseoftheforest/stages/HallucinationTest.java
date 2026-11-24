package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HallucinationTest {

    @Test
    void visionsTakeTurnsBetweenTheCaveAndTheForest() {
        assertEquals(Hallucination.Place.CAVE, Hallucination.placeFor(1));
        assertEquals(Hallucination.Place.FOREST, Hallucination.placeFor(2));
        assertEquals(Hallucination.Place.CAVE, Hallucination.placeFor(3));
    }

    @Test
    void visionLastsEightSecondsAndReportsItsEndOnce() {
        Hallucination vision = new Hallucination();
        vision.start(Hallucination.Place.FOREST);
        int endings = 0;
        for (int frame = 0; frame < 79; frame++) {
            if (vision.update(0.1f)) endings++;
        }
        assertTrue(vision.isActive(), "still going after 7.9 s");
        for (int frame = 0; frame < 5; frame++) {
            if (vision.update(0.1f)) endings++;
        }
        assertFalse(vision.isActive());
        assertNull(vision.getPlace());
        assertEquals(1, endings);
    }

    @Test
    void effectFadesInHoldsAndFadesOut() {
        Hallucination vision = new Hallucination();
        assertEquals(0f, vision.strength());
        assertEquals(0f, vision.tilt());
        vision.start(Hallucination.Place.CAVE);
        vision.update(0.5f);
        assertEquals(0.5f, vision.strength(), 1e-4);
        vision.update(3f);
        assertEquals(1f, vision.strength(), 1e-4);
        assertEquals(Hallucination.MAX_TINT, vision.tint(), 1e-4);
        assertTrue(Math.abs(vision.tilt()) <= Hallucination.MAX_TILT);
        vision.update(4f); // 0.5 s left
        assertEquals(0.5f, vision.strength(), 1e-4);
    }
}
