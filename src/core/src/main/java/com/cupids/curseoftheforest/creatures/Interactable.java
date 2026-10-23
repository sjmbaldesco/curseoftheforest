package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;

/** Anything Dayo can interact with, whether or not it is a Creature (e.g. GlowingBush). */
public interface Interactable {
    void interact(Player p);
}
