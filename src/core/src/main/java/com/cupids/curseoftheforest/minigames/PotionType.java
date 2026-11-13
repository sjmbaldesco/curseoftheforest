package com.cupids.curseoftheforest.minigames;

/** The three potion kinds in the Stage 5 duel. */
public enum PotionType {
    STUN,
    DAMAGE,
    HEAL;

    /** Stun interrupts damage, damage overwhelms healing, healing cures stun. */
    public boolean beats(PotionType other) {
        if (other == null) throw new IllegalArgumentException("Choose a potion type.");
        return (this == STUN && other == DAMAGE)
                || (this == DAMAGE && other == HEAL)
                || (this == HEAL && other == STUN);
    }
}
