package com.cupids.curseoftheforest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class AudioManagerTest {
    /** With no sound device (as in tests) every call must be a quiet no-op, never a crash. */
    @Test
    void doesNothingWithoutAudioDevice() {
        AudioManager audio = new AudioManager();
        assertDoesNotThrow(() -> audio.play("coin"));
        assertDoesNotThrow(() -> audio.playMusic("menu"));
        assertDoesNotThrow(audio::stopMusic);
        assertNull(audio.getMusicId());
        assertDoesNotThrow(audio::dispose);
    }

    @Test
    void muteToggles() {
        AudioManager audio = new AudioManager();
        assertFalse(audio.isMuted());
        audio.toggleMute();
        assertTrue(audio.isMuted());
        audio.toggleMute();
        assertFalse(audio.isMuted());
    }

    /** Every id the game plays must exist as a file, or the sound would silently never play. */
    @Test
    void everySoundTheGameUsesExists() throws Exception {
        Path root = Path.of("..", "assets", "audio");
        if (!Files.isDirectory(root)) {
            root = Path.of("assets", "audio");
        }
        String[] music = {"menu", "story", "simula", "cave", "forest", "nuno", "duel"};
        String[] sfx = {"jump", "coin", "hurt", "wakwak", "use_item", "buy", "denied", "correct", "wrong",
                "potion_throw", "hit_stun", "round_win", "round_lose", "tikbalang", "piece", "bush",
                "tone0", "tone1", "tone2", "timer_tick", "heartbeat", "win", "lose", "ending", "ui_select",
                "ui_tick", "whoosh"};
        for (String id : music) {
            assertTrue(Files.exists(root.resolve("music").resolve(id + ".ogg")), "missing music " + id);
        }
        for (String id : sfx) {
            assertTrue(Files.exists(root.resolve("sfx").resolve(id + ".wav")), "missing sfx " + id);
        }
    }
}
