package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;

class TileMapTest {

    private static MapLayer objectLayer(String name, RectangleMapObject... objects) {
        MapLayer layer = new MapLayer();
        layer.setName(name);
        for (RectangleMapObject object : objects) {
            layer.getObjects().add(object);
        }
        return layer;
    }

    private static RectangleMapObject rect(String name, float x, float y) {
        RectangleMapObject object = new RectangleMapObject(x, y, 32, 32);
        object.setName(name);
        return object;
    }

    private static TiledMap mapWithSolidLayer() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer solid = new TiledMapTileLayer(4, 3, 32, 32);
        solid.setName(TileMap.SOLID_LAYER);
        solid.setCell(0, 0, new TiledMapTileLayer.Cell());
        map.getLayers().add(solid);
        return map;
    }

    @Test
    void readsNamedMarkersAndExtraObjectLayers() {
        TiledMap map = mapWithSolidLayer();
        map.getLayers().add(objectLayer(TileMap.MARKER_LAYER,
                rect("spawn", 32, 64), rect("exit", 96, 32), rect("tikbalang", 64, 32)));
        map.getLayers().add(objectLayer("bushes", rect(null, 10, 20), rect(null, 30, 40)));

        TileMap tileMap = new TileMap();
        tileMap.read(map);

        assertTrue(tileMap.isSolid(0, 0));
        assertEquals(32f, tileMap.getSpawn().x);
        assertEquals(96f, tileMap.getExit().x);
        Rectangle tikbalang = tileMap.getMarker("tikbalang");
        assertEquals(64f, tikbalang.x);
        assertNull(tileMap.getMarker("missing"));
        assertEquals(2, tileMap.getObjects("bushes").size());
        assertTrue(tileMap.getObjects("no-such-layer").isEmpty());

        // Callers get copies, so they cannot move the map's markers by accident.
        tikbalang.x = 0;
        assertEquals(64f, tileMap.getMarker("tikbalang").x);
    }

    @Test
    void mapWithoutSolidLayerIsRejected() {
        assertThrows(IllegalStateException.class, () -> new TileMap().read(new TiledMap()));
    }
}
