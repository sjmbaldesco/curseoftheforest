package com.cupids.curseoftheforest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.GameOverScreen;
import com.cupids.curseoftheforest.screens.MainMenuScreen;
import com.cupids.curseoftheforest.stages.GameStage;
import com.cupids.curseoftheforest.stages.GubatStage;
import com.cupids.curseoftheforest.stages.MangkukulamStage;
import com.cupids.curseoftheforest.stages.PalengkeStage;
import com.cupids.curseoftheforest.stages.SimulaStage;
import com.cupids.curseoftheforest.stages.TulayStage;

/**
 * Main game class. Owns the resources shared by every screen (batch, font, assets, the player)
 * and decides which screen comes next: menu, the five stages in order, and game over.
 */
public class ForestGame extends Game {
    public static final int WORLD_WIDTH = 960;
    public static final int WORLD_HEIGHT = 540;
    public static final int STAGE_COUNT = 5;
    /** Art hook: the intro's own background; Stage 4's bridge is the stand-in until it exists. */
    private static final String INTRO_BACKGROUND = "images/backgrounds/intro.png";
    private static final String INTRO_FALLBACK = "images/backgrounds/tulay.png";

    private AssetLoader assetLoader;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private GameStage currentStage;
    private CutScene introScene;
    private int stageIndex;

    @Override
    public void create() {
        assetLoader = new AssetLoader();
        batch = new SpriteBatch();
        font = new BitmapFont();
        setScreen(new MainMenuScreen(this));
    }

    /** Starts a new run with a fresh Dayo: the introduction first, then Stage 1. */
    public void newGame() {
        player = new Player(0, 0);
        player.setSprite(assetLoader.loadImage("images/player.png"));
        playIntro();
    }

    private void playIntro() {
        disposeIntro();
        String background = Gdx.files.internal(INTRO_BACKGROUND).exists() ? INTRO_BACKGROUND : INTRO_FALLBACK;
        introScene = new CutScene(CutScene.intro(), assetLoader.loadImage(background)) {
            @Override
            public void render(float delta) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    toMainMenu();
                    return;
                }
                super.render(delta);
            }
        };
        introScene.setOnFinished(() -> startStage(0));
        setScreen(introScene);
    }

    private void disposeIntro() {
        if (introScene != null) {
            introScene.dispose();
            introScene = null;
        }
    }

    private GameStage createStage(int index) {
        return switch (index) {
            case 0 -> new SimulaStage(this);
            case 1 -> new PalengkeStage(this);
            case 2 -> new GubatStage(this);
            case 3 -> new TulayStage(this);
            case 4 -> new MangkukulamStage(this);
            default -> throw new IllegalArgumentException("No stage " + index);
        };
    }

    private void startStage(int index) {
        disposeIntro();
        disposeCurrentStage();
        stageIndex = index;
        currentStage = createStage(index);
        currentStage.begin();
        setScreen(currentStage);
    }

    /** Called by a stage when its goal is met. */
    public void onStageComplete(GameStage stage) {
        if (stageIndex + 1 < STAGE_COUNT) {
            startStage(stageIndex + 1);
        } else {
            // Stage 5 already played the serenade and the ending (Ang Sagot); the journey is over.
            toMainMenu();
        }
    }

    /** Called by a stage when Dayo falls, runs out of time, or loses all Courage or Sanity. */
    public void onStageFailed(GameStage stage) {
        setScreen(new GameOverScreen(this, stage));
    }

    public void retryStage(GameStage stage) {
        stage.retry();
        setScreen(stage);
    }

    public void toMainMenu() {
        disposeIntro();
        disposeCurrentStage();
        setScreen(new MainMenuScreen(this));
    }

    private void disposeCurrentStage() {
        if (currentStage != null) {
            currentStage.dispose();
            currentStage = null;
        }
    }

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeIntro();
        disposeCurrentStage();
        batch.dispose();
        font.dispose();
        assetLoader.dispose();
    }
}
