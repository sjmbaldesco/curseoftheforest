package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Mangkukulam;
import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionDuel;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.DialogueBox;

/** Potion duel against a witch who hides her choice, then the final story scenes. */
public class MangkukulamStage extends GameStage {
    private static final String[] POTION_NAMES = {"Stun", "Damage", "Heal"};
    private Mangkukulam witch;
    private TextureRegion witchPortrait;
    /** The duel status shown in the question box, so the box is only refreshed when it changes. */
    private String askedStatus = "";
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
        witch = new Mangkukulam(480, 192);
        Texture witchSheet = game.getAssetLoader().loadImage("images/characters/mangkukulam.png");
        witch.setSpriteSheet(witchSheet, 5);
        witchPortrait = DialogueBox.portraitFrom(witchSheet, 5);
        askedStatus = "";
        setGoal("Face the Mangkukulam");
    }

    @Override
    protected void handleInput() {
        if (witch.getDuel() == null && witch.isNear(player)) {
            showPrompt("E", "Face her", witch.getX() + 32, witch.getY() + 64);
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) witch.interact(player);
        }
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
                    if (choice == enemy) {
                        toast.show(reveal + "Tie: both potions are spent.");
                    } else if (choice.beats(enemy)) {
                        toast.show(reveal + "Your potion wins! She loses 2 health.");
                    } else {
                        toast.warn(reveal + "Her potion wins! You lose 2 health.");
                    }
                    return;
                }
            }
            toast.warn("No " + choice + " potion left. Choose another.");
            return;
        }
    }

    @Override
    public void update(float delta) {
        witch.update(delta);
        PotionDuel duel = witch.getDuel();
        if (duel == null) return;
        if (duel.isOver() && !duel.isWon()) {
            failed = true;
        }
        showDuel(duel);
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
    }

    /** Keeps the duel's question box up to date while the duel runs; closes it when it ends. */
    private void showDuel(PotionDuel duel) {
        if (duel.isOver()) {
            if (dialogue.isOpen() && !dialogue.isBlocking()) dialogue.close();
            if (duel.isWon()) setGoal("You won! Walk to the far right exit");
            return;
        }
        setGoal("Potion duel: Stun > Damage > Heal > Stun");
        String status = "She hides her potion. Choose yours.   Dayo HP " + duel.getPlayerHealth()
                + "   Her HP " + duel.getEnemyHealth() + "   (potions left: " + duel.getPlayerPotions().size() + ")";
        if (!status.equals(askedStatus) || !dialogue.isOpen()) {
            dialogue.ask(witch.getName(), witchPortrait, status, POTION_NAMES);
            askedStatus = status;
        }
    }

    @Override
    protected void drawWorld(SpriteBatch batch) {
        witch.render(batch);
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        super.dispose();
    }
}
