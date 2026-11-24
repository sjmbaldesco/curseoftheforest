package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cupids.curseoftheforest.ForestGame;
import com.cupids.curseoftheforest.creatures.DuwendeTonyo;
import com.cupids.curseoftheforest.creatures.Torch;
import com.cupids.curseoftheforest.creatures.Wakwak;
import com.cupids.curseoftheforest.items.Consumable;
import com.cupids.curseoftheforest.items.Item;
import com.cupids.curseoftheforest.minigames.InsufficientCoinsException;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;
import com.cupids.curseoftheforest.screens.DialogueBox;
import com.cupids.curseoftheforest.screens.Theme;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 2, the Test of Courage. Dayo crosses the forest edge, meets Duwende Tonyo, then has
 * three minutes in the Wakwak Cave to grab coins while a swarm hunts him. Torches burn for 5
 * seconds when touched and slow any Wakwak in their light. The cave exit leads back to Tonyo's
 * shop, where the coins are spent.
 */
public class PalengkeStage extends GameStage {
    static final float CAVE_SECONDS = 180f;
    static final int CATCH_COURAGE_LOSS = 10;
    static final int ROSARY_PROTECTION = 2;
    private static final float CATCH_COOLDOWN = 1.5f;
    private static final float HURRY_SECONDS = 30f;
    private static final int[] SHOP_KEYS = {
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5
    };
    private static final Color CAVE_DARK = new Color(0.02f, 0.0f, 0.06f, 0.72f);
    private static final Color TORCH_GLOW = new Color(1f, 0.62f, 0.3f, 0.55f);
    private static final Color DAYO_GLOW = new Color(0.7f, 0.7f, 1f, 0.18f);
    private final Matrix4 shopProjection = new Matrix4()
            .setToOrtho2D(0, 0, ForestGame.WORLD_WIDTH, ForestGame.WORLD_HEIGHT);
    private final List<Wakwak> swarm = new ArrayList<>();
    private final List<Torch> torches = new ArrayList<>();
    private DuwendeTonyo tonyo;
    private TextureRegion tonyoPortrait;
    private Rectangle caveMouth;
    private boolean talkedToTonyo;
    private boolean inCave;
    private boolean warnedHurry;
    private float catchCooldown;
    private boolean reachedShop;
    private String shopMessage;
    private Texture glow;
    private float flicker;

    public PalengkeStage(ForestGame game) {
        super(game, "Stage 2: Ang Palengke ng Duwende", "maps/palengke.tmx");
    }

    /** Courage a bite costs: the Rosary softens it. */
    static int bitePenalty(Player p) {
        return p.getInventory().has("Rosary") ? CATCH_COURAGE_LOSS - ROSARY_PROTECTION : CATCH_COURAGE_LOSS;
    }

    @Override
    protected void init() {
        // The cave clock waits at 3:00 until Dayo walks in.
        timer = new StageTimer(CAVE_SECONDS, false);

        Rectangle stall = tileMap.getMarker("tonyo");
        if (stall == null) {
            stall = new Rectangle(tileMap.getSpawn().x + 160, tileMap.getSpawn().y, 64, 64);
        }
        tonyo = new DuwendeTonyo(stall.x, stall.y);
        Texture tonyoSheet = game.getAssetLoader().loadImage("images/duwende-tonyo.png");
        tonyo.setSpriteSheet(tonyoSheet, 5);
        tonyoPortrait = DialogueBox.portraitFrom(tonyoSheet, 5);
        caveMouth = tileMap.getMarker("cave");

        Texture wakwakSheet = game.getAssetLoader().loadImage("images/wakwak.png");
        swarm.clear();
        for (Rectangle roost : tileMap.getObjects("wakwak")) {
            Wakwak wakwak = new Wakwak(roost.x + roost.width / 2, roost.y);
            wakwak.setSpriteSheet(wakwakSheet, 5);
            wakwak.interact(player);
            swarm.add(wakwak);
        }
        torches.clear();
        for (Rectangle spot : tileMap.getObjects("torches")) {
            torches.add(new Torch(spot));
        }

        // On a retry Dayo already knows the way, so Tonyo's speech is skipped.
        talkedToTonyo = retried || caveMouth == null;
        if (talkedToTonyo) {
            tonyo.interact(player);
        }
        inCave = false;
        warnedHurry = false;
        catchCooldown = 0f;
        reachedShop = false;
        shopMessage = "";
        setGoal(talkedToTonyo ? "Enter the Wakwak Cave" : "Cross the forest edge to Tonyo's stall");
    }

