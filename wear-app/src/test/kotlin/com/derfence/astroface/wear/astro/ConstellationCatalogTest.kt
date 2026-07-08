package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.horizon
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstellationCatalogTest {
    private val catalog = DefaultConstellationCatalog.value

    @Test
    fun catalogIsInternallyConsistent() {
        val starIds = catalog.stars.map { it.id }
        assertEquals(starIds.toSet().size, starIds.size)

        val starsById = catalog.stars.associateBy { it.id }
        catalog.stars.forEach { star ->
            assertTrue(star.raHours >= 0.0)
            assertTrue(star.raHours < 24.0)
            assertTrue(star.decDegrees >= -90.0)
            assertTrue(star.decDegrees <= 90.0)
            assertTrue(!star.raHours.isNaN() && !star.raHours.isInfinite())
            assertTrue(!star.decDegrees.isNaN() && !star.decDegrees.isInfinite())
        }

        catalog.segments.forEach { segment ->
            assertTrue(starsById.containsKey(segment.fromStarId))
            assertTrue(starsById.containsKey(segment.toStarId))
        }
    }

    @Test
    fun catalogConstellationsMatchVisibleSourceConstellations() {
        val expected = expectedVisibleConstellationIds()
        val actual = catalog.segments.map { it.constellationId }.toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun catalogContainsEveryExpectedSourceSegmentAndStar() {
        val sourceStars = sourceStars()
        val expectedConstellations = expectedVisibleConstellationIds()
        val expectedSegments = sourceLines()
            .filter { it.constellationId in expectedConstellations }
            .flatMap { it.toSegments() }
            .toSet()
        val actualSegments = catalog.segments
            .map { SegmentKey(it.constellationId, it.fromStarId, it.toStarId) }
            .toSet()

        assertEquals(expectedSegments, actualSegments)

        val expectedStarIds = expectedSegments.flatMap { listOf(it.fromStarId, it.toStarId) }.toSet()
        val actualStarsById = catalog.stars.associateBy { it.id }
        assertEquals(expectedStarIds, actualStarsById.keys)

        expectedStarIds.forEach { starId ->
            val expectedStar = sourceStars.getValue(starId)
            val actualStar = actualStarsById.getValue(starId)
            assertEquals(expectedStar.raHours, actualStar.raHours, COORDINATE_TOLERANCE)
            assertEquals(expectedStar.decDegrees, actualStar.decDegrees, COORDINATE_TOLERANCE)
        }
    }

    @Test
    fun annualMidnightSweepDisplaysEveryRetainedConstellation() {
        val expected = expectedVisibleConstellationIds()
        val source = AstronomyEngineConstellationSource(
            catalog = catalog,
            refreshPolicy = ConstellationRefreshPolicy(SixAmSunriseProvider)
        )

        val displayed = visibilityDates()
            .flatMap { targetDate ->
                source.constellationsAt(
                    targetDate.minusDays(1)
                        .atTime(LocalTime.of(6, 0))
                        .atZone(AstroObserver.DEFAULT.zoneId)
                        .toInstant(),
                    AstroObserver.DEFAULT
                ).lines
            }
            .map { it.constellationId }
            .toSet()

        assertEquals(expected, displayed)
    }

    private fun expectedVisibleConstellationIds(): Set<String> {
        val visibleStars = visibleStarIds()
        return sourceLines()
            .filter { sourceLine -> sourceLine.starIds.any { it in visibleStars } }
            .map { it.constellationId }
            .toSet()
    }

    private fun visibleStarIds(): Set<String> {
        val dates = visibilityDates()
            .map { it.atStartOfDay(AstroObserver.DEFAULT.zoneId).toInstant() }
        val observer = AstroObserver.DEFAULT.toAstronomyObserver()
        return sourceStars()
            .filterValues { star ->
                dates.any { date ->
                    val horizontal = horizon(
                        date.toAstronomyTime(),
                        observer,
                        star.raHours,
                        star.decDegrees,
                        Refraction.None
                    )
                    90.0 - horizontal.altitude <= ZENITH_RADIUS_DEGREES
                }
            }
            .keys
    }

    private fun visibilityDates(): List<LocalDate> =
        (0 until VISIBILITY_DAYS).map { VISIBILITY_START.plusDays(it.toLong()) }

    private fun sourceLines(): List<SourceLine> =
        resourceLines(SOURCE_LINES_RESOURCE).map { line ->
            val parts = line.split("\t")
            SourceLine(
                constellationId = parts[0],
                starIds = parts[1].split(",")
            )
        }

    private fun sourceStars(): Map<String, SourceStar> =
        resourceLines(HIPPARCOS_STARS_RESOURCE).associate { line ->
            val parts = line.split("\t")
            parts[0] to SourceStar(
                id = parts[0],
                raHours = parts[1].toDouble(),
                decDegrees = parts[2].toDouble()
            )
        }

    private fun resourceLines(name: String): List<String> {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream(name)) {
            "Missing test resource: $name"
        }
        return stream.bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.toList()
        }
    }

    private fun SourceLine.toSegments(): List<SegmentKey> =
        starIds.zipWithNext { fromStarId, toStarId ->
            SegmentKey(constellationId, fromStarId, toStarId)
        }

    private data class SourceLine(
        val constellationId: String,
        val starIds: List<String>
    )

    private data class SourceStar(
        val id: String,
        val raHours: Double,
        val decDegrees: Double
    )

    private data class SegmentKey(
        val constellationId: String,
        val fromStarId: String,
        val toStarId: String
    )

    private object SixAmSunriseProvider : SunriseProvider {
        override fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant =
            date.atTime(LocalTime.of(6, 0))
                .atZone(observer.zoneId)
                .toInstant()
    }

    private companion object {
        private const val SOURCE_LINES_RESOURCE = "astro/constellation-source-lines.tsv"
        private const val HIPPARCOS_STARS_RESOURCE = "astro/hipparcos-stars.tsv"
        private val VISIBILITY_START: LocalDate = LocalDate.of(2024, 1, 1)
        private const val VISIBILITY_DAYS = 366
        private const val ZENITH_RADIUS_DEGREES = 100.0
        private const val COORDINATE_TOLERANCE = 0.00000001
    }
}
