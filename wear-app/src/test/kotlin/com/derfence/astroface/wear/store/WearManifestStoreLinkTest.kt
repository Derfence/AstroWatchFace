package com.derfence.astroface.wear.store

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
