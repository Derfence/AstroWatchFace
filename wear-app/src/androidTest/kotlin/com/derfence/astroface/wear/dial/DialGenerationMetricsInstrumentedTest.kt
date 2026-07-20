package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialGenerationMetricsInstrumentedTest {
    @Test
    fun reportsOptimizedTimelineGenerationMetrics() {
        val start = Instant.parse("2026-07-07T10:00:00Z")
        val instants = (0 until 12).map { start.plusSeconds(it * 600L) }
        val bitmaps = mutableListOf<Bitmap>()
        val startedAt = SystemClock.elapsedRealtimeNanos()

        instants.forEach { instant ->
            bitmaps += Dial24hRenderer().renderAt(instant)
            bitmaps += CelestialOverlayRenderer().renderAt(instant)
        }
        val elapsedNanos = SystemClock.elapsedRealtimeNanos() - startedAt
        val rawBytes = bitmaps.sumOf { it.allocationByteCount.toLong() }

        Log.i(
            TAG,
            JSONObject()
                .put("name", "optimized_full_mode_2h")
                .put("frames", bitmaps.size)
                .put("elapsedNanos", elapsedNanos)
                .put("rawBitmapBytes", rawBytes)
                .toString()
        )

        assertTrue(elapsedNanos > 0L)
        assertTrue(rawBytes > 0L)
        bitmaps.forEach(Bitmap::recycle)
    }

    private companion object {
        const val TAG = "AstroFaceGeneration"
    }
}
