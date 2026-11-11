package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Nuno;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.items.Potion;
import com.cupids.curseoftheforest.minigames.PotionType;
import com.cupids.curseoftheforest.minigames.Riddle;
import com.cupids.curseoftheforest.screens.MenuText;
import com.cupids.curseoftheforest.screens.CutScene;

/** Three food riddles reward the three potion types needed by the next stage. */
public class TulayStage extends GameStage {
    private final Riddle[] riddles = {
        new Riddle("I wear a rough brown coat, with water and white flesh inside. What am I?", "Buko"),
        new Riddle("Wrapped in a leaf, I am sticky rice served as a sweet treat. What am I?", "Suman"),
        new Riddle("I am a long yellow fruit that grows in a bunch. What am I?", "Saging")
    };
    private static final String[] OFFERINGS = {"Buko", "Suman", "Saging"};
    private Nuno nuno;
    private int answered;
    private String message;
    private CutScene scene;

    public TulayStage(ForestGame game) {
        super(game, "Stage 4: Tulay ng Nuno sa Punso", "maps/tulay.tmx");
    }

    @Override
    protected void init() {
        if (scene != null) scene.dispose();
        scene = new CutScene(CutScene.flashbackOne(), game.getAssetLoader().loadImage("images/backgrounds/tulay.png"));
        answered = 0;
        message = "Approach Nuno and press E. Tabi-tabi po!";
        nuno = new Nuno(320, 256);
        nuno.setSpriteSheet(game.getAssetLoader().loadImage("images/characters/nuno.png"), 5);
        boolean hasJuice = false;
        for (Item item : player.getInventory().getAll()) {
            if ("Malunggay Juice".equalsIgnoreCase(item.getName())) hasJuice = true;
        }
        timer = new StageTimer(hasJuice ? 300 : 180);
    }

    @Override
    public void update(float delta) {
        nuno.update(delta);
        completed = answered == riddles.length && reachedExit() && !isFailed();
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && nuno.isNear(player)) nuno.interact(player);
        if (!nuno.isGreeted() || !nuno.isNear(player) || answered == riddles.length) return;
        for (int i = 0; i < OFFERINGS.length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                if (riddles[answered].isCorrect(OFFERINGS[i])) {
                    PotionType reward = PotionType.values()[answered++];
                    player.getInventory().add(new Potion(reward));
                    message = "Correct! Received " + reward + " potion.";
                    if (answered == riddles.length) message += " Cross to the far right exit.";
                } else {
                    player.changeSanity(-10);
                    message = "That is not the offering. Sanity -10. Try again.";
                }
                break;
            }
        }
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
        if (game.getScreen() != this) return;
        SpriteBatch batch = game.getBatch();
        batch.begin();
        nuno.render(batch);
        batch.end();
        MenuText.begin(batch);
        game.getFont().setColor(Color.WHITE);
        game.getFont().draw(batch, message, 20, 160);
        game.getFont().draw(batch, "Riddles: " + answered + "/3", 20, 135);
        if (nuno.isGreeted() && answered < riddles.length) {
            game.getFont().draw(batch, riddles[answered].getQuestion(), 20, 110);
            game.getFont().draw(batch, "Near Nuno: 1 Buko    2 Suman    3 Saging", 20, 85);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        super.dispose();
    }
}
