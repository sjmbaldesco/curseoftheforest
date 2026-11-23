package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.minigames.PotionDuel;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.player.Player;
import java.util.Objects;
import java.util.Random;

/**
 * Owns one duel per encounter; repeated interaction cannot reset health. She picks her potion
 * at random each round and keeps it hidden until Dayo has chosen his.
 */
public class Mangkukulam extends Creature {
    private final Random random;
    private PotionDuel duel;

    public Mangkukulam(float x, float y) {
        this(x, y, new Random());
    }

    /** Tests pass a seeded Random so the witch's choices are predictable. */
    public Mangkukulam(float x, float y, Random random) {
        super("Mangkukulam", x, y);
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public void interact(Player player) {
        if (duel == null) duel = new PotionDuel(player);
    }

    /** The witch's secret potion for this round. */
    public PotionType choosePotion() {
        PotionType[] types = PotionType.values();
        return types[random.nextInt(types.length)];
    }

    public PotionDuel getDuel() { return duel; }
}
