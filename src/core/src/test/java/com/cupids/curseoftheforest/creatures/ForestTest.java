package com.cupids.curseoftheforest.creatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.items.Weapon;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.stages.StageTimer;
import org.junit.jupiter.api.Test;

/** Stage 3 pieces: the glowing bush search and the Tikbalang's timer. */
class ForestTest {
    private static final Rectangle SPOT = new Rectangle(0, 0, 32, 32);

    @Test
    void weaponShortensTheSearch() {
        Player dayo = new Player(0, 0);
        GlowingBush bush = new GlowingBush(SPOT, null, 0, 8f);
        assertEquals(8f, bush.searchTimeFor(dayo), 0.001f);
        dayo.getInventory().add(new Weapon(Shop.PUNYAL, 3, "", Shop.PUNYAL_CUT_SPEED));
        assertEquals(8f / 1.5f, bush.searchTimeFor(dayo), 0.001f);
        dayo.getInventory().add(new Weapon(Shop.ITAK, 3, "", Shop.ITAK_CUT_SPEED));
        assertEquals(4f, bush.searchTimeFor(dayo), 0.001f);
    }

    @Test
    void searchFinishesAfterTheTimeAndGivesThePieceAndCoinsOnce() {
        Player dayo = new Player(0, 0);
        GlowingBush bush = new GlowingBush(SPOT, "Baliktarin", 2, 4f);
        bush.interact(dayo);
        bush.update(3f);
        assertTrue(bush.isSearching());
        assertNull(bush.takePiece(), "No piece before the search ends");
        bush.update(1f);
        assertTrue(bush.isSearched());
        assertEquals(1f, bush.getProgress());
        assertEquals(2, dayo.getCoins());
        assertEquals("Baliktarin", bush.takePiece());
        assertNull(bush.takePiece(), "A piece is given only once");

        bush.interact(dayo);
        bush.update(10f);
        assertEquals(2, dayo.getCoins(), "A searched bush gives nothing more");
    }

    @Test
    void cancellingLosesProgress() {
        Player dayo = new Player(0, 0);
        GlowingBush bush = new GlowingBush(SPOT, null, 1, 4f);
        bush.interact(dayo);
        bush.update(3f);
        bush.cancelSearch();
        assertFalse(bush.isSearching());
        assertEquals(0f, bush.getProgress());
        bush.interact(dayo);
        bush.update(3f);
        assertFalse(bush.isSearched());
    }

    @Test
    void invalidBushesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GlowingBush(SPOT, null, 0, 0f));
        assertThrows(IllegalArgumentException.class, () -> new GlowingBush(SPOT, null, -1, 5f));
    }

    @Test
    void tikbalangStartsTheForestTimer() {
        StageTimer timer = new StageTimer(90f, false);
        Interactable tikbalang = new Tikbalang(0, 0, timer);
        timer.tick(5f);
        assertEquals(90f, timer.getRemaining());
        tikbalang.interact(new Player(0, 0));
        assertTrue(timer.isRunning());
        timer.tick(5f);
        assertEquals(85f, timer.getRemaining(), 0.001f);
    }
}
