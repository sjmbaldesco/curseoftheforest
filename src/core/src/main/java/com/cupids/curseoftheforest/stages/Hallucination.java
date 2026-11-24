package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.math.MathUtils;

/**
 * Stage 4's fear vision. When the Nuno breaks Dayo's Courage, he does not lose the stage: for
 * {@link #SECONDS} he finds himself back in a place he already survived, the Wakwak Cave or the
 * Tikbalang forest, with the screen tilting and tinted purple. Nothing can hurt him there. When it
 * ends he wakes by the Nuno with half his Courage and the riddles start over.
 *
 * <p>This class only keeps the vision's clock and how strong the effect is; it needs no graphics,
 * so it can be tested. {@link TulayStage} swaps the map and draws it.
 */
public class Hallucination {
    public static final float SECONDS = 8f;
    /** How far the screen tilts at the vision's strongest, in degrees. */
    static final float MAX_TILT = 4f;
    /** How much purple covers the screen at the vision's strongest (0 to 1). */
    static final float MAX_TINT = 0.32f;
    /** The effect fades in and out over this long, so the switch is not a hard cut. */
    private static final float FADE_SECONDS = 1f;

    /** The places Dayo can be sent back to, each with its map, art set and phantom creature. */
    public enum Place {
        CAVE("maps/palengke.tmx", "cave", "stage2-wakwak-cave", "images/wakwak.png", "Wakwak Cave", 0.45f),
        FOREST("maps/gubat.tmx", null, "stage3-gubat", "images/tikbalang.png", "Tikbalang's forest", 0.15f);

        public final String mapPath;
        /** The map marker Dayo appears just past, or null for the map's own spawn. */
        public final String arrivalMarker;
        /** The parallax art set shown during the vision (a folder name under images/parallax-v2). */
        public final String backdrop;
        public final String phantomImage;
        public final String label;
        /** Extra black over the place at the vision's strongest: the cave is darker than the forest. */
        public final float darkness;

        Place(String mapPath, String arrivalMarker, String backdrop, String phantomImage, String label,
                float darkness) {
            this.darkness = darkness;
            this.mapPath = mapPath;
            this.arrivalMarker = arrivalMarker;
            this.backdrop = backdrop;
            this.phantomImage = phantomImage;
            this.label = label;
        }
    }

    private Place place;
    private float left;
    private float time;

    /** The first vision is the cave, the second the forest, and so on, taking turns. */
    public static Place placeFor(int visionNumber) {
        return visionNumber % 2 == 1 ? Place.CAVE : Place.FOREST;
    }

    public void start(Place where) {
        place = java.util.Objects.requireNonNull(where, "place");
        left = SECONDS;
        time = 0;
    }

    /** Runs the vision's clock. Returns true on the one frame the vision ends. */
    public boolean update(float delta) {
        if (!isActive() || delta <= 0) {
            return false;
        }
        time += delta;
        left -= delta;
        if (left <= 0) {
            stop();
            return true;
        }
        return false;
    }

    public void stop() {
        left = 0;
        place = null;
    }

    public boolean isActive() {
        return place != null && left > 0;
    }

    /** Where Dayo is now, or null when there is no vision. */
    public Place getPlace() {
        return isActive() ? place : null;
    }

    public float getSecondsLeft() {
        return Math.max(0, left);
    }

    /** 0 to 1: rises over the first second, holds, and falls over the last second. */
    public float strength() {
        if (!isActive()) {
            return 0f;
        }
        float in = Math.min(1f, time / FADE_SECONDS);
        float out = Math.min(1f, left / FADE_SECONDS);
        return Math.min(in, out);
    }

    /** The screen's tilt this frame, in degrees: a slow, uneven sway. */
    public float tilt() {
        float sway = MathUtils.sin(time * 2.1f) * 0.7f + MathUtils.sin(time * 3.7f) * 0.3f;
        return MAX_TILT * strength() * sway;
    }

    /** How opaque the black shade over the place is this frame. */
    public float darkness() {
        return isActive() ? place.darkness * strength() : 0f;
    }

    /** How opaque the purple tint is this frame. */
    public float tint() {
        return MAX_TINT * strength();
    }
}
