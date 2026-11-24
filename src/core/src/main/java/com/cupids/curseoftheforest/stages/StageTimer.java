package com.cupids.curseoftheforest.stages;

/**
 * Countdown for timed stages. It stops at zero instead of going negative.
 *
 * <p>A timer can be created paused (Stage 3 waits until Dayo talks to the Tikbalang). A paused
 * timer ignores {@link #tick(float)} and never counts as expired.
 */
public class StageTimer {
    private float remainingSeconds;
    private boolean running;

    /** A timer that starts counting immediately. */
    public StageTimer(float seconds) {
        this(seconds, true);
    }

    public StageTimer(float seconds, boolean startRunning) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Timer cannot start below zero: " + seconds);
        }
        remainingSeconds = seconds;
        running = startRunning;
    }

    /** Starts (or resumes) the countdown. Calling it again has no effect. */
    public void start() {
        running = true;
    }

    public void tick(float delta) {
        if (running) {
            remainingSeconds = Math.max(0f, remainingSeconds - delta);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isExpired() {
        return running && remainingSeconds <= 0f;
    }

    public float getRemaining() {
        return remainingSeconds;
    }
}
