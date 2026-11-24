package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.cupids.curseoftheforest.items.Weapon;
import com.cupids.curseoftheforest.player.Player;
import java.util.Objects;

/**
 * Stage 3 bush that may hide a clue piece and some coins. It is not a Creature, but Dayo
 * interacts with it the same way, which is why both share the {@link Interactable} interface.
 *
 * <p>{@link #interact(Player)} starts a search; {@link #update(float)} advances it. Searching takes
 * {@code searchSeconds / weapon cut speed}, so a sharper weapon is faster. A bush can be searched
 * only once, and its piece can be taken only once.
 */
public class GlowingBush implements Interactable {
    private final Rectangle bounds;
    private final String piece;
    private final int coins;
    private final float searchSeconds;

    private Player searcher;
    private float requiredSeconds;
    private float progressSeconds;
    private boolean searched;
    private boolean pieceTaken;

    private Texture sprite;
    private Texture searchedSprite;

    /**
     * @param piece the clue word hidden here, or null if the bush hides none
     * @param coins coins Dayo finds when the search finishes
     */
    public GlowingBush(Rectangle bounds, String piece, int coins, float searchSeconds) {
        if (searchSeconds <= 0) {
            throw new IllegalArgumentException("Search time must be positive: " + searchSeconds);
        }
        if (coins < 0) {
            throw new IllegalArgumentException("Coins cannot be negative: " + coins);
        }
        this.bounds = new Rectangle(Objects.requireNonNull(bounds, "bounds"));
        this.piece = piece;
        this.coins = coins;
        this.searchSeconds = searchSeconds;
    }

    /** How long this bush takes for Dayo with his current best weapon (bare hands count as 1). */
    public float searchTimeFor(Player p) {
        Weapon weapon = p.getWeapon();
        float cutSpeed = weapon == null ? 1f : Math.max(1f, weapon.getCutSpeed());
        return searchSeconds / cutSpeed;
    }

    /** Starts searching. Does nothing if the bush is already searched or being searched. */
    @Override
    public void interact(Player p) {
        Objects.requireNonNull(p, "player");
        if (searched || isSearching()) {
            return;
        }
        searcher = p;
        requiredSeconds = searchTimeFor(p);
        progressSeconds = 0f;
    }

    /** Advances an ongoing search. When it finishes, Dayo receives the bush's coins. */
    public void update(float delta) {
        if (!isSearching() || delta <= 0) {
            return;
        }
        progressSeconds += delta;
        if (progressSeconds >= requiredSeconds) {
            searched = true;
            if (coins > 0) {
                searcher.addCoins(coins);
            }
            searcher = null;
        }
    }

    /** Stops an unfinished search (Dayo let go of E or walked away); progress is lost. */
    public void cancelSearch() {
        if (!searched) {
            searcher = null;
            progressSeconds = 0f;
        }
    }

    /** Returns the clue piece the first time it is called after a finished search, else null. */
    public String takePiece() {
        if (!searched || piece == null || pieceTaken) {
            return null;
        }
        pieceTaken = true;
        return piece;
    }

    /** True if Dayo's body touches the bush (with a little reach on each side). */
    public boolean isNear(Player p) {
        Rectangle body = p.getBounds();
        return body.x < bounds.x + bounds.width + 16 && body.x + body.width > bounds.x - 16
                && body.y < bounds.y + bounds.height + 16 && body.y + body.height > bounds.y - 16;
    }

    public boolean isSearching() {
        return searcher != null;
    }

    public boolean isSearched() {
        return searched;
    }

    public boolean hasPiece() {
        return piece != null;
    }

    public int getCoins() {
        return coins;
    }

    /** Search progress from 0 to 1, for a progress bar. A finished bush reports 1. */
    public float getProgress() {
        if (searched) {
            return 1f;
        }
        return isSearching() ? Math.min(1f, progressSeconds / requiredSeconds) : 0f;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    // ---------------------------------------------------------------- drawing hooks

    /** Art hook: the glowing bush and the emptied bush. Textures stay owned by AssetLoader. */
    public void setSprites(Texture sprite, Texture searchedSprite) {
        this.sprite = sprite;
        this.searchedSprite = searchedSprite;
    }

    public void render(SpriteBatch batch) {
        Texture texture = searched && searchedSprite != null ? searchedSprite : sprite;
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
