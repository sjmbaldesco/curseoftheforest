"""Turns a TrueType font into a LibGDX bitmap font (BMFont text .fnt + .png atlas).

The game loads bitmap fonts, so a new .ttf only needs this script, no extra libraries in the game.
Glyphs are rendered at 2x the size they appear at in the 960x540 window, so text stays sharp in
1920x1080 full screen; Theme draws the font at half scale.

Usage (from the repo root):
    python3 tools/make_bitmap_font.py src/assets/fonts/VCR_OSD_MONO_1.001.ttf src/assets/fonts/vcr 32
"""
import sys
from PIL import Image, ImageDraw, ImageFont

CHARS = "".join(chr(c) for c in range(32, 127)) + "ñÑéáíóú’‘“”"


def main(ttf, out_base, size):
    font = ImageFont.truetype(ttf, size)
    # The baseline is where capital letters stand (this font reports no descent of its own).
    ascent = font.getbbox("H", anchor="la")[3]
    glyphs = []
    for ch in CHARS:
        if ch != " " and (font.getmask(ch).getbbox() is None or font.getmask(ch) == font.getmask("\uffff")):
            continue  # the font has no drawing for this character
        box = font.getbbox(ch, anchor="la")  # relative to the line top
        glyphs.append((ch, box, round(font.getlength(ch))))

    # Shelf-pack the glyphs into a 512-wide atlas with 2 px gaps.
    width, pad = 512, 2
    x = y = pad
    shelf = 0
    places = []
    for ch, (l, t, r, b), adv in glyphs:
        w, h = max(0, r - l), max(0, b - t)
        if x + w + pad > width:
            x, y, shelf = pad, y + shelf + pad, 0
        places.append((x, y, w, h))
        x += w + pad
        shelf = max(shelf, h)
    height = 1
    while height < y + shelf + pad:
        height *= 2

    atlas = Image.new("RGBA", (width, height), (255, 255, 255, 0))
    lowest = 0
    for (ch, (l, t, r, b), adv), (px, py, w, h) in zip(glyphs, places):
        if w and h:
            mask = Image.new("L", (w, h), 0)
            ImageDraw.Draw(mask).text((-l, -t), ch, font=font, fill=255, anchor="la")
            atlas.paste(Image.new("RGBA", (w, h), (255, 255, 255, 255)), (px, py), mask)
        lowest = max(lowest, b)

    line_height = round(size * 1.25)
    name = out_base.replace("\\", "/").split("/")[-1]
    lines = [
        f'info face="{font.getname()[0]}" size={size} bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1',
        f"common lineHeight={line_height} base={ascent} scaleW={width} scaleH={height} pages=1 packed=0",
        f'page id=0 file="{name}.png"',
        f"chars count={len(glyphs)}",
    ]
    for (ch, (l, t, r, b), adv), (px, py, w, h) in zip(glyphs, places):
        lines.append(f"char id={ord(ch)} x={px} y={py} width={w} height={h} xoffset={l} yoffset={t} xadvance={adv} page=0 chnl=15")
    atlas.save(out_base + ".png")
    with open(out_base + ".fnt", "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"{len(glyphs)} glyphs, atlas {width}x{height}, lineHeight {line_height}, base {ascent}, lowest ink {lowest}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2], int(sys.argv[3]))
