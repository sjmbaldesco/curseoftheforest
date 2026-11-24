package com.cupids.curseoftheforest.creatures;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.player.Player;
import org.junit.jupiter.api.Test;

class TorchTest {

    @Test
    void burnsFiveSecondsThenNeedsTenToRelight() {
        Torch torch = new Torch(new Rectangle(100, 0, 16, 32));
        Player dayo = new Player(0, 0);
        assertTrue(torch.isReady());

        torch.interact(dayo);
        assertTrue(torch.isLit());
        torch.update(Torch.LIT_SECONDS - 0.1f);
        assertTrue(torch.isLit());
        torch.update(0.2f);
        assertFalse(torch.isLit());

        torch.interact(dayo); // still cooling down: nothing happens
        assertFalse(torch.isLit());
        torch.update(Torch.RELIGHT_SECONDS);
        assertTrue(torch.isReady());
        torch.interact(dayo);
        assertTrue(torch.isLit());
    }

    @Test
    void lightReachesOnlyItsRadiusAndOnlyWhileBurning() {
        Torch torch = new Torch(new Rectangle(100, 0, 16, 32)); // centre (108, 16)
        assertFalse(torch.lights(108, 16)); // not lit yet
        torch.interact(new Player(0, 0));
        assertTrue(torch.lights(108 + Torch.LIGHT_RADIUS - 1, 16));
        assertFalse(torch.lights(108 + Torch.LIGHT_RADIUS + 1, 16));
    }

    @Test
    void wakwakCrawlsInLightAndBacksOffAfterABite() {
        Wakwak wakwak = new Wakwak(0, 0);
        assertTrue(wakwak.getSpeed() == Wakwak.FLIGHT_SPEED);
        wakwak.setSlowed(true);
        assertTrue(wakwak.getSpeed() < Wakwak.FLIGHT_SPEED / 2);

        Player dayo = new Player(0, -Player.HEIGHT - 16); // Dayo's bite point is the Wakwak's spot
        wakwak.interact(dayo);
        assertTrue(wakwak.hasCaughtDayo());
        wakwak.retreat();
        assertFalse(wakwak.hasCaughtDayo(), "a retreating Wakwak cannot bite again at once");
        wakwak.update(Wakwak.RETREAT_SECONDS + 0.1f);
        assertFalse(wakwak.isRetreating());
    }
}
