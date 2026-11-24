package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.Hud;
import com.cupids.curseoftheforest.screens.Theme;

/**
 * Base class for the five stages. It runs the shared frame loop (input, player physics, coins,
 * timer, win/lose checks, drawing) and leaves the stage's own mechanics to subclasses through
 * {@link #init()}, {@link #update(float)} and {@link #handleInput()}.
 */
public abstract class GameStage extends ScreenAdapter {
    /** Caps a frame's delta so a lag spike cannot make the player pass through a tile. */
    private static final float MAX_DELTA = 1 / 30f;
    /** How far below the map the player can fall before the stage counts as failed. */
    private static final float FALL_LIMIT = -64f;
    private static final String COIN_SHEET = "images/coin-sheet.png";
    /**
     * How much of the world the camera shows, compared with the window. At 0.5 the camera sees a
     * 480 by 270 slice, so every tile and sprite is drawn exactly twice as big (crisp pixel art).
     */
    public static final float CAMERA_ZOOM = 0.5f;

    protected final ForestGame game;
    protected final String name;
    protected final String mapPath;
    protected StageTimer timer;
    protected boolean completed;
    protected boolean failed;
    protected TileMap tileMap = new TileMap();
    protected Player player;
    protected Checkpoint checkpoint;
    protected Hud hud;
    /** True after the player retries from the game-over screen, so stages can skip story scenes. */
    protected boolean retried;

    private final OrthographicCamera camera = new OrthographicCamera();
    private Texture coinTexture;
    /** Spinning coin frames; null if the sprite sheet is missing, then the still coin is used. */
    private Animation<TextureRegion> coinSpin;
    private float coinTime;
    private ParallaxBackground parallax;
    /** Fractions of a point drained so far; stats are whole numbers, so drain is banked. */
    private float courageDrainBank;
    private float sanityDrainBank;

    protected GameStage(ForestGame game, String name, String mapPath) {
        this.game = game;
        this.name = name;
        this.mapPath = mapPath;
    }

    // ---------------------------------------------------------------- what each stage defines

    /** Sets up the stage's own state. Called on first entry and again on every retry. */
    protected abstract void init();

    /** The stage's own logic for one frame, after the player has moved. */
    public abstract void update(float delta);

    /** Stage-specific keys (answering riddles, picking potions...). Movement is handled here. */
    protected abstract void handleInput();

    /** Courage lost per second while in this stage. Safe stages keep the default of 0. */
    protected float courageDrainPerSecond() {
        return 0f;
    }

    /** Sanity lost per second while in this stage. Safe stages keep the default of 0. */
    protected float sanityDrainPerSecond() {
        return 0f;
    }

    // ---------------------------------------------------------------- lifecycle

    /** Enters the stage for the first time: remembers Dayo's state for retries, then sets up. */
    public void begin() {
        // Set up here rather than in the constructor so a stage can be built without graphics (tests).
        camera.setToOrtho(false, ForestGame.WORLD_WIDTH * CAMERA_ZOOM, ForestGame.WORLD_HEIGHT * CAMERA_ZOOM);
        player = game.getPlayer();
        checkpoint = player.saveCheckpoint();
        coinTexture = game.getAssetLoader().loadImage("images/coin.png");
        if (Gdx.files.internal(COIN_SHEET).exists()) {
            Texture sheet = game.getAssetLoader().loadImage(COIN_SHEET);
            sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            coinSpin = new Animation<>(0.08f, TextureRegion.split(sheet, 32, 32)[0]);
            coinSpin.setPlayMode(Animation.PlayMode.LOOP);
        }
        parallax = ParallaxBackground.forStage(game.getAssetLoader(), name);
        setUp();
    }

    /** Restarts the stage with Dayo's coins, stats and items as they were on entry. */
    public void retry() {
        retried = true;
        player.restore(checkpoint);
        setUp();
    }

    private void setUp() {
        tileMap.load(mapPath); // reloading also puts collected coins back
        player.setPosition(tileMap.getSpawn().x, tileMap.getSpawn().y);
        completed = false;
        failed = false;
        timer = null;
        courageDrainBank = 0f;
        sanityDrainBank = 0f;
        init();
        hud = new Hud(player, timer, game.getFont(), name);
        hud.setIcons(game.getAssetLoader());
    }

    // ---------------------------------------------------------------- frame loop

