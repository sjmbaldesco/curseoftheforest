package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Item kept and worn for a lasting effect: Rosary, Agimat ni Enteng, Kwerdas ng Gitara.
 * TODO(Johndel): stub from the UML.
 */
public class Accessory extends Item {
    private final int effectAmount;

    public Accessory(String name, int price, String description, int effectAmount) {
        super(name, price, description);
        this.effectAmount = effectAmount;
    }

    @Override
    public void use(Player p) {
        // TODO(Johndel): apply the accessory's effect.
    }

    public int getEffectAmount() {
        return effectAmount;
    }
}
