package com.cupids.curseoftheforest.player;

import com.cupids.curseoftheforest.items.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dayo's items. Minimal version so Player and Checkpoint work.
 * TODO(Johndel): owner of this class; extend as the shop and items need.
 */
public class Inventory {
    private final ArrayList<Item> items = new ArrayList<>();

    public void add(Item item) {
        items.add(item);
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public boolean has(Item item) {
        return items.contains(item);
    }

    /** Checks for an item with the given exact name. */
    public boolean has(String itemName) {
        if (itemName == null) {
            return false;
        }
        for (Item item : items) {
            if (item != null && itemName.equals(item.getName())) {
                return true;
            }
        }
        return false;
    }

    /** Read-only live view: later add/remove calls are reflected in the returned list. */
    public List<Item> getAll() {
        return Collections.unmodifiableList(items);
    }

    void clear() {
        items.clear();
    }
}
