package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.GlowingBush;
import com.cupids.curseoftheforest.creatures.Tikbalang;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.MenuText;
import com.cupids.curseoftheforest.screens.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 3: Ang Gubat ng Tikbalang. Dayo talks to the Tikbalang, which starts the clock, then
 * searches glowing bushes for the three pieces of the clue "Baliktarin ang damit". With all three
 * he breaks the spell and can leave through the exit.
 *
 * <p>The clock is 3 minutes with the Agimat and 1.5 minutes without it. While it runs, the forest
 * drains Courage and Sanity. A weapon from Tonyo's shop makes each bush faster to search.
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
    private static final float SANITY_DRAIN = 0.3f;
    private static final String BUSH_IMAGE = "images/glowing-bush.png";
    private static final String SEARCHED_BUSH_IMAGE = "images/glowing-bush-searched.png";

    private Tikbalang tikbalang;
    private final List<GlowingBush> bushes = new ArrayList<>();
    /** Clue pieces found so far (the proposal's ArrayList of collected pieces). */
    private final List<String> pieces = new ArrayList<>();
    private String message;
    private Texture pixel;
    private boolean hasBushSprite;

    public GubatStage(ForestGame game) {
        super(game, "Stage 3: Ang Gubat ng Tikbalang", "maps/gubat.tmx");
    }

    /** Stage 3's time limit: the Agimat doubles it. */
    static float timeLimitFor(Player p) {
        return p.getInventory().has(Shop.AGIMAT) ? TIME_WITH_AGIMAT : TIME_WITHOUT_AGIMAT;
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
        tikbalang.setSpriteSheet(game.getAssetLoader().loadImage("images/tikbalang.png"), 5);

        hasBushSprite = Gdx.files.internal(BUSH_IMAGE).exists();
        bushes.clear();
        List<Rectangle> spots = tileMap.getObjects("bushes");
        for (int i = 0; i < spots.size(); i++) {
            String piece = pieceFor(i);
            GlowingBush bush = new GlowingBush(spots.get(i), piece,
                    piece == null ? COINS_PER_EMPTY_BUSH : 0, BUSH_SEARCH_SECONDS);
            bush.setSprites(optionalImage(BUSH_IMAGE), optionalImage(SEARCHED_BUSH_IMAGE));
            bushes.add(bush);
        }
        pieces.clear();
        message = player.getInventory().has(Shop.AGIMAT)
                ? "The Agimat glows. Walk to the Tikbalang and press E."
                : "Walk to the Tikbalang and press E.";
    }

    private static String pieceFor(int bushIndex) {
        for (int i = 0; i < PIECE_BUSHES.length; i++) {
            if (PIECE_BUSHES[i] == bushIndex) {
                return CLUE[i];
            }
        }
        return null;
    }

    /** Art hook: uses an image only if the file exists, so a missing bush sprite is not magenta. */
    private Texture optionalImage(String path) {
        return Gdx.files.internal(path).exists() ? game.getAssetLoader().loadImage(path) : null;
    }

    @Override
    protected void handleInput() {
        boolean pressedE = Gdx.input.isKeyJustPressed(Input.Keys.E);
        if (pressedE && !tikbalang.hasChallenged() && tikbalang.isNear(player)) {
            tikbalang.interact(player);
            message = tikbalang.getLine();
            return;
        }

        boolean holdingE = Gdx.input.isKeyPressed(Input.Keys.E);
        for (GlowingBush bush : bushes) {
            boolean canSearch = holdingE && bush.isNear(player) && !bush.isSearched();
            if (canSearch && !timer.isRunning()) {
                if (pressedE) {
                    message = "The bushes will not open yet. Talk to the Tikbalang first.";
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
            completed = reachedExit() && !isFailed();
        } else if (reachedExit() && tikbalang.hasChallenged()) {
            message = "The path loops back into the forest. Find every piece of the clue first.";
        }
    }

    private void onBushSearched(GlowingBush bush) {
        String piece = bush.takePiece();
        if (piece == null) {
            message = "Only leaves here... and " + bush.getCoins() + " coin.";
            return;
        }
        pieces.add(piece);
        message = hasAllPieces()
                ? "\"Baliktarin ang damit!\" Dayo turns his shirt inside out. The spell breaks. Run to the exit!"
                : "You found a piece of the clue: \"" + piece + "\"";
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
        return timer != null && timer.isRunning() && !hasAllPieces() ? SANITY_DRAIN : 0f;
    }

    // ---------------------------------------------------------------- drawing

    @Override
    public void render(float delta) {
        super.render(delta);
        if (game.getScreen() != this) {
            return;
        }
        SpriteBatch batch = game.getBatch();
        hud.beginWorld(batch);
        drawBushes(batch);
        tikbalang.render(batch);
        batch.end();

        MenuText.begin(batch);
        Theme.hintPanel(batch);
        BitmapFont font = game.getFont();
        font.setColor(Theme.PAPER);
        font.draw(batch, message, 20, 175, 920, Align.left, true);
        font.draw(batch, "Clue: " + clueSoFar() + "   (" + pieces.size() + "/" + CLUE.length + ")", 20, 125);
        GlowingBush searching = searchingBush();
        if (searching != null) {
            int percent = (int) (searching.getProgress() * 100);
            font.draw(batch, "Searching the bush... " + percent + "%  (keep holding E)", 20, 100);
        } else if (timer.isRunning() && !hasAllPieces()) {
            font.draw(batch, "Hold E at a glowing bush to search it.", 20, 100);
        }
        batch.end();
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
