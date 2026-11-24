# Credits

Curse of the Forest is the OOP 101 final project of Team CUPIDS (BatStateU, CS-2103):

- Sir Jairus Matthew M. Baldesco
- Johndel M. Cuentas
- Phebe A. De Galicia

## Fonts

| Font | Author | Source | License |
|---|---|---|---|
| VCR OSD Mono (all in-game text) | Riciery Leal | [DaFont](https://www.dafont.com/vcr-osd-mono.font) | Listed on DaFont as "100% Free" |

The `.ttf` is in `src/assets/fonts/`. The game uses bitmap copies at five sizes made from it with `tools/make_bitmap_font.py` (`vcr-14` to `vcr-56`).

## Art

- UI frames, buttons, icons and mockups (`src/assets/images/ui/`, `design/ui-revamp/`): made by the team for this game, with help from Claude.
- Animated sprites, fog bands and parallax v2 (`src/assets/images/animated/`, `src/assets/images/parallax-v2/`): made by the team in Blender for this game. Notes: `design/blender/MANIFEST.md`.
- Character sprites, backgrounds and maps: made by the team for this game.

## Libraries and tools

- [LibGDX](https://libgdx.com/) 1.13.1 (Apache License 2.0)
- [Tiled](https://www.mapeditor.org/) map editor
- JUnit 5
