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

    @Test void witchChoiceIsRandomButRepeatableWithASeed() {
        Mangkukulam a = new Mangkukulam(0, 0, new java.util.Random(7));
        Mangkukulam b = new Mangkukulam(0, 0, new java.util.Random(7));
        java.util.Set<com.cupids.curseoftheforest.minigames.PotionType> seen = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) {
            var choice = a.choosePotion();
            assertEquals(choice, b.choosePotion());
            seen.add(choice);
        }
        assertEquals(3, seen.size(), "Over many rounds she uses every potion type");
    }
}
