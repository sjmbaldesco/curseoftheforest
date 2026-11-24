package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Nuno;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.DialogueBox;

/** Three food riddles reward the three potion types needed by the next stage. */
public class TulayStage extends GameStage {
    private static final String[] OFFERINGS = {"Atis", "Sampalok", "Kalabasa"};
    private Nuno nuno;
    private TextureRegion nunoPortrait;
    /** The riddle currently shown in the question box, so it is only re-opened when it changes. */
    private String askedQuestion = "";
    private CutScene scene;

    public TulayStage(ForestGame game) {
        super(game, "Stage 4: Tulay ng Nuno sa Punso", "maps/tulay.tmx");
    }

    @Override
    protected void init() {
        if (scene != null) scene.dispose();
        // The flashback plays once; a retry goes straight back to the riddles.
        scene = retried ? null
                : new CutScene(CutScene.flashbackOne(), game.getAssetLoader().loadImage("images/backgrounds/flashback-1.png"));
        nuno = new Nuno(320, 256);
        Texture nunoSheet = game.getAssetLoader().loadImage("images/characters/nuno.png");
        nuno.setSpriteSheet(nunoSheet, 5);
        nunoPortrait = DialogueBox.portraitFrom(nunoSheet, 5);
        askedQuestion = "";
        setGoal("Greet the Nuno: Tabi-tabi po!");
        boolean hasJuice = player.getInventory().has(Shop.MALUNGGAY_JUICE);
        timer = new StageTimer(hasJuice ? 300 : 180);
    }

    @Override
    public void update(float delta) {
        nuno.update(delta);
        completed = nuno.isComplete() && reachedExit() && !isFailed();
        if (nuno.isComplete()) {
            setGoal("Cross the bridge to the far right exit");
        } else if (nuno.hasStarted()) {
            setGoal("Answer the Nuno's riddles (" + nuno.getAnsweredCount() + "/3)");
        }
        showRiddle();
    }

    /** Keeps the riddle box up while Dayo stands by the Nuno, and puts it away when he leaves. */
    private void showRiddle() {
        boolean riddleTime = nuno.hasStarted() && !nuno.isComplete() && nuno.isNear(player);
        if (!riddleTime) {
            if (dialogue.isOpen() && !dialogue.isBlocking()) dialogue.close();
            askedQuestion = "";
            return;
        }
        String question = nuno.getQuestion();
        if (!question.equals(askedQuestion) || !dialogue.isOpen()) {
            dialogue.ask(nuno.getName(), nunoPortrait, question, OFFERINGS);
            askedQuestion = question;
        }
    }

    @Override
    protected void handleInput() {
        if (!nuno.hasStarted() && nuno.isNear(player)) {
            showPrompt("E", "Greet", nuno.getX() + 32, nuno.getY() + 64);
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                nuno.interact(player);
                dialogue.say(nuno.getName(), nunoPortrait,
                        "Who walks over my mound without asking?",
                        "Answer my riddles with the right offering, and I will give you a potion for each.");
            }
            return;
        }
        if (!nuno.hasStarted() || !nuno.isNear(player) || nuno.isComplete() || isFailed()) return;
        for (int i = 0; i < OFFERINGS.length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                if (nuno.offer(player, OFFERINGS[i])) {
                    toast.show("Correct! The Nuno gives you a potion.");
                } else {
                    player.changeSanity(-10);
                    toast.warn("That is not the offering. Sanity -10");
                }
                break;
            }
        }
    }

    @Override
    protected void drawWorld(SpriteBatch batch) {
        nuno.render(batch);
    }

    @Override
    public void render(float delta) {
        if (scene != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.toMainMenu(); return; }
            scene.render(delta);
            if (scene.isFinished()) { scene.dispose(); scene = null; }
            return;
        }
        super.render(delta);
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        super.dispose();
    }
}
