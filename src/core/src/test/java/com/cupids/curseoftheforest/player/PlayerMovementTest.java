package com.cupids.curseoftheforest.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cupids.curseoftheforest.stages.TileMap;
import org.junit.jupiter.api.Test;

class PlayerMovementTest {
    private static final int TILE = 32;
    private static final float FRAME = 1 / 60f;

    /** 10 x 6 map: solid floor on row 0 and a wall at column 6, rows 1 to 3. */
    private static TileMap testMap() {
        boolean[][] solid = new boolean[6][10];
        for (int col = 0; col < 10; col++) {
            solid[0][col] = true;
        }
        for (int row = 1; row <= 3; row++) {
            solid[row][6] = true;
        }
        return new TileMap(solid, TILE);
    }

    private static void step(Player p, TileMap map, int frames) {
        for (int i = 0; i < frames; i++) {
            p.update(FRAME, map);
        }
    }

    @Test
    void landsOnFloorAndStops() {
        TileMap map = testMap();
        Player p = new Player(64, 100);
        step(p, map, 120);
        assertEquals(TILE, p.getY(), 0.001f);
        assertTrue(p.isOnGround());
    }

    @Test
    void wallBlocksRunningRight() {
        TileMap map = testMap();
        Player p = new Player(64, TILE);
        for (int i = 0; i < 120; i++) {
            p.move(1);
            p.update(FRAME, map);
        }
        assertEquals(6 * TILE - Player.WIDTH, p.getX(), 0.001f);
    }

    @Test
    void jumpsOnlyFromTheGround() {
        TileMap map = testMap();
        Player p = new Player(64, TILE);
        step(p, map, 2);
        p.jump();
        step(p, map, 10);
        assertFalse(p.isOnGround());
        float heightAfterJump = p.getY();
        assertTrue(heightAfterJump > TILE);

        p.jump(); // in mid-air: must not boost again
        step(p, map, 1);
        assertTrue(p.getY() < heightAfterJump + Player.JUMP_SPEED * FRAME);
    }

    @Test
    void mapEdgesActAsWalls() {
        TileMap map = testMap();
        Player p = new Player(10, TILE);
        for (int i = 0; i < 60; i++) {
            p.move(-1);
            p.update(FRAME, map);
        }
        assertEquals(0, p.getX(), 0.001f);
    }
}
