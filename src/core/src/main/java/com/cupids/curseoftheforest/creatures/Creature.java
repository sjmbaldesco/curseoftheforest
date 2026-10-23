package com.cupids.curseoftheforest.creatures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cupids.curseoftheforest.player.Player;

/**
 * Parent of the folklore characters.
 * TODO(Johndel): stub from the UML.
 */
public abstract class Creature implements Interactable {
    private final String name;
    protected float x;
    protected float y;
    protected Texture sprite;

    protected Creature(String name, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    @Override
    public abstract void interact(Player p);

    public void update(float delta) {
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            batch.draw(sprite, x, y);
        }
    }

    public String getName() {
        return name;
    }
}
