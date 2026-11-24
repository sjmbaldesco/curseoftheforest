package com.cupids.curseoftheforest.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DialogueBoxTest {

    @Test
    void conversationTypesOutThenAdvancesAndCloses() {
        DialogueBox box = new DialogueBox();
        int[] closed = {0};
        box.say("Lola Pacita", null, "Bakit ngayon ka lang?", "Do you also hear the call of the forest?");
        box.setOnClose(() -> closed[0]++);
        assertTrue(box.isBlocking());

        box.update(0.2f); // about 9 characters at 45 per second
        assertTrue(box.isTyping());
        assertEquals("Bakit", box.getVisibleText().substring(0, 5));

        box.advance(); // first press finishes the line
        assertFalse(box.isTyping());
        assertEquals("Bakit ngayon ka lang?", box.getVisibleText());

        box.advance(); // second press goes to the next line
        assertEquals("Do you also hear the call of the forest?", box.getCurrentLine());
        box.advance();
        box.advance();
        assertFalse(box.isOpen());
        assertEquals(1, closed[0]);
    }

    @Test
    void questionsDoNotPauseTheGame() {
        DialogueBox box = new DialogueBox();
        box.ask("Nuno", null, "Brown pod, sour pulp. What fruit am I?", "Atis", "Sampalok", "Kalabasa");
        assertTrue(box.isOpen());
        assertFalse(box.isBlocking());
    }

    @Test
    void blankLinesAreRejected() {
        DialogueBox box = new DialogueBox();
        assertThrows(IllegalArgumentException.class, () -> box.say("Dayo", null, " "));
        assertThrows(IllegalArgumentException.class, () -> box.say("Dayo", null));
    }
}
