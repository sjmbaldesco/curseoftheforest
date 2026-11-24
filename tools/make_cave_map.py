"""Generates src/assets/maps/palengke.tmx: forest-edge parkour, Tonyo's stall, then the Wakwak Cave."""
import sys
W, H, T = 112, 17, 32
g = [[0] * W for _ in range(H)]

def fill(x0, x1, y0, y1, t=1):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            g[y][x] = t

# ---- Zone A: forest edge (x 0..39). Ground top is row 15.
ground_gaps = [(8, 9), (14, 16), (32, 33)]       # pits (Dayo can clear 5 tiles flat; keep <= 3)
def ground(x0, x1):
    fill(x0, x1, 15, 16)
x = 0
for a, b in ground_gaps + [(W, W)]:
    if x <= a - 1:
        ground(x, a - 1)
    x = b + 1
fill(23, 26, 13, 14)      # raised block, 2 tiles up
fill(15, 15, 12, 12)      # stepping stone over the 3-wide pit
fill(28, 30, 12, 12)      # small ledge

# ---- Zone B: the cave (x 40..111). Ceiling rows 0..6, floor rows 15..16.
fill(40, W - 1, 0, 6)
for sx in (46, 47, 58, 66, 67, 80, 92, 93, 104):   # stalactites hanging to row 7
    g[7][sx] = 1
for a, b in [(50, 51), (75, 76), (96, 97)]:        # cave pits
    fill(a, b, 15, 16, 0)
fill(55, 59, 12, 12)      # ledges 3 up
fill(61, 62, 9, 9)        # high ledge 6 up (from the row-12 ledge)
fill(70, 74, 12, 12)
fill(84, 88, 12, 12)
fill(89, 90, 9, 9)
fill(100, 103, 12, 12)
fill(W - 1, W - 1, 7, 14)  # back wall

def obj_px(cx, row_top, w=16, h=16):
    return cx * T + (T - w) // 2, row_top * T + (T - h) // 2, w, h

coins = []
# parkour: 3 coins
coins += [obj_px(8, 12), obj_px(24, 11), obj_px(29, 10)]
# cave: 12 coins (Q8 cap 15 total)
for cx, row in [(44, 14), (50, 12), (57, 10), (61, 8), (68, 14), (72, 10),
                (78, 14), (86, 10), (89, 8), (95, 14), (101, 10), (106, 14)]:
    coins.append(obj_px(cx, row))

torches = []   # 16 x 32, standing on a floor or ledge (row = tile the torch occupies)
for cx, row in [(42, 14), (53, 14), (57, 11), (62, 8), (72, 11), (81, 14), (89, 8), (99, 14), (102, 11)]:
    torches.append((cx * T + 8, row * T, 16, 32))

roosts = [(52, 8), (66, 8), (82, 8), (98, 8)]   # Wakwak start points, just under the ceiling

markers = {
    'spawn': (64, 448, 32, 32),
    'tonyo': (35 * T, 13 * T, 64, 64),
    'cave': (40 * T, 7 * T, 32, 8 * T),
    'exit': (109 * T, 12 * T, 32, 96),
}

out = []
oid = 1
out.append('<?xml version="1.0" encoding="UTF-8"?>')
out.append(f'<map version="1.10" tiledversion="1.11.0" orientation="orthogonal" renderorder="right-down" width="{W}" height="{H}" tilewidth="32" tileheight="32" infinite="0" nextlayerid="6" nextobjectid="NEXT">')
out.append(' <tileset firstgid="1" name="tiles" tilewidth="32" tileheight="32" tilecount="2" columns="2">')
out.append('  <image source="tiles.png" width="64" height="32"/>')
out.append(' </tileset>')
out.append(f' <layer id="1" name="solid" width="{W}" height="{H}">')
out.append('  <data encoding="csv">')
rows = []
for y in range(H):
    # a solid tile with open air above it is drawn as grass (tile 2), others as earth (tile 1)
    r = []
    for x in range(W):
        t = g[y][x]
        if t and (y == 0 or not g[y - 1][x]) and not (40 <= x and y <= 7):
            t = 2
        r.append(str(t))
    rows.append(','.join(r))
out.append(',\n'.join(rows))
out.append('  </data>')
out.append(' </layer>')
def group(gid, name, items, named=None):
    global oid
    out.append(f' <objectgroup id="{gid}" name="{name}">')
    for i, it in enumerate(items):
        x, y, w, h = it
        nm = f' name="{named[i]}"' if named else ''
        out.append(f'  <object id="{oid}"{nm} x="{x}" y="{y}" width="{w}" height="{h}"/>')
        oid += 1
    out.append(' </objectgroup>')
group(2, 'coins', coins)
group(3, 'torches', torches)
group(4, 'wakwak', [(cx * T, row * T, 32, 32) for cx, row in roosts])
group(5, 'markers', list(markers.values()), list(markers.keys()))
out.append('</map>')
text = '\n'.join(out).replace('NEXT', str(oid))
open(sys.argv[1], 'w').write(text + '\n')
print('coins', len(coins), 'torches', len(torches))
