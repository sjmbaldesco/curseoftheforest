package com.cupids.curseoftheforest.creatures;

import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.minigames.Riddle;
import com.cupids.curseoftheforest.player.Player;

/** Asks three riddles and rewards each correct offering with a potion. */
public class Nuno extends Creature {
    private final Riddle[] riddles = {
            new Riddle("Brown pod, sour pulp. What fruit am I?", "Sampalok"),
            new Riddle("Scaly green skin, sweet white flesh, and black seeds. What fruit am I?", "Atis"),
            new Riddle("I grow on a vine and turn orange inside. What vegetable am I?", "Kalabasa")
    };
    private final PotionType[] rewards = {PotionType.STUN, PotionType.DAMAGE, PotionType.HEAL};
    private int answered;
    private boolean started;

    public Nuno(float x, float y) {
        super("Nuno", x, y);
    }

    @Override
    public void interact(Player player) {
        started = true;
    }

    public boolean hasStarted() {
        return started;
    }

    public String getQuestion() {
        return started && !isComplete() ? riddles[answered].getQuestion() : "";
    }

    public boolean offer(Player player, String food) {
        if (!started || isComplete() || !riddles[answered].isCorrect(food)) {
            return false;
        }
        player.getInventory().add(new Potion(rewards[answered]));
        answered++;
        return true;
    }

    public boolean isComplete() {
        return answered == riddles.length;
    }
}
