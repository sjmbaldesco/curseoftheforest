package com.cupids.curseoftheforest.items;

import com.cupids.curseoftheforest.player.Player;

/**
 * Lumang Itak and Lumang Punyal. Used in Stage 3; cutSpeed shortens bush search time.
 * TODO(Johndel): stub from the UML.
 */
public class Weapon extends Item {
    private final float cutSpeed;

    public Weapon(String name, int price, String description, float cutSpeed) {
        super(name, price, description);
        this.cutSpeed = cutSpeed;
    }

    @Override
    public void use(Player p) {
        // TODO(Johndel): weapons are used through GlowingBush, not directly.
    }

    public float getCutSpeed() {
        return cutSpeed;
    }
}
