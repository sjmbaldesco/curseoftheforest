package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cupids.curseoftheforest.items.Accessory;
import com.cupids.curseoftheforest.player.Player;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class PalengkeStageTest {

    @Test
    void rosarySoftensTheBite() {
        Player dayo = new Player(0, 0);
        assertEquals(PalengkeStage.CATCH_COURAGE_LOSS, PalengkeStage.bitePenalty(dayo));
        dayo.getInventory().add(new Accessory("Rosary", 0, "", 0));
        assertEquals(PalengkeStage.CATCH_COURAGE_LOSS - PalengkeStage.ROSARY_PROTECTION, PalengkeStage.bitePenalty(dayo));
    }

    @Test
    void caveMapHasTorchesSwarmAndFifteenCoins() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("../assets/maps/palengke.tmx"));
        Map<String, Integer> objectsPerLayer = new HashMap<>();
        Map<String, Boolean> markers = new HashMap<>();
        NodeList groups = doc.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            NodeList objects = group.getElementsByTagName("object");
            objectsPerLayer.put(group.getAttribute("name"), objects.getLength());
            if (group.getAttribute("name").equals("markers")) {
                for (int j = 0; j < objects.getLength(); j++) {
                    markers.put(((Element) objects.item(j)).getAttribute("name"), true);
                }
            }
        }
        assertEquals(15, objectsPerLayer.get("coins"), "the proposal's coin cap, raised to 15");
        assertTrue(objectsPerLayer.get("torches") >= 6);
        assertTrue(objectsPerLayer.get("wakwak") >= 3, "a swarm, not a single Wakwak");
        for (String name : new String[] {"spawn", "exit", "tonyo", "cave"}) {
            assertTrue(markers.containsKey(name), "missing marker " + name);
        }
    }
}
