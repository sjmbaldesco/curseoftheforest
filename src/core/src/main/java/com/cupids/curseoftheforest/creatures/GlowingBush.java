package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.player.Player;

/**
 * Stage 3 bush that may hide a clue piece. Searching takes less time with a sharper weapon.
 * TODO(Johndel): stub from the UML.
 */
public class GlowingBush implements Interactable {
    private final boolean hasPiece;
    private final float searchSeconds;
    private boolean searched;
    private final Rectangle bounds;

    public GlowingBush(Rectangle bounds, boolean hasPiece, float searchSeconds) {
        this.bounds = bounds;
        this.hasPiece = hasPiece;
        this.searchSeconds = searchSeconds;
    }

    @Override
    public void interact(Player p) {
        // TODO(Johndel): search time = searchSeconds / weapon cutSpeed; reveal the piece once.
    }
}
