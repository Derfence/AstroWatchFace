package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstronomyEngineSolarSystemPositionSource
import com.derfence.astroface.wear.astro.ConstellationSource
import com.derfence.astroface.wear.astro.SharedAstronomySources
import com.derfence.astroface.wear.astro.SolarSystemBody
import com.derfence.astroface.wear.astro.SolarSystemPositionSource
import com.derfence.astroface.wear.display.DisplayMode
import java.time.Clock
import java.time.Instant

class ModeOverlayRenderer(
    private val mode: DisplayMode,
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val constellationSource: ConstellationSource = SharedAstronomySources.constellationSource,
    private val solarSystemPositionSource: SolarSystemPositionSource =
        AstronomyEngineSolarSystemPositionSource(),
    private val nightConstellationBackgroundRenderer: ConstellationBackgroundRenderer =
        ConstellationBackgroundRenderer(
            lineColor = Color.RED,
            starColor = Color.RED
        )
) : DialRenderer {
    private val nightRenderer = ConstellationLayerRenderer(
        style = ConstellationLayerStyle.NIGHT,
        clock = clock,
        observer = observer,
        constellationSource = constellationSource,
        backgroundRenderer = nightConstellationBackgroundRenderer
    )
    override val contentDescription: String =
        when (mode) {
            DisplayMode.FULL_DIAL -> "Mode AstroFace complet"
            DisplayMode.CONSTELLATIONS_NIGHT -> "Mode constellations AstroFace"
            DisplayMode.SOLAR_SYSTEM -> "Mode système solaire AstroFace"
        }

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderFrameAt(instant: Instant): RenderedDialFrame =
        if (mode == DisplayMode.CONSTELLATIONS_NIGHT) {
            nightRenderer.renderFrameAt(instant)
        } else {
            super.renderFrameAt(instant)
        }

    override fun renderAt(instant: Instant): Bitmap {
        if (mode == DisplayMode.CONSTELLATIONS_NIGHT) {
            return nightRenderer.renderAt(instant)
        }
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )

        if (mode == DisplayMode.FULL_DIAL) {
            return bitmap
        }

        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.BLACK)

        when (mode) {
            DisplayMode.FULL_DIAL -> Unit
            DisplayMode.CONSTELLATIONS_NIGHT -> Unit
            DisplayMode.SOLAR_SYSTEM -> drawSolarSystem(canvas, paint, instant)
        }

        return bitmap
    }

    private fun drawSolarSystem(canvas: Canvas, paint: Paint, instant: Instant) {
        val placements = solarSystemPositionSource.positionsAt(instant).positions.map { position ->
            SolarSystemPlacement(
                body = position.body,
                angleDegrees = position.heliocentricLongitudeDegrees.toFloat(),
                radius = orbitRadiusFor(position.body),
                scale = bodyScale(position.body)
            )
        }
        drawSolarBackgroundStars(canvas, paint)
        drawSolarOrbitTails(canvas, paint, placements)

        placements.firstOrNull { it.body == SolarSystemBody.SUN }?.let {
            CelestialBodyIconPainter.draw(
                canvas = canvas,
                paint = paint,
                body = it.body,
                x = DialGeometry.center,
                y = DialGeometry.center,
                scale = it.scale
            )
        }

        placements
            .filterNot { it.body == SolarSystemBody.SUN }
            .forEach { placement ->
                val point = DialGeometry.point(
                    radius = placement.radius,
                    angleDegrees = placement.angleDegrees
                )
                CelestialBodyIconPainter.draw(canvas, paint, placement.body, point.x, point.y, placement.scale)
            }
    }

    private fun drawSolarBackgroundStars(canvas: Canvas, paint: Paint) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        SOLAR_BACKGROUND_STARS.forEach { star ->
            paint.color = Color.argb(star.alpha, 255, 255, 255)
            canvas.drawCircle(star.x, star.y, star.radius, paint)
        }
    }

    private fun drawSolarOrbitTails(
        canvas: Canvas,
        paint: Paint,
        placements: List<SolarSystemPlacement>
    ) {
        placements
            .filterNot { it.body == SolarSystemBody.SUN }
            .forEach { placement ->
                OrbitTailPainter.draw(
                    canvas = canvas,
                    paint = paint,
                    radius = placement.radius,
                    angleDegrees = placement.angleDegrees,
                    baseColor = OrbitTailPainter.colorFor(placement.body)
                )
            }
    }

    private fun orbitRadiusFor(body: SolarSystemBody): Float =
        FIRST_PLANET_ORBIT_RADIUS + ORBIT_SPACING * planetOrbitIndexFor(body)

    private fun planetOrbitIndexFor(body: SolarSystemBody): Int =
        when (body) {
            SolarSystemBody.SUN -> 0
            SolarSystemBody.MERCURY -> 0
            SolarSystemBody.VENUS -> 1
            SolarSystemBody.EARTH -> 2
            SolarSystemBody.MARS -> 3
            SolarSystemBody.JUPITER -> 4
            SolarSystemBody.SATURN -> 5
            SolarSystemBody.URANUS -> 6
            SolarSystemBody.NEPTUNE -> 7
        }

    private fun bodyScale(body: SolarSystemBody): Float =
        when (body) {
            SolarSystemBody.SUN -> 3.5f
            SolarSystemBody.MERCURY -> 1.0f
            SolarSystemBody.VENUS -> 1.5f
            SolarSystemBody.EARTH -> 1.5f
            SolarSystemBody.MARS -> 1.5f
            SolarSystemBody.JUPITER -> 2.5f
            SolarSystemBody.SATURN -> 2.5f
            SolarSystemBody.URANUS -> 2.0f
            SolarSystemBody.NEPTUNE -> 2.0f
        }

    private data class SolarSystemPlacement(
        val body: SolarSystemBody,
        val angleDegrees: Float,
        val radius: Float,
        val scale: Float,
    )

    private data class BackgroundStar(
        val x: Float,
        val y: Float,
        val radius: Float,
        val alpha: Int
    )

    private companion object {
        private const val FIRST_PLANET_ORBIT_RADIUS = 50f
        private const val ORBIT_SPACING = 19f

        private val SOLAR_BACKGROUND_STARS = listOf(
            BackgroundStar(92f, 96f, 1.1f, 150),
            BackgroundStar(126f, 54f, 0.8f, 118),
            BackgroundStar(192f, 38f, 0.7f, 104),
            BackgroundStar(274f, 42f, 1.0f, 136),
            BackgroundStar(333f, 76f, 0.8f, 112),
            BackgroundStar(377f, 125f, 1.2f, 142),
            BackgroundStar(405f, 205f, 0.7f, 108),
            BackgroundStar(390f, 288f, 1.0f, 126),
            BackgroundStar(339f, 371f, 0.8f, 112),
            BackgroundStar(268f, 413f, 1.1f, 148),
            BackgroundStar(179f, 405f, 0.7f, 104),
            BackgroundStar(106f, 366f, 1.0f, 134),
            BackgroundStar(55f, 287f, 0.8f, 118),
            BackgroundStar(47f, 202f, 1.1f, 146),
            BackgroundStar(61f, 143f, 0.7f, 102),
            BackgroundStar(146f, 132f, 0.6f, 96),
            BackgroundStar(300f, 138f, 0.7f, 108),
            BackgroundStar(322f, 314f, 0.6f, 100),
            BackgroundStar(151f, 304f, 0.8f, 120),
            BackgroundStar(224f, 88f, 0.7f, 106),
            BackgroundStar(92f, 62f, 3.0f, 111),
            BackgroundStar(103f, 51f, 0.0f, 134),
            BackgroundStar(90f, 72f, 0.2f, 121),
            BackgroundStar(68f, 97f, 1.2f, 118),
            BackgroundStar(33f, 118f, 1.5f, 148),
            BackgroundStar(118f, 37f, 1.5f, 113),
            BackgroundStar(122f, 40f, 1.9f, 125),
            BackgroundStar(118f, 52f, 0.4f, 116),
            BackgroundStar(97f, 94f, 1.8f, 135),
            BackgroundStar(133f, 28f, 1.7f, 119),
            BackgroundStar(124f, 58f, 0.8f, 137),
            BackgroundStar(48f, 129f, 0.4f, 145),
            BackgroundStar(125f, 60f, 0.6f, 106),
            BackgroundStar(105f, 92f, 0.2f, 148),
            BackgroundStar(27f, 142f, 1.3f, 147),
            BackgroundStar(91f, 115f, 0.3f, 140),
            BackgroundStar(105f, 102f, 0.4f, 104),
            BackgroundStar(148f, 23f, 1.1f, 105),
            BackgroundStar(48f, 147f, 0.4f, 137),
            BackgroundStar(98f, 119f, 0.2f, 108),
            BackgroundStar(14f, 161f, 1.0f, 132),
            BackgroundStar(24f, 162f, 1.1f, 126),
            BackgroundStar(136f, 97f, 1.8f, 147),
            BackgroundStar(67f, 157f, 0.7f, 124),
            BackgroundStar(93f, 43f, 0.9f, 145),
            BackgroundStar(21f, 170f, 1.9f, 110),
            BackgroundStar(11f, 73f, 0.2f, 149),
            BackgroundStar(42f, 169f, 1.2f, 141),
            BackgroundStar(120f, 31f, 0.9f, 112),
            BackgroundStar(8f, 179f, 0.1f, 121),
            BackgroundStar(25f, 179f, 3.0f, 107),
            BackgroundStar(147f, 106f, 1.6f, 105),
            BackgroundStar(161f, 87f, 0.0f, 137),
            BackgroundStar(20f, 183f, 1.8f, 121),
            BackgroundStar(185f, 34f, 1.5f, 116),
            BackgroundStar(179f, 60f, 0.8f, 129),
            BackgroundStar(150f, 115f, 1.1f, 134),
            BackgroundStar(16f, 191f, 1.5f, 134),
            BackgroundStar(126f, 146f, 1.3f, 117),
            BackgroundStar(180f, 72f, 1.3f, 140),
            BackgroundStar(39f, 193f, 1.9f, 145),
            BackgroundStar(120f, 158f, 1.6f, 121),
            BackgroundStar(193f, 51f, 0.1f, 122),
            BackgroundStar(161f, 119f, 0.2f, 117),
            BackgroundStar(187f, 81f, 0.4f, 149),
            BackgroundStar(206f, 2f, 0.5f, 130),
            BackgroundStar(125f, 163f, 1.4f, 129),
            BackgroundStar(5f, 208f, 1.7f, 112),
            BackgroundStar(0f, 209f, 0.3f, 143),
            BackgroundStar(34f, 206f, 1.9f, 142),
            BackgroundStar(193f, 83f, 0.9f, 129),
            BackgroundStar(213f, 20f, 1.8f, 133),
            BackgroundStar(11f, 15f, 0.1f, 136),
            BackgroundStar(213f, 34f, 0.4f, 107),
            BackgroundStar(17f, 216f, 0.1f, 116),
            BackgroundStar(171f, 139f, 1.3f, 136),
            BackgroundStar(217f, 44f, 1.9f, 140),
            BackgroundStar(170f, 145f, 1.7f, 112),
            BackgroundStar(109f, 196f, 0.0f, 105),
            BackgroundStar(161f, 156f, 0.2f, 109),
            BackgroundStar(7f, 226f, 0.1f, 122),
            BackgroundStar(212f, 81f, 1.2f, 123),
            BackgroundStar(81f, 217f, 1.0f, 111),
            BackgroundStar(128f, 196f, 3.0f, 116),
            BackgroundStar(186f, 144f, 0.1f, 109),
            BackgroundStar(160f, 173f, 1.0f, 124),
            BackgroundStar(135f, 195f, 1.8f, 144),
            BackgroundStar(236f, 25f, 3.0f, 147),
            BackgroundStar(100f, 216f, 0.5f, 137),
            BackgroundStar(226f, 77f, 1.1f, 100),
            BackgroundStar(150f, 187f, 0.6f, 140),
            BackgroundStar(135f, 199f, 1.9f, 106),
            BackgroundStar(241f, 10f, 0.5f, 111),
            BackgroundStar(165f, 176f, 1.5f, 136),
            BackgroundStar(228f, 85f, 1.5f, 129),
            BackgroundStar(187f, 156f, 0.9f, 142),
            BackgroundStar(77f, 232f, 1.3f, 146),
            BackgroundStar(233f, 77f, 1.3f, 125),
            BackgroundStar(64f, 243f, 1.6f, 120),
            BackgroundStar(153f, 200f, 0.9f, 134),
            BackgroundStar(219f, 129f, 1.1f, 110),
            BackgroundStar(219f, 132f, 1.2f, 116),
            BackgroundStar(165f, 97f, 1.2f, 108),
            BackgroundStar(140f, 220f, 0.6f, 142),
            BackgroundStar(241f, 103f, 0.1f, 129),
            BackgroundStar(253f, 70f, 0.3f, 106),
            BackgroundStar(229f, 129f, 1.2f, 119),
            BackgroundStar(213f, 155f, 0.9f, 146),
            BackgroundStar(175f, 97f, 1.8f, 125),
            BackgroundStar(36f, 262f, 3.0f, 135),
            BackgroundStar(6f, 266f, 1.5f, 124),
            BackgroundStar(263f, 68f, 0.7f, 107),
            BackgroundStar(91f, 257f, 0.6f, 100),
            BackgroundStar(44f, 270f, 1.0f, 128),
            BackgroundStar(130f, 41f, 1.3f, 110),
            BackgroundStar(122f, 246f, 1.6f, 105),
            BackgroundStar(210f, 176f, 1.4f, 107),
            BackgroundStar(265f, 71f, 0.4f, 136),
            BackgroundStar(198f, 191f, 0.9f, 113),
            BackgroundStar(277f, 9f, 0.2f, 148),
            BackgroundStar(228f, 158f, 1.7f, 102),
            BackgroundStar(30f, 278f, 1.9f, 130),
            BackgroundStar(136f, 246f, 1.4f, 140),
            BackgroundStar(99f, 265f, 0.9f, 140),
            BackgroundStar(269f, 87f, 0.6f, 113),
            BackgroundStar(219f, 183f, 0.1f, 149),
            BackgroundStar(251f, 136f, 3.0f, 109),
            BackgroundStar(63f, 279f, 1.6f, 111),
            BackgroundStar(197f, 08f, 1.0f, 110),
            BackgroundStar(228f, 74f, 0.4f, 144),
            BackgroundStar(227f, 177f, 0.5f, 122),
            BackgroundStar(68f, 280f, 0.5f, 133),
            BackgroundStar(213f, 198f, 1.8f, 124),
            BackgroundStar(213f, 98f, 1.7f, 103),
            BackgroundStar(11f, 291f, 1.5f, 131),
            BackgroundStar(164f, 242f, 1.9f, 124),
            BackgroundStar(292f, 27f, 1.8f, 130),
            BackgroundStar(220f, 194f, 0.9f, 101),
            BackgroundStar(296f, 9f, 1.8f, 117),
            BackgroundStar(254f, 157f, 0.7f, 123),
            BackgroundStar(161f, 255f, 1.2f, 125),
            BackgroundStar(100f, 285f, 0.0f, 149),
            BackgroundStar(100f, 286f, 1.1f, 124),
            BackgroundStar(277f, 125f, 1.4f, 132),
            BackgroundStar(285f, 112f, 1.0f, 101),
            BackgroundStar(299f, 69f, 1.7f, 145),
            BackgroundStar(303f, 50f, 0.6f, 112),
            BackgroundStar(304f, 45f, 0.4f, 147),
            BackgroundStar(307f, 42f, 1.6f, 102),
            BackgroundStar(138f, 277f, 1.6f, 140),
            BackgroundStar(263f, 170f, 1.2f, 115),
            BackgroundStar(105f, 295f, 0.7f, 135),
            BackgroundStar(98f, 301f, 0.1f, 133),
            BackgroundStar(294f, 120f, 0.0f, 120),
            BackgroundStar(316f, 44f, 0.8f, 138),
            BackgroundStar(309f, 78f, 0.3f, 108),
            BackgroundStar(291f, 136f, 1.5f, 120),
            BackgroundStar(320f, 27f, 0.8f, 110),
            BackgroundStar(39f, 23f, 1.1f, 186),
            BackgroundStar(126f, 301f, 1.3f, 133),
            BackgroundStar(305f, 119f, 1.3f, 108),
            BackgroundStar(135f, 298f, 1.5f, 148),
            BackgroundStar(80f, 18f, 0.1f, 135),
            BackgroundStar(322f, 66f, 0.8f, 124),
            BackgroundStar(308f, 114f, 1.5f, 145),
            BackgroundStar(277f, 176f, 0.4f, 102),
            BackgroundStar(133f, 300f, 1.0f, 118),
            BackgroundStar(45f, 328f, 0.2f, 141),
            BackgroundStar(132f, 304f, 1.3f, 105),
            BackgroundStar(189f, 273f, 1.5f, 119),
            BackgroundStar(297f, 153f, 0.9f, 144),
            BackgroundStar(36f, 333f, 1.9f, 148),
            BackgroundStar(299f, 151f, 1.3f, 100),
            BackgroundStar(160f, 298f, 1.8f, 135),
            BackgroundStar(257f, 21f, 1.4f, 103),
            BackgroundStar(270f, 06f, 0.4f, 108),
            BackgroundStar(111f, 321f, 1.9f, 146),
            BackgroundStar(315f, 133f, 0.3f, 115),
            BackgroundStar(341f, 45f, 0.3f, 115),
            BackgroundStar(122f, 322f, 1.2f, 102),
            BackgroundStar(76f, 339f, 0.8f, 137),
            BackgroundStar(110f, 330f, 1.5f, 139),
            BackgroundStar(207f, 281f, 1.7f, 129),
            BackgroundStar(348f, 38f, 1.9f, 137),
            BackgroundStar(114f, 331f, 0.4f, 150),
            BackgroundStar(46f, 348f, 1.0f, 145),
            BackgroundStar(347f, 60f, 1.7f, 103),
            BackgroundStar(337f, 109f, 1.6f, 144),
            BackgroundStar(349f, 59f, 0.1f, 119),
            BackgroundStar(284f, 211f, 1.6f, 144),
            BackgroundStar(69f, 348f, 1.8f, 126),
            BackgroundStar(103f, 43f, 1.5f, 100),
            BackgroundStar(329f, 142f, 0.5f, 133),
            BackgroundStar(353f, 67f, 0.7f, 118),
            BackgroundStar(295f, 205f, 1.0f, 112),
            BackgroundStar(299f, 202f, 0.1f, 111),
            BackgroundStar(346f, 111f, 1.6f, 126),
            BackgroundStar(232f, 280f, 0.9f, 122),
            BackgroundStar(51f, 360f, 1.4f, 106),
            BackgroundStar(161f, 26f, 3.0f, 114),
            BackgroundStar(298f, 212f, 1.3f, 127),
            BackgroundStar(133f, 346f, 1.9f, 121),
            BackgroundStar(230f, 291f, 1.2f, 111),
            BackgroundStar(120f, 351f, 0.5f, 143),
            BackgroundStar(345f, 138f, 1.5f, 149),
            BackgroundStar(241f, 283f, 0.2f, 111),
            BackgroundStar(358f, 102f, 1.4f, 113),
            BackgroundStar(364f, 85f, 0.5f, 142),
            BackgroundStar(146f, 345f, 0.5f, 148),
            BackgroundStar(279f, 251f, 1.4f, 144),
            BackgroundStar(356f, 120f, 1.7f, 144),
            BackgroundStar(354f, 128f, 1.3f, 149),
            BackgroundStar(242f, 291f, 1.7f, 143),
            BackgroundStar(349f, 152f, 0.1f, 103),
            BackgroundStar(373f, 81f, 1.8f, 112),
            BackgroundStar(358f, 133f, 1.4f, 105),
            BackgroundStar(300f, 241f, 1.5f, 132),
            BackgroundStar(288f, 256f, 1.3f, 132),
            BackgroundStar(335f, 193f, 1.5f, 138),
            BackgroundStar(326f, 211f, 0.3f, 122),
            BackgroundStar(268f, 285f, 0.3f, 144),
            BackgroundStar(112f, 377f, 0.9f, 143),
            BackgroundStar(126f, 373f, 1.7f, 140),
            BackgroundStar(364f, 160f, 1.6f, 111),
            BackgroundStar(199f, 347f, 1.5f, 131),
            BackgroundStar(229f, 333f, 0.1f, 139),
            BackgroundStar(307f, 264f, 0.6f, 126),
            BackgroundStar(258f, 314f, 0.5f, 135),
            BackgroundStar(396f, 98f, 1.3f, 118),
            BackgroundStar(283f, 303f, 1.2f, 103),
            BackgroundStar(301f, 285f, 1.9f, 102),
            BackgroundStar(247f, 334f, 1.5f, 146),
            BackgroundStar(339f, 241f, 0.5f, 118),
            BackgroundStar(387f, 156f, 0.8f, 100),
            BackgroundStar(351f, 234f, 1.1f, 118),
            BackgroundStar(202f, 371f, 0.1f, 125),
            BackgroundStar(401f, 134f, 0.4f, 117),
            BackgroundStar(172f, 387f, 1.1f, 128),
            BackgroundStar(398f, 151f, 1.1f, 141),
            BackgroundStar(288f, 317f, 0.0f, 149),
            BackgroundStar(308f, 299f, 1.7f, 122),
            BackgroundStar(378f, 204f, 0.9f, 125),
            BackgroundStar(275f, 330f, 0.7f, 147),
            BackgroundStar(352f, 247f, 1.0f, 149),
            BackgroundStar(135f, 411f, 3.0f, 134),
            BackgroundStar(224f, 375f, 1.9f, 116),
            BackgroundStar(413f, 48f, 0.6f, 125),
            BackgroundStar(200f, 390f, 0.5f, 100),
            BackgroundStar(395f, 192f, 1.9f, 112),
            BackgroundStar(277f, 342f, 0.1f, 105),
            BackgroundStar(151f, 413f, 1.8f, 129),
            BackgroundStar(152f, 413f, 0.5f, 110),
            BackgroundStar(384f, 220f, 0.4f, 116),
            BackgroundStar(256f, 362f, 0.9f, 106),
            BackgroundStar(160f, 414f, 1.7f, 135),
            BackgroundStar(414f, 162f, 1.7f, 118),
            BackgroundStar(332f, 00f, 1.7f, 140),
            BackgroundStar(146f, 24f, 1.2f, 149),
            BackgroundStar(173f, 414f, 3.0f, 110),
            BackgroundStar(147f, 425f, 0.4f, 145),
            BackgroundStar(427f, 142f, 1.4f, 141),
            BackgroundStar(261f, 368f, 0.0f, 121),
            BackgroundStar(330f, 310f, 0.2f, 103),
            BackgroundStar(411f, 190f, 1.2f, 111),
            BackgroundStar(384f, 241f, 1.7f, 101),
            BackgroundStar(322f, 320f, 1.6f, 144),
            BackgroundStar(161f, 426f, 0.3f, 120),
            BackgroundStar(394f, 228f, 0.9f, 138),
            BackgroundStar(372f, 266f, 0.0f, 148),
            BackgroundStar(380f, 56f, 0.4f, 105),
            BackgroundStar(267f, 74f, 1.1f, 122),
            BackgroundStar(423f, 185f, 3.0f, 123),
            BackgroundStar(172f, 431f, 1.9f, 114),
            BackgroundStar(420f, 200f, 1.2f, 123),
            BackgroundStar(256f, 388f, 0.7f, 141),
            BackgroundStar(392f, 251f, 0.8f, 107),
            BackgroundStar(388f, 261f, 1.6f, 130),
            BackgroundStar(405f, 235f, 0.5f, 126),
            BackgroundStar(357f, 303f, 1.6f, 111),
            BackgroundStar(377f, 285f, 0.9f, 115),
            BackgroundStar(304f, 362f, 0.1f, 101),
            BackgroundStar(376f, 89f, 1.3f, 112),
            BackgroundStar(185f, 438f, 0.5f, 129),
            BackgroundStar(384f, 280f, 0.2f, 133),
            BackgroundStar(224f, 420f, 1.9f, 137),
            BackgroundStar(281f, 386f, 1.8f, 122),
            BackgroundStar(348f, 329f, 0.8f, 111),
            BackgroundStar(189f, 440f, 3.0f, 121),
            BackgroundStar(325f, 356f, 0.2f, 118),
            BackgroundStar(235f, 422f, 0.4f, 101),
            BackgroundStar(321f, 361f, 0.9f, 139),
            BackgroundStar(398f, 278f, 0.1f, 141),
            BackgroundStar(390f, 296f, 0.3f, 105),
            BackgroundStar(309f, 390f, 1.1f, 122),
            BackgroundStar(333f, 370f, 0.1f, 128),
            BackgroundStar(290f, 406f, 0.0f, 143),
            BackgroundStar(384f, 322f, 0.3f, 146),
            BackgroundStar(381f, 328f, 0.0f, 139),
            BackgroundStar(239f, 442f, 1.7f, 119),
            BackgroundStar(230f, 447f, 1.9f, 119),
            BackgroundStar(295f, 408f, 1.2f, 138),
            BackgroundStar(376f, 334f, 1.7f, 138),
            BackgroundStar(290f, 414f, 0.5f, 122),
            BackgroundStar(361f, 355f, 1.9f, 115),
            BackgroundStar(291f, 415f, 1.9f, 117),
            BackgroundStar(278f, 424f, 1.3f, 101),
            BackgroundStar(429f, 272f, 1.8f, 113),
            BackgroundStar(397f, 322f, 1.8f, 104),
            BackgroundStar(307f, 411f, 0.9f, 120),
            BackgroundStar(308f, 411f, 0.2f, 107),
            BackgroundStar(444f, 260f, 3.0f, 135),
            BackgroundStar(441f, 267f, 1.3f, 119),
            BackgroundStar(308f, 413f, 0.1f, 122),
            BackgroundStar(355f, 378f, 0.9f, 124),
            BackgroundStar(433f, 288f, 1.1f, 107),
            BackgroundStar(289f, 438f, 0.2f, 147),
            BackgroundStar(436f, 297f, 1.5f, 121),
            BackgroundStar(324f, 423f, 0.1f, 119),
            BackgroundStar(396f, 358f, 1.8f, 139),
            BackgroundStar(391f, 371f, 0.4f, 130),
            BackgroundStar(375f, 388f, 0.7f, 129)
        )
    }
}
