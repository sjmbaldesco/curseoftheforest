package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.Test;

class FoliageTest {
    /** Stand-in sprites: one empty frame each, so plants can be grown without graphics. */
    private static final Animation<TextureRegion> GRASS = new Animation<>(0.1f, new TextureRegion());
    private static final Animation<TextureRegion> FERN = new Animation<>(0.1f, new TextureRegion());

    /** A flat floor 200 tiles wide (row 0) with a 3-tile ledge on row 4 at columns 10 to 12. */
    private static TileMap floorWithLedge() {
        boolean[][] solid = new boolean[8][200];
        for (int col = 0; col < 200; col++) solid[0][col] = true;
        for (int col = 10; col <= 12; col++) solid[4][col] = true;
        return new TileMap(solid, 32);
    }

    @Test
    void plantsStandOnTopOfGroundTilesOnly() {
        Foliage foliage = new Foliage(floorWithLedge(), x -> true, GRASS, FERN);
        assertFalse(foliage.plants.isEmpty());
        for (Foliage.Plant plant : foliage.plants) {
            assertTrue(plant.y() == 32f || plant.y() == 160f, "plant at y " + plant.y() + " floats in the air");
        }
    }

    @Test
    void theSameMapAlwaysGrowsTheSamePlants() {
        Foliage first = new Foliage(floorWithLedge(), x -> true, GRASS, FERN);
        Foliage second = new Foliage(floorWithLedge(), x -> true, GRASS, FERN);
        assertEquals(first.plants, second.plants);
        // Roughly a third to a half of the 203 surface tiles get a plant: lively but not a carpet.
        assertTrue(first.plants.size() > 50 && first.plants.size() < 110, "plants: " + first.plants.size());
    }

    @Test
    void bareAreasAndMissingSpritesGrowNothing() {
        Foliage leftOnly = new Foliage(floorWithLedge(), x -> x < 32 * 50, GRASS, FERN);
        assertTrue(leftOnly.plants.stream().allMatch(p -> p.x() < 32 * 50));
        assertTrue(new Foliage(floorWithLedge(), x -> true, null, null).plants.isEmpty());
    }
}