    @Override
    public void update(float delta) {
        tonyo.update(delta);
        flicker = (flicker + delta) % 1000f;
        if (!inCave) {
            if (talkedToTonyo && (caveMouth == null || caveMouth.overlaps(player.getBounds()))) {
                inCave = true;
                timer.start();
                setGoal("Grab coins, dodge the swarm, find the cave exit");
                toast.show("Torches burn for 5 seconds when you touch them.");
            }
            return;
        }

        for (Torch torch : torches) {
            boolean wasLit = torch.isLit();
            torch.update(delta);
            if (torch.touches(player) && torch.isReady()) {
                torch.interact(player);
                toast.show("The torch flares up!");
            } else if (wasLit && !torch.isLit()) {
                toast.show("A torch dies down...");
            }
        }
        catchCooldown = Math.max(0f, catchCooldown - delta);
        for (Wakwak wakwak : swarm) {
            wakwak.setSlowed(isInLight(wakwak.getX(), wakwak.getY()));
            wakwak.update(delta);
            if (catchCooldown == 0f && wakwak.hasCaughtDayo()) {
                int lost = Math.min(player.getCourage(), bitePenalty(player));
                player.changeCourage(-lost);
                catchCooldown = CATCH_COOLDOWN;
                wakwak.retreat();
                toast.warn("The Wakwak caught you! Courage -" + lost);
            }
        }
        if (!warnedHurry && timer.getRemaining() <= HURRY_SECONDS) {
            warnedHurry = true;
            toast.warn("The swarm is getting restless. Hurry!");
        }
        if (reachedExit() && !isFailed()) {
            reachedShop = true;
        }
    }

    /** True if any burning torch lights the point (x, y). */
    boolean isInLight(float x, float y) {
        for (Torch torch : torches) {
            if (torch.lights(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void handleInput() {
        if (reachedShop) {
            handleShopInput();
            return;
        }
        if (talkedToTonyo) {
            return;
        }
        if (tonyo.isNear(player)) {
            showPrompt("E", "Talk", tonyo.getX() + 32, tonyo.getY() + 64);
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                talkToTonyo();
            }
        }
        // The cave stays closed until Tonyo has explained the torches.
        if (caveMouth != null && player.getX() + Player.WIDTH > caveMouth.x) {
            player.setPosition(caveMouth.x - Player.WIDTH - 1, player.getY());
            toast.show("Talk to Duwende Tonyo before you go in.");
        }
    }

    private void talkToTonyo() {
        dialogue.say(tonyo.getName(), tonyoPortrait,
                "Ho there, traveller! Tabi, tabi. You're standing in front of the finest stall in the Narra Forest.",
                "But nothing is free. Bring me coins. You get them from the platform trials, or from looting bushes along the way.",
                "No coins? Then go to the Kuweba ng Wakwak. Collect as many as you can, and don't get caught by the swarm.",
                "There are torches inside. Touch one and it burns for 5 seconds, then it needs 10 seconds to light again.",
                "The Wakwak hate light. Near a burning torch they slow to a crawl. Plan your path from torch to torch.");
        dialogue.setOnClose(() -> {
            talkedToTonyo = true;
            tonyo.interact(player); // his stall is open for when Dayo comes back
            setGoal("Enter the Wakwak Cave");
        });
    }

    private void handleShopInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            completed = true;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            drinkJuice();
            return;
        }

        List<Item> stock = tonyo.getShop().listStock();
        for (int i = 0; i < stock.size(); i++) {
            if (Gdx.input.isKeyJustPressed(SHOP_KEYS[i])) {
                buy(stock.get(i));
                return;
            }
        }
    }

    private void drinkJuice() {
        for (Item item : player.getInventory().getAll()) {
            if (item instanceof Consumable && Shop.MALUNGGAY_JUICE.equals(item.getName())) {
                int courageBefore = player.getCourage();
                if (courageBefore == Player.MAX_STAT) {
                    shopMessage = "Courage is full. Save the juice for Stage 4.";
                    return;
                }
                player.useItem(item);
                shopMessage = "Drank Malunggay Juice. Recovered "
                        + (player.getCourage() - courageBefore) + " Courage.";
                return;
            }
        }
        shopMessage = "Buy Malunggay Juice first.";
    }

    private void buy(Item item) {
        if (player.getInventory().has(item)) {
            shopMessage = "You already bought " + item.getName() + ".";
            return;
        }
        try {
            tonyo.getShop().purchase(player, item);
            shopMessage = "Bought " + item.getName() + ".";
        } catch (InsufficientCoinsException exception) {
            shopMessage = exception.getMessage();
        }
    }

    @Override
    public void render(float delta) {
        if (reachedShop) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.toMainMenu();
                return;
            }
            handleShopInput();
            if (isComplete()) {
                game.onStageComplete(this);
                return;
            }
            drawShop();
            return;
        }

