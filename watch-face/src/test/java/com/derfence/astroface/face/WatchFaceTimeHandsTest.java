package com.derfence.astroface.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WatchFaceTimeHandsTest {
    @Test
    public void watchFaceUsesRotating24HourWffBitmapHandAndTickingSecondHand() throws Exception {
        Document document = loadWatchFaceXml();

        assertEquals(0, document.getElementsByTagName("HourHand").getLength());
        assertTrue(document.getElementsByTagName("Transform").getLength() > 0);
        assertEquals(6, document.getElementsByTagName("ComplicationSlot").getLength());
        assertSlotOrder(document, "6", "1", "7", "2", "4", "5");
        assertSlotsAreNotCustomizable(document);
        assertNull(complicationSlotWithId(document, "3"));

        Element hourHandImage = partImageNamed(document, "Hour24hHand");
        assertNotNull(hourHandImage);
        assertEquals("216", hourHandImage.getAttribute("x"));
        assertEquals("89", hourHandImage.getAttribute("y"));
        assertEquals("18", hourHandImage.getAttribute("width"));
        assertEquals("140", hourHandImage.getAttribute("height"));
        assertEquals("0.5", hourHandImage.getAttribute("pivotX"));
        assertEquals("0.97", hourHandImage.getAttribute("pivotY"));
        assertEquals("hour_hand_bitmap", firstChild(hourHandImage, "Image").getAttribute("resource"));

        Element hourHandTransform = firstChild(hourHandImage, "Transform");
        assertNotNull(hourHandTransform);
        assertEquals("angle", hourHandTransform.getAttribute("target"));
        assertEquals("[HOUR_0_23_MINUTE] * 15", hourHandTransform.getAttribute("value"));
        PngAssetAssertions.assertPng(mainResourceFile("drawable-nodpi/hour_hand_bitmap.png"), 18, 140);

        Element nowSeparator = partImageNamed(document, "NowSeparator");
        assertNotNull(nowSeparator);
        assertEquals("219", nowSeparator.getAttribute("x"));
        assertEquals("3", nowSeparator.getAttribute("y"));
        assertEquals("12", nowSeparator.getAttribute("width"));
        assertEquals("222", nowSeparator.getAttribute("height"));
        assertEquals("[HOUR_0_23_MINUTE] * 15", firstChild(nowSeparator, "Transform").getAttribute("value"));
        PngAssetAssertions.assertPng(mainResourceFile("drawable-nodpi/now_separator.png"), 12, 222);

        Element constellationSlot = complicationSlotWithId(document, "6");
        assertNotNull(constellationSlot);
        assertEquals("0", constellationSlot.getAttribute("x"));
        assertEquals("450", constellationSlot.getAttribute("width"));
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.ConstellationBackgroundDataSourceService",
            firstChild(constellationSlot, "DefaultProviderPolicy").getAttribute("primaryProvider")
        );

        Element horizonSlot = complicationSlotWithId(document, "7");
        assertNotNull(horizonSlot);
        assertEquals("55", horizonSlot.getAttribute("x"));
        assertEquals("55", horizonSlot.getAttribute("y"));
        assertEquals("340", horizonSlot.getAttribute("width"));
        assertEquals("340", horizonSlot.getAttribute("height"));
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.CelestialHorizonDataSourceService",
            firstChild(horizonSlot, "DefaultProviderPolicy").getAttribute("primaryProvider")
        );

        Element celestialOverlaySlot = complicationSlotWithId(document, "2");
        assertNotNull(celestialOverlaySlot);
        assertEquals("@string/slot_celestial_overlay_name", celestialOverlaySlot.getAttribute("displayName"));
        assertEquals("PHOTO_IMAGE EMPTY", celestialOverlaySlot.getAttribute("supportedTypes"));
        assertEquals("55", celestialOverlaySlot.getAttribute("x"));
        assertEquals("55", celestialOverlaySlot.getAttribute("y"));
        assertEquals("340", celestialOverlaySlot.getAttribute("width"));
        assertEquals("340", celestialOverlaySlot.getAttribute("height"));

        Element overlayPolicy = firstChild(celestialOverlaySlot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.CelestialOverlayDataSourceService",
            overlayPolicy.getAttribute("primaryProvider")
        );
        assertEquals("PHOTO_IMAGE", overlayPolicy.getAttribute("primaryProviderType"));

        Element overlayImage = firstChild(celestialOverlaySlot, "PartImage");
        assertEquals("CelestialOverlayImage", overlayImage.getAttribute("name"));
        assertEquals("[COMPLICATION.PHOTO_IMAGE]", firstChild(overlayImage, "Image").getAttribute("resource"));

        Element statusOverlaySlot = complicationSlotWithId(document, "4");
        assertNotNull(statusOverlaySlot);
        assertEquals("@string/slot_status_overlay_name", statusOverlaySlot.getAttribute("displayName"));
        assertEquals("PHOTO_IMAGE EMPTY", statusOverlaySlot.getAttribute("supportedTypes"));
        assertEquals("125", statusOverlaySlot.getAttribute("x"));
        assertEquals("120", statusOverlaySlot.getAttribute("y"));
        assertEquals("200", statusOverlaySlot.getAttribute("width"));
        assertEquals("190", statusOverlaySlot.getAttribute("height"));

        Element statusPolicy = firstChild(statusOverlaySlot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.StatusOverlayDataSourceService",
            statusPolicy.getAttribute("primaryProvider")
        );
        assertEquals("PHOTO_IMAGE", statusPolicy.getAttribute("primaryProviderType"));

        Element statusImage = firstChild(statusOverlaySlot, "PartImage");
        assertEquals("StatusOverlayImage", statusImage.getAttribute("name"));
        assertEquals("[COMPLICATION.PHOTO_IMAGE]", firstChild(statusImage, "Image").getAttribute("resource"));

        Element modeOverlaySlot = complicationSlotWithId(document, "5");
        assertNotNull(modeOverlaySlot);
        assertEquals("@string/slot_mode_overlay_name", modeOverlaySlot.getAttribute("displayName"));
        assertEquals("PHOTO_IMAGE EMPTY", modeOverlaySlot.getAttribute("supportedTypes"));

        Element modePolicy = firstChild(modeOverlaySlot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.ModeOverlayDataSourceService",
            modePolicy.getAttribute("primaryProvider")
        );
        assertEquals("PHOTO_IMAGE", modePolicy.getAttribute("primaryProviderType"));

        Element modeImage = firstChild(modeOverlaySlot, "PartImage");
        assertEquals("ModeOverlayImage", modeImage.getAttribute("name"));
        assertEquals("[COMPLICATION.PHOTO_IMAGE]", firstChild(modeImage, "Image").getAttribute("resource"));

        assertEquals(1, document.getElementsByTagName("MinuteHand").getLength());
        assertEquals(0, document.getElementsByTagName("Sweep").getLength());

        NodeList secondHands = document.getElementsByTagName("SecondHand");
        assertEquals(1, secondHands.getLength());

        Element tick = firstChild((Element) secondHands.item(0), "Tick");
        assertEquals("0.05", tick.getAttribute("duration"));
        assertEquals("1.0", tick.getAttribute("strength"));

        Element batteryOutline = partDrawNamed(document, "BatteryOutline");
        assertNotNull(batteryOutline);
        assertTrue(document.getElementsByTagName("Condition").getLength() > 0);
        assertTrue(document.getDocumentElement().getTextContent().contains("[BATTERY_PERCENT]"));
        assertElementOrder(batteryOutline, modeOverlaySlot);
        assertElementOrder(partDrawNamed(document, "BatteryFillDefault"), modeOverlaySlot);

        assertPartImageOrder(document, "CenterCap", "ModeOverlayImage");
        assertPartImageOrder(document, "NowSeparator", "CelestialHorizonImage");
        assertPartImageOrder(document, "CelestialHorizonImage", "CelestialOverlayImage");

        Element tapTarget = partDrawNamed(document, "ModeTapTarget");
        assertNotNull(tapTarget);
        Element launch = firstChild(tapTarget, "Launch");
        assertNotNull(launch);
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.ModeCycleActivity",
            launch.getAttribute("target")
        );
    }

    private static Document loadWatchFaceXml() throws Exception {
        File file = mainResourceFile("raw/watchface.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(file);
    }

    private static File mainResourceFile(String relativePath) {
        File file = new File("src/main/res/" + relativePath);
        if (!file.exists()) {
            file = new File("watch-face/src/main/res/" + relativePath);
        }
        return file;
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

    private static Element partDrawNamed(Document document, String name) {
        NodeList nodes = document.getElementsByTagName("PartDraw");
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        return null;
    }

    private static void assertPartImageOrder(Document document, String firstName, String secondName) {
        NodeList nodes = document.getElementsByTagName("PartImage");
        int firstIndex = -1;
        int secondIndex = -1;
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            if (firstName.equals(element.getAttribute("name"))) {
                firstIndex = i;
            }
            if (secondName.equals(element.getAttribute("name"))) {
                secondIndex = i;
            }
        }
        assertNotNull(partImageNamed(document, firstName));
        assertNotNull(partImageNamed(document, secondName));
        assertTrue(firstIndex < secondIndex);
    }

    private static void assertElementOrder(Node first, Node second) {
        assertNotNull(first);
        assertNotNull(second);
        short position = first.compareDocumentPosition(second);
        assertTrue((position & Node.DOCUMENT_POSITION_FOLLOWING) != 0);
    }

    private static void assertSlotOrder(Document document, String... expectedSlotIds) {
        NodeList nodes = document.getElementsByTagName("ComplicationSlot");
        assertEquals(expectedSlotIds.length, nodes.getLength());
        for (int i = 0; i < expectedSlotIds.length; i += 1) {
            Element element = (Element) nodes.item(i);
            assertEquals(expectedSlotIds[i], element.getAttribute("slotId"));
        }
    }

    private static void assertSlotsAreNotCustomizable(Document document) {
        NodeList nodes = document.getElementsByTagName("ComplicationSlot");
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            assertEquals("FALSE", element.getAttribute("isCustomizable"));
        }
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
