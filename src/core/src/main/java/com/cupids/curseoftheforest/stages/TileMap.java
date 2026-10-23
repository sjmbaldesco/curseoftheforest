package com.cupids.curseoftheforest.stages;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A level made in Tiled. The "solid" tile layer is copied into a 2D boolean array for collision,
 * and the "coins" and "markers" object layers give coin positions, the spawn point and the exit.
 *
 * <p>Row 0 is the bottom row, matching LibGDX's y-up coordinates.
 */
public class TileMap implements Disposable {
    public static final String SOLID_LAYER = "solid";
    public static final String COIN_LAYER = "coins";
    public static final String MARKER_LAYER = "markers";

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;
    private boolean[][] solid = new boolean[0][0];
    private int tileSize;
    private final ArrayList<Rectangle> coinMarkers = new ArrayList<>();
    private final Vector2 spawn = new Vector2();
    private final Rectangle exit = new Rectangle();

    public TileMap() {
    }

    /** Builds a map from a grid alone, with no Tiled file or graphics. Used by unit tests. */
    public TileMap(boolean[][] solid, int tileSize) {
        this.solid = solid;
        this.tileSize = tileSize;
    }

    /** Loads a .tmx file from the assets folder. Throws if the file or its "solid" layer is missing. */
    public void load(String path) {
        dispose();
        tiledMap = new TmxMapLoader().load(path);

        if (!(tiledMap.getLayers().get(SOLID_LAYER) instanceof TiledMapTileLayer layer)) {
            throw new IllegalStateException(path + " has no tile layer named '" + SOLID_LAYER + "'.");
        }
        tileSize = (int) layer.getTileWidth();
        solid = new boolean[layer.getHeight()][layer.getWidth()];
        for (int row = 0; row < layer.getHeight(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {
                solid[row][col] = layer.getCell(col, row) != null;
            }
        }

        coinMarkers.clear();
        for (Rectangle r : rectangles(COIN_LAYER)) {
            coinMarkers.add(r);
        }
        MapLayer markers = tiledMap.getLayers().get(MARKER_LAYER);
        if (markers != null) {
            for (MapObject object : markers.getObjects()) {
                if (object instanceof RectangleMapObject rect) {
                    if ("spawn".equals(object.getName())) {
                        rect.getRectangle().getPosition(spawn);
                    } else if ("exit".equals(object.getName())) {
                        exit.set(rect.getRectangle());
                    }
                }
            }
        }
        renderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    private List<Rectangle> rectangles(String layerName) {
        List<Rectangle> result = new ArrayList<>();
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject rect) {
                    result.add(new Rectangle(rect.getRectangle()));
                }
            }
        }
        return result;
    }

    /**
     * True if the tile at (col, row) blocks movement. The left and right edges of the map count as
     * walls; above and below the map is open, so the player can fall out of a pit.
     */
    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= getWidthInTiles()) {
            return true;
        }
        if (row < 0 || row >= solid.length) {
            return false;
        }
        return solid[row][col];
    }

    /** Removes and reports the first coin that overlaps the given area. */
    public boolean collectCoinAt(Rectangle r) {
        Iterator<Rectangle> it = coinMarkers.iterator();
        while (it.hasNext()) {
            if (it.next().overlaps(r)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void render(OrthographicCamera cam) {
        if (renderer != null) {
            renderer.setView(cam);
            renderer.render();
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getWidthInTiles() {
        return solid.length == 0 ? 0 : solid[0].length;
    }

    public float getPixelWidth() {
        return getWidthInTiles() * tileSize;
    }

    public float getPixelHeight() {
        return solid.length * tileSize;
    }

    public List<Rectangle> getCoinMarkers() {
        return Collections.unmodifiableList(coinMarkers);
    }

    public Vector2 getSpawn() {
        return spawn;
    }

    public Rectangle getExit() {
        return exit;
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        if (tiledMap != null) {
            tiledMap.dispose();
            tiledMap = null;
        }
    }
}
