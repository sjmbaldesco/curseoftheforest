package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.player.Player;

/**
 * One of the Nuno's potions, spent in the Stage 5 duel.
 * TODO(Johndel): stub from the UML.
 */
public class Potion extends Consumable {
    private final PotionType type;

    public Potion(PotionType type) {
        super(type.name() + " Potion", 0, "A potion from the Nuno.", 0);
        this.type = type;
    }

    @Override
    public void use(Player p) {
        // TODO(Johndel): potions are spent through PotionDuel.
    }

    public PotionType getType() {
        return type;
    }
}
