package com.cupids.curseoftheforest.screens;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CutSceneTest {
    @Test void dialogueIsDefensivelyCopiedAndEndsWithoutOverflow() {
        String[] lines = {"First", "Last"};
        CutScene scene = new CutScene(lines, null);
        lines[0] = "Changed";
        assertEquals("First", scene.getCurrentLine());
        scene.advance();
        assertEquals("Last", scene.getCurrentLine());
        assertFalse(scene.isFinished());
        scene.advance();
        scene.advance();
        assertTrue(scene.isFinished());
        assertEquals("", scene.getCurrentLine());
        scene.dispose();
    }

    @Test void emptySceneFinishesAndInvalidDialogueIsRejected() {
        assertTrue(new CutScene(new String[0], null).isFinished());
        assertThrows(IllegalArgumentException.class, () -> new CutScene(new String[]{" "}, null));
        assertThrows(IllegalArgumentException.class, () -> new CutScene(new String[]{null}, null));
    }
}
