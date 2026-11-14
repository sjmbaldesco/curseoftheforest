package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Something Dayo can own and use. Subclasses decide what use() does.
 */
public abstract class Item {
    private final String name;
    private final int price;
    private final String description;

    protected Item(String name, int price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    /** Applies a direct-use effect when the item has one. */
    public abstract void use(Player p);

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
