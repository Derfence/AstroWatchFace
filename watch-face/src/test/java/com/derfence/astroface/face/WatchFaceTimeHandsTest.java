package com.derfence.astroface.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WatchFaceTimeHandsTest {
    @Test
    public void watchFaceUses24HourImageHandAndTickingSecondHand() throws Exception {
        Document document = loadWatchFaceXml();

        assertEquals(0, document.getElementsByTagName("HourHand").getLength());
        assertEquals(0, document.getElementsByTagName("Transform").getLength());
        assertNull(partImageNamed(document, "TwentyFourHourHand"));

        Element hourHandSlot = complicationSlotWithId(document, "3");
        assertNotNull(hourHandSlot);
        assertEquals("@string/slot_24h_hand_name", hourHandSlot.getAttribute("displayName"));
        assertEquals("PHOTO_IMAGE EMPTY", hourHandSlot.getAttribute("supportedTypes"));

        Element policy = firstChild(hourHandSlot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.Hour24hHandDataSourceService",
            policy.getAttribute("primaryProvider")
        );
        assertEquals("PHOTO_IMAGE", policy.getAttribute("primaryProviderType"));

        Element hourHandImage = firstChild(hourHandSlot, "PartImage");
        assertEquals("Hour24hHandImage", hourHandImage.getAttribute("name"));
        assertEquals("[COMPLICATION.PHOTO_IMAGE]", firstChild(hourHandImage, "Image").getAttribute("resource"));

        assertEquals(1, document.getElementsByTagName("MinuteHand").getLength());
        assertEquals(0, document.getElementsByTagName("Sweep").getLength());

        NodeList secondHands = document.getElementsByTagName("SecondHand");
        assertEquals(1, secondHands.getLength());

        Element tick = firstChild((Element) secondHands.item(0), "Tick");
        assertEquals("0.05", tick.getAttribute("duration"));
        assertEquals("1.0", tick.getAttribute("strength"));
    }

    private static Document loadWatchFaceXml() throws Exception {
        File file = new File("src/main/res/raw/watchface.xml");
        if (!file.exists()) {
            file = new File("watch-face/src/main/res/raw/watchface.xml");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(file);
    }

    private static Element partImageNamed(Document document, String name) {
        NodeList nodes = document.getElementsByTagName("PartImage");
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        return null;
    }

    private static Element complicationSlotWithId(Document document, String slotId) {
        NodeList nodes = document.getElementsByTagName("ComplicationSlot");
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            if (slotId.equals(element.getAttribute("slotId"))) {
                return element;
            }
        }
        return null;
    }

    private static Element firstChild(Element parent, String tagName) {
        return (Element) parent.getElementsByTagName(tagName).item(0);
    }
}
