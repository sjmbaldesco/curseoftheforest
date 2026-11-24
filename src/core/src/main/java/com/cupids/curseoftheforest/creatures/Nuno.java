package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.minigames.Riddle;
import com.cupids.curseoftheforest.player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Asks three riddles and rewards each correct offering with a potion. If fear breaks Dayo, the
 * Nuno takes its potions back and starts again from the first riddle ({@link #reset}).
 */
public class Nuno extends Creature {
    private final Riddle[] riddles = {
            new Riddle("Brown pod, sour pulp. What fruit am I?", "Sampalok"),
            new Riddle("Scaly green skin, sweet white flesh, and black seeds. What fruit am I?", "Atis"),
            new Riddle("I grow on a vine and turn orange inside. What vegetable am I?", "Kalabasa")
    };
    private final PotionType[] rewards = {PotionType.STUN, PotionType.DAMAGE, PotionType.HEAL};
    /** The potions handed out so far, so a reset can take exactly these back. */
    private final List<Potion> given = new ArrayList<>();
    private int answered;
    private boolean started;

    public Nuno(float x, float y) {
        super("Nuno", x, y);
    }

    @Override
    public void interact(Player player) {
        java.util.Objects.requireNonNull(player, "player");
        started = true;
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean isGreeted() { return hasStarted(); }

    public int getAnsweredCount() { return answered; }

    public String getQuestion() {
        return started && !isComplete() ? riddles[answered].getQuestion() : "";
    }

    public boolean offer(Player player, String food) {
        if (!started || isComplete() || !riddles[answered].isCorrect(food)) {
            return false;
        }
        Potion potion = new Potion(rewards[answered]);
        player.getInventory().add(potion);
        given.add(potion);
        answered++;
        return true;
    }

    /**
     * Back to the first riddle: the Nuno takes back every potion it gave, so failing cannot be used
     * to collect extra potions. Dayo stays greeted, so he does not need to say "Tabi-tabi po" again.
     */
    public void reset(Player player) {
        java.util.Objects.requireNonNull(player, "player");
        for (Potion potion : given) {
            player.getInventory().remove(potion);
        }
        given.clear();
        answered = 0;
    }

    public boolean isComplete() {
        return answered == riddles.length;
    }
}
