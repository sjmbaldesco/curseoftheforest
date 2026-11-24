package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.Hud;
import com.cupids.curseoftheforest.screens.KeyPrompt;
import com.cupids.curseoftheforest.screens.MenuText;
import com.cupids.curseoftheforest.screens.Theme;
import com.cupids.curseoftheforest.screens.Toast;
import java.util.List;
import java.util.Objects;

/**
 * Base class for the five stages. It runs the shared frame loop (input, player physics, coins,
 * timer, win/lose checks, drawing) and leaves the stage's own mechanics to subclasses through
 * {@link #init()}, {@link #update(float)} and {@link #handleInput()}. Stages draw their own
 * things through {@link #drawWorld} and {@link #drawOverlay}, and talk to the player through the
 * shared {@link #dialogue} box, {@link #toast} notices, {@link #showPrompt} tags and the HUD goal.
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
    /** Conversations and questions, drawn at the top of the screen. */
    protected final DialogueBox dialogue = new DialogueBox();
    /** Short notices that fade on their own. */
    protected final Toast toast = new Toast();

    private final OrthographicCamera camera = new OrthographicCamera();
    private Texture coinTexture;
    /** Spinning coin frames; null if the sprite sheet is missing, then the still coin is used. */
    private Animation<TextureRegion> coinSpin;
    private float coinTime;
    private ParallaxBackground parallax;
    /** Fractions of a point drained so far; stats are whole numbers, so drain is banked. */
    private float courageDrainBank;
    private float sanityDrainBank;
    /** One-line current objective, shown in the HUD bar. */
    private String goal = "";
    /** This frame's "[E] Talk" tag, set by the stage each frame; null when there is none. */
    private String promptKey;
    private String promptAction;
    private final Vector3 promptSpot = new Vector3();

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

    /** Draws the stage's creatures and props in world coordinates; the batch is already started. */
    protected void drawWorld(SpriteBatch batch) {
    }

    /** Draws stage extras in window coordinates (960 by 540); the batch is already started. */
    protected void drawOverlay(SpriteBatch batch) {
    }

    // ---------------------------------------------------------------- talking to the player

    /** Sets the one-line objective shown in the HUD, e.g. "Talk to the Tikbalang". */
    /** How far the screen tilts this frame, in degrees. Stage 4's fear vision overrides it. */
    protected float screenTilt() {
        return 0f;
    }

    /** False while coins on the current map should be hidden and cannot be taken. */
    protected boolean coinsActive() {
        return true;
    }

    /**
     * Shows another map and background in place of this stage's own (Stage 4 sends Dayo back to an
     * earlier stage in a vision). The stage keeps its own references to swap them back later.
     */
    protected void showScenery(TileMap map, ParallaxBackground background) {
        tileMap = Objects.requireNonNull(map, "map");
        parallax = background;
    }

    protected ParallaxBackground getParallax() {
        return parallax;
    }

    protected void setGoal(String goal) {
        this.goal = goal == null ? "" : goal;
    }

    String getGoal() {
        return goal;
    }

    /**
     * Shows a "[key] action" tag above a world position for this frame only. Call it every frame
     * while the tag should show (from {@link #handleInput()} or {@link #update(float)}).
     */
    protected void showPrompt(String key, String action, float worldX, float worldY) {
        promptKey = key;
        promptAction = action;
        promptSpot.set(worldX, worldY, 0);
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
        goal = "";
        dialogue.close();
        toast.clear();
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
        toast.update(delta);
        dialogue.update(delta);
        if (dialogue.isBlocking()) {
            // A conversation pauses the stage: no movement, timers or drain until it is read.
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)
                    || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                dialogue.advance();
            }
            promptKey = null;
            draw();
            return;
        }
        promptKey = null;
        readMovementKeys();
        handleInput();

        player.update(delta, tileMap);
        if (coinsActive() && tileMap.collectCoinAt(player.getBounds())) {
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
        for (Rectangle coin : coinsActive() ? tileMap.getCoinMarkers() : List.<Rectangle>of()) {
            if (coinSpin != null) {
                // The coin art is 32 px but the map marks coins as 16 px; centre the sprite on the marker.
                batch.draw(coinSpin.getKeyFrame(coinTime), coin.x + coin.width / 2 - 16, coin.y + coin.height / 2 - 16, 32, 32);
            } else {
                batch.draw(coinTexture, coin.x, coin.y, coin.width, coin.height);
            }
        }
        drawWorld(batch);
        player.render(batch);
        batch.end();
        if (parallax != null) {
            parallax.renderForeground(batch, camera);
            // The foreground draws in window space; put the world camera back for the HUD to save.
            batch.setProjectionMatrix(camera.combined);
        }

        hud.setDraining(courageDrainPerSecond() > 0, sanityDrainPerSecond() > 0);
        hud.setGoal(goal);
        hud.render(batch);

        BitmapFont font = game.getFont();
        MenuText.begin(batch);
        drawOverlay(batch);
        if (promptKey != null && !dialogue.isBlocking()) {
            camera.project(promptSpot, 0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
            KeyPrompt.draw(batch, font, promptKey, promptAction, promptSpot.x, promptSpot.y + 6);
        }
        // Under the HUD bar, or just above the item bar while the talking box is up.
        toast.render(batch, font, dialogue.isOpen() ? 130f : 468f);
        dialogue.render(batch, font);
        batch.end();
    }

    /** Centers the camera on Dayo without showing anything past the map edges. */
    private void followPlayer() {
        float halfW = camera.viewportWidth / 2;
        float halfH = camera.viewportHeight / 2;
        float x = player.getX() + Player.WIDTH / 2;
        camera.position.x = snap(MathUtils.clamp(x, halfW, Math.max(halfW, tileMap.getPixelWidth() - halfW)));
        float y = player.getY() + Player.HEIGHT / 2;
        camera.position.y = snap(MathUtils.clamp(y, halfH, Math.max(halfH, tileMap.getPixelHeight() - halfH)));
        camera.up.set(0, 1, 0);
        camera.direction.set(0, 0, -1);
        float tilt = screenTilt();
        if (tilt != 0) {
            camera.rotate(tilt);
        }
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
