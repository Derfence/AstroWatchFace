package com.derfence.astroface.wear.store

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

class WearManifestStoreLinkTest {
    @Test
    fun manifestCanQueryWatchFacePackage() {
        val manifest = loadManifest()
        val queries = manifest.getElementsByTagName("queries").item(0) as Element
        val packageNode = queries.getElementsByTagName("package").item(0) as Element

        assertEquals(WATCH_FACE_PACKAGE, packageNode.getAttribute("android:name"))
    }

    @Test
    fun manifestDeclaresStandaloneWearApp() {
        val manifest = loadManifest()
        val metaData = manifest.getElementsByTagName("meta-data")
        var standaloneNode: Element? = null

        for (index in 0 until metaData.length) {
            val element = metaData.item(index) as Element
            if (element.getAttribute("android:name") == "com.google.android.wearable.standalone") {
                standaloneNode = element
                break
            }
        }

        assertNotNull(standaloneNode)
        assertEquals("true", standaloneNode?.getAttribute("android:value"))
    }

    @Test
    fun manifestDeclaresThreeRangedCelestialMotionProviders() {
        val manifest = loadManifest()
        val services = manifest.getElementsByTagName("service")
        val expected = setOf(
            ".complication.CelestialInnerMotionDataSourceService",
            ".complication.CelestialMiddleMotionDataSourceService",
            ".complication.CelestialOuterMotionDataSourceService"
        )
        val found = mutableSetOf<String>()

        for (index in 0 until services.length) {
            val service = services.item(index) as Element
            val name = service.getAttribute("android:name")
            assertFalse(name.endsWith("CelestialOverlayDataSourceService"))
            if (name in expected) {
                found += name
                val metadata = service.getElementsByTagName("meta-data")
                var supportedTypes: String? = null
                var updatePeriod: String? = null
                for (metadataIndex in 0 until metadata.length) {
                    val element = metadata.item(metadataIndex) as Element
                    when (element.getAttribute("android:name")) {
                        "android.support.wearable.complications.SUPPORTED_TYPES" ->
                            supportedTypes = element.getAttribute("android:value")
                        "android.support.wearable.complications.UPDATE_PERIOD_SECONDS" ->
                            updatePeriod = element.getAttribute("android:value")
                    }
                }
                assertEquals("RANGED_VALUE", supportedTypes)
                assertEquals("7200", updatePeriod)
            }
        }

        assertEquals(expected, found)
        assertTrue(found.size == 3)
    }

    private fun loadManifest(): Document {
        var file = File("src/main/AndroidManifest.xml")
        if (!file.exists()) {
            file = File("wear-app/src/main/AndroidManifest.xml")
        }
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        return factory.newDocumentBuilder().parse(file)
    }
}
