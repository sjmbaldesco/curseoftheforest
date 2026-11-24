package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cupids.curseoftheforest.player.Player;
import java.util.Objects;

/**
 * A torch on the Wakwak Cave wall. When Dayo reaches it, it burns for {@link #LIT_SECONDS}, then
 * needs {@link #RELIGHT_SECONDS} before it can be lit again. While it burns, any Wakwak inside its
 * light slows to a crawl, so the player plans a path from torch to torch.
 *
 * <p>Like {@link GlowingBush}, it is not a creature, but Dayo uses it through the same
 * {@link Interactable} interface.
 */
public class Torch implements Interactable {
    public static final float LIT_SECONDS = 5f;
    public static final float RELIGHT_SECONDS = 10f;
    /** How far the light reaches from the torch's centre, in world pixels. */
    public static final float LIGHT_RADIUS = 128f;

    private final Rectangle bounds;
    private final Vector2 centre = new Vector2();
    private float litLeft;
    private float relightLeft;

    public Torch(Rectangle bounds) {
        this.bounds = new Rectangle(Objects.requireNonNull(bounds, "bounds"));
        this.bounds.getCenter(centre);
    }

    /** Lights the torch if it is ready. Returns quietly if it is burning or still cooling down. */
    @Override
    public void interact(Player p) {
        Objects.requireNonNull(p, "player");
        if (isReady()) {
            litLeft = LIT_SECONDS;
        }
    }

    public void update(float delta) {
        if (delta <= 0) {
            return;
        }
        if (litLeft > 0) {
            litLeft -= delta;
            if (litLeft <= 0) {
                litLeft = 0;
                relightLeft = RELIGHT_SECONDS; // burnt out: it needs time before it can light again
            }
        } else if (relightLeft > 0) {
            relightLeft = Math.max(0, relightLeft - delta);
        }
    }

    public boolean isLit() {
        return litLeft > 0;
    }

    /** True when the torch is out and has cooled down, so touching it lights it. */
    public boolean isReady() {
        return litLeft <= 0 && relightLeft <= 0;
    }

    /** True if this torch is burning and the point (x, y) is inside its light. */
    public boolean lights(float x, float y) {
        return isLit() && centre.dst2(x, y) <= LIGHT_RADIUS * LIGHT_RADIUS;
    }

    /** True when Dayo's body overlaps the torch. */
    public boolean touches(Player p) {
        return bounds.overlaps(p.getBounds());
    }

    /** 1 just after lighting, falling to 0 as the flame dies. */
    public float getFlameLeft() {
        return litLeft / LIT_SECONDS;
    }

    /** 0 just after burning out, rising to 1 when the torch can be lit again. */
    public float getRelightProgress() {
        return isLit() ? 0f : 1f - relightLeft / RELIGHT_SECONDS;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getCentreX() {
        return centre.x;
    }

    public float getCentreY() {
        return centre.y;
    }
}
