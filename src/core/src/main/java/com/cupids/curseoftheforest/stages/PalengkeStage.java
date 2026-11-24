package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.cupids.curseoftheforest.screens.Hud;
import com.cupids.curseoftheforest.screens.KeyPrompt;
import com.cupids.curseoftheforest.screens.UiSkin;
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
    /** Burning-torch frames and the dead torch; null keeps the torch drawn in code. */
    private Animation<TextureRegion> torchLit;
    private TextureRegion torchUnlit;

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
        if (caveMouth != null && getParallax() != null) {
            // Past the cave mouth the forest backdrop gives way to rock walls and stalactites.
            getParallax().switchAt(caveMouth.x, new ParallaxBackground(game.getAssetLoader(), "stage2-wakwak-cave"));
        }
        torchLit = game.getAssetLoader().loadStrip("images/animated/torch_lit.png", 32, 0.08f);
        Animation<TextureRegion> unlit = game.getAssetLoader().loadStrip("images/animated/torch_unlit.png", 32, 1f);
        torchUnlit = unlit == null ? null : unlit.getKeyFrame(0);

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
                // Two out-of-step waves make the flame flicker unevenly, like a real fire.
                float r = Torch.LIGHT_RADIUS * (0.93f + 0.04f * MathUtils.sin(flicker * 13f) + 0.03f * MathUtils.sin(flicker * 31f + torch.getCentreX()));
                // The glow sits on the sprite's flame, near the top of its 64 px post.
                float glowY = torchLit != null ? torch.getBounds().y + 50 : torch.getCentreY();
                drawGlow(batch, TORCH_GLOW, torch.getCentreX(), glowY, r);
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

    @Override
    protected boolean growsFoliageAt(float worldX) {
        return caveMouth == null || worldX + 32 < caveMouth.x;
    }

    /** A wooden post with a flame on top; a dim ember and a refill bar while it cools down. */
    private void drawTorch(SpriteBatch batch, Torch torch) {
        Rectangle b = torch.getBounds();
        float cx = b.x + b.width / 2;
        if (torchLit != null && torchUnlit != null) {
            // The 32 by 64 sprite stands on the torch's spot; each torch flickers out of step.
            TextureRegion frame = torch.isLit() ? torchLit.getKeyFrame(flicker + b.x * 0.01f) : torchUnlit;
            batch.draw(frame, cx - 16, b.y, 32, 64);
            if (!torch.isLit() && !torch.isReady()) {
                Theme.rect(batch, Theme.INK, cx - 9, b.y - 6, 18, 3);
                Theme.rect(batch, Theme.GOLD, cx - 9, b.y - 6, 18 * torch.getRelightProgress(), 3);
            }
            return;
        }
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

    /** Short card text for each item (mockup 08); anything else uses the item's own description. */
    private static final java.util.Map<String, String> CARD_TEXT = java.util.Map.of(
            Shop.ITAK, "Search bushes 2x faster.",
            Shop.PUNYAL, "Search bushes 1.5x faster.",
            Shop.AGIMAT, "Doubles Stage 3 time.",
            Shop.MALUNGGAY_JUICE, "+25 Courage when used.",
            Shop.KWERDAS, "Serenade bonus.");

    /** Tonyo's stall: a header, five item cards and the key help (layout from mockup 08). */
    private void drawShop() {
        ScreenUtils.clear(Theme.NIGHT);
        SpriteBatch batch = game.getBatch();
        float top = ForestGame.WORLD_HEIGHT;
        batch.setProjectionMatrix(shopProjection);
        batch.begin();
        Texture backdrop = Theme.loadBackdrop(game.getAssetLoader(), "images/backgrounds/shop.png");
        Theme.drawBackdrop(batch, backdrop);

        UiSkin.frame(batch, UiSkin.Frame.PANEL, 16, top - 84, 928, 68);
        UiSkin.frame(batch, UiSkin.Frame.SLOT, 26, top - 78, 56, 56);
        if (tonyoPortrait != null) {
            batch.draw(tonyoPortrait, 30, top - 74, 48, 48);
        }
        Theme.text(batch, Theme.Size.HEADING, tonyo.getName() + "'s Stall", 104, top - 22, Theme.GOLD);
        Theme.text(batch, Theme.Size.SMALL, "\"Coins first, Dayo. Choose well, you cannot buy it all!\"", 104, top - 60, Theme.PAPER);
        UiSkin.image(batch, "icon-coin", 792, top - 48);
        Theme.text(batch, Theme.Size.SMALL, "Coins", 822, top - 30, Theme.PAPER);
        Theme.textRight(batch, Theme.Size.HEADING, String.valueOf(player.getCoins()), 924, top - 44, Theme.GOLD);

        List<Item> stock = tonyo.getShop().listStock();
        float cardW = 164;
        float gap = (ForestGame.WORLD_WIDTH - 32 - stock.size() * cardW) / Math.max(1, stock.size() - 1);
        for (int i = 0; i < stock.size(); i++) {
            drawCard(batch, stock.get(i), i + 1, 16 + i * (cardW + gap), top - 382, cardW, 270);
        }

        UiSkin.frame(batch, UiSkin.Frame.TOAST, 162, top - 442, 636, 32);
        Theme.textCentred(batch, Theme.Size.BODY, "[1-5] Buy    [U] Drink juice    [ENTER] Continue to Stage 3", 480, top - 418, Theme.PAPER);
        if (!shopMessage.isEmpty()) {
            UiSkin.frame(batch, UiSkin.Frame.TOAST, 162, top - 490, 636, 32);
            Theme.textCentred(batch, Theme.Size.BODY, shopMessage, 480, top - 466, Theme.GOLD);
        }
        batch.end();
    }

    /** One item card: number tag, icon box, name, what it does, and its price or "OWNED". */
    private void drawCard(SpriteBatch batch, Item item, int number, float x, float y, float w, float h) {
        boolean owned = player.getInventory().has(item);
        boolean affordable = player.getCoins() >= item.getPrice();
        float centre = x + w / 2;
        batch.setColor(1f, 1f, 1f, owned ? 0.6f : 1f);
        UiSkin.frame(batch, UiSkin.Frame.PANEL, x, y, w, h);
        UiSkin.frame(batch, UiSkin.Frame.SLOT, x + 18, y + h - 128, w - 36, 104);
        String iconPath = Hud.iconPath(item.getName());
        if (iconPath != null) {
            Texture icon = game.getAssetLoader().loadImage(iconPath);
            batch.draw(icon, centre - 40, y + h - 116, 80, 80);
        }
        batch.setColor(Color.WHITE);
        KeyPrompt.keyCap(batch, String.valueOf(number), x + 8, y + h - 36);

        // Long names drop to the small font so they stay inside the card.
        Theme.Size nameSize = Theme.width(Theme.font(Theme.Size.BODY), item.getName()) <= w - 16 ? Theme.Size.BODY : Theme.Size.SMALL;
        Theme.textCentred(batch, nameSize, item.getName(), centre, y + h - 144, owned ? Theme.DIM : Theme.PAPER);
        BitmapFont small = Theme.font(Theme.Size.SMALL);
        small.setColor(owned ? Theme.DIM : Theme.MUTED);
        small.draw(batch, CARD_TEXT.getOrDefault(item.getName(), item.getDescription()), x + 10, y + h - 170, w - 20,
                com.badlogic.gdx.utils.Align.center, true);
        if (owned) {
            Theme.textCentred(batch, Theme.Size.TALK, "OWNED", centre, y + 52, Theme.SUCCESS);
        } else if (affordable) {
            drawPrice(batch, item.getPrice(), centre, y + 52, Theme.GOLD);
        } else {
            // Too dear right now: the price sits in a red box with the reason under it.
            UiSkin.frame(batch, UiSkin.Frame.TOAST_WARNING, x + 10, y + 8, w - 20, 60);
            drawPrice(batch, item.getPrice(), centre, y + 58, Theme.WARNING);
            Theme.textCentred(batch, Theme.Size.SMALL, "Not enough coins", centre, y + 30, Theme.WARNING);
        }
    }

    private static void drawPrice(SpriteBatch batch, int price, float centre, float top, Color color) {
        String text = String.valueOf(price);
        float width = 28 + Theme.width(Theme.font(Theme.Size.TALK), text);
        float left = Math.round(centre - width / 2);
        UiSkin.image(batch, "icon-coin", left, top - 21);
        Theme.text(batch, Theme.Size.TALK, text, left + 28, top, color);
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
