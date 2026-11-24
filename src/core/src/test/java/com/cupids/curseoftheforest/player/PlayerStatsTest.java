package com.cupids.curseoftheforest.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cupids.curseoftheforest.items.Weapon;
import com.cupids.curseoftheforest.stages.Checkpoint;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

    @Test
    void statsStayBetweenZeroAndHundred() {
        Player p = new Player(0, 0);
        p.changeCourage(50);
        assertEquals(100, p.getCourage());
        p.changeSanity(-250);
        assertEquals(0, p.getSanity());
    }

    @Test
    void cannotSpendMoreCoinsThanOwned() {
        Player p = new Player(0, 0);
        p.addCoins(3);
        assertThrows(IllegalArgumentException.class, () -> p.spendCoins(5));
        assertEquals(3, p.getCoins());
    }

    @Test
    void cannotUseItemNotInInventory() {
        Player p = new Player(0, 0);
        Weapon itak = new Weapon("Lumang Itak", 3, "An old bolo.", 1.5f);
        assertThrows(IllegalArgumentException.class, () -> p.useItem(itak));
    }

    @Test
    void getWeaponPicksTheFastestOne() {
        Player p = new Player(0, 0);
        assertNull(p.getWeapon());
        Weapon punyal = new Weapon("Lumang Punyal", 3, "An old dagger.", 1.5f);
        Weapon itak = new Weapon("Lumang Itak", 3, "An old bolo.", 2f);
        p.getInventory().add(punyal);
        assertSame(punyal, p.getWeapon());
        p.getInventory().add(itak);
        assertSame(itak, p.getWeapon());
    }

    @Test
    void restoreUndoesChangesSinceCheckpoint() {
        Player p = new Player(0, 0);
        p.addCoins(4);
        Checkpoint start = p.saveCheckpoint();

        p.addCoins(6);
        p.changeCourage(-40);
        p.getInventory().add(new Weapon("Lumang Punyal", 3, "An old dagger.", 1.2f));

        p.restore(start);
        assertEquals(4, p.getCoins());
        assertEquals(100, p.getCourage());
        assertEquals(0, p.getInventory().getAll().size());
    }
}
