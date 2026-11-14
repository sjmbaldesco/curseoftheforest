package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.minigames.PotionDuel;
import com.cupids.curseoftheforest.player.Player;

/** Owns one duel per encounter; repeated interaction cannot reset health. */
public class Mangkukulam extends Creature {
    private PotionDuel duel;
    public Mangkukulam(float x, float y) { super("Mangkukulam", x, y); }

    @Override
    public void interact(Player player) {
        if (duel == null) duel = new PotionDuel(player);
    }

    public PotionDuel getDuel() { return duel; }
}
