package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.GlowingBush;
import com.cupids.curseoftheforest.creatures.Tikbalang;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 3: Ang Gubat ng Tikbalang. Dayo talks to the Tikbalang, which starts the clock, then
 * searches glowing bushes for the three pieces of the clue "Baliktarin ang damit". With all three
 * he breaks the spell and can leave through the exit.
 *
 * <p>The clock is 3 minutes with the Agimat and 1.5 minutes without it. While it runs, the forest
 * drains Courage and Sanity; the Agimat also halves the Sanity drain. A weapon from Tonyo's shop
 * makes each bush faster to search.
 */
public class GubatStage extends GameStage {
    /** The clue, one word per hidden piece, in reading order. */
    static final String[] CLUE = {"Baliktarin", "ang", "damit"};
    /** Which bushes in the map's "bushes" layer (in map order) hide a piece. */
    private static final int[] PIECE_BUSHES = {1, 3, 5};
    static final float TIME_WITH_AGIMAT = 180f;
    static final float TIME_WITHOUT_AGIMAT = 90f;
    private static final float BUSH_SEARCH_SECONDS = 8f;
    private static final int COINS_PER_EMPTY_BUSH = 1;
    private static final float COURAGE_DRAIN = 0.2f;
    static final float SANITY_DRAIN = 0.8f;
    /** The Agimat protects the mind: Sanity drains at this share of the normal rate. */
    static final float AGIMAT_SANITY_SHARE = 0.5f;
    private static final String BUSH_IMAGE = "images/glowing-bush.png";
    private static final String SEARCHED_BUSH_IMAGE = "images/glowing-bush-searched.png";
    private static final String BUSH_IDLE_SHEET = "images/animated/bush_glowing_idle.png";
    private static final String SEARCHING_BUSH_IMAGE = "images/glowing-bush-searching.png";

    private Tikbalang tikbalang;
    private final List<GlowingBush> bushes = new ArrayList<>();
    /** Clue pieces found so far (the proposal's ArrayList of collected pieces). */
    private final List<String> pieces = new ArrayList<>();
    private Texture pixel;
    private TextureRegion tikbalangPortrait;
    private TextureRegion dayoPortrait;
    private boolean hasBushSprite;

    public GubatStage(ForestGame game) {
        super(game, "Stage 3: Ang Gubat ng Tikbalang", "maps/gubat.tmx");
    }

    /** Stage 3's time limit: the Agimat doubles it. */
    static float timeLimitFor(Player p) {
        return p.getInventory().has(Shop.AGIMAT) ? TIME_WITH_AGIMAT : TIME_WITHOUT_AGIMAT;
    }

    /** Sanity lost per second while the Tikbalang's clock runs: halved by the Agimat. */
    static float sanityDrainFor(Player p) {
        return p.getInventory().has(Shop.AGIMAT) ? SANITY_DRAIN * AGIMAT_SANITY_SHARE : SANITY_DRAIN;
    }

    @Override
    protected void init() {
        // Paused until the Tikbalang speaks; created now so the HUD can show it.
        timer = new StageTimer(timeLimitFor(player), false);

        Rectangle spot = tileMap.getMarker("tikbalang");
        if (spot == null) {
            spot = new Rectangle(tileMap.getSpawn().x + 128, tileMap.getSpawn().y, 64, 64);
        }
        tikbalang = new Tikbalang(spot.x, spot.y, timer);
        Texture tikbalangSheet = game.getAssetLoader().loadImage("images/tikbalang.png");
        tikbalang.setSpriteSheet(tikbalangSheet, 5);
        tikbalangPortrait = DialogueBox.portraitFrom(tikbalangSheet, 5);
        dayoPortrait = DialogueBox.portraitFrom(
                game.getAssetLoader().loadImage("images/dayo.png"), 5);

        hasBushSprite = Gdx.files.internal(BUSH_IMAGE).exists();
        Animation<TextureRegion> bushIdle = game.getAssetLoader().loadStrip(BUSH_IDLE_SHEET, 64, 0.15f);
        Texture searchingBush = optionalImage(SEARCHING_BUSH_IMAGE);
        Texture searchedBush = optionalImage(SEARCHED_BUSH_IMAGE);
        bushes.clear();
        List<Rectangle> spots = tileMap.getObjects("bushes");
        for (int i = 0; i < spots.size(); i++) {
            String piece = pieceFor(i);
            GlowingBush bush = new GlowingBush(spots.get(i), piece,
                    piece == null ? COINS_PER_EMPTY_BUSH : 0, BUSH_SEARCH_SECONDS);
            bush.setSprites(optionalImage(BUSH_IMAGE), searchedBush);
            if (bushIdle != null) {
                bush.setAnimatedSprites(bushIdle, region(searchingBush), region(searchedBush));
            }
            bushes.add(bush);
        }
        pieces.clear();
        setGoal("Talk to the Tikbalang");
        if (player.getInventory().has(Shop.AGIMAT)) {
            toast.show("The Agimat glows: your mind is shielded.");
        }
    }

    private static String pieceFor(int bushIndex) {
        for (int i = 0; i < PIECE_BUSHES.length; i++) {
            if (PIECE_BUSHES[i] == bushIndex) {
                return CLUE[i];
            }
        }
        return null;
    }

