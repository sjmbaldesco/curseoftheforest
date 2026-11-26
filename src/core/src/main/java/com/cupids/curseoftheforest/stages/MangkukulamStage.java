package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.cupids.curseoftheforest.screens.Hud;
import com.cupids.curseoftheforest.screens.Theme;
import com.cupids.curseoftheforest.screens.UiSkin;

/** Potion duel against a witch who hides her choice, then the final story scenes. */
public class MangkukulamStage extends GameStage {
    private static final String[] POTION_NAMES = {"Stun", "Damage", "Heal"};
    private static final String[] BEATS = {"beats Damage", "beats Heal", "beats Stun"};
    private Mangkukulam witch;
    private TextureRegion witchPortrait;
    /** Health and potions at the start of the duel, for the "3/4" counters and the round number. */
    private int startHealth;
    private int startPotions;
    /** What happened in the last round, shown in the result panel; empty before the first round. */
    private String lastThrows = "";
    private String lastOutcome = "";
    private boolean lastOutcomeBad;
    /** Clock for the bubbling bottles on the potion cards. */
    private float duelTime;
    private final java.util.Map<PotionType, Animation<TextureRegion>> bottles = new java.util.EnumMap<>(PotionType.class);
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
        lastThrows = "";
        lastOutcome = "";
        startHealth = 0;
        setGoal("Face the Mangkukulam");
    }

    @Override
    protected void handleInput() {
        if (witch.getDuel() == null && witch.isNear(player)) {
            showPrompt("E", "Face her", witch.getX() + 32, witch.getY() + 64);
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                witch.interact(player);
                if (witch.getDuel() != null) {
                    startHealth = witch.getDuel().getPlayerHealth();
                    startPotions = witch.getDuel().getPlayerPotions().size();
                }
            }
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
                    sfx("potion_throw");
                    sfx(choice == enemy ? "hit_stun" : choice.beats(enemy) ? "round_win" : "round_lose");
                    lastThrows = "You: " + POTION_NAMES[choice.ordinal()] + "  vs  " + witch.getName() + ": "
                            + POTION_NAMES[enemy.ordinal()];
                    lastOutcomeBad = false;
                    if (choice == enemy) {
                        lastOutcome = "A tie. Both potions are spent.";
                    } else if (choice.beats(enemy)) {
                        lastOutcome = POTION_NAMES[choice.ordinal()] + " beats " + POTION_NAMES[enemy.ordinal()] + "! She loses 2 HP.";
                    } else {
                        lastOutcome = POTION_NAMES[enemy.ordinal()] + " beats " + POTION_NAMES[choice.ordinal()] + "! You lose 2 HP.";
                        lastOutcomeBad = true;
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
        duelTime = (duelTime + delta) % 1000f;
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

    /** Sets the goal line while the duel runs and after it ends. */
    private void showDuel(PotionDuel duel) {
        if (duel.isOver()) {
            if (duel.isWon()) setGoal("You won! Walk to the far right exit");
            return;
        }
        setGoal("Win the potion duel");
    }

    /**
     * The duel screen (mockup 09), under the HUD: both health panels, the round, the last
     * round's result and one card per potion kind. Cards for kinds Dayo has run out of are dimmed.
     */
    @Override
    protected void drawOverlay(SpriteBatch batch) {
        PotionDuel duel = witch.getDuel();
        if (duel == null || (duel.isOver() && lastThrows.isEmpty())) {
            return;
        }
        float top = ForestGame.WORLD_HEIGHT;
        int maxHealth = Math.max(startHealth, duel.getPlayerHealth());
        healthPanel(batch, "Dayo", duel.getPlayerHealth(), maxHealth, 16, top - 142, false);
        healthPanel(batch, witch.getName(), duel.getEnemyHealth(), 3, 668, top - 142, true);
        int round = Math.max(1, startPotions - duel.getPlayerPotions().size() + (duel.isOver() ? 0 : 1));
        UiSkin.frame(batch, UiSkin.Frame.TOAST, 394, top - 112, 172, 32);
        Theme.textCentred(batch, Theme.Size.TALK, "Round " + round, 480, top - 86, Theme.GOLD);
        if (!lastThrows.isEmpty()) {
            UiSkin.frame(batch, UiSkin.Frame.PANEL, 244, top - 214, 472, 58);
            Theme.textCentred(batch, Theme.Size.BODY, lastThrows, 480, top - 168, Theme.PAPER);
            Theme.textCentred(batch, Theme.Size.BODY, lastOutcome, 480, top - 192, lastOutcomeBad ? Theme.WARNING : Theme.GOLD);
        }
        if (duel.isOver()) {
            return;
        }
        for (int i = 0; i < PotionType.values().length; i++) {
            PotionType type = PotionType.values()[i];
            long left = duel.getPlayerPotions().stream().filter(p -> p.getType() == type).count();
            potionCard(batch, i, type, left, 132 + i * 240, top - 480);
        }
        String hint = "[1-3] Choose potion    She hides hers until you throw";
        float hintWidth = Theme.width(Theme.font(Theme.Size.SMALL), hint) + 32;
        UiSkin.frame(batch, UiSkin.Frame.TOAST, Math.round((ForestGame.WORLD_WIDTH - hintWidth) / 4) * 2, top - 362, hintWidth, 28);
        Theme.textCentred(batch, Theme.Size.SMALL, hint, 480, top - 342, Theme.PAPER);
    }

    private void healthPanel(SpriteBatch batch, String name, int health, int max, float x, float y, boolean rightAligned) {
        UiSkin.frame(batch, UiSkin.Frame.PANEL, x, y, 276, 70);
        if (rightAligned) {
            Theme.textRight(batch, Theme.Size.TALK, name, x + 256, y + 60, Theme.WARNING);
        } else {
            Theme.text(batch, Theme.Size.TALK, name, x + 20, y + 60, Theme.GOLD);
        }
        TextureRegion full = UiSkin.region("hp-pips", 0, 0, 8, 8);
        TextureRegion empty = UiSkin.region("hp-pips", 8, 0, 8, 8);
        float pipX = rightAligned ? x + 104 : x + 20;
        for (int i = 0; i < max; i++) {
            TextureRegion pip = i < health ? full : empty;
            if (pip != null) {
                batch.draw(pip, pipX + i * 18, y + 14, 16, 16);
            } else {
                Theme.rect(batch, i < health ? Theme.COURAGE : Theme.NIGHT, pipX + i * 18, y + 16, 14, 12);
            }
        }
        Theme.textRight(batch, Theme.Size.BODY, health + "/" + max, x + 256, y + 30, Theme.PAPER);
    }

    private void potionCard(SpriteBatch batch, int index, PotionType type, long left, float x, float y) {
        float alpha = left > 0 ? 1f : 0.45f;
        batch.setColor(1f, 1f, 1f, alpha);
        UiSkin.frame(batch, UiSkin.Frame.BUTTON, x, y, 216, 114);
        Animation<TextureRegion> bottle = bottles.computeIfAbsent(type,
                t -> game.getAssetLoader().loadStrip(Hud.potionSheet(t), 64, 0.12f));
        if (bottle != null) {
            batch.draw(bottle.getKeyFrame(duelTime), x + 12, y + 26, 64, 64);
        }
        batch.setColor(Color.WHITE);
        Color text = left > 0 ? Theme.PAPER : Theme.DIM;
        Theme.text(batch, Theme.Size.BODY, "[" + (index + 1) + "]  x" + left, x + 88, y + 100, Theme.GOLD);
        Theme.text(batch, Theme.Size.HEADING, POTION_NAMES[index], x + 88, y + 74, text);
        Theme.text(batch, Theme.Size.SMALL, BEATS[index], x + 88, y + 34, left > 0 ? Theme.MUTED : Theme.DIM);
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
