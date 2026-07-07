package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Clock

class Hour24hHandRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId)
) : DialRenderer {
    override val contentDescription = "Aiguille des heures 24 heures AstroFace"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val time = clock.instant()
            .atZone(AstroObserver.DEFAULT.zoneId)
            .toLocalTime()
            .withSecond(0)
            .withNano(0)
        val angle = DialGeometry.angleForTime(time)

        canvas.save()
        canvas.rotate(angle, DialGeometry.center, DialGeometry.center)
        drawHand(canvas, paint)
        canvas.restore()

        return bitmap
    }

    private fun drawHand(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL

        paint.color = Color.WHITE
        canvas.drawPath(mainHandPath(), paint)

        paint.color = Color.rgb(229, 57, 53)
        canvas.drawPath(accentPath(), paint)
    }

    private fun mainHandPath(): Path = Path().apply {
        moveTo(224f, 104f)
        lineTo(226f, 104f)
        lineTo(229f, 214f)
        lineTo(225f, 226f)
        lineTo(221f, 214f)
        close()
    }

    private fun accentPath(): Path = Path().apply {
        moveTo(224.4f, 118f)
        lineTo(225.6f, 118f)
        lineTo(226.5f, 206f)
        lineTo(223.5f, 206f)
        close()
    }
}
