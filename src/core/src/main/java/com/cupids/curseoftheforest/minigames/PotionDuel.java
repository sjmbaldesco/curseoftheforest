package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 5 duel against the Mangkukulam.
 * TODO(Johndel): stub from the UML.
 */
public class PotionDuel {
    private int playerHealth;
    private int enemyHealth;
    private final List<Potion> playerPotions = new ArrayList<>();

    public PotionDuel(Player p) {
        // TODO(Johndel): take the potions from p's inventory; scale health by Courage and Sanity.
    }

    public void resolveRound(Potion p, PotionType e) {
        throw new UnsupportedOperationException("TODO(Johndel)");
    }

    public boolean beats(PotionType a, PotionType b) {
        throw new UnsupportedOperationException("TODO(Johndel)");
    }

    public boolean isOver() {
        throw new UnsupportedOperationException("TODO(Johndel)");
    }
}
