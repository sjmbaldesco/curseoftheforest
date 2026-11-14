package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Single-use item that restores Courage, such as Malunggay Juice.
 */
public class Consumable extends Item {
    private final int effectAmount;

    public Consumable(String name, int price, String description, int effectAmount) {
        super(name, price, description);
        this.effectAmount = effectAmount;
    }

    @Override
    public void use(Player player) {
        if (player.getInventory().remove(this)) {
            player.changeCourage(effectAmount);
        }
    }

    public int getEffectAmount() {
        return effectAmount;
    }
}
