package com.cupids.curseoftheforest.minigames;

import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Single-use potions: ties consume a potion; highest remaining health wins. */
public class PotionDuel {
    private int playerHealth;
    private int enemyHealth = 3;
    private int roundsPlayed;
    private final Player player;
    private final List<Potion> playerPotions = new ArrayList<>();

    public PotionDuel(Player player) {
        this.player = Objects.requireNonNull(player, "player");
        playerHealth = Math.max(1, (player.getCourage() + player.getSanity() + 49) / 50);
        for (Item item : player.getInventory().getAll()) {
            if (item instanceof Potion potion) playerPotions.add(potion);
        }
    }

    public void resolveRound(Potion potion, PotionType enemy) {
        if (potion == null || enemy == null || !playerPotions.contains(potion)
                || !player.getInventory().has(potion)) {
            throw new IllegalArgumentException("Choose an available potion.");
        }
        if (isOver()) throw new IllegalStateException("The duel has ended.");
        playerPotions.remove(potion);
        player.getInventory().remove(potion);
        roundsPlayed++;
        if (beats(potion.getType(), enemy)) enemyHealth = Math.max(0, enemyHealth - 2);
        else if (beats(enemy, potion.getType())) playerHealth = Math.max(0, playerHealth - 2);
    }

    public boolean beats(PotionType a, PotionType b) {
        if (a == null) throw new IllegalArgumentException("Choose a potion type.");
        return a.beats(b);
    }

    public boolean isOver() {
        return playerHealth == 0 || enemyHealth == 0 || playerPotions.isEmpty();
    }

    public boolean isWon() {
        return roundsPlayed > 0 && isOver() && playerHealth > 0 && (enemyHealth == 0 || playerHealth > enemyHealth);
    }

    public int getPlayerHealth() { return playerHealth; }
    public int getEnemyHealth() { return enemyHealth; }
    public List<Potion> getPlayerPotions() { return List.copyOf(playerPotions); }
}
