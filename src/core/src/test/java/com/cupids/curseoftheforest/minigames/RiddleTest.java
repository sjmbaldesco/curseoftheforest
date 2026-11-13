package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiddleTest {
    @Test void riddleValidatesAndNormalizesAnswers() {
        Riddle riddle = new Riddle("A leaf-wrapped rice treat?", "Suman");
        assertTrue(riddle.isCorrect("  sUMaN "));
        assertFalse(riddle.isCorrect(null));
        assertFalse(riddle.isCorrect(""));
        assertFalse(riddle.isCorrect("Buko"));
        assertThrows(IllegalArgumentException.class, () -> new Riddle("", "Suman"));
        assertThrows(IllegalArgumentException.class, () -> new Riddle("Question", null));
    }

}
