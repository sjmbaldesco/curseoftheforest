#!/usr/bin/env python3
"""Generates the original chiptune music and 8-bit sound effects for Curse of the Forest.

Everything here is synthesised from scratch (square/triangle/noise waves), so the files are
original to the team and need no outside license. Needs numpy and ffmpeg.
Run from the repo root:  python3 tools/make_audio.py
Output: src/assets/audio/sfx/*.wav and src/assets/audio/music/*.ogg
"""
import subprocess
import sys
import wave
from pathlib import Path

import numpy as np

SR = 44100
OUT = Path(__file__).resolve().parent.parent / "src" / "assets" / "audio"
rng = np.random.default_rng(7)


def midi(n):
    return 440.0 * 2 ** ((n - 69) / 12)


def osc(kind, freq, dur, duty=0.5):
    """freq may be a number or an array (pitch glide); returns one waveform."""
    n = int(SR * dur)
    f = np.full(n, freq, dtype=float) if np.isscalar(freq) else np.interp(
        np.linspace(0, 1, n), np.linspace(0, 1, len(freq)), freq)
    phase = np.cumsum(f) / SR
    p = phase % 1.0
    if kind == "square":
        return np.where(p < duty, 1.0, -1.0)
    if kind == "tri":
        return 4 * np.abs(p - 0.5) - 1
    if kind == "saw":
        return 2 * p - 1
    if kind == "noise":
        return rng.uniform(-1, 1, n)
    raise ValueError(kind)


def env(n, attack=0.005, decay=None, sustain=1.0, release=0.02):
    """Simple envelope; decay=None means flat sustain."""
    e = np.ones(n) * sustain
    a = min(n, max(1, int(SR * attack)))
    e[:a] = np.linspace(0, 1, a)
    if decay:
        d = int(SR * decay)
        end = min(n, a + d)
        e[a:end] = np.linspace(1, sustain, end - a)
    r = min(n, max(1, int(SR * release)))
    e[-r:] *= np.linspace(1, 0, r)
    return e


def tone(kind, freq, dur, vol=0.5, duty=0.5, decay=None, sustain=1.0, attack=0.004, release=0.03):
    w = osc(kind, freq, dur, duty)
    return w * env(len(w), attack, decay, sustain, release) * vol


def pluck(freq, dur, vol=0.5):
    """Guitar-ish pluck: pulse wave with a fast decay."""
    return tone("square", freq, dur, vol, duty=0.25, decay=dur * 0.9, sustain=0.0, attack=0.002, release=0.01)


def mix(*parts, length=None):
    n = length or max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p[:n]
    return out


def seq(notes):
    """notes: list of waveforms played one after another."""
    return np.concatenate(notes)


def place(base, wave_, at):
    i = int(SR * at)
    end = min(len(base), i + len(wave_))
    base[i:end] += wave_[:end - i]


def write_wav(path, data, gain=0.9):
    data = np.asarray(data, dtype=float)
    peak = np.max(np.abs(data)) or 1.0
    data = data / peak * gain
    pcm = (data * 32767).astype("<i2")
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())


# ---------------------------------------------------------------- sound effects

