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

    /** Read-only view so callers cannot change the list without going through add/remove. */
    public List<Item> getAll() {
        return Collections.unmodifiableList(items);
    }

    void clear() {
        items.clear();
    }
}