        super.render(delta);
    }

    @Override
    protected void drawWorld(SpriteBatch batch) {
        tonyo.render(batch);
        if (caveMouth == null) {
            for (Wakwak wakwak : swarm) wakwak.render(batch);
            return;
        }
        // The cave is dark: shade everything right of the mouth, then add light where it burns.
        Theme.rect(batch, CAVE_DARK, caveMouth.x, 0, tileMap.getPixelWidth() - caveMouth.x, tileMap.getPixelHeight());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Torch torch : torches) {
            if (torch.isLit()) {
                float r = Torch.LIGHT_RADIUS * (0.95f + 0.05f * MathUtils.sin(flicker * 13f));
                drawGlow(batch, TORCH_GLOW, torch.getCentreX(), torch.getCentreY(), r);
            }
        }
        if (player.getX() > caveMouth.x - 64) {
            drawGlow(batch, DAYO_GLOW, player.getX() + Player.WIDTH / 2, player.getY() + Player.HEIGHT / 2, 64f);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for (Torch torch : torches) {
            drawTorch(batch, torch);
        }
        for (Wakwak wakwak : swarm) {
            wakwak.render(batch);
        }
    }

    private void drawGlow(SpriteBatch batch, Color tint, float cx, float cy, float radius) {
        Color previous = new Color(batch.getColor());
        batch.setColor(tint);
        batch.draw(glowTexture(), cx - radius, cy - radius, radius * 2, radius * 2);
        batch.setColor(previous);
    }

    /** A wooden post with a flame on top; a dim ember and a refill bar while it cools down. */
    private void drawTorch(SpriteBatch batch, Torch torch) {
        Rectangle b = torch.getBounds();
        float cx = b.x + b.width / 2;
        Theme.rect(batch, Theme.INK, cx - 3, b.y, 6, b.height - 6);
        Theme.rect(batch, Color.valueOf("6B4A2B"), cx - 2, b.y, 4, b.height - 7);
        Theme.rect(batch, Theme.INK, cx - 5, b.y + b.height - 9, 10, 5);
        if (torch.isLit()) {
            float h = 10 + 4 * MathUtils.sin(flicker * 17f) + 6 * torch.getFlameLeft();
            Theme.rect(batch, Color.valueOf("FF7A2B"), cx - 4, b.y + b.height - 5, 8, h);
            Theme.rect(batch, Theme.GOLD, cx - 2, b.y + b.height - 4, 4, h * 0.6f);
        } else if (torch.isReady()) {
            Theme.rect(batch, Color.valueOf("8A3B1E"), cx - 3, b.y + b.height - 5, 6, 4);
        } else {
            Theme.rect(batch, Color.valueOf("3A2A22"), cx - 3, b.y + b.height - 5, 6, 3);
            Theme.rect(batch, Theme.INK, cx - 9, b.y - 6, 18, 3);
            Theme.rect(batch, Theme.GOLD, cx - 9, b.y - 6, 18 * torch.getRelightProgress(), 3);
        }
    }

    /** A soft white circle that fades to clear at the edge, made once in code. */
    private Texture glowTexture() {
        if (glow == null) {
            int size = 64;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            float c = (size - 1) / 2f;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float d = (float) Math.sqrt((x - c) * (x - c) + (y - c) * (y - c)) / c;
                    float a = MathUtils.clamp(1f - d, 0f, 1f);
                    pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a * a));
                }
            }
            glow = new Texture(pixmap);
            glow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
        }
        return glow;
    }

    private void drawShop() {
        ScreenUtils.clear(0.09f, 0.16f, 0.12f, 1f);
        SpriteBatch batch = game.getBatch();
        BitmapFont font = game.getFont();
        float left = 48f;
        float top = ForestGame.WORLD_HEIGHT - 48f;
        batch.setProjectionMatrix(shopProjection);
        batch.begin();
        font.draw(batch, tonyo.getName() + "'s Shop", left, top);
        font.draw(batch, "Dayo's coins: " + player.getCoins()
                + "   Courage: " + player.getCourage(), left, top - 32);
        {
            font.draw(batch, "Tonyo: \"Ha! You came back with all your fingers. Let's see those coins.\"", left, top - 72);
            font.draw(batch, "Press 1-5 to buy an item. Press Enter to continue to Stage 3.", left, top - 100);
            List<Item> stock = tonyo.getShop().listStock();
            for (int i = 0; i < stock.size(); i++) {
                Item item = stock.get(i);
                String price = player.getInventory().has(item) ? "Bought" : item.getPrice() + " coins";
                font.draw(batch, (i + 1) + "   " + item.getName() + "   " + price, left, top - 140 - i * 30);
            }
            font.draw(batch, "U: Drink Malunggay Juice (+25 Courage). Save it for +2 minutes in Stage 4.",
                    left, top - 300);
            font.draw(batch, shopMessage, left, top - 332);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (glow != null) {
            glow.dispose();
            glow = null;
        }
        super.dispose();
    }
}
