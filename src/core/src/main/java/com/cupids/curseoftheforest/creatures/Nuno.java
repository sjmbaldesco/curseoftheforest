package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import java.util.Objects;

/** Starts the riddle encounter once Dayo approaches respectfully. */
public class Nuno extends Creature {
    private boolean greeted;
    public Nuno(float x, float y) { super("Nuno", x, y); }

    @Override
    public void interact(Player player) {
        Objects.requireNonNull(player, "player");
        greeted = true;
    }

    public boolean isGreeted() { return greeted; }
}
