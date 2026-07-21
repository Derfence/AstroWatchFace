package com.derfence.astroface.wear.complication

import org.junit.Assert.assertEquals
import org.junit.Test

class DialUpdateRequesterTest {
    @Test
    fun allTargetsContainTheEightUniqueDialProviders() {
        assertEquals(
            setOf(
                ConstellationBackgroundDataSourceService::class.java,
                Dial24hDataSourceService::class.java,
                CelestialHorizonDataSourceService::class.java,
                CelestialInnerMotionDataSourceService::class.java,
                CelestialMiddleMotionDataSourceService::class.java,
                CelestialOuterMotionDataSourceService::class.java,
                StatusOverlayDataSourceService::class.java,
                ModeOverlayDataSourceService::class.java
            ),
            DialUpdatePlans.ALL.serviceClasses.toSet()
        )
        assertEquals(8, DialUpdatePlans.ALL.serviceClasses.size)
        assertEquals(true, DialUpdatePlans.ALL.marksManualRefresh)
    }

    @Test
    fun modeOnlyTargetsTheModeOverlayProvider() {
        assertEquals(
            listOf(ModeOverlayDataSourceService::class.java),
            DialUpdatePlans.MODE_ONLY.serviceClasses
        )
        assertEquals(false, DialUpdatePlans.MODE_ONLY.marksManualRefresh)
    }
}
