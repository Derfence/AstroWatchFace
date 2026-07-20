package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.ConstellationSource
import com.derfence.astroface.wear.astro.SharedAstronomySources
import com.derfence.astroface.wear.astro.SynchronizedLruCache
import java.time.Clock
import java.time.Instant

enum class ConstellationLayerStyle {
    MAIN,
    NIGHT
}

class ConstellationLayerRenderer(
    private val style: ConstellationLayerStyle,
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val constellationSource: ConstellationSource = SharedAstronomySources.constellationSource,
    private val backgroundRenderer: ConstellationBackgroundRenderer = rendererFor(style)
) : DialRenderer {
    override val contentDescription: String = when (style) {
        ConstellationLayerStyle.MAIN -> "Fond de constellations AstroFace"
        ConstellationLayerStyle.NIGHT -> "Mode constellations AstroFace"
    }

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderAt(instant: Instant): Bitmap = renderFrameAt(instant).bitmap

    override fun renderFrameAt(instant: Instant): RenderedDialFrame {
        val snapshot = constellationSource.constellationsAt(instant, observer)
        val key = CacheKey(style, observer, constellationSource, snapshot.refreshInstant)
        val bitmap = bitmapCache.getOrPut(key) {
            Bitmap.createBitmap(
                DialGeometry.canvasSize,
                DialGeometry.canvasSize,
                Bitmap.Config.ARGB_8888
            ).also { target ->
                val canvas = Canvas(target)
                if (style == ConstellationLayerStyle.NIGHT) {
                    canvas.drawColor(Color.BLACK)
                }
                backgroundRenderer.render(
                    canvas,
                    Paint(Paint.ANTI_ALIAS_FLAG),
                    snapshot.lines
                )
            }
        }
        return RenderedDialFrame(
            bitmap = bitmap,
            contentKey = key,
            validUntil = snapshot.nextRefreshInstant
        )
    }

    private data class CacheKey(
        val style: ConstellationLayerStyle,
        val observer: AstroObserver,
        val source: ConstellationSource,
        val refreshInstant: Instant
    )

    companion object {
        private val bitmapCache = SynchronizedLruCache<CacheKey, Bitmap>(4)

        private fun rendererFor(style: ConstellationLayerStyle): ConstellationBackgroundRenderer =
            when (style) {
                ConstellationLayerStyle.MAIN -> ConstellationBackgroundRenderer()
                ConstellationLayerStyle.NIGHT -> ConstellationBackgroundRenderer(
                    lineColor = Color.RED,
                    starColor = Color.RED
                )
            }
    }
}