    private static TextureRegion region(Texture texture) {
        return texture == null ? null : new TextureRegion(texture);
    }

    /** Art hook: uses an image only if the file exists, so a missing bush sprite is not magenta. */
    private Texture optionalImage(String path) {
        return Gdx.files.internal(path).exists() ? game.getAssetLoader().loadImage(path) : null;
    }

    @Override
    protected void handleInput() {
        boolean pressedE = Gdx.input.isKeyJustPressed(Input.Keys.E);
        if (!tikbalang.hasChallenged() && tikbalang.isNear(player)) {
            showPrompt("E", "Talk", tikbalang.getX() + 32, tikbalang.getY() + 64);
            if (pressedE) {
                tikbalang.interact(player);
                sfx("tikbalang");
                dialogue.say(tikbalang.getName(), tikbalangPortrait, tikbalang.getLine());
                return;
            }
        }

        boolean holdingE = Gdx.input.isKeyPressed(Input.Keys.E);
        for (GlowingBush bush : bushes) {
            boolean near = bush.isNear(player) && !bush.isSearched();
            if (near && !bush.isSearching()) {
                Rectangle b = bush.getBounds();
                showPrompt("E", timer.isRunning() ? "Hold to search" : "Locked", b.x + b.width / 2, b.y + b.height + 8);
            }
            boolean canSearch = holdingE && near;
            if (canSearch && !timer.isRunning()) {
                if (pressedE) {
                    toast.show("The bushes will not open yet. Talk to the Tikbalang first.");
                }
            } else if (canSearch) {
                bush.interact(player); // does nothing if this bush is already being searched
            } else if (bush.isSearching()) {
                bush.cancelSearch();
            }
        }
    }

    @Override
    public void update(float delta) {
        tikbalang.update(delta);
        for (GlowingBush bush : bushes) {
            boolean wasSearched = bush.isSearched();
            bush.update(delta);
            if (!wasSearched && bush.isSearched()) {
                onBushSearched(bush);
            }
        }

        if (hasAllPieces()) {
            setGoal("The spell is broken: run to the exit");
            completed = reachedExit() && !isFailed();
        } else if (tikbalang.hasChallenged()) {
            setGoal("Clue: " + clueSoFar() + "  (" + pieces.size() + "/" + CLUE.length + ")");
            if (reachedExit()) {
                toast.show("The path loops back into the forest. Find every piece first.");
            }
        }
    }

    private void onBushSearched(GlowingBush bush) {
        String piece = bush.takePiece();
        if (piece == null) {
            sfx("bush");
            toast.show("Only leaves here... and " + bush.getCoins() + " coin.");
            return;
        }
        pieces.add(piece);
        sfx("piece");
        if (hasAllPieces()) {
            dialogue.say("Dayo", dayoPortrait, "\"Baliktarin ang damit!\" Dayo turns his shirt inside out.",
                    "The forest stops spinning. The spell is broken. Run to the exit!");
        } else {
            toast.show("You found a piece of the clue: \"" + piece + "\"");
        }
    }

    boolean hasAllPieces() {
        return pieces.size() == CLUE.length;
    }

    /** The forest drains Dayo only while the Tikbalang's clock runs and the spell holds. */
    @Override
    protected float courageDrainPerSecond() {
        return timer != null && timer.isRunning() && !hasAllPieces() ? COURAGE_DRAIN : 0f;
    }

    @Override
    protected float sanityDrainPerSecond() {
        return timer != null && timer.isRunning() && !hasAllPieces() ? sanityDrainFor(player) : 0f;
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void drawWorld(SpriteBatch batch) {
        drawBushes(batch);
        tikbalang.render(batch);
        GlowingBush searching = searchingBush();
        if (searching != null) {
            // Search progress bar just above the bush.
            Rectangle b = searching.getBounds();
            float width = Math.max(b.width, 32f);
            float x = b.x + b.width / 2 - width / 2;
            batch.setColor(Theme.INK);
            batch.draw(pixel(), x - 1, b.y + b.height + 5, width + 2, 6);
            batch.setColor(Theme.GOLD);
            batch.draw(pixel(), x, b.y + b.height + 6, width * searching.getProgress(), 4);
            batch.setColor(Color.WHITE);
        }
    }

    /** Placeholder until the art thread adds bush sprites: a glowing green block, dark when emptied. */
    private void drawBushes(SpriteBatch batch) {
        for (GlowingBush bush : bushes) {
            Rectangle b = bush.getBounds();
            if (hasBushSprite) {
                bush.render(batch);
                continue;
            }
            batch.setColor(bush.isSearched() ? Color.DARK_GRAY : Color.valueOf("7CFC9A"));
            batch.draw(pixel(), b.x, b.y, b.width, b.height);
            batch.setColor(Color.WHITE);
        }
    }

    private Texture pixel() {
        if (pixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return pixel;
    }

    private String clueSoFar() {
        StringBuilder clue = new StringBuilder();
        for (String word : CLUE) {
            if (clue.length() > 0) {
                clue.append(' ');
            }
            clue.append(pieces.contains(word) ? word : "_____");
        }
        return clue.toString();
    }

    private GlowingBush searchingBush() {
        for (GlowingBush bush : bushes) {
            if (bush.isSearching()) {
                return bush;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
        super.dispose();
    }
}
