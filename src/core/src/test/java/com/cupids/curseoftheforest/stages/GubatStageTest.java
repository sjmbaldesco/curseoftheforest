package com.cupids.curseoftheforest.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cupids.curseoftheforest.items.Accessory;
import com.cupids.curseoftheforest.minigames.Shop;
import com.cupids.curseoftheforest.player.Player;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class GubatStageTest {

    @Test
    void agimatDoublesTheForestTime() {
        Player dayo = new Player(0, 0);
        assertEquals(90f, GubatStage.timeLimitFor(dayo));
        // The shop's real item name, not "Agimat", is what the stage checks.
        dayo.getInventory().add(new Accessory(Shop.AGIMAT, 5, "", 0));
        assertEquals(180f, GubatStage.timeLimitFor(dayo));
    }

    @Test
    void agimatHalvesTheSanityDrain() {
        Player dayo = new Player(0, 0);
        float withoutAgimat = GubatStage.sanityDrainFor(dayo);
        dayo.getInventory().add(new Accessory(Shop.AGIMAT, 5, "", 0));
        assertEquals(withoutAgimat / 2, GubatStage.sanityDrainFor(dayo), 0.0001f);
        assertTrue(withoutAgimat > 0.5f, "the drain must be strong enough to notice");
    }

    @Test
    void gubatMapFollowsTheMapContract() throws Exception {
        File map = new File("../assets/maps/gubat.tmx");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(map);

        Set<String> layers = new HashSet<>();
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element e = (Element) all.item(i);
            if (e.hasAttribute("name") && (e.getTagName().equals("layer") || e.getTagName().equals("objectgroup"))) {
                layers.add(e.getAttribute("name"));
            }
        }
        assertTrue(layers.containsAll(Set.of("solid", "coins", "markers", "bushes")), layers.toString());

        Set<String> markers = new HashSet<>();
        int bushes = 0;
        NodeList groups = doc.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            NodeList objects = group.getElementsByTagName("object");
            for (int j = 0; j < objects.getLength(); j++) {
                Element object = (Element) objects.item(j);
                if (group.getAttribute("name").equals("markers")) {
                    markers.add(object.getAttribute("name"));
                } else if (group.getAttribute("name").equals("bushes")) {
                    bushes++;
                }
            }
        }
        assertTrue(markers.containsAll(Set.of("spawn", "exit", "tikbalang")), markers.toString());
        assertEquals(6, bushes, "Six bushes: three hide the clue pieces");
        assertEquals(3, GubatStage.CLUE.length);
    }
}
