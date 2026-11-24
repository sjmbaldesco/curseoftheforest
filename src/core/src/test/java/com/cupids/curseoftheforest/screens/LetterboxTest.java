package com.cupids.curseoftheforest.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LetterboxTest {

    private static void assertBox(Letterbox box, int x, int y, int width, int height) {
        assertEquals(x, box.x, "x");
        assertEquals(y, box.y, "y");
        assertEquals(width, box.width, "width");
        assertEquals(height, box.height, "height");
    }

    @Test
    void sixteenByNineScreensAreFilledExactly() {
        assertBox(Letterbox.fit(960, 540, 960, 540), 0, 0, 960, 540);
        assertBox(Letterbox.fit(1920, 1080, 960, 540), 0, 0, 1920, 1080);
        assertBox(Letterbox.fit(2560, 1440, 960, 540), 0, 0, 2560, 1440);
    }

    @Test
    void otherShapesGetBlackBarsWithoutStretching() {
        // 16:10 laptop: bars above and below.
        assertBox(Letterbox.fit(1920, 1200, 960, 540), 0, 60, 1920, 1080);
        // Ultrawide: bars left and right.
        assertBox(Letterbox.fit(3440, 1440, 960, 540), 440, 0, 2560, 1440);
    }

    @Test
    void aMinimizedWindowDoesNotBreak() {
        assertBox(Letterbox.fit(0, 0, 960, 540), 0, 0, 0, 0);
    }
}
