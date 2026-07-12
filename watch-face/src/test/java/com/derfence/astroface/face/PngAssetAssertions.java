package com.derfence.astroface.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

final class PngAssetAssertions {
    private PngAssetAssertions() {
    }

    static void assertPng(File file, int expectedWidth, int expectedHeight) throws Exception {
        assertTrue(file.isFile());
        assertTrue(file.length() > 100);

        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            assertEquals(0x89504E470D0A1A0AL, input.readLong());
            input.skipBytes(8);
            assertEquals(expectedWidth, input.readInt());
            assertEquals(expectedHeight, input.readInt());
        }
    }
}
