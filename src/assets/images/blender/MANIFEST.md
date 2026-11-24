# Blender exports: Curse of the Forest

Made 2026-11-23 in `../Assets.blend` (scene **Sprites**; one collection per asset; build code in the text block `cotf_lib.py`).
Every file is a transparent PNG at its final pixel size, with hard pixel edges and a 1 px dark outline, matching the repo's pixel-art sprites (Dayo, Nuno, coin, potions).
Nothing in the repo was touched. Copy files in when wiring them up.

## Tiles (32 x 32)
Each tile repeats cleanly sideways (and up/down for fills). Tileset strips are 1 row, left to right in this order:

`tiles/forest_tileset.png` (11 tiles, 352 x 32). Stage 3 forest set
| # | file | use |
|---|---|---|
| 0 | grass_left | ground top, left edge |
| 1 | grass_mid | ground top, repeat |
| 2 | grass_right | ground top, right edge |
| 3 | dirt_left | ground fill, left edge |
| 4 | dirt_mid | ground fill, repeat |
| 5 | dirt_right | ground fill, right edge |
| 6 | dirt_roots | fill variant with roots |
| 7-9 | log_left / log_mid / log_right | one-way log platform (top 60% of tile) |
| 10 | stone_mossy | stone block with moss top |

`tiles/shared_tileset.png` (5 tiles, 160 x 32). Shared set
| # | file | use |
|---|---|---|
| 0 | stone_top | stone wall, top row |
| 1 | stone_fill | stone wall fill |
| 2-4 | plank_left / plank_mid / plank_right | wooden plank platform (top half of tile) |

Single tiles are also in `tiles/forest/` and `tiles/shared/`.

## Props
| file | size | notes |
|---|---|---|
| props/bush_glowing.png | 64 x 64 | bush with glowing berries and a soft gold halo (not searched yet) |
| props/bush_searching.png | 64 x 64 | same, with leaves flying off (while holding E) |
| props/bush_searched.png | 64 x 64 | darker, no glow, fallen leaves (emptied) |
| props/coin_sheet.png | 256 x 32 | 8 frames of 32 x 32, left to right, loops (half-turn; both faces match) |
| props/potion-red.png, potion-green.png, potion-light-blue.png | 64 x 64 | same names as the repo's current 1254 px potions, so they drop in |

## Item icons (48 x 48, HUD)
`icons/itak.png`, `punyal.png`, `agimat-ni-enteng.png`, `malunggay-juice.png`, `kwerdas.png`, `rosary.png`, `lumang-gitara.png`.
The shop name is "Agimat ni Enteng", so the file uses that name.

## Parallax (1920 x 540, 3 layers per stage)
`parallax/<stage>/far.png`, `mid.png`, `near.png` for stage1-simula, stage2-palengke, stage3-gubat, stage4-tulay, stage5-mangkukulam.
- far: tree line, palms and hills, with a thin mist at the base. Suggested scroll speed 0.2x.
- mid: trunks, branches, hanging vines and canopy. Suggested 0.5x.
- near: grass, ferns and low mist along the bottom. Suggested 0.9x (draw in front of the stage background, behind the player).
- Each layer wraps sideways seamlessly. Drawn at 640 x 180 and scaled 3x with no smoothing, so the pixels are as chunky as the stage backgrounds.
- Colours follow each stage: teal (1), dusky violet (2), deep green (3), the Stage 4 blue, the Stage 5 purple.

## Other files
- `contact-sheet.png`: everything on one page.
- `_preview/`: raw Blender renders and test sheets. Safe to ignore or delete.

## Wiring notes (for the visuals thread)
- Load PNGs with `Texture.TextureFilter.Nearest` to keep pixels crisp.
- Coin: `TextureRegion.split(tex, 32, 32)[0]` into an `Animation` at about 0.08 s per frame.
- Parallax wrap: `tex.setWrap(Repeat, ClampToEdge)` and draw with a u-offset of `cameraX * speed / 1920`.
- Tiled: add the tilesets as new tilesets (32 x 32, 0 margin, 0 spacing); `tiles.png` stays as-is so existing maps keep working.

---

# Update 2026-11-24: animated ambience and parallax v2

## Animated sheets (`animated/`), one row, frames left to right, all loop
| file | frame size | frames | suggested speed | use |
|---|---|---|---|---|
| bush_glowing_idle.png | 64 x 64 | 6 | 0.15 s | unsearched bush: leaves sway, berries and halo pulse (replaces static bush_glowing) |
| torch_lit.png | 32 x 64 | 8 | 0.08 s | Wakwak Cave torch, burning (flame, sparks, light halo) |
| torch_unlit.png | 32 x 64 | 1 | - | torch out / relighting (ember and smoke) |
| coin_spin_glow.png | 32 x 32 | 8 | 0.08 s | coin spin with pulsing gold halo; add a bob in code (y + 2*sin(t*3)) |
| potion-red_glow.png, potion-green_glow.png, potion-light-blue_glow.png | 64 x 64 | 8 | 0.12 s | rising bubbles and pulsing halo; bob in code like the coin |
| grass_tuft_sway.png | 32 x 32 | 6 | 0.15 s | grass tufts to scatter on ground tiles |
| fern_sway.png | 48 x 48 | 6 | 0.18 s | fern decoration |
| firefly.png | 16 x 16 | 8 | 0.1 s | one firefly; spawn many with random offsets and slow drift |
| leaf_green_tumble.png / leaf_brown_tumble.png / leaf_violet_tumble.png | 16 x 16 | 8 | 0.1 s | falling leaves; move down-and-sideways in code. Green: Stage 3, brown: Stages 1 to 2, violet: Stage 5 |

`animated/fog/fog_<stage>.png` (960 x 180, transparent, tiles sideways): a soft fog band per stage. Scroll it slowly in code (about 8 px/s), draw over the near layer at about 70% opacity. Made in Python, not Blender (it's just a stepped-transparency band).

## Parallax v2 (`parallax-v2/<stage>/far.png, mid.png, near.png`, 1920 x 540)
Richer than v1, following the UI-REVAMP parallax reference:
- far: two ranks of tree lines on hills (back rank flatter for depth), palms.
- mid: full trees with crowns, saplings, hanging vines.
- near: grass with ferns, rocks, red mushrooms, white flowers and a mossy stump.
- New set **stage2-wakwak-cave**: rock wall with dark tunnels (far), stalactites, stalagmites and columns (mid), rocky floor with blue crystals (near).
- Same stages and colours as v1. v1 stays in `parallax/` in case you prefer it.

Contact sheets: `contact-sheet-animated.png`, `contact-sheet-parallax-v2.png`.
