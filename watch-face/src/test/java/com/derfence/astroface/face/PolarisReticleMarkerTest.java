package com.derfence.astroface.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;
import org.junit.Test;

public class PolarisReticleMarkerTest {
    private static final int MARKER_SIZE = 20;

    @Test
    public void markerIsASymmetricRedFourPointStarWithBlackOutline() throws Exception {
        File file = resourceFile("drawable-nodpi/polaris_reticle_marker.png");
        PngAssetAssertions.assertPng(file, MARKER_SIZE, MARKER_SIZE);

        byte[] pixels = readUnfilteredRgba(file, MARKER_SIZE, MARKER_SIZE);
        assertNotNull(pixels);
        boolean hasOpaqueRed = false;
        boolean hasOpaqueBlack = false;
        boolean hasTransparentPixel = false;

        for (int y = 0; y < MARKER_SIZE; y += 1) {
            for (int x = 0; x < MARKER_SIZE; x += 1) {
                int offset = (y * MARKER_SIZE + x) * 4;
                int red = pixels[offset] & 0xff;
                int green = pixels[offset + 1] & 0xff;
                int blue = pixels[offset + 2] & 0xff;
                int alpha = pixels[offset + 3] & 0xff;
                hasOpaqueRed |= alpha == 255 && red == 255 && green == 0 && blue == 0;
                hasOpaqueBlack |= alpha == 255 && red == 0 && green == 0 && blue == 0;
                hasTransparentPixel |= alpha == 0;

                assertPixelEquals(pixels, x, y, MARKER_SIZE - 1 - x, y);
                assertPixelEquals(pixels, x, y, x, MARKER_SIZE - 1 - y);
            }
        }

        assertTrue(hasOpaqueRed);
        assertTrue(hasOpaqueBlack);
        assertTrue(hasTransparentPixel);
    }

    private static byte[] readUnfilteredRgba(File file, int width, int height) throws Exception {
        byte[] png = Files.readAllBytes(file.toPath());
        ByteBuffer input = ByteBuffer.wrap(png);
        input.position(8);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        while (input.remaining() >= 12) {
            int length = input.getInt();
            byte[] typeBytes = new byte[4];
            input.get(typeBytes);
            byte[] payload = new byte[length];
            input.get(payload);
            input.getInt();
            if ("IDAT".equals(new String(typeBytes, StandardCharsets.US_ASCII))) {
                compressed.write(payload);
            }
        }

        byte[] scanlines;
        try (InflaterInputStream inflater = new InflaterInputStream(
            new ByteArrayInputStream(compressed.toByteArray())
        )) {
            scanlines = inflater.readAllBytes();
        }
        assertEquals(height * (1 + width * 4), scanlines.length);

        byte[] pixels = new byte[width * height * 4];
        for (int y = 0; y < height; y += 1) {
            int rowOffset = y * (1 + width * 4);
            assertEquals(0, scanlines[rowOffset]);
            System.arraycopy(scanlines, rowOffset + 1, pixels, y * width * 4, width * 4);
        }
        return pixels;
    }

    private static void assertPixelEquals(
        byte[] pixels,
        int firstX,
        int firstY,
        int secondX,
        int secondY
    ) {
        int firstOffset = (firstY * MARKER_SIZE + firstX) * 4;
        int secondOffset = (secondY * MARKER_SIZE + secondX) * 4;
        for (int channel = 0; channel < 4; channel += 1) {
            assertEquals(pixels[firstOffset + channel], pixels[secondOffset + channel]);
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
