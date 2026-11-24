package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.math.Vector2;
import com.cupids.curseoftheforest.player.Player;

/**
 * A Wakwak of the cave swarm. It hangs near its roost until Dayo comes close, then hunts him.
 * Light slows it to a crawl, and after a bite it flies back to its roost for a moment.
 */
public class Wakwak extends Creature {
    static final float FLIGHT_SPEED = 145f;
    /** Share of its speed a Wakwak keeps inside a torch's light. */
    static final float LIGHT_SLOWDOWN = 0.25f;
    /** It only hunts Dayo when he is this close (world pixels); otherwise it returns to its roost. */
    static final float CHASE_RANGE = 320f;
    /** Seconds it spends flying home after biting, so one Wakwak cannot bite twice in a row. */
    static final float RETREAT_SECONDS = 2.5f;
    private static final float FLIGHT_HEIGHT = 16f;
    private static final float CATCH_RADIUS = 16f;
    private final Vector2 flightStep = new Vector2();
    private final float homeX;
    private final float homeY;
    private Player target;
    private boolean slowed;
    private float retreatLeft;

    public Wakwak(float x, float y) {
        super("Wakwak", x, y);
        homeX = x;
        homeY = y;
    }

    @Override
    public void interact(Player player) {
        target = player;
    }

    @Override
    public void update(float delta) {
        super.update(delta); // keeps the wings flapping
        if (target == null) {
            return;
        }
        retreatLeft = Math.max(0f, retreatLeft - delta);

        boolean hunting = retreatLeft == 0f && isInChaseRange();
        float goalX = hunting ? targetX() : homeX;
        float goalY = hunting ? targetY() : homeY;
        flightStep.set(goalX - x, goalY - y);
        flightStep.limit(getSpeed() * delta);
        x += flightStep.x;
        y += flightStep.y;
        if (flightStep.x != 0) {
            // The sheet faces left; mirror it while flying right.
            flipX = flightStep.x > 0;
        }
    }

    /** The Wakwak's position is the middle of its body, so centre the picture on it. */
    @Override
    protected float drawX(int frameWidth) {
        return x - frameWidth / 2f;
    }

    public boolean hasCaughtDayo() {
        return target != null && retreatLeft == 0f
                && Vector2.dst2(x, y, targetX(), targetY()) <= CATCH_RADIUS * CATCH_RADIUS;
    }

    /** Set each frame by the stage: true while this Wakwak is inside a burning torch's light. */
    public void setSlowed(boolean slowed) {
        this.slowed = slowed;
    }

    /** Current flying speed in pixels per second; light cuts it down. */
    public float getSpeed() {
        return slowed ? FLIGHT_SPEED * LIGHT_SLOWDOWN : FLIGHT_SPEED;
    }

    /** After a bite: fly back to the roost and leave Dayo alone for a moment. */
    public void retreat() {
        retreatLeft = RETREAT_SECONDS;
    }

    public boolean isRetreating() {
        return retreatLeft > 0f;
    }

    private boolean isInChaseRange() {
        return Vector2.dst2(x, y, targetX(), targetY()) <= CHASE_RANGE * CHASE_RANGE;
    }

    private float targetX() {
        return target.getX() + Player.WIDTH / 2;
    }

    private float targetY() {
        return target.getY() + Player.HEIGHT + FLIGHT_HEIGHT;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
