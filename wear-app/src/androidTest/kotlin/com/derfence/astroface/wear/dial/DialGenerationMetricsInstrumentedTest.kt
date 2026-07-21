package com.derfence.astroface.wear.dial

import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.derfence.astroface.wear.complication.CelestialMotionComplicationDataFactory
import com.derfence.astroface.wear.complication.CelestialMotionGroup
import com.derfence.astroface.wear.complication.Dial24hComplicationDataFactory
import java.time.Instant
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialGenerationMetricsInstrumentedTest {
    @Test
    fun reportsOptimizedTimelineGenerationMetrics() {
        val start = Instant.parse("2026-07-07T10:00:00Z")
        val startedAt = SystemClock.elapsedRealtimeNanos()

        val dialTimeline = Dial24hComplicationDataFactory.createTimeline(start)
        val motionEntries = CelestialMotionGroup.entries.sumOf { group ->
            CelestialMotionComplicationDataFactory.createTimeline(group, start)
                .timelineEntries.size
        }
        val elapsedNanos = SystemClock.elapsedRealtimeNanos() - startedAt

        Log.i(
            TAG,
            JSONObject()
                .put("name", "optimized_full_mode_motion_10h")
                .put("dial24hCoverageHours", 10)
                .put("dial24hTimelineEntries", dialTimeline.timelineEntries.size)
                .put("dial24hBitmapBytes", 0)
                .put("celestialCoverageHours", 10)
                .put("celestialTimelineEntries", motionEntries)
                .put("celestialBitmapBytes", 0)
                .put("elapsedNanos", elapsedNanos)
                .toString()
        )

        assertTrue(elapsedNanos > 0L)
        assertTrue(dialTimeline.timelineEntries.isNotEmpty())
        assertEquals(180, motionEntries)
    }

    private companion object {
        const val TAG = "AstroFaceGeneration"
    }
}