def build_sfx():
    s = {}
    # movement
    s["jump"] = tone("square", np.linspace(280, 700, 200), 0.16, 0.5, duty=0.25, decay=0.16, sustain=0.1)
    s["land"] = mix(tone("noise", 0, 0.07, 0.5, decay=0.07, sustain=0.0), tone("tri", np.linspace(140, 60, 100), 0.07, 0.6))
    # coin: two quick rising notes
    s["coin"] = seq([tone("square", midi(83), 0.06, 0.4, duty=0.5), tone("square", midi(88), 0.22, 0.4, decay=0.22, sustain=0.0)])
    s["pickup"] = seq([tone("square", midi(n), 0.06, 0.35, duty=0.25) for n in (72, 76, 79, 84)])
    s["piece"] = seq([tone("tri", midi(n), 0.09, 0.6, decay=0.09, sustain=0.2) for n in (67, 71, 74, 79)])
    s["bush"] = tone("noise", 0, 0.28, 0.5, attack=0.03, decay=0.28, sustain=0.0) * np.sin(np.linspace(0, 9 * np.pi, int(SR * 0.28))) ** 2
    # damage
    s["hurt"] = mix(tone("square", np.linspace(400, 90, 300), 0.3, 0.5, duty=0.5, decay=0.3, sustain=0.0),
                    tone("noise", 0, 0.12, 0.35, decay=0.12, sustain=0.0))
    s["fall"] = tone("square", np.linspace(600, 60, 400), 0.7, 0.4, duty=0.5, decay=0.7, sustain=0.0)
    # creatures
    n = int(SR * 0.55)
    t = np.linspace(0, 1, n)
    screech = osc("saw", 900 + 700 * np.sin(2 * np.pi * 14 * t) + 600 * t, 0.55)
    s["wakwak"] = screech * env(n, 0.02, 0.5, 0.0, 0.05) * 0.5
    n = int(SR * 0.7)
    t = np.linspace(0, 1, n)
    neigh = osc("saw", 520 - 260 * t + 60 * np.sin(2 * np.pi * 22 * t), 0.7) * (0.6 + 0.4 * np.sin(2 * np.pi * 22 * t) ** 2)
    s["tikbalang"] = neigh * env(n, 0.03, 0.6, 0.0, 0.05) * 0.5
    s["nuno"] = seq([tone("tri", midi(n_), 0.18, 0.7, decay=0.18, sustain=0.0) for n_ in (62, 69, 74)])
    s["correct"] = seq([tone("square", midi(n_), 0.09, 0.4, duty=0.5) for n_ in (72, 79, 84)] + [tone("square", midi(88), 0.25, 0.4, decay=0.25, sustain=0.0)])
    s["wrong"] = seq([tone("square", midi(n_), 0.16, 0.45, duty=0.5, decay=0.16, sustain=0.3) for n_ in (58, 52)])
    s["whoosh"] = tone("noise", 0, 0.5, 0.5, attack=0.2, release=0.25) * np.linspace(0.2, 1, int(SR * 0.5)) ** 0.5
    # shop
    s["buy"] = seq([tone("square", midi(n_), 0.07, 0.4, duty=0.25) for n_ in (76, 83)] + [tone("square", midi(88), 0.2, 0.4, duty=0.25, decay=0.2, sustain=0.0)])
    s["denied"] = seq([tone("square", 140, 0.1, 0.5, duty=0.5), tone("square", 140, 0.1, 0.0), tone("square", 140, 0.15, 0.5)])
    s["use_item"] = seq([tone("tri", midi(n_), 0.07, 0.7) for n_ in (60, 64, 67, 72, 76)])
    # timer and stats
    s["timer_tick"] = tone("square", midi(84), 0.04, 0.4, duty=0.125, decay=0.04, sustain=0.0)
    s["time_up"] = seq([tone("square", midi(n_), 0.2, 0.5, duty=0.5, decay=0.2, sustain=0.5) for n_ in (67, 62, 55)])
    hb = np.zeros(int(SR * 0.9))
    for at in (0.0, 0.22):
        place(hb, tone("tri", np.linspace(90, 45, 400), 0.16, 0.9, decay=0.16, sustain=0.0), at)
    s["heartbeat"] = hb
    # pattern game: four colour tones + chime
    for i, n_ in enumerate((60, 64, 67, 72)):
        s[f"tone{i}"] = mix(tone("square", midi(n_), 0.35, 0.35, duty=0.5, decay=0.35, sustain=0.4),
                            tone("tri", midi(n_ - 12), 0.35, 0.4, decay=0.35, sustain=0.4))
    # duel
    s["potion_throw"] = tone("square", np.linspace(300, 900, 200), 0.18, 0.4, duty=0.125, decay=0.18, sustain=0.0)
    s["hit_stun"] = mix(tone("saw", np.linspace(800, 200, 300), 0.3, 0.5, decay=0.3, sustain=0.0), tone("noise", 0, 0.05, 0.4, decay=0.05, sustain=0.0))
    s["hit_damage"] = mix(tone("noise", 0, 0.2, 0.7, decay=0.2, sustain=0.0), tone("square", np.linspace(220, 60, 300), 0.2, 0.6, decay=0.2, sustain=0.0))
    s["heal"] = seq([tone("tri", midi(n_), 0.08, 0.7, decay=0.08, sustain=0.4) for n_ in (72, 76, 79, 84, 88)])
    s["round_win"] = seq([tone("square", midi(n_), 0.09, 0.4, duty=0.25) for n_ in (67, 72, 76)] + [tone("square", midi(79), 0.25, 0.4, duty=0.25, decay=0.25, sustain=0.0)])
    s["round_lose"] = seq([tone("tri", midi(n_), 0.14, 0.7, decay=0.14, sustain=0.4) for n_ in (64, 60, 55)])
    # jingles
    s["win"] = seq([tone("square", midi(n_), d, 0.4, duty=0.25, decay=d, sustain=0.3) for n_, d in
                    ((72, 0.1), (72, 0.1), (72, 0.1), (76, 0.2), (79, 0.5))])
    s["lose"] = seq([tone("tri", midi(n_), d, 0.8, decay=d, sustain=0.4) for n_, d in
                     ((67, 0.22), (66, 0.22), (65, 0.22), (60, 0.8))])
    s["ending"] = seq([tone("square", midi(n_), d, 0.35, duty=0.25, decay=d, sustain=0.4) for n_, d in
                       ((60, 0.2), (64, 0.2), (67, 0.2), (72, 0.2), (76, 0.2), (79, 0.2), (84, 0.9))])
    return s


