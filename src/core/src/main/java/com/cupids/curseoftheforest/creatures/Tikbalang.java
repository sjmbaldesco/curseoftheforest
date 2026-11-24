package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.stages.StageTimer;
import java.util.Objects;

/**
 * The Tikbalang guards the forest. Speaking to him starts Stage 3's countdown: until then the
 * clock is paused and the glowing bushes cannot be searched.
 */
public class Tikbalang extends Creature {
    private static final String CHALLENGE =
            "You are lost in my forest, Dayo. Find the pieces of the old saying before time runs out!";

    private final StageTimer forestTimer;
    private boolean challenged;

    public Tikbalang(float x, float y, StageTimer forestTimer) {
        super("Tikbalang", x, y);
        this.forestTimer = Objects.requireNonNull(forestTimer, "forestTimer");
    }

    /** Starts the forest timer the first time; talking again changes nothing. */
    @Override
    public void interact(Player p) {
        Objects.requireNonNull(p, "player");
        challenged = true;
        forestTimer.start();
    }

    public boolean hasChallenged() {
        return challenged;
    }

    /** What the Tikbalang says once Dayo has spoken to him, or an empty string before that. */
    public String getLine() {
        return challenged ? CHALLENGE : "";
    }
}
