package com.cupids.curseoftheforest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.GameOverScreen;
import com.cupids.curseoftheforest.screens.Letterbox;
import com.cupids.curseoftheforest.screens.MainMenuScreen;
import com.cupids.curseoftheforest.screens.Theme;
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
    /** Dayo's 5-frame strip: 4 walking frames, then a jump frame. */
    private static final String DAYO_SHEET = "images/dayo.png";
    private static final int DAYO_FRAMES = 5;

    /** Music id for each stage, in stage order (audio/music/*.ogg). */
    private static final String[] STAGE_MUSIC = {"simula", "cave", "forest", "nuno", "duel"};

    private AssetLoader assetLoader;
    private final AudioManager audio = new AudioManager();
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Player player;
    private GameStage currentStage;
    private CutScene introScene;
    private int stageIndex;
    private final DisplayToggle displayToggle;

    /** Uses libGDX's standard fullscreen for F11. */
    public ForestGame() {
        this(null);
    }

    /** @param displayToggle how F11 switches to full screen; null for libGDX's standard fullscreen */
    public ForestGame(DisplayToggle displayToggle) {
        this.displayToggle = displayToggle != null ? displayToggle : DisplayToggle.standard();
    }

    /**
     * Each frame: F11 (or Alt+Enter) toggles full screen, then the current screen draws into the
     * letterboxed 960 by 540 area, so the game and its UI scale up without stretching.
     */
    @Override
    public void render() {
        boolean altEnter = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                && (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT));
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11) || altEnter) {
            displayToggle.toggleFullscreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            audio.toggleMute();
        }

        int screenWidth = Gdx.graphics.getBackBufferWidth();
        int screenHeight = Gdx.graphics.getBackBufferHeight();
        Letterbox box = Letterbox.fit(screenWidth, screenHeight, WORLD_WIDTH, WORLD_HEIGHT);
        // Black bars first, then limit drawing (and each screen's own clear) to the game area.
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glViewport(box.x, box.y, box.width, box.height);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(box.x, box.y, box.width, box.height);
        super.render();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override
    public void create() {
        assetLoader = new AssetLoader();
        batch = new SpriteBatch();
        font = Theme.loadFont();
        titleFont = Theme.loadTitleFont();
        setScreen(new MainMenuScreen(this));
        audio.playMusic("menu");
    }

    /** Starts a new run with a fresh Dayo: the introduction first, then Stage 1. */
    public void newGame() {
        audio.play("ui_select");
        player = new Player(0, 0);
        Texture dayo = assetLoader.loadImage(DAYO_SHEET);
        dayo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        player.setSpriteSheet(dayo, DAYO_FRAMES);
        playIntro();
    }

    private void playIntro() {
        disposeIntro();
        audio.playMusic("story");
        String background = Gdx.files.internal(INTRO_BACKGROUND).exists() ? INTRO_BACKGROUND : INTRO_FALLBACK;
        introScene = new CutScene(CutScene.intro(), assetLoader.loadImage(background));
        introScene.setPortrait("Dayo", DialogueBox.portraitFrom(assetLoader.loadImage(DAYO_SHEET), DAYO_FRAMES));
        introScene.setPortrait("Lola Pacita", DialogueBox.portraitFrom(
                assetLoader.loadImage("images/lola-pacita.png"), DAYO_FRAMES));
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
        audio.playMusic(STAGE_MUSIC[index]);
        currentStage = createStage(index);
        currentStage.begin();
        setScreen(currentStage);
    }

    /** Called by a stage when its goal is met. */
    public void onStageComplete(GameStage stage) {
        if (stageIndex + 1 < STAGE_COUNT) {
            audio.play("win");
            startStage(stageIndex + 1);
        } else {
            audio.play("ending");
            // Stage 5 already played the serenade and the ending (Ang Sagot); the journey is over.
            toMainMenu();
        }
    }

    /** Called by a stage when Dayo falls, runs out of time, or loses all Courage or Sanity. */
    public void onStageFailed(GameStage stage) {
        audio.stopMusic();
        audio.play("lose");
        setScreen(new GameOverScreen(this, stage));
    }

    public void retryStage(GameStage stage) {
        audio.playMusic(STAGE_MUSIC[stageIndex]);
        stage.retry();
        setScreen(stage);
    }

    public void toMainMenu() {
        audio.playMusic("menu");
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

    public AudioManager getAudio() {
        return audio;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    /** The heading font (28 px); Theme owns it and frees it with the others. */
    public BitmapFont getTitleFont() {
        return titleFont;
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
        Theme.dispose();
        assetLoader.dispose();
        audio.dispose();
    }
}
