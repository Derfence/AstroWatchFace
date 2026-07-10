package com.derfence.astroface.wear.dial

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.SolarSystemBody

object CelestialBodyIconPainter {
    fun draw(
        canvas: Canvas,
        paint: Paint,
        body: CelestialBody,
        x: Float,
        y: Float,
        scale: Float = 1f
    ) {
        canvas.save()
        canvas.scale(scale, scale, x, y)
        drawBacking(canvas, paint, x, y)
        when (body) {
            CelestialBody.SUN -> drawSun(canvas, paint, x, y)
            CelestialBody.MOON -> drawMoon(canvas, paint, x, y)
            CelestialBody.MERCURY -> drawMercury(canvas, paint, x, y)
            CelestialBody.VENUS -> drawVenus(canvas, paint, x, y)
            CelestialBody.MARS -> drawMars(canvas, paint, x, y)
            CelestialBody.JUPITER -> drawJupiter(canvas, paint, x, y)
            CelestialBody.SATURN -> drawSaturn(canvas, paint, x, y)
            CelestialBody.URANUS -> drawUranus(canvas, paint, x, y)
            CelestialBody.NEPTUNE -> drawNeptune(canvas, paint, x, y)
        }
        canvas.restore()
    }

    fun draw(
        canvas: Canvas,
        paint: Paint,
        body: SolarSystemBody,
        x: Float,
        y: Float,
        scale: Float = 1f
    ) {
        canvas.save()
        canvas.scale(scale, scale, x, y)
        drawBacking(canvas, paint, x, y)
        when (body) {
            SolarSystemBody.SUN -> drawSun(canvas, paint, x, y)
            SolarSystemBody.MERCURY -> drawMercury(canvas, paint, x, y)
            SolarSystemBody.VENUS -> drawVenus(canvas, paint, x, y)
            SolarSystemBody.EARTH -> drawEarth(canvas, paint, x, y)
            SolarSystemBody.MARS -> drawMars(canvas, paint, x, y)
            SolarSystemBody.JUPITER -> drawJupiter(canvas, paint, x, y)
            SolarSystemBody.SATURN -> drawSaturn(canvas, paint, x, y)
            SolarSystemBody.URANUS -> drawUranus(canvas, paint, x, y)
            SolarSystemBody.NEPTUNE -> drawNeptune(canvas, paint, x, y)
        }
        canvas.restore()
    }

    private fun drawBacking(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(215, 0, 0, 0)
        canvas.drawCircle(x, y, 8.8f, paint)
    }

    private fun drawSun(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(255, 204, 82)
        repeat(8) { index ->
            val angle = index * 45f
            val inner = DialGeometry.pointAround(x, y, 5.4f, angle)
            val outer = DialGeometry.pointAround(x, y, 7.4f, angle)
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 224, 112)
        canvas.drawCircle(x, y, 4.8f, paint)
    }

    private fun drawMoon(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(235, 238, 242)
        canvas.drawCircle(x, y, 5.4f, paint)
        paint.color = Color.rgb(0, 0, 0)
        canvas.drawCircle(x + 2.5f, y - 0.4f, 5.1f, paint)
    }

    private fun drawMercury(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(166, 145, 119)
        canvas.drawCircle(x, y, 4.9f, paint)

        paint.color = Color.rgb(108, 96, 84)
        canvas.drawCircle(x - 1.6f, y - 1.0f, 0.9f, paint)
        canvas.drawCircle(x + 1.5f, y + 1.1f, 0.7f, paint)
    }

    private fun drawVenus(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 188, 48)
        canvas.drawCircle(x, y, 5.7f, paint)

        paint.color = Color.rgb(0, 0, 0)
        canvas.drawCircle(x + 2.6f, y - 0.2f, 5.5f, paint)

        paint.color = Color.rgb(255, 228, 104)
        canvas.drawCircle(x - 1.8f, y - 1.8f, 1.1f, paint)
    }

    private fun drawEarth(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(58, 139, 220)
        canvas.drawCircle(x, y, 5.6f, paint)

        paint.color = Color.rgb(88, 196, 118)
        canvas.drawOval(RectF(x - 4.0f, y - 2.8f, x + 0.6f, y + 1.0f), paint)
        canvas.drawOval(RectF(x + 1.0f, y + 0.9f, x + 4.3f, y + 3.5f), paint)

        paint.color = Color.rgb(230, 244, 250)
        canvas.drawOval(RectF(x - 1.8f, y - 5.0f, x + 1.8f, y - 3.4f), paint)
    }

    private fun drawMars(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(214, 68, 48)
        canvas.drawCircle(x, y, 5.2f, paint)

        paint.color = Color.rgb(122, 43, 34)
        canvas.drawCircle(x - 1.6f, y + 1.1f, 1.2f, paint)

        paint.color = Color.rgb(248, 230, 208)
        canvas.drawOval(RectF(x - 2.2f, y - 5.1f, x + 2.2f, y - 2.8f), paint)
    }

    private fun drawJupiter(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(229, 169, 98)
        canvas.drawCircle(x, y, 5.8f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = Color.rgb(255, 222, 166)
        canvas.drawLine(x - 4.8f, y - 1.6f, x + 4.8f, y - 1.6f, paint)
        canvas.drawLine(x - 4.2f, y + 1.8f, x + 4.2f, y + 1.8f, paint)
    }

    private fun drawSaturn(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.color = Color.rgb(238, 215, 142)
        canvas.save()
        canvas.rotate(-18f, x, y)
        canvas.drawOval(RectF(x - 8.5f, y - 3.2f, x + 8.5f, y + 3.2f), paint)
        canvas.restore()

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(227, 199, 112)
        canvas.drawCircle(x, y, 4.7f, paint)
    }

    private fun drawUranus(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(104, 211, 210)
        canvas.drawCircle(x, y, 5.1f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f
        paint.color = Color.rgb(179, 244, 241)
        canvas.drawOval(RectF(x - 3.4f, y - 6.2f, x + 3.4f, y + 6.2f), paint)
    }

    private fun drawNeptune(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(69, 113, 225)
        canvas.drawCircle(x, y, 5.2f, paint)

        paint.color = Color.rgb(126, 166, 255)
        canvas.drawCircle(x - 1.8f, y - 1.8f, 1.3f, paint)
    }

    private data class RelativePoint(val x: Float, val y: Float)

    private fun DialGeometry.pointAround(
        centerX: Float,
        centerY: Float,
        radius: Float,
        angleDegrees: Float
    ): RelativePoint {
        val point = point(radius, angleDegrees)
        return RelativePoint(
            x = centerX + point.x - center,
            y = centerY + point.y - center
        )
    }
}
