package com.cupids.curseoftheforest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Plays the game's background music and sound effects. Sounds are looked up by a short id
 * ("coin" is {@code audio/sfx/coin.wav}, "forest" is {@code audio/music/forest.ogg}).
 *
 * <p>Audio is optional: a missing or broken file is logged once and then skipped, and when
 * there is no sound device (tests, headless runs) every call quietly does nothing. This follows
 * the same missing-asset rule as {@link AssetLoader}, but a missing sound is silent instead of
 * a placeholder.
 */
public class AudioManager implements Disposable {
    private static final String MUSIC_DIR = "audio/music/";
    private static final String SFX_DIR = "audio/sfx/";
    private static final float MUSIC_VOLUME = 0.45f;
    private static final float SFX_VOLUME = 0.8f;

    private final Map<String, Sound> sounds = new HashMap<>();
    private final Set<String> missing = new HashSet<>();
    private Music music;
    private String musicId;
    private boolean muted;

    /** Plays a sound effect once. Unknown or missing ids are ignored. */
    public void play(String id) {
        if (muted || !hasAudioDevice()) {
            return;
        }
        Sound sound = loadSound(id);
        if (sound != null) {
            sound.play(SFX_VOLUME);
        }
    }

    /** Starts looping music. Does nothing if that track is already playing. */
    public void playMusic(String id) {
        if (id == null || id.equals(musicId) || !hasAudioDevice()) {
            return;
        }
        stopMusic();
        String path = MUSIC_DIR + id + ".ogg";
        if (missing.contains(path)) {
            return;
        }
        try {
            if (!Gdx.files.internal(path).exists()) {
                throw new GdxRuntimeException("File not found: " + path);
            }
            music = Gdx.audio.newMusic(Gdx.files.internal(path));
            music.setLooping(true);
            music.setVolume(MUSIC_VOLUME);
            musicId = id;
            if (!muted) {
                music.play();
            }
        } catch (GdxRuntimeException e) {
            missing.add(path);
            music = null;
            musicId = null;
            Gdx.app.error("AudioManager", "Could not load " + path + ", continuing without it.", e);
        }
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
        musicId = null;
    }

    /** Mutes or unmutes everything (the F9 key). */
    public void toggleMute() {
        muted = !muted;
        if (music != null) {
            if (muted) {
                music.pause();
            } else {
                music.play();
            }
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public String getMusicId() {
        return musicId;
    }

    private Sound loadSound(String id) {
        String path = SFX_DIR + id + ".wav";
        Sound cached = sounds.get(path);
        if (cached != null || missing.contains(path)) {
            return cached;
        }
        try {
            if (!Gdx.files.internal(path).exists()) {
                throw new GdxRuntimeException("File not found: " + path);
            }
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
            sounds.put(path, sound);
            return sound;
        } catch (GdxRuntimeException e) {
            missing.add(path);
            Gdx.app.error("AudioManager", "Could not load " + path + ", continuing without it.", e);
            return null;
        }
    }

    private boolean hasAudioDevice() {
        return Gdx.audio != null && Gdx.files != null;
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
