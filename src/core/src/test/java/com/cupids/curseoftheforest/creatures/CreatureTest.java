package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreatureTest {
    @Test void nunoKeepsMainBranchRiddlesAndRewardsEachAnswerOnlyOnce() {
        Player player = new Player(0, 0);
        Nuno nuno = new Nuno(0, 0);
        assertFalse(nuno.offer(player, "Sampalok"));
        nuno.interact(player);
        assertFalse(nuno.offer(player, "Atis"));
        assertEquals(0, nuno.getAnsweredCount());
        assertTrue(nuno.offer(player, " sampalok "));
        assertFalse(nuno.offer(player, "Sampalok"));
        assertTrue(nuno.offer(player, "ATIS"));
        assertTrue(nuno.offer(player, "Kalabasa"));
        assertTrue(nuno.isComplete());
        assertEquals(3, player.getInventory().getAll().size());
        assertEquals(3, nuno.getAnsweredCount());
        nuno.interact(player);
        assertFalse(nuno.offer(player, "Kalabasa"));
        assertEquals(3, player.getInventory().getAll().size());
    }

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
