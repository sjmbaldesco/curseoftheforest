package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NunoTest {
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

    @Test void resetTakesBackItsPotionsAndStartsFromTheFirstRiddle() {
        Player player = new Player(0, 0);
        player.getInventory().add(new com.cupids.curseoftheforest.items.Accessory("Rosary", 0, "", 0));
        Nuno nuno = new Nuno(0, 0);
        nuno.interact(player);
        assertTrue(nuno.offer(player, "Sampalok"));
        assertTrue(nuno.offer(player, "Atis"));
        assertEquals(3, player.getInventory().getAll().size());

        nuno.reset(player);
        assertEquals(0, nuno.getAnsweredCount());
        assertTrue(nuno.hasStarted(), "no need to greet it again");
        assertEquals(1, player.getInventory().getAll().size(), "only the Nuno's potions are taken");
        assertTrue(player.getInventory().has("Rosary"));
        assertTrue(nuno.offer(player, "Sampalok"), "the first riddle is asked again");
    }
}
