package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreatureRenderingTest {
    @Test void proximityUsesBothWorldCoordinates() {
        Nuno nuno = new Nuno(100, 100);
        assertTrue(nuno.isNear(new Player(200, 196)));
        assertFalse(nuno.isNear(new Player(201, 100)));
        assertFalse(nuno.isNear(new Player(100, 197)));
    }

    @Test void creatureRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Creature(" ", 0, 0) {
            @Override public void interact(Player player) { }
        });
    }
}
