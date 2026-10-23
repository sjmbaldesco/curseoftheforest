package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StageTimerTest {

    @Test
    void countsDownAndExpires() {
        StageTimer timer = new StageTimer(1.5f);
        timer.tick(1f);
        assertFalse(timer.isExpired());
        assertEquals(0.5f, timer.getRemaining(), 0.0001f);
        timer.tick(1f);
        assertTrue(timer.isExpired());
        assertEquals(0f, timer.getRemaining());
    }

    @Test
    void rejectsNegativeStart() {
        assertThrows(IllegalArgumentException.class, () -> new StageTimer(-1f));
    }
}
