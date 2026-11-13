package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.player.Player;

/**
 * Parent of the folklore characters.
 * Shared positioning and rendering; subclasses define their interactions.
 */
public abstract class Creature implements Interactable {
    private final String name;
    protected float x;
    protected float y;
    protected Texture sprite;
    private int frameCount = 1;
    private float animationTime;

    protected Creature(String name, float x, float y) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Creature needs a name.");
        this.name = name;
        this.x = x;
        this.y = y;
    }

    @Override
    public abstract void interact(Player p);

    public void update(float delta) {
        if (Float.isFinite(delta) && delta > 0) animationTime = (animationTime + delta) % 1000;
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            int width = sprite.getWidth() / frameCount;
            int frame = (int) (animationTime / 0.18f) % frameCount;
            batch.draw(sprite, x, y, width, sprite.getHeight(),
                    frame * width, 0, width, sprite.getHeight(), false, false);
        }
    }

    public String getName() {
        return name;
    }

    /** Texture ownership remains with AssetLoader. */
    public void setSprite(Texture sprite) { this.sprite = sprite; }

    public void setSpriteSheet(Texture sprite, int frames) {
        if (sprite == null || frames < 1) {
            throw new IllegalArgumentException("Sprite sheet and positive frame count are required.");
        }
        this.sprite = sprite;
        // A missing-image placeholder is a single frame, not a five-frame strip.
        frameCount = sprite.getWidth() % frames == 0 ? frames : 1;
    }

    public boolean isNear(Player player) {
        return Math.abs(player.getX() - x) <= 100 && Math.abs(player.getY() - y) <= 96;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
