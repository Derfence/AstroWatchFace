package com.derfence.astroface.face;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class CelestialAssetsTest {
    private static final String[] BODIES = {
        "sun", "moon", "mercury", "venus", "mars",
        "jupiter", "saturn", "uranus", "neptune"
    };

    private static final int[][] TAIL_SIZES = {
        {117, 133}, {122, 138}, {127, 144}, {132, 150}, {137, 156},
        {142, 162}, {147, 168}, {152, 174}, {157, 179}
    };

    @Test
    public void celestialAssetsContainSeparateCroppedIconsAndTails() throws Exception {
        String layout = new String(
            Files.readAllBytes(resourceFile("raw/celestial_asset_layout.json").toPath()),
            StandardCharsets.UTF_8
        );

        assertTrue(layout.contains("\"width\": 340"));
        assertTrue(layout.contains("\"iconBackground\": \"transparent\""));
        assertTrue(layout.contains("\"opaqueShadowBodies\": ["));
        assertTrue(layout.contains("\"moon\""));
        assertTrue(layout.contains("\"venus\""));
        assertTrue(layout.contains("icons remain screen-upright"));

        for (int index = 0; index < BODIES.length; index += 1) {
            String body = BODIES[index];
            PngAssetAssertions.assertPng(
                resourceFile("drawable-nodpi/celestial_" + body + "_icon.png"),
                22,
                22
            );
            PngAssetAssertions.assertPng(
                resourceFile("drawable-nodpi/celestial_" + body + "_tail.png"),
                TAIL_SIZES[index][0],
                TAIL_SIZES[index][1]
            );
            assertTrue(layout.contains("\"resource\": \"celestial_" + body + "_icon\""));
            assertTrue(layout.contains("\"resource\": \"celestial_" + body + "_tail\""));
        }
    }

    private static File resourceFile(String relativePath) {
        File file = new File("src/main/res/" + relativePath);
        if (!file.exists()) {
            file = new File("watch-face/src/main/res/" + relativePath);
        }
        return file;
    }
}
