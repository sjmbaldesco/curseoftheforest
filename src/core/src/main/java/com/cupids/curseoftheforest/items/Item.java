package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Something Dayo can own and use. Subclasses decide what use() does.
 * TODO(Johndel): owner of the item hierarchy.
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
