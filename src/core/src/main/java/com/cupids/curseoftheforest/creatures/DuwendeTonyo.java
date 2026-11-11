package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;

/** Duwende Tonyo opens his shop when Dayo speaks to him. */
public class DuwendeTonyo extends Creature {
    private final Shop shop = new Shop();
    private boolean shopOpen;

    public DuwendeTonyo(float x, float y) {
        super("Duwende Tonyo", x, y);
    }

    @Override
    public void interact(Player p) {
        shopOpen = true;
    }

    public boolean isShopOpen() {
        return shopOpen;
    }

    public Shop getShop() {
        return shop;
    }
}
