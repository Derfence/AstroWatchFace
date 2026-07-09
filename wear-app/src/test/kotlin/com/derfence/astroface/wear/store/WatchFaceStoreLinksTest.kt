package com.derfence.astroface.wear.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchFaceStoreLinksTest {
    @Test
    fun marketUriTargetsWatchFacePackage() {
        assertEquals(
            "market://details?id=com.derfence.astroface.face",
            watchFaceMarketUri()
        )
    }

    @Test
    fun webUriTargetsWatchFacePackage() {
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.derfence.astroface.face",
            watchFaceWebUri()
        )
    }

    @Test
    fun availabilityChecksTheWatchFacePackage() {
        var checkedPackage: String? = null
        val availability = WatchFaceAvailability(
            InstalledPackageLookup { packageName ->
                checkedPackage = packageName
                true
            }
        )

        assertTrue(availability.isWatchFaceInstalled())
        assertEquals(WATCH_FACE_PACKAGE, checkedPackage)
    }

    @Test
    fun availabilityReportsAbsentPackage() {
        val availability = WatchFaceAvailability(InstalledPackageLookup { false })

        assertFalse(availability.isWatchFaceInstalled())
    }
}