    @Override
    public void render(float delta) {
        delta = Math.min(delta, MAX_DELTA);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.toMainMenu();
            return;
        }
        readMovementKeys();
        handleInput();

        player.update(delta, tileMap);
        if (tileMap.collectCoinAt(player.getBounds())) {
            player.addCoins(1);
        }
        if (timer != null) {
            timer.tick(delta);
        }
        applyDrain(delta);
        update(delta);

        if (isComplete()) {
            game.onStageComplete(this);
            return;
        }
        if (isFailed()) {
            game.onStageFailed(this);
            return;
        }
        draw();
    }

    /** Takes this stage's Courage and Sanity drain for one frame, whole points at a time. */
    void applyDrain(float delta) {
        courageDrainBank += Math.max(0f, courageDrainPerSecond()) * delta;
        sanityDrainBank += Math.max(0f, sanityDrainPerSecond()) * delta;
        int courageLoss = (int) courageDrainBank;
        int sanityLoss = (int) sanityDrainBank;
        if (courageLoss > 0) {
            player.changeCourage(-courageLoss);
            courageDrainBank -= courageLoss;
        }
        if (sanityLoss > 0) {
            player.changeSanity(-sanityLoss);
            sanityDrainBank -= sanityLoss;
        }
    }

    private void readMovementKeys() {
        int dir = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            dir -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            dir += 1;
        }
        player.move(dir);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.jump();
        }
    }

    private void draw() {
        ScreenUtils.clear(parallax != null ? Theme.NIGHT : Color.valueOf("6B9EBF"));
        followPlayer();
        SpriteBatch batch = game.getBatch();
        if (parallax != null) {
            parallax.renderBack(batch, camera);
        }
        tileMap.render(camera);
        if (parallax != null) {
            parallax.renderFront(batch, camera);
        }

        coinTime += Gdx.graphics.getDeltaTime();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Rectangle coin : tileMap.getCoinMarkers()) {
            if (coinSpin != null) {
                // The coin art is 32 px but the map marks coins as 16 px; centre the sprite on the marker.
                batch.draw(coinSpin.getKeyFrame(coinTime), coin.x + coin.width / 2 - 16, coin.y + coin.height / 2 - 16, 32, 32);
            } else {
                batch.draw(coinTexture, coin.x, coin.y, coin.width, coin.height);
            }
        }
        player.render(batch);
        batch.end();
        if (parallax != null) {
            parallax.renderForeground(batch, camera);
            // The foreground draws in window space; put the world camera back for the HUD to save.
            batch.setProjectionMatrix(camera.combined);
        }

        hud.setDraining(courageDrainPerSecond() > 0, sanityDrainPerSecond() > 0);
        hud.render(batch);
    }

    /** Centers the camera on Dayo without showing anything past the map edges. */
    private void followPlayer() {
        float halfW = camera.viewportWidth / 2;
        float halfH = camera.viewportHeight / 2;
        float x = player.getX() + Player.WIDTH / 2;
        camera.position.x = snap(MathUtils.clamp(x, halfW, Math.max(halfW, tileMap.getPixelWidth() - halfW)));
        float y = player.getY() + Player.HEIGHT / 2;
        camera.position.y = snap(MathUtils.clamp(y, halfH, Math.max(halfH, tileMap.getPixelHeight() - halfH)));
        camera.update();
    }

    /** Rounds a camera position to a whole window pixel so zoomed pixel art does not shimmer. */
    private static float snap(float worldPosition) {
        return Math.round(worldPosition / CAMERA_ZOOM) * CAMERA_ZOOM;
    }

    // ---------------------------------------------------------------- state checks

    public boolean isComplete() {
        return completed;
    }

    /** A stage is lost by falling out of the map, running out of time, or Courage or Sanity hitting 0. */
    public boolean isFailed() {
        return failed
                || player.getY() < FALL_LIMIT
                || player.getCourage() <= 0
                || player.getSanity() <= 0
                || (timer != null && timer.isExpired());
    }

    /** True when Dayo touches the map's exit marker. Platform stages use this as their goal. */
    protected boolean reachedExit() {
        Rectangle exit = tileMap.getExit();
        return exit.width > 0 && exit.overlaps(player.getBounds());
    }

    public String getName() {
        return name;
    }

    @Override
    public void dispose() {
        tileMap.dispose();
    }
}
