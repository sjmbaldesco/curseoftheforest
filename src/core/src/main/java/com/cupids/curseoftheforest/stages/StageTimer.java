package com.cupids.curseoftheforest.stages;

/** Countdown for timed stages. It stops at zero instead of going negative. */
public class StageTimer {
    private float remainingSeconds;

    public StageTimer(float seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Timer cannot start below zero: " + seconds);
        }
        remainingSeconds = seconds;
    }

    public void tick(float delta) {
        remainingSeconds = Math.max(0f, remainingSeconds - delta);
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    public float getRemaining() {
        return remainingSeconds;
    }
}
