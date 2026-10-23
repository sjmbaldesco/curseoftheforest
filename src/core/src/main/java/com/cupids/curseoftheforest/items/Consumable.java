package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Single-use item, such as the Malunggay Juice.
 * TODO(Johndel): stub from the UML.
 */
public class Consumable extends Item {
    private final int effectAmount;

    public Consumable(String name, int price, String description, int effectAmount) {
        super(name, price, description);
        this.effectAmount = effectAmount;
    }

    @Override
    public void use(Player p) {
        // TODO(Johndel): apply the effect and remove the item from the inventory.
    }

    public int getEffectAmount() {
        return effectAmount;
    }
}