# ---------------------------------------------------------------- music

SCALES = {
    "minor": [0, 2, 3, 5, 7, 8, 10],
    "major": [0, 2, 4, 5, 7, 9, 11],
    "phrygian": [0, 1, 3, 5, 7, 8, 10],
    "dorian": [0, 2, 3, 5, 7, 9, 10],
    "whole": [0, 2, 4, 6, 8, 10, 12],
}


def scale_note(root, scale, degree):
    steps = SCALES[scale]
    octave, idx = divmod(degree, 7)
    return root + 12 * octave + steps[idx]


def compose(name, root, scale, bpm, chords, lead="square", feel="plain", bars=16, seed=1, lead_vol=0.22,
            perc=True, lead_octave=1):
    """chords: list of scale degrees (one per bar, cycled). Builds bass + arpeggio + melody + drums."""
    r = np.random.default_rng(seed)
    beat = 60.0 / bpm
    bar = 4 * beat
    total = bars * bar
    buf = np.zeros(int(SR * (total + 1.0)))
    motif = r.integers(0, 5, size=8)  # a repeating melodic cell, varied each phrase
    for b in range(bars):
        t0 = b * bar
        deg = chords[b % len(chords)]
        triad = [scale_note(root, scale, deg + k) for k in (0, 2, 4)]
        # bass: root on beats 1 and 3 (tri)
        for k, at in enumerate((0, 2)):
            place(buf, tone("tri", midi(triad[0] - 12 + (7 if (k and feel == "driving") else 0)), beat * 1.9, 0.5, decay=beat * 1.8, sustain=0.4), t0 + at * beat)
        if feel == "driving":
            for at in (1, 3):
                place(buf, tone("tri", midi(triad[0] - 12), beat * 0.9, 0.45, decay=beat * 0.8, sustain=0.3), t0 + at * beat)
        # arpeggio / guitar-like pluck on eighth notes
        arp = [triad[0], triad[1], triad[2], triad[1]] if feel != "sparse" else [triad[0], None, triad[2], None]
        for i in range(8):
            n_ = arp[i % len(arp)]
            if n_ is None:
                continue
            place(buf, pluck(midi(n_ + 12), beat * 0.9, 0.2), t0 + i * beat / 2)
        # melody: scale walk using the motif, rests keep it from nagging
        step = beat if feel in ("sparse", "slow") else beat / 2
        pos = 0.0
        d = deg + 7 * lead_octave
        i = 0
        while pos < bar - 1e-6:
            m = motif[(b + i) % len(motif)] if (b // 4) % 2 == 0 else motif[(i * 3 + b) % len(motif)]
            if r.random() < (0.35 if feel in ("sparse", "slow") else 0.2):
                pos += step
                i += 1
                continue
            n_ = scale_note(root, scale, d + m)
            length = step * (2 if r.random() < 0.3 else 1)
            length = min(length, bar - pos)
            duty = 0.25 if lead == "square" else 0.5
            place(buf, tone("square" if lead == "square" else "tri", midi(n_), length * 0.95, lead_vol, duty=duty,
                            decay=length, sustain=0.5), t0 + pos)
            pos += length
            i += 1
        # drums
        if perc:
            for at in ((0, 2) if feel != "driving" else (0, 1, 2, 3)):
                place(buf, tone("tri", np.linspace(150, 45, 300), 0.09, 0.7, decay=0.09, sustain=0.0), t0 + at * beat)
            for at in (1, 3):
                place(buf, tone("noise", 0, 0.07, 0.25, decay=0.07, sustain=0.0), t0 + at * beat)
            if feel == "driving":
                for i in range(8):
                    place(buf, tone("noise", 0, 0.025, 0.1, decay=0.025, sustain=0.0), t0 + i * beat / 2 + beat / 4)
    # seamless loop: fold the tail back onto the start
    n = int(SR * total)
    tail = buf[n:]
    out = buf[:n].copy()
    out[:len(tail)] += tail
    return out


def build_music():
    t = {}
    t["menu"] = compose("menu", 57, "minor", 84, [0, 5, 3, 4], lead="tri", feel="slow", bars=16, seed=11, lead_vol=0.3)
    t["story"] = compose("story", 50, "dorian", 66, [0, 3, 0, 4], lead="tri", feel="sparse", bars=12, seed=21, lead_vol=0.3, perc=False)
    t["simula"] = compose("simula", 60, "major", 100, [0, 4, 5, 3], lead="square", feel="plain", bars=16, seed=31)
    t["cave"] = compose("cave", 52, "phrygian", 78, [0, 1, 0, 6], lead="tri", feel="sparse", bars=16, seed=41, lead_vol=0.3)
    t["forest"] = compose("forest", 50, "minor", 132, [0, 5, 6, 4], lead="square", feel="driving", bars=16, seed=51)
    t["nuno"] = compose("nuno", 54, "whole", 88, [0, 2, 1, 3], lead="tri", feel="sparse", bars=12, seed=61, lead_vol=0.3, perc=False)
    t["duel"] = compose("duel", 57, "minor", 148, [0, 6, 5, 4], lead="square", feel="driving", bars=16, seed=71, lead_vol=0.25)
    return t


def to_ogg(data, path):
    data = np.asarray(data)
    data = data / (np.max(np.abs(data)) or 1.0) * 0.8
    pcm = (data * 32767).astype("<i2").tobytes()
    path.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-f", "s16le", "-ar", str(SR), "-ac", "1", "-i", "-",
                    "-c:a", "libvorbis", "-q:a", "3", str(path)], input=pcm, check=True)


def main():
    for name, data in build_sfx().items():
        write_wav(OUT / "sfx" / f"{name}.wav", data)
    for name, data in build_music().items():
        to_ogg(data, OUT / "music" / f"{name}.ogg")
    print("done")


if __name__ == "__main__":
    sys.exit(main())
