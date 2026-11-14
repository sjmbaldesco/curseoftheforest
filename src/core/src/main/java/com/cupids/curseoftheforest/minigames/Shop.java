package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Accessory;
import com.cupids.curseoftheforest.items.Consumable;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.items.Weapon;
import com.cupids.curseoftheforest.player.Player;
import java.util.List;

/**
 * Duwende Tonyo's store.
 */
public class Shop {
    private final List<Item> stock = List.of(
            new Weapon("Lumang Itak", 3, "An old bolo for clearing bushes.", 1f),
            new Weapon("Lumang Punyal", 3, "An old dagger for clearing bushes.", 1f),
            new Accessory("Agimat ni Enteng", 5, "A protective charm.", 0),
            new Consumable("Malunggay Juice", 7, "Restores 25 Courage or grants extra time if kept for Nuno's riddles.", 25),
            new Accessory("Kwerdas ng Gitara", 10, "Strings for Dayo's guitar.", 0)
    );

    public void purchase(Player player, Item item) throws InsufficientCoinsException {
        if (!stock.contains(item) || player.getInventory().has(item)) {
            throw new IllegalArgumentException("This item is unavailable.");
        }
        if (player.getCoins() < item.getPrice()) {
            throw new InsufficientCoinsException("You need " + item.getPrice() + " coins for " + item.getName() + ".");
        }

        player.spendCoins(item.getPrice());
        player.getInventory().add(item);
    }

    public List<Item> listStock() {
        return stock;
    }
}
