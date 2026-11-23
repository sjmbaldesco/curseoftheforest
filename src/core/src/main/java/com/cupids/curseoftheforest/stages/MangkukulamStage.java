package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Mangkukulam;
import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionDuel;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.MenuText;
import com.cupids.curseoftheforest.screens.Theme;

/** Potion duel against a witch who hides her choice, then the final story scenes. */
public class MangkukulamStage extends GameStage {
    private Mangkukulam witch;
    private String message;
    private CutScene scene;
    private int sceneIndex;

    public MangkukulamStage(ForestGame game) {
        super(game, "Stage 5: Ang Tukso ng Mangkukulam", "maps/mangkukulam.tmx");
    }

    @Override
    protected void init() {
        if (scene != null) scene.dispose();
        // The flashback plays once; a retry goes straight back to the duel.
        scene = retried ? null
                : new CutScene(CutScene.flashbackTwo(), game.getAssetLoader().loadImage("images/backgrounds/flashback-2.png"));
        sceneIndex = retried ? 1 : 0;
        message = "Approach the Mangkukulam and press E.";
        witch = new Mangkukulam(480, 192);
        witch.setSpriteSheet(game.getAssetLoader().loadImage("images/characters/mangkukulam.png"), 5);
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && witch.isNear(player)) witch.interact(player);
        PotionDuel duel = witch.getDuel();
        if (duel == null || duel.isOver()) return;
        for (int i = 0; i < PotionType.values().length; i++) {
            if (!Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) continue;
            PotionType choice = PotionType.values()[i];
            for (Potion potion : duel.getPlayerPotions()) {
                if (potion.getType() == choice) {
                    PotionType enemy = witch.choosePotion();
                    duel.resolveRound(potion, enemy);
                    String reveal = "She threw " + enemy + ". ";
                    message = reveal + (choice == enemy ? "Tie: both potions are spent."
                            : choice.beats(enemy) ? "Your potion wins! Enemy loses 2 health."
                            : "Enemy potion wins! You lose 2 health.");
                    return;
                }
            }
            message = "No " + choice + " potion remaining. Choose another.";
            return;
        }
    }

    @Override
    public void update(float delta) {
        witch.update(delta);
        PotionDuel duel = witch.getDuel();
        if (duel == null) return;
        if (duel.isOver() && !duel.isWon()) {
            message = "The duel is lost. Retry restores your stage-entry potions.";
            failed = true;
        }
        if (duel.isWon() && reachedExit() && !isFailed() && sceneIndex == 1) {
            boolean hasGuitarStrings = player.getInventory().has(Shop.KWERDAS);
            scene = new CutScene(CutScene.serenade(hasGuitarStrings),
                    game.getAssetLoader().loadImage("images/backgrounds/serenade.png"));
            sceneIndex = 2;
        }
    }

    @Override
    public void render(float delta) {
        if (scene != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.toMainMenu(); return; }
            scene.render(delta);
            if (scene.isFinished()) {
                scene.dispose();
                scene = null;
                if (sceneIndex == 0) sceneIndex = 1;
                else if (sceneIndex == 2) {
                    scene = new CutScene(CutScene.ending(), game.getAssetLoader().loadImage("images/backgrounds/ending.png"));
                    sceneIndex = 3;
                } else {
                    completed = true;
                    game.onStageComplete(this);
                }
            }
            return;
        }
        super.render(delta);
        if (game.getScreen() != this || scene != null) return;
        SpriteBatch batch = game.getBatch();
        hud.beginWorld(batch);
        witch.render(batch);
        batch.end();
        MenuText.begin(batch);
        Theme.hintPanel(batch);
        game.getFont().setColor(Theme.PAPER);
        game.getFont().draw(batch, message, 20, 175);
        PotionDuel duel = witch.getDuel();
        if (duel != null) {
            game.getFont().draw(batch, "Dayo HP: " + duel.getPlayerHealth() + "    Mangkukulam HP: " + duel.getEnemyHealth(), 20, 150);
            game.getFont().draw(batch, duel.isWon() ? "You won! Walk to the far right exit." : "The Mangkukulam hides her potion. Choose yours.", 20, 125);
        }
        game.getFont().draw(batch, "1 Stun > Damage     2 Damage > Heal     3 Heal > Stun", 20, 100);
        game.getFont().draw(batch, "Each potion is used once. Ties cost a potion. Higher HP wins if supplies run out.", 20, 75);
        batch.end();
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        super.dispose();
    }
}
