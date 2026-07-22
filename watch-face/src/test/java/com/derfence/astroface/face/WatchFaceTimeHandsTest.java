package com.derfence.astroface.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        assertEquals(8, document.getElementsByTagName("ComplicationSlot").getLength());
        assertSlotOrder(document, "6", "1", "7", "2", "3", "8", "4", "5");
        assertSlotsAreNotCustomizable(document);
        assertNull(complicationSlotWithId(document, "9"));

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

        assertNumericDialSlot(document);

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

        assertMotionSlot(
            document,
            "2",
            "@string/slot_celestial_inner_name",
            "CelestialInnerMotionDataSourceService"
        );
        assertMotionSlot(
            document,
            "3",
            "@string/slot_celestial_middle_name",
            "CelestialMiddleMotionDataSourceService"
        );
        assertMotionSlot(
            document,
            "8",
            "@string/slot_celestial_outer_name",
            "CelestialOuterMotionDataSourceService"
        );

        assertMotionBody(document, "Sun", "celestial_sun", "RANGED_VALUE_MIN", null);
        assertMotionBody(document, "Moon", "celestial_moon", "RANGED_VALUE_VALUE", "4194304");
        assertMotionBody(document, "Mercury", "celestial_mercury", "RANGED_VALUE_MAX", "8388608");
        assertMotionBody(document, "Venus", "celestial_venus", "RANGED_VALUE_MIN", null);
        assertMotionBody(document, "Mars", "celestial_mars", "RANGED_VALUE_VALUE", "4194304");
        assertMotionBody(document, "Jupiter", "celestial_jupiter", "RANGED_VALUE_MAX", "8388608");
        assertMotionBody(document, "Saturn", "celestial_saturn", "RANGED_VALUE_MIN", null);
        assertMotionBody(document, "Uranus", "celestial_uranus", "RANGED_VALUE_VALUE", "4194304");
        assertMotionBody(document, "Neptune", "celestial_neptune", "RANGED_VALUE_MAX", "8388608");
        assertPolarisReticleMarker(document);

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
        assertPartImageOrder(document, "CelestialHorizonImage", "SunTail");
        assertPartImageOrder(document, "NeptuneIcon", "StatusOverlayImage");
        assertElementOrder(complicationSlotWithId(document, "8"), groupNamed(document, "PolarisReticlePosition"));
        assertElementOrder(groupNamed(document, "PolarisReticlePosition"), modeOverlaySlot);

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

    private static Element groupNamed(Document document, String name) {
        NodeList nodes = document.getElementsByTagName("Group");
        for (int i = 0; i < nodes.getLength(); i += 1) {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        return null;
    }

    private static void assertMotionSlot(
        Document document,
        String slotId,
        String displayName,
        String serviceName
    ) {
        Element slot = complicationSlotWithId(document, slotId);
        assertNotNull(slot);
        assertEquals(displayName, slot.getAttribute("displayName"));
        assertEquals("RANGED_VALUE EMPTY", slot.getAttribute("supportedTypes"));
        assertEquals("55", slot.getAttribute("x"));
        assertEquals("55", slot.getAttribute("y"));
        assertEquals("340", slot.getAttribute("width"));
        assertEquals("340", slot.getAttribute("height"));
        Element policy = firstChild(slot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication." + serviceName,
            policy.getAttribute("primaryProvider")
        );
        assertEquals("RANGED_VALUE", policy.getAttribute("primaryProviderType"));
    }

    private static void assertNumericDialSlot(Document document) throws Exception {
        Element slot = complicationSlotWithId(document, "1");
        assertNotNull(slot);
        assertEquals("RANGED_VALUE EMPTY", slot.getAttribute("supportedTypes"));
        Element policy = firstChild(slot, "DefaultProviderPolicy");
        assertEquals(
            "com.derfence.astroface.wear/com.derfence.astroface.wear.complication.Dial24hDataSourceService",
            policy.getAttribute("primaryProvider")
        );
        assertEquals("RANGED_VALUE", policy.getAttribute("primaryProviderType"));
        assertNull(partImageNamed(document, "Dial24hImage"));

        int hourTickCount = 0;
        int majorHourTickCount = 0;
        int minuteTickCount = 0;
        NodeList groups = document.getElementsByTagName("Group");
        for (int i = 0; i < groups.getLength(); i += 1) {
            Element group = (Element) groups.item(i);
            String name = group.getAttribute("name");
            if (name.startsWith("Dial24hHourTick")) {
                int hour = Integer.parseInt(name.substring("Dial24hHourTick".length()));
                assertEquals(hour * 15.0, Double.parseDouble(group.getAttribute("angle")), 0.001);
                assertEquals(1, group.getElementsByTagName("Line").getLength());
                assertEquals(0, group.getElementsByTagName("Ellipse").getLength());

                Element line = (Element) group.getElementsByTagName("Line").item(0);
                if (hour % 3 == 0) {
                    assertEquals("29", line.getAttribute("startY"));
                    majorHourTickCount += 1;
                } else {
                    assertEquals("20", line.getAttribute("startY"));
                }
                hourTickCount += 1;
            } else if (name.startsWith("DialMinuteTick")) {
                int minute = Integer.parseInt(name.substring("DialMinuteTick".length()));
                assertEquals(0, minute % 5);
                assertEquals(minute * 6.0, Double.parseDouble(group.getAttribute("angle")), 0.001);
                assertEquals(0, group.getElementsByTagName("Line").getLength());
                assertEquals(1, group.getElementsByTagName("Ellipse").getLength());
                minuteTickCount += 1;
            }
        }
        assertEquals(24, hourTickCount);
        assertEquals(8, majorHourTickCount);
        assertEquals(12, minuteTickCount);
        assertEquals(7, slot.getElementsByTagName("PartText").getLength());

        String[] drawParts = {
            "AstronomicalNightArc",
            "MorningTwilightArc",
            "SunlightArc",
            "EveningTwilightArc",
            "MoonVisibilityFullDay",
            "Dial24hOuterCircle"
        };
        for (String partName : drawParts) {
            assertNotNull(partDrawNamed(document, partName));
        }
        String[] markers = {
            "AstronomicalDawnMarker",
            "SunriseMarker",
            "SunsetMarker",
            "AstronomicalDuskMarker",
            "MoonriseMarker",
            "MoonsetMarker"
        };
        for (String markerName : markers) {
            assertNotNull(groupNamed(document, markerName));
        }

        String xml = new String(
            Files.readAllBytes(mainResourceFile("raw/watchface.xml").toPath()),
            StandardCharsets.UTF_8
        );
        assertTrue(xml.contains("floor([COMPLICATION.RANGED_VALUE_MIN] / 2048)"));
        assertTrue(xml.contains("[COMPLICATION.RANGED_VALUE_MIN] % 2048"));
        assertTrue(xml.contains("floor(([COMPLICATION.RANGED_VALUE_VALUE] - 4194304) / 2048)"));
        assertTrue(xml.contains("([COMPLICATION.RANGED_VALUE_VALUE] - 4194304) % 2048"));
        assertTrue(xml.contains("floor(([COMPLICATION.RANGED_VALUE_MAX] - 8388608) / 2048)"));
        assertTrue(xml.contains("([COMPLICATION.RANGED_VALUE_MAX] - 8388608) % 2048"));
        assertTrue(xml.contains(
            "0.25 * (([COMPLICATION.RANGED_VALUE_VALUE] - 4194304) % 2048)"
        ));
        assertTrue(xml.contains(
            "0.25 * (([COMPLICATION.RANGED_VALUE_MAX] - 8388608) % 2048)"
        ));
        assertTrue(!xml.contains(
            "0.25 * ([COMPLICATION.RANGED_VALUE_VALUE] - 4194304) % 2048"
        ));
        assertTrue(!xml.contains(
            "0.25 * ([COMPLICATION.RANGED_VALUE_MAX] - 8388608) % 2048"
        ));
        assertTrue(!xml.contains("[SECOND]"));
    }

    private static void assertMotionBody(
        Document document,
        String bodyName,
        String resourcePrefix,
        String fieldName,
        String band
    ) {
        Element group = groupNamed(document, bodyName + "Motion");
        assertNotNull(group);
        assertEquals("0.5", group.getAttribute("pivotX"));
        assertEquals("0.5", group.getAttribute("pivotY"));
        String angle = firstChild(group, "Transform").getAttribute("value");
        assertTrue(angle.contains("[COMPLICATION." + fieldName + "]"));
        assertTrue(angle.contains("[MINUTE] % 10"));
        assertTrue(angle.contains("floor("));
        assertTrue(angle.contains("% 2048"));
        if (band != null) {
            assertTrue(angle.contains("- " + band));
        }

        Element tail = partImageNamed(document, bodyName + "Tail");
        Element icon = partImageNamed(document, bodyName + "Icon");
        assertNotNull(tail);
        assertNotNull(icon);
        assertEquals(resourcePrefix + "_tail", firstChild(tail, "Image").getAttribute("resource"));
        assertEquals(resourcePrefix + "_icon", firstChild(icon, "Image").getAttribute("resource"));
        assertEquals("0.5", icon.getAttribute("pivotX"));
        assertEquals("0.5", icon.getAttribute("pivotY"));
        assertEquals("-" + angle, firstChild(icon, "Transform").getAttribute("value"));
    }

    private static void assertPolarisReticleMarker(Document document) throws Exception {
        Element group = groupNamed(document, "PolarisReticlePosition");
        assertNotNull(group);
        assertEquals("0.5", group.getAttribute("pivotX"));
        assertEquals("0.5", group.getAttribute("pivotY"));

        String angle = firstChild(group, "Transform").getAttribute("value");
        assertTrue(angle.contains("[UTC_TIMESTAMP]"));
        assertTrue(angle.contains("1767225600000"));
        assertTrue(angle.contains("% 86164091"));
        assertTrue(angle.contains("0.001125874"));

        Element marker = partImageNamed(document, "PolarisReticleMarker");
        assertNotNull(marker);
        assertEquals("215", marker.getAttribute("x"));
        assertEquals("-2", marker.getAttribute("y"));
        assertEquals("20", marker.getAttribute("width"));
        assertEquals("20", marker.getAttribute("height"));
        assertEquals("0.5", marker.getAttribute("pivotX"));
        assertEquals("0.5", marker.getAttribute("pivotY"));
        assertEquals(
            "polaris_reticle_marker",
            firstChild(marker, "Image").getAttribute("resource")
        );
        assertEquals(
            "-1 * (" + angle + ")",
            firstChild(marker, "Transform").getAttribute("value")
        );

        double markerCenterX = Double.parseDouble(marker.getAttribute("x"))
            + Double.parseDouble(marker.getAttribute("width")) / 2.0;
        double markerCenterY = Double.parseDouble(marker.getAttribute("y"))
            + Double.parseDouble(marker.getAttribute("height")) / 2.0;
        double radius = Math.hypot(markerCenterX - 225.0, markerCenterY - 225.0);
        assertEquals(217.0, radius, 0.001);
        assertTrue(radius > 211.0);
        assertEquals(
            227.0,
            radius + Double.parseDouble(marker.getAttribute("height")) / 2.0,
            0.001
        );

        PngAssetAssertions.assertPng(
            mainResourceFile("drawable-nodpi/polaris_reticle_marker.png"),
            20,
            20
        );
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
