package com.cupids.curseoftheforest.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.items.Weapon;
import com.cupids.curseoftheforest.stages.Checkpoint;
import com.cupids.curseoftheforest.stages.TileMap;

/**
 * Dayo, the player character: his stats, his possessions, and his movement through a tile map.
 *
 * <p>All fields are private. Stats only change through methods that clamp them to 0..100, and
 * coins can never go negative, so no other class can put the player in an invalid state.
 */
public class Player {
    public static final int MAX_STAT = 100;

    // Movement tuning, in pixels and seconds.
    public static final float WIDTH = 24f;
    public static final float HEIGHT = 44f;
    public static final float MOVE_SPEED = 220f;
    public static final float JUMP_SPEED = 620f;
    public static final float GRAVITY = -1600f;
    public static final float MAX_FALL_SPEED = 900f;

    private int courage = MAX_STAT;
    private int sanity = MAX_STAT;
    private int resistance;
    private int coins;
    private final Inventory inventory = new Inventory();

    private float x;
    private float y;
    private final Vector2 velocity = new Vector2();
    private final Rectangle bounds = new Rectangle(0, 0, WIDTH, HEIGHT);
    private boolean onGround;
    private boolean facingRight = true;
    private Texture sprite;

    public Player(float x, float y) {
        setPosition(x, y);
    }

    // ---------------------------------------------------------------- movement

    /** Sets horizontal movement for this frame: -1 = left, 0 = stop, 1 = right. */
    public void move(int dir) {
        int d = Integer.signum(dir);
        velocity.x = d * MOVE_SPEED;
        if (d != 0) {
            facingRight = d > 0;
        }
    }

    /** Jumps only when standing on solid ground, so the player cannot jump in mid-air. */
    public void jump() {
        if (onGround) {
            velocity.y = JUMP_SPEED;
            onGround = false;
        }
    }

    /**
     * Applies gravity and velocity, then pushes the player out of any solid tile.
     * The X and Y axes are moved and resolved separately so that walls stop sideways motion and
     * floors and ceilings stop vertical motion without the two interfering.
     */
    public void update(float delta, TileMap map) {
        velocity.y = Math.max(velocity.y + GRAVITY * delta, -MAX_FALL_SPEED);

        x += velocity.x * delta;
        resolveHorizontal(map);

        y += velocity.y * delta;
        onGround = false;
        resolveVertical(map);

        bounds.setPosition(x, y);
    }

    private void resolveHorizontal(TileMap map) {
        if (velocity.x == 0) {
            return;
        }
        int size = map.getTileSize();
        int rowBottom = tileIndex(y, size);
        int rowTop = tileIndex(y + HEIGHT - 0.01f, size);
        int col = velocity.x > 0 ? tileIndex(x + WIDTH, size) : tileIndex(x, size);
        for (int row = rowBottom; row <= rowTop; row++) {
            if (map.isSolid(col, row)) {
                x = velocity.x > 0 ? col * size - WIDTH : (col + 1) * size;
                velocity.x = 0;
                return;
            }
        }
    }

    private void resolveVertical(TileMap map) {
        if (velocity.y == 0) {
            return;
        }
        int size = map.getTileSize();
        int colLeft = tileIndex(x, size);
        int colRight = tileIndex(x + WIDTH - 0.01f, size);
        int row = velocity.y > 0 ? tileIndex(y + HEIGHT, size) : tileIndex(y, size);
        for (int col = colLeft; col <= colRight; col++) {
            if (map.isSolid(col, row)) {
                if (velocity.y < 0) {
                    y = (row + 1) * size;
                    onGround = true;
                } else {
                    y = row * size - HEIGHT;
                }
                velocity.y = 0;
                return;
            }
        }
    }

    private static int tileIndex(float pixels, int tileSize) {
        return MathUtils.floor(pixels / tileSize);
    }

    public void render(SpriteBatch batch) {
        if (sprite == null) {
            return;
        }
        // Flip the sprite horizontally when facing left.
        float drawX = facingRight ? x : x + WIDTH;
        float drawWidth = facingRight ? WIDTH : -WIDTH;
        batch.draw(sprite, drawX, y, drawWidth, HEIGHT);
    }

    // ---------------------------------------------------------------- stats and coins

    public void changeCourage(int amount) {
        courage = MathUtils.clamp(courage + amount, 0, MAX_STAT);
    }

    public void changeSanity(int amount) {
        sanity = MathUtils.clamp(sanity + amount, 0, MAX_STAT);
    }

    public void addCoins(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Use spendCoins() to remove coins.");
        }
        coins += amount;
    }

    /** Removes coins. The Shop checks the price first and throws InsufficientCoinsException. */
    public void spendCoins(int amount) {
        if (amount < 0 || amount > coins) {
            throw new IllegalArgumentException("Cannot spend " + amount + " coins with only " + coins + ".");
        }
        coins -= amount;
    }

    /** Uses an item Dayo owns. Using an item he does not have is an invalid action. */
    public void useItem(Item item) {
        if (!inventory.has(item)) {
            throw new IllegalArgumentException("Dayo does not have " + item.getName() + ".");
        }
        item.use(this);
    }

    /** Returns the first weapon in the inventory, or null if Dayo has none. */
    public Weapon getWeapon() {
        for (Item item : inventory.getAll()) {
            if (item instanceof Weapon weapon) {
                return weapon;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- checkpoints

    public Checkpoint saveCheckpoint() {
        return new Checkpoint(this);
    }

    /** Puts coins, stats and items back to how they were when the checkpoint was taken. */
    public void restore(Checkpoint c) {
        coins = c.getCoins();
        courage = c.getCourage();
        sanity = c.getSanity();
        inventory.clear();
        for (Item item : c.getItems()) {
            inventory.add(item);
        }
    }

    // ---------------------------------------------------------------- accessors

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        velocity.setZero();
        bounds.setPosition(x, y);
    }

    public void setSprite(Texture sprite) {
        this.sprite = sprite;
    }

    public int getCourage() {
        return courage;
    }

    public int getSanity() {
        return sanity;
    }

    public int getResistance() {
        return resistance;
    }

    public void setResistance(int resistance) {
        this.resistance = Math.max(0, resistance);
    }

    public int getCoins() {
        return coins;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
