package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.CelestialPositionSnapshot
import com.derfence.astroface.wear.astro.CelestialPositionSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class CelestialMotionTimelineEntry(
    val start: Instant,
    val end: Instant,
    val fields: CelestialMotionFields
)

class CelestialMotionTimelinePlanner(
    private val positionSource: CelestialPositionSource,
    private val observer: AstroObserver,
    private val zoneId: ZoneId,
    private val cadence: Duration = CADENCE,
    private val horizon: Duration = HORIZON
) {
    init {
        require(!cadence.isZero && !cadence.isNegative) { "Cadence must be positive." }
        require(!horizon.isZero && !horizon.isNegative) { "Horizon must be positive." }
        require(cadence.toMinutes() > 0 && 60 % cadence.toMinutes() == 0L) {
            "Cadence must divide a local hour exactly."
        }
    }

    fun plan(
        requestInstant: Instant,
        group: CelestialMotionGroup
    ): List<CelestialMotionTimelineEntry> {
        val timelineEnd = requestInstant.plus(horizon)
        val snapshots = mutableMapOf<Instant, CelestialPositionSnapshot>()
        val entries = mutableListOf<CelestialMotionTimelineEntry>()
        var boundaryStart = alignedBoundaryAtOrBefore(requestInstant)

        while (boundaryStart.isBefore(timelineEnd)) {
            val boundaryEnd = boundaryStart.plus(cadence)
            val entryStart = maxOf(requestInstant, boundaryStart)
            val entryEnd = minOf(timelineEnd, boundaryEnd)
            if (entryStart.isBefore(entryEnd)) {
                val startPositions = snapshots.getOrPut(boundaryStart) {
                    positionSource.positionsAt(boundaryStart, observer)
                }.positions.associateBy { it.body }
                val endPositions = snapshots.getOrPut(boundaryEnd) {
                    positionSource.positionsAt(boundaryEnd, observer)
                }.positions.associateBy { it.body }
                val samples = group.bodies.map { body ->
                    CelestialMotionCodec.sample(
                        body = body,
                        startAzimuthDegrees = requireNotNull(startPositions[body]) {
                            "Missing start position for $body."
                        }.azimuthDegrees,
                        endAzimuthDegrees = requireNotNull(endPositions[body]) {
                            "Missing end position for $body."
                        }.azimuthDegrees
                    )
                }
                entries += CelestialMotionTimelineEntry(
                    start = entryStart,
                    end = entryEnd,
                    fields = CelestialMotionCodec.fieldsFor(samples)
                )
            }
            boundaryStart = boundaryEnd
        }

        return entries
    }

    internal fun alignedBoundaryAtOrBefore(instant: Instant): Instant {
        val local = instant.atZone(zoneId)
        val cadenceMinutes = cadence.toMinutes().toInt()
        return local
            .withMinute(local.minute / cadenceMinutes * cadenceMinutes)
            .withSecond(0)
            .withNano(0)
            .toInstant()
    }

    companion object {
        val CADENCE: Duration = Duration.ofMinutes(10)
        val HORIZON: Duration = Duration.ofHours(10)
    }
}
