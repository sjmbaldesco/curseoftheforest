package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.math.Vector2;
import com.cupids.curseoftheforest.player.Player;

/** Wakwak flies toward Dayo during the coin run. */
public class Wakwak extends Creature {
    private static final float FLIGHT_SPEED = 145f;
    private static final float FLIGHT_HEIGHT = 16f;
    private static final float CATCH_RADIUS = 16f;
    private final Vector2 flightStep = new Vector2();
    private Player target;

    public Wakwak(float x, float y) {
        super("Wakwak", x, y);
    }

    @Override
    public void interact(Player player) {
        target = player;
    }

    @Override
    public void update(float delta) {
        if (target == null) {
            return;
        }

        flightStep.set(targetX() - x, targetY() - y);
        flightStep.limit(FLIGHT_SPEED * delta);
        x += flightStep.x;
        y += flightStep.y;
    }

    public boolean hasCaughtDayo() {
        return target != null
                && Vector2.dst2(x, y, targetX(), targetY()) <= CATCH_RADIUS * CATCH_RADIUS;
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
