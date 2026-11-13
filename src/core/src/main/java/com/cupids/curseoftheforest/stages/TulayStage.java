package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Nuno;
import com.cupids.curseoftheforest.screens.MenuText;
import com.cupids.curseoftheforest.screens.CutScene;

/** Three food riddles reward the three potion types needed by the next stage. */
public class TulayStage extends GameStage {
    private static final String[] OFFERINGS = {"Atis", "Sampalok", "Kalabasa"};
    private Nuno nuno;
    private String message;
    private CutScene scene;

    public TulayStage(ForestGame game) {
        super(game, "Stage 4: Tulay ng Nuno sa Punso", "maps/tulay.tmx");
    }

    @Override
    protected void init() {
        if (scene != null) scene.dispose();
        scene = new CutScene(CutScene.flashbackOne(), game.getAssetLoader().loadImage("images/backgrounds/tulay.png"));
        message = "Approach Nuno and press E. Tabi-tabi po!";
        nuno = new Nuno(320, 256);
        nuno.setSpriteSheet(game.getAssetLoader().loadImage("images/characters/nuno.png"), 5);
        boolean hasJuice = player.getInventory().has("Malunggay Juice");
        timer = new StageTimer(hasJuice ? 300 : 180);
    }

    @Override
    public void update(float delta) {
        nuno.update(delta);
        completed = nuno.isComplete() && reachedExit() && !isFailed();
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && nuno.isNear(player)) nuno.interact(player);
        if (!nuno.hasStarted() || !nuno.isNear(player) || nuno.isComplete() || isFailed()) return;
        for (int i = 0; i < OFFERINGS.length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                if (nuno.offer(player, OFFERINGS[i])) {
                    message = "Correct! You received a potion.";
                    if (nuno.isComplete()) message += " Cross to the far right exit.";
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
        hud.beginWorld(batch);
        nuno.render(batch);
        batch.end();
        MenuText.begin(batch);
        game.getFont().setColor(Color.WHITE);
        game.getFont().draw(batch, message, 20, 160);
        game.getFont().draw(batch, "Riddles: " + nuno.getAnsweredCount() + "/3", 20, 135);
        if (nuno.hasStarted() && !nuno.isComplete()) {
            game.getFont().draw(batch, nuno.getQuestion(), 20, 110);
            game.getFont().draw(batch, "Near Nuno: 1 Atis    2 Sampalok    3 Kalabasa", 20, 85);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        super.dispose();
    }
}
