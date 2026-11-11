package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhebeMinigamesTest {
    @Test void riddleValidatesAndNormalizesAnswers() {
        Riddle riddle = new Riddle("A leaf-wrapped rice treat?", "Suman");
        assertTrue(riddle.isCorrect("  sUMaN "));
        assertFalse(riddle.isCorrect(null));
        assertFalse(riddle.isCorrect(""));
        assertFalse(riddle.isCorrect("Buko"));
        assertThrows(IllegalArgumentException.class, () -> new Riddle("", "Suman"));
        assertThrows(IllegalArgumentException.class, () -> new Riddle("Question", null));
    }

    @Test void eachPotionBeatsExactlyOneOtherType() {
        assertTrue(PotionType.STUN.beats(PotionType.DAMAGE));
        assertTrue(PotionType.DAMAGE.beats(PotionType.HEAL));
        assertTrue(PotionType.HEAL.beats(PotionType.STUN));
        for (PotionType a : PotionType.values()) {
            assertFalse(a.beats(a));
            for (PotionType b : PotionType.values()) {
                if (a != b) assertNotEquals(a.beats(b), b.beats(a));
            }
        }
    }

    @Test void winningConsumesPotionsAndCheckpointRestoresThem() {
        Player player = new Player(0, 0);
        Potion stun = new Potion(PotionType.STUN);
        Potion damage = new Potion(PotionType.DAMAGE);
        player.getInventory().add(stun);
        player.getInventory().add(damage);
        var checkpoint = player.saveCheckpoint();
        PotionDuel duel = new PotionDuel(player);
        duel.resolveRound(stun, PotionType.DAMAGE);
        assertFalse(player.getInventory().has(stun));
        assertEquals(1, duel.getEnemyHealth());
        duel.resolveRound(damage, PotionType.HEAL);
        assertTrue(duel.isWon());
        player.restore(checkpoint);
        assertEquals(2, player.getInventory().getAll().size());
    }

    @Test void invalidSelectionDoesNotConsumeAnyPotion() {
        Player player = new Player(0, 0);
        Potion potion = new Potion(PotionType.HEAL);
        player.getInventory().add(potion);
        PotionDuel duel = new PotionDuel(player);
        assertThrows(IllegalArgumentException.class, () -> duel.resolveRound(new Potion(PotionType.HEAL), PotionType.STUN));
        assertThrows(IllegalArgumentException.class, () -> duel.resolveRound(potion, null));
        assertEquals(1, duel.getPlayerPotions().size());
        assertEquals(3, duel.getEnemyHealth());
        assertThrows(UnsupportedOperationException.class, () -> duel.getPlayerPotions().clear());
    }

    @Test void emptyInventoryIsALossAndTiesConsumePotions() {
        assertFalse(new PotionDuel(new Player(0, 0)).isWon());
        Player player = new Player(0, 0);
        player.changeCourage(-50);
        player.changeSanity(-50);
        Potion potion = new Potion(PotionType.STUN);
        player.getInventory().add(potion);
        PotionDuel duel = new PotionDuel(player);
        duel.resolveRound(potion, PotionType.STUN);
        assertTrue(duel.isOver());
        assertFalse(duel.isWon());
        assertEquals(2, duel.getPlayerHealth());
        assertTrue(player.getInventory().getAll().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> duel.resolveRound(potion, PotionType.STUN));
    }

    @Test void losingRoundReducesHealthAndEndsAtZero() {
        Player player = new Player(0, 0);
        player.changeCourage(-75);
        player.changeSanity(-75);
        Potion potion = new Potion(PotionType.STUN);
        player.getInventory().add(potion);
        PotionDuel duel = new PotionDuel(player);
        duel.resolveRound(potion, PotionType.HEAL);
        assertEquals(0, duel.getPlayerHealth());
        assertTrue(duel.isOver());
        assertFalse(duel.isWon());
    }
}
