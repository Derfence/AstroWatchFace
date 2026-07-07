package com.derfence.astroface.wear.dial

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class DialPoint(val x: Float, val y: Float)

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
}
