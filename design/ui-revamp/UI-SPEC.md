# Curse of the Forest: UI spec sheet (v1, 2026-11-24)

Look approved by Jai on 2026-11-24: carved narra-wood frames, thin gold hairline, near-black green fill, VCR OSD Mono font.

## Scale and files
- Screen 960x540. **1 art px = 2 screen px.** Every PNG in `ui/` is at **1x art scale**. Draw it at 2x with nearest filtering (`Texture.TextureFilter.Nearest`).
- All screen-px numbers below must be even, so they stay on the 2x grid.
- Mockups are in `mockups/` (960x540). `00-ui-pieces-sheet.png` shows every piece enlarged with its size.

## 9-slice pieces (LibGDX `NinePatch(texture, left, right, top, bottom)`, in art px)
| Piece | Size (1x) | Corners L/R/T/B | Used for |
|---|---|---|---|
| panel.png | 24x24 | 8/8/8/8 | menus, shop cards, game over, duel HP, credits |
| talkbox.png | 24x24 | 8/8/8/8 | talk box, cutscene caption |
| portrait-frame.png | 40x40 | 8/8/8/8 | 120x120 screen frame; portrait 96x96 drawn at +12,+12 |
| nametag.png | 32x14 | 6/6/6/6 | name on the talk-box top edge, 36 screen px tall |
| bar-frame.png | 40x10 | 3/3/3/3 | Courage/Sanity track (150x20 screen) |
| keycap.png | 14x14 | 4/4/4/4 | [E], [1] chips; 28x28 screen |
| toast.png, toast-warning.png | 24x16 | 5/5/5/5 | notices, footers, key-prompt label |
| button.png, button-selected.png | 40x16 | 6/6/6/6 | menu, answer and potion buttons |
| slot.png, slot-selected.png | 24x24 | 6/6/6/6 | item slots (48x48 screen), shop icon box, chest dials |
| hud-strip.png | 8x31 | stretch width only | HUD top band (960x62) |
| itembar-strip.png | 8x28 | stretch width only | item bar (960x56) |

Not 9-slice: bar-courage / bar-sanity / bar-drain (16x4, stretch to width), icons (12x12), icon-drain (8x7), pointer (6x7, flip it for the right-hand side), hp-pips (left 8x6 = full, right 8x6 = empty), chest-closed / chest-open (44x34, drawn at 4x in the mockup), banderitas (24x14 tile, repeat across the top of the menu), logo (220x70).

## Font
- **VCR OSD Mono** (in `UI-REVAMP\vcr_osd_mono.zip`) for everything. It replaces Pixelify Sans.
- Sizes (screen px): 14 small hints and descriptions, 16 HUD and labels, 20 dialogue and buttons, 24-28 titles, 48-56 "Game Over" / "The End".
- Generate with gdx-freetype (`FreeTypeFontParameter.mono = true`, `hinting = Full`), or make Hiero .fnt files at 16/20/28. Keep anti-aliasing **off**.
- The font has no `·`, `←→↑↓` or `▼` glyphs. Use `|`, `</>`, `^/v`, and draw the continue arrow as a small triangle (or use the pointer sprite rotated).
- Text shadow: 2 px down-right in night `#0B1A12`. Title text uses a 2 px night outline.

## Colours
| Use | Hex |
|---|---|
| Body text | paper `#FFF3D8` |
| Stage name, goal, hints, selected text | gold `#E8B83A` (selected button text `#F7D977`) |
| Secondary text (descriptions) | `#B9C7BD` |
| Dimmed / owned item name | `#7E8F84` |
| Warning text (timer under 10 s, Courage -10, not enough coins) | warning `#FF8A6B` |
| Owned / success | `#52B07C` |
| Panel fill | `#14241B` (paper text on it is about 13:1) |
| Name-tag text | night `#0B1A12` on paper |
| Wood ramp | `#2E1C12` `#4A2F1E` `#6E4629` `#94603A` `#B98250` |

## Layout (screen px, origin top-left)
**HUD (01):** band 0-62. Stage name at (16,6) size 16 gold. Bars on y=36: icon at x, label +28, frame 150x20 right after the label (+10), value 8 px after the frame. Courage starts at x=16, Sanity at x=380. Fill = frame inset 6 px on every side (8 px tall), width = (150-12) x value/100. Coin icon (700,6) + count size 20; timer icon (780,6) + "m:ss" size 20; goal right-aligned at (944,34) size 16 gold.
**Item bar (01):** band 484-540. 8 slots 48x48, gap 8, centred (x0=256), y=488. Icon 48x48 drawn at the slot origin. Slot number size 14 bottom-right with outline. "ITEMS" label at (16,508).
**Talk box (01, 10):** (16,84) 928x136 (answers: 928x176). Portrait frame (26,92) 120x120. Name tag at (176,66), height 36, width = text + 40. Text at x=170, first line y=112, line step 30, size 20. Hint bottom-right "[E] continue" + triangle, size 16 gold.
**Answer buttons (10):** inside the talk box at y=+100, 180x48 each, gap 14, starting x=170.
**Key prompt (01):** keycap 28x28 + label toast (24 px tall) to its right, centred 40 px above the object's head.
**Notices (01):** right-aligned to x=944, 32 px tall, 8 px gap, stacked downward. Start y=70 when no talk box, y=232 when the talk box is open. Max 3.
**Menu buttons (06, 07):** 300-320 x 48, centred; selected one gets pointers 30 px outside each side.

## UI animation notes
- Draining bar: swap the fill to `bar-drain.png` and back every 0.25 s while it drains, and show `icon-drain.png` next to the value.
- Timer under 10 s: text turns warning and blinks every 0.5 s.
- Talk text types at about 40 characters per second. [E] skips to the full line, then continues. The continue triangle blinks every 0.5 s.
- Selected button: pointers bob 2 px left/right every 0.4 s.
- Notice: fades in over 0.2 s, holds 2 s, fades out over 0.5 s. The ones below move up when one disappears.
- Key prompt: bobs 2 px up and down every 0.6 s.
- Shop: a card you cannot afford keeps the red box. A bought card shows "OWNED" and dims.

## Open items for Jai
- Portraits are crops of the in-game sprites. Swap in the new character art when it is ready (96x96 opening).
- Item descriptions on the shop cards come from the current code (Itak 2x, Punyal 1.5x, Agimat doubles Stage 3 time, Malunggay +25 Courage). Kwerdas reads "Serenade bonus" until its effect is final.
- The credits list three members (from the approved proposal), not four as the handoff brief said.
