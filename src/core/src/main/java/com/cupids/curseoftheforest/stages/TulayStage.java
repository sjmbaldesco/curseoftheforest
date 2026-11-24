package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.Nuno;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.screens.CutScene;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.Theme;

/**
 * Stage 4, the Test of the Mind. Three food riddles reward the three potions the last stage needs.
 *
 * <p>Standing before the Nuno is frightening: Courage drains while its riddle is up, and a wrong
 * offering costs 10 more. If Courage hits 0, Dayo is not beaten. He falls into a
 * {@link Hallucination}: a few seconds back in the Wakwak Cave or the Tikbalang forest, where
 * nothing can hurt him. He wakes by the Nuno with half his Courage, and the riddles start over.
 */
public class TulayStage extends GameStage {
    private static final String[] OFFERINGS = {"Atis", "Sampalok", "Kalabasa"};
    /** Courage lost per second while the Nuno's riddle is up. */
    static final float RIDDLE_COURAGE_DRAIN = 1f;
    static final int WRONG_OFFERING_COURAGE = 10;
    /** Dayo wakes from a vision with this much Courage. */
    static final int COURAGE_AFTER_VISION = 50;
    /** The vision's purple; only its alpha changes from frame to frame. */
    private final Color visionPurple = new Color(0.42f, 0.08f, 0.55f, 0f);
    private final Color visionShade = new Color(0.02f, 0f, 0.05f, 0f);

    private Nuno nuno;
    private TextureRegion nunoPortrait;
    /** The riddle currently shown in the question box, so it is only re-opened when it changes. */
    private String askedQuestion = "";
    private CutScene scene;

