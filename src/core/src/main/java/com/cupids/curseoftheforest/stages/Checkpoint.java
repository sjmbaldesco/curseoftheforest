package com.cupids.curseoftheforest.stages;

import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.player.Player;
import java.util.ArrayList;
import java.util.List;

/** Snapshot of Dayo's coins, stats and items at the start of a stage, restored on retry. */
public class Checkpoint {
    private final int coins;
    private final int courage;
    private final int sanity;
    private final List<Item> items;

    public Checkpoint(Player p) {
        coins = p.getCoins();
        courage = p.getCourage();
        sanity = p.getSanity();
        // Copy the list so later purchases do not change the snapshot.
        items = new ArrayList<>(p.getInventory().getAll());
    }

    public int getCoins() {
        return coins;
    }

    public int getCourage() {
        return courage;
    }

    public int getSanity() {
        return sanity;
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }
}
