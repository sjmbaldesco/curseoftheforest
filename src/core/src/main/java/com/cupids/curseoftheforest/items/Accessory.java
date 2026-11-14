package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Item with a passive effect while carried, such as the Rosary.
 */
public class Accessory extends Item {
    private final int effectAmount;

    public Accessory(String name, int price, String description, int effectAmount) {
        super(name, price, description);
        this.effectAmount = effectAmount;
    }

    @Override
    public void use(Player p) {
        // Stages check the player's inventory when applying passive effects.
    }

    public int getEffectAmount() {
        return effectAmount;
    }
}
