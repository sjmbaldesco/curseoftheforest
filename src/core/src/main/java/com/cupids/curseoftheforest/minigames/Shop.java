package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Duwende Tonyo's store.
 * TODO(Johndel): stub from the UML.
 */
public class Shop {
    private final ArrayList<Item> stock = new ArrayList<>();

    public void purchase(Player p, Item item) throws InsufficientCoinsException {
        // TODO(Johndel): check p.getCoins() against item.getPrice(), throw if short,
        // otherwise p.spendCoins(...) and add the item to p.getInventory().
        throw new UnsupportedOperationException("TODO(Johndel)");
    }

    public List<Item> listStock() {
        return List.copyOf(stock);
    }
}
