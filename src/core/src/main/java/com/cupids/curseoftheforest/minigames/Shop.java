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
    // Item names in one place: stages check the inventory by exact name, so a typo would
    // silently switch an item's effect off.
    public static final String ITAK = "Lumang Itak";
    public static final String PUNYAL = "Lumang Punyal";
    public static final String AGIMAT = "Agimat ni Enteng";
    public static final String MALUNGGAY_JUICE = "Malunggay Juice";
    public static final String KWERDAS = "Kwerdas ng Gitara";

    /** The bolo clears bushes twice as fast as bare hands; the dagger 1.5 times as fast. */
    public static final float ITAK_CUT_SPEED = 2f;
    public static final float PUNYAL_CUT_SPEED = 1.5f;

    private final List<Item> stock = List.of(
            new Weapon(ITAK, 3, "An old bolo. Searches bushes twice as fast.", ITAK_CUT_SPEED),
            new Weapon(PUNYAL, 3, "An old dagger. Searches bushes 1.5 times as fast.", PUNYAL_CUT_SPEED),
            new Accessory(AGIMAT, 5, "A protective charm. Doubles the time in the Tikbalang's forest.", 0),
            new Consumable(MALUNGGAY_JUICE, 7, "Restores 25 Courage or grants extra time if kept for Nuno's riddles.", 25),
            new Accessory(KWERDAS, 10, "Strings for Dayo's guitar.", 0)
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
