package com.derfence.astroface.wear.dial

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class DialPoint(val x: Float, val y: Float)

data class DialArcSegment(
    val startAngleDegrees: Float,
    val sweepDegrees: Float
)

data class DialTick(
    val index: Int,
    val angleDegrees: Float,
    val isMajor: Boolean,
    val label: String?
)

object DialGeometry {
    const val canvasSize = 450
    const val center = 225f

    fun twentyFourHourTicks(): List<DialTick> =
        ticks(total = 24, labelEvery = 3) { it.toString().padStart(2, '0') }

    fun twelveHourTicks(): List<DialTick> =
        ticks(total = 12, labelEvery = 0) { "" }

    fun point(radius: Float, angleDegrees: Float): DialPoint {
        val radians = angleDegrees * PI / 180.0
        return DialPoint(
            x = center + (sin(radians) * radius).toFloat(),
            y = center - (cos(radians) * radius).toFloat()
        )
    }

    fun angleForTime(time: LocalTime): Float {
        val secondsOfDay = time.toSecondOfDay() + time.nano / 1_000_000_000.0
        return (secondsOfDay * 360.0 / SECONDS_PER_DAY).toFloat()
    }

    fun angleForInstant(instant: Instant, zoneId: ZoneId): Float =
        angleForTime(instant.atZone(zoneId).toLocalTime())

    fun arcSegmentsFor(
        start: Instant,
        end: Instant,
        zoneId: ZoneId
    ): List<DialArcSegment> {
        if (!end.isAfter(start)) {
            return emptyList()
        }

        val segments = mutableListOf<DialArcSegment>()
        var cursor = start.atZone(zoneId)

        while (cursor.toInstant().isBefore(end)) {
            val nextMidnight = cursor.toLocalDate()
                .plusDays(1)
                .atStartOfDay(zoneId)
            val segmentEnd = minOfInstant(end, nextMidnight.toInstant()).atZone(zoneId)
            addSegment(cursor, segmentEnd, segments)
            cursor = segmentEnd
        }

        return segments
    }

    private fun ticks(
        total: Int,
        labelEvery: Int,
        labelFor: (Int) -> String
    ): List<DialTick> =
        (0 until total).map { index ->
            val hasLabel = labelEvery > 0 && index % labelEvery == 0
            DialTick(
                index = index,
                angleDegrees = index * 360f / total,
                isMajor = hasLabel || index % (total / 4) == 0,
                label = if (hasLabel) labelFor(index) else null
            )
        }

    private fun addSegment(
        start: ZonedDateTime,
        end: ZonedDateTime,
        segments: MutableList<DialArcSegment>
    ) {
        val startAngle = angleForTime(start.toLocalTime())
        val endAngle = angleForTime(end.toLocalTime())
        val sweep = when {
            end.toLocalTime() == LocalTime.MIDNIGHT && end.toLocalDate().isAfter(start.toLocalDate()) ->
                FULL_CIRCLE_DEGREES - startAngle
            endAngle >= startAngle -> endAngle - startAngle
            else -> FULL_CIRCLE_DEGREES - startAngle + endAngle
        }

        if (sweep > 0.001f) {
            segments += DialArcSegment(startAngle, sweep)
        }
    }

    private fun minOfInstant(first: Instant, second: Instant): Instant =
        if (first <= second) first else second

    private const val SECONDS_PER_DAY = 24 * 60 * 60
    private const val FULL_CIRCLE_DEGREES = 360f
}
