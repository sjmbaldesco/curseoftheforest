package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.items.Accessory;
import com.cupids.curseoftheforest.player.Inventory;
import com.cupids.curseoftheforest.player.Player;

/** Gives Dayo the Rosary and the Lumang Gitara. */
public class LolaPacita extends Creature {
    private static final Accessory ROSARY = new Accessory("Rosary", 0, "Reduces Courage lost to Wakwak by 2.", 0);
    private static final Accessory GITARA = new Accessory("Lumang Gitara", 0, "Dayo's guitar for the serenade.", 0);

    public LolaPacita(float x, float y) {
        super("Lola Pacita", x, y);
    }

    @Override
    public void interact(Player player) {
        Inventory inventory = player.getInventory();
        if (!inventory.has(ROSARY)) {
            inventory.add(ROSARY);
        }
        if (!inventory.has(GITARA)) {
            inventory.add(GITARA);
        }
    }
}