    private final Hallucination vision = new Hallucination();
    private int visionCount;
    /** The bridge map and its art, kept while a vision shows another place. */
    private TileMap homeMap;
    private ParallaxBackground homeBackground;
    private TileMap visionMap;
    private TextureRegion phantom;
    /** Where Dayo stood when the vision took him, so he wakes in the same spot. */
    private float wakeX;
    private float wakeY;

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
        vision.stop();
        visionCount = 0;
        setGoal("Greet the Nuno: Tabi-tabi po!");
        boolean hasJuice = player.getInventory().has(Shop.MALUNGGAY_JUICE);
        timer = new StageTimer(hasJuice ? 300 : 180);
    }

    @Override
    public void update(float delta) {
        if (vision.isActive()) {
            if (vision.update(delta)) {
                wakeUp();
            }
            return;
        }
        if (player.getCourage() <= 0) {
            fallIntoVision();
            return;
        }
        nuno.update(delta);
        completed = nuno.isComplete() && reachedExit() && !isFailed();
        if (nuno.isComplete()) {
            setGoal("Cross the bridge to the far right exit");
        } else if (nuno.hasStarted()) {
            setGoal("Answer the Nuno's riddles (" + nuno.getAnsweredCount() + "/3)");
        }
        showRiddle();
    }

    /** True while the Nuno's riddle is up: Dayo has greeted it, stands near, and riddles remain. */
    private boolean riddleIsUp() {
        return nuno != null && !vision.isActive() && nuno.hasStarted() && !nuno.isComplete() && nuno.isNear(player);
    }

    /** Keeps the riddle box up while Dayo stands by the Nuno, and puts it away when he leaves. */
    private void showRiddle() {
        if (!riddleIsUp()) {
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
        if (vision.isActive()) return;
        if (!nuno.hasStarted() && nuno.isNear(player)) {
            showPrompt("E", "Greet", nuno.getX() + 32, nuno.getY() + 64);
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                nuno.interact(player);
                dialogue.say(nuno.getName(), nunoPortrait,
                        "Who walks over my mound without asking?",
                        "Answer my riddles with the right offering, and I will give you a potion for each.",
                        "But keep your nerve. Fear is heavy on my bridge.");
            }
            return;
        }
        if (!riddleIsUp() || isFailed()) return;
        for (int i = 0; i < OFFERINGS.length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                if (nuno.offer(player, OFFERINGS[i])) {
                    toast.show("Correct! The Nuno gives you a potion.");
                    sfx("correct");
                } else {
                    player.changeCourage(-WRONG_OFFERING_COURAGE);
                    sfx("wrong");
                    toast.warn("That is not the offering. Courage -" + WRONG_OFFERING_COURAGE);
                }
                break;
            }
        }
    }

    // ---------------------------------------------------------------- the fear vision

    private void fallIntoVision() {
        visionCount++;
        Hallucination.Place place = Hallucination.placeFor(visionCount);
        wakeX = player.getX();
        wakeY = player.getY();
        dialogue.close();
        askedQuestion = "";

        homeMap = tileMap;
        homeBackground = getParallax();
        if (visionMap == null) visionMap = new TileMap();
        visionMap.load(place.mapPath);
        showScenery(visionMap, new ParallaxBackground(game.getAssetLoader(), place.backdrop));
        Rectangle arrival = place.arrivalMarker == null ? null : visionMap.getMarker(place.arrivalMarker);
        if (arrival != null) {
            player.setPosition(arrival.x + 128, arrival.y); // just inside, e.g. past the cave mouth
        } else {
            player.setPosition(visionMap.getSpawn().x, visionMap.getSpawn().y);
        }
        phantom = DialogueBox.portraitFrom(game.getAssetLoader().loadImage(place.phantomImage), 5);
        if (phantom != null) {
            // The whole first frame, not the face crop used for portraits.
            phantom = new TextureRegion(phantom.getTexture(), 0, 0, 64, 64);
        }

        vision.start(place);
        sfx("whoosh");
        game.getAudio().playMusic(place.mapPath.contains("gubat") ? "forest" : "cave");
        toast.warn("Your Courage breaks... you are back in the " + place.label + "!");
        setGoal("It is only fear. Hold on...");
    }

    private void wakeUp() {
        restoreBridge();
        sfx("whoosh");
        game.getAudio().playMusic("nuno");
        player.setPosition(wakeX, wakeY);
        player.changeCourage(COURAGE_AFTER_VISION - player.getCourage());
        nuno.reset(player);
        askedQuestion = "";
        setGoal("Answer the Nuno's riddles (0/3)");
        dialogue.say(nuno.getName(), nunoPortrait,
                "Hah! Fear made you see things that are not there.",
                "I have taken back my potions. We begin again from the first riddle.");
    }

    /** Puts the bridge back if a vision was showing another place. */
    private void restoreBridge() {
        vision.stop();
        if (homeMap != null) {
            showScenery(homeMap, homeBackground);
            homeMap = null;
            homeBackground = null;
        }
    }

    /** During a vision Dayo cannot fall out or run out of Courage; only the clock still counts. */
    @Override
    public boolean isFailed() {
        if (vision.isActive()) {
            return timer != null && timer.isExpired();
        }
        return super.isFailed();
    }

    @Override
    protected float courageDrainPerSecond() {
        return riddleIsUp() ? RIDDLE_COURAGE_DRAIN : 0f;
    }

    @Override
    protected boolean growsFoliageAt(float worldX) {
        // In the cave vision the rock floor past the cave mouth stays bare, as in Stage 2.
        Hallucination.Place place = vision.getPlace();
        Rectangle mouth = place == null || place.arrivalMarker == null ? null : tileMap.getMarker(place.arrivalMarker);
        return mouth == null || worldX + 32 < mouth.x;
    }

    @Override
    protected boolean coinsActive() {
        return !vision.isActive();
    }

    @Override
    protected float screenTilt() {
        return vision.tilt();
    }

    @Override
    public void retry() {
        restoreBridge();
        super.retry();
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void drawWorld(SpriteBatch batch) {
        if (!vision.isActive()) {
            nuno.render(batch);
            return;
        }
        if (phantom == null) return;
        // The phantom circles Dayo, half seen. It cannot touch him.
        float t = Hallucination.SECONDS - vision.getSecondsLeft();
        float x = player.getX() + 12 + MathUtils.cos(t * 1.6f) * 90f - 32;
        float y = player.getY() + 40 + MathUtils.sin(t * 2.3f) * 30f;
        boolean movingLeft = MathUtils.sin(t * 1.6f) > 0;
        batch.setColor(1f, 0.8f, 1f, 0.55f * vision.strength());
        batch.draw(phantom, movingLeft ? x + 64 : x, y, movingLeft ? -64 : 64, 64);
        batch.setColor(Color.WHITE);
    }

    @Override
    protected void drawOverlay(SpriteBatch batch) {
        float tint = vision.tint();
        if (tint > 0) {
            visionShade.a = vision.darkness();
            Theme.rect(batch, visionShade, 0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
            visionPurple.a = tint;
            Theme.rect(batch, visionPurple, 0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
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
    }

    @Override
    public void dispose() {
        if (scene != null) { scene.dispose(); scene = null; }
        restoreBridge();
        if (visionMap != null) { visionMap.dispose(); visionMap = null; }
        super.dispose();
    }

    // ---------------------------------------------------------------- for tests

    boolean isHallucinating() {
        return vision.isActive();
    }
}
