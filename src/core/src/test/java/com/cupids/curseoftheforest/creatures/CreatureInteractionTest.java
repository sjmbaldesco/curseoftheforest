package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreatureInteractionTest {
    @Test void interactionIsPolymorphicAndDoesNotResetDuel() {
        Player player = new Player(0, 0);
        Nuno nuno = new Nuno(20, 0);
        Mangkukulam witch = new Mangkukulam(300, 0);
        Creature[] creatures = {nuno, witch};
        for (Creature creature : creatures) creature.interact(player);
        assertTrue(nuno.isGreeted());
        assertTrue(nuno.isNear(player));
        assertFalse(witch.isNear(player));
        var duel = witch.getDuel();
        witch.interact(player);
        assertSame(duel, witch.getDuel());
    }
}
