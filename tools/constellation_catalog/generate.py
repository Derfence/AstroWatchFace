#!/usr/bin/env python3
"""Generate the AstroFace constellation catalog from locked source data."""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import unicodedata
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path
from zoneinfo import ZoneInfo


PROJECT_ROOT = Path(__file__).resolve().parents[2]
SOURCE_DIR = PROJECT_ROOT / "tools" / "constellation_catalog"
STELLARIUM_INDEX = SOURCE_DIR / "stellarium-modern-index.json"
HIPPARCOS_STARS = SOURCE_DIR / "hipparcos-stars.tsv"
KOTLIN_OUTPUT = (
    PROJECT_ROOT
    / "wear-app"
    / "src"
    / "main"
    / "kotlin"
    / "com"
    / "derfence"
    / "astroface"
    / "wear"
    / "astro"
    / "ConstellationCatalog.kt"
)
TEST_RESOURCE_DIR = PROJECT_ROOT / "wear-app" / "src" / "test" / "resources" / "astro"
TEST_LINES_OUTPUT = TEST_RESOURCE_DIR / "constellation-source-lines.tsv"
TEST_STARS_OUTPUT = TEST_RESOURCE_DIR / "hipparcos-stars.tsv"

STELLARIUM_COMMIT = "be3d960963f450ab0651e60f95cc3273a4f01638"
HIPPARCOS_SOURCE_SHA256 = (
    "58ceabb104d647160d9437ce6e513a02a036bb4ad9f8879a5a22fd52943616e0"
)
OBSERVER_LATITUDE = 45.665694
OBSERVER_LONGITUDE = 2.944194
OBSERVER_ZONE = "Europe/Paris"
VISIBILITY_YEAR = 2024
VISIBILITY_DAYS = 366


@dataclass(frozen=True)
class SourceConstellation:
    id: str
    lines: list[list[str]]


@dataclass(frozen=True)
class SourceStar:
    id: str
    hip: int
    ra_hours: float
    dec_degrees: float


@dataclass(frozen=True)
class Segment:
    constellation_id: str
    from_star_id: str
    to_star_id: str


def main() -> None:
    args = parse_args()
    source_constellations = read_stellarium_constellations(args.stellarium_index)
    all_hips = sorted(
        {
            int(star_id.removeprefix("hip_"))
            for constellation in source_constellations
            for line in constellation.lines
            for star_id in line
        }
    )

    if args.hip_main:
        write_hipparcos_subset(args.hip_main, all_hips, args.hipparcos_stars)

    source_stars = read_hipparcos_stars(args.hipparcos_stars)
    ensure_complete_source(source_constellations, source_stars)

    visible_star_ids = visible_stars(source_stars)
    retained_constellations = [
        constellation
        for constellation in source_constellations
        if any(star_id in visible_star_ids for line in constellation.lines for star_id in line)
    ]
    retained_segments = segments_for(retained_constellations)
    retained_star_ids = sorted(
        {segment.from_star_id for segment in retained_segments}
        | {segment.to_star_id for segment in retained_segments},
        key=star_sort_key,
    )
    retained_stars = [source_stars[star_id] for star_id in retained_star_ids]

    args.kotlin_output.parent.mkdir(parents=True, exist_ok=True)
    args.kotlin_output.write_text(
        render_kotlin(retained_constellations, retained_stars, retained_segments, args),
        encoding="utf-8",
    )

    args.test_resource_dir.mkdir(parents=True, exist_ok=True)
    args.test_lines_output.write_text(
        render_source_lines(source_constellations, args),
        encoding="utf-8",
    )
    args.test_stars_output.write_text(
        args.hipparcos_stars.read_text(encoding="utf-8"),
        encoding="utf-8",
    )

    print(
        "Generated "
        f"{len(retained_constellations)} constellations, "
        f"{len(retained_stars)} stars, "
        f"{len(retained_segments)} segments."
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--stellarium-index", type=Path, default=STELLARIUM_INDEX)
    parser.add_argument("--hipparcos-stars", type=Path, default=HIPPARCOS_STARS)
    parser.add_argument("--hip-main", type=Path)
    parser.add_argument("--kotlin-output", type=Path, default=KOTLIN_OUTPUT)
    parser.add_argument("--test-resource-dir", type=Path, default=TEST_RESOURCE_DIR)
    parser.add_argument("--test-lines-output", type=Path, default=TEST_LINES_OUTPUT)
    parser.add_argument("--test-stars-output", type=Path, default=TEST_STARS_OUTPUT)
    return parser.parse_args()


def read_stellarium_constellations(path: Path) -> list[SourceConstellation]:
    data = json.loads(path.read_text(encoding="utf-8"))
    constellations = []
    for item in data["constellations"]:
        common_name = item["common_name"]
        name = ascii_name(common_name.get("native") or common_name["english"])
        lines = [[f"hip_{int(star_id)}" for star_id in line] for line in item["lines"]]
        constellations.append(SourceConstellation(name, lines))
    return constellations


def write_hipparcos_subset(path: Path, needed_hips: list[int], output: Path) -> None:
    needed = set(needed_hips)
    rows: dict[int, SourceStar] = {}
    with path.open(encoding="ascii", errors="replace") as file:
        for line in file:
            try:
                hip = int(line[8:14])
            except ValueError:
                continue
            if hip not in needed:
                continue

            ra_degrees = line[51:63].strip()
            dec_degrees = line[64:76].strip()
            if not ra_degrees or not dec_degrees:
                continue

            rows[hip] = SourceStar(
                id=f"hip_{hip}",
                hip=hip,
                ra_hours=float(ra_degrees) / 15.0,
                dec_degrees=float(dec_degrees),
            )

    missing = sorted(needed - set(rows))
    if missing:
        raise ValueError(f"Missing Hipparcos rows: {missing}")

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(render_hipparcos_stars(rows), encoding="utf-8")


def read_hipparcos_stars(path: Path) -> dict[str, SourceStar]:
    stars = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        star_id, ra_hours, dec_degrees = line.split("\t")
        hip = int(star_id.removeprefix("hip_"))
        stars[star_id] = SourceStar(
            id=star_id,
            hip=hip,
            ra_hours=float(ra_hours),
            dec_degrees=float(dec_degrees),
        )
    return stars


def ensure_complete_source(
    constellations: list[SourceConstellation],
    stars: dict[str, SourceStar],
) -> None:
    missing = sorted(
        {
            star_id
            for constellation in constellations
            for line in constellation.lines
            for star_id in line
            if star_id not in stars
        },
        key=star_sort_key,
    )
    if missing:
        raise ValueError(f"Missing coordinates for source stars: {missing}")


def visible_stars(stars: dict[str, SourceStar]) -> set[str]:
    dates = visibility_midnights()
    return {
        star.id
        for star in stars.values()
        if any(zenith_distance(star, date) <= 100.0 for date in dates)
    }


def visibility_midnights() -> list[datetime]:
    zone = ZoneInfo(OBSERVER_ZONE)
    start = datetime(VISIBILITY_YEAR, 1, 1, tzinfo=zone)
    return [
        (start + timedelta(days=day)).astimezone(timezone.utc)
        for day in range(VISIBILITY_DAYS)
    ]


def zenith_distance(star: SourceStar, date: datetime) -> float:
    latitude = math.radians(OBSERVER_LATITUDE)
    declination = math.radians(star.dec_degrees)
    hour_angle = math.radians(
        normalize_degrees(local_sidereal_degrees(date) - star.ra_hours * 15.0)
    )
    if hour_angle > math.pi:
        hour_angle -= 2.0 * math.pi
    altitude = math.asin(
        clamp(
            math.sin(latitude) * math.sin(declination)
            + math.cos(latitude) * math.cos(declination) * math.cos(hour_angle)
        )
    )
    return 90.0 - math.degrees(altitude)


def local_sidereal_degrees(date: datetime) -> float:
    return normalize_degrees(gmst_degrees(date) + OBSERVER_LONGITUDE)


def gmst_degrees(date: datetime) -> float:
    jd = julian_day(date)
    t = (jd - 2451545.0) / 36525.0
    return normalize_degrees(
        280.46061837
        + 360.98564736629 * (jd - 2451545.0)
        + 0.000387933 * t * t
        - t * t * t / 38710000.0
    )


def julian_day(date: datetime) -> float:
    utc = date.astimezone(timezone.utc)
    year = utc.year
    month = utc.month
    day = utc.day + (
        utc.hour + (utc.minute + (utc.second + utc.microsecond / 1_000_000.0) / 60.0) / 60.0
    ) / 24.0
    if month <= 2:
        year -= 1
        month += 12
    a = math.floor(year / 100)
    b = 2 - a + math.floor(a / 4)
    return (
        math.floor(365.25 * (year + 4716))
        + math.floor(30.6001 * (month + 1))
        + day
        + b
        - 1524.5
    )


def segments_for(constellations: list[SourceConstellation]) -> list[Segment]:
    segments = []
    for constellation in constellations:
        for line in constellation.lines:
            segments.extend(
                Segment(constellation.id, from_star, to_star)
                for from_star, to_star in zip(line, line[1:])
            )
    return segments


def render_kotlin(
    constellations: list[SourceConstellation],
    stars: list[SourceStar],
    segments: list[Segment],
    args: argparse.Namespace,
) -> str:
    constellation_count = len(constellations)
    source_sha = sha256(args.stellarium_index)
    subset_sha = sha256(args.hipparcos_stars)
    lines = [
        "package com.derfence.astroface.wear.astro",
        "",
        "internal data class ConstellationCatalog(",
        "    val stars: List<ConstellationStar>,",
        "    val segments: List<ConstellationSegment>",
        ")",
        "",
        "internal data class ConstellationStar(",
        "    val id: String,",
        "    val raHours: Double,",
        "    val decDegrees: Double",
        ")",
        "",
        "internal data class ConstellationSegment(",
        "    val constellationId: String,",
        "    val fromStarId: String,",
        "    val toStarId: String",
        ")",
        "",
        "internal object DefaultConstellationCatalog {",
        "    /*",
        "     * Generated by tools/constellation_catalog/generate.py.",
        f"     * Stellarium modern sky culture commit: {STELLARIUM_COMMIT}.",
        f"     * Stellarium index sha256: {source_sha}.",
        "     * Hipparcos source: CDS I/239 hip_main.dat.",
        f"     * Hipparcos source sha256: {HIPPARCOS_SOURCE_SHA256}.",
        f"     * Hipparcos subset sha256: {subset_sha}.",
        "     * Visibility rule: retain a constellation when at least one traced",
        "     * star reaches zenithDistanceDegrees <= 100.0 from AstroObserver.DEFAULT",
        f"     * across {VISIBILITY_DAYS} local midnights in {VISIBILITY_YEAR}.",
        f"     * Observer: {OBSERVER_LATITUDE} N, {OBSERVER_LONGITUDE} E, {OBSERVER_ZONE}.",
        f"     * Counts: {constellation_count} constellations, {len(stars)} stars,",
        f"     * {len(segments)} segments.",
        "     */",
        "    val value = ConstellationCatalog(",
        "        stars = listOf(",
    ]
    lines.extend(
        f"            star(\"{star.id}\", {star.ra_hours:.8f}, {star.dec_degrees:.8f}),"
        for star in stars[:-1]
    )
    if stars:
        star = stars[-1]
        lines.append(
            f"            star(\"{star.id}\", {star.ra_hours:.8f}, {star.dec_degrees:.8f})"
        )
    lines.extend(
        [
            "        ),",
            "        segments = listOf(",
        ]
    )
    lines.extend(
        "            "
        f"segment(\"{segment.constellation_id}\", \"{segment.from_star_id}\", "
        f"\"{segment.to_star_id}\"),"
        for segment in segments[:-1]
    )
    if segments:
        segment = segments[-1]
        lines.append(
            "            "
            f"segment(\"{segment.constellation_id}\", \"{segment.from_star_id}\", "
            f"\"{segment.to_star_id}\")"
        )
    lines.extend(
        [
            "        )",
            "    )",
            "",
            "    private fun star(id: String, raHours: Double, decDegrees: Double): ConstellationStar =",
            "        ConstellationStar(id, raHours, decDegrees)",
            "",
            "    private fun segment(",
            "        constellationId: String,",
            "        fromStarId: String,",
            "        toStarId: String",
            "    ): ConstellationSegment =",
            "        ConstellationSegment(constellationId, fromStarId, toStarId)",
            "}",
            "",
        ]
    )
    return "\n".join(lines)


def render_source_lines(constellations: list[SourceConstellation], args: argparse.Namespace) -> str:
    source_sha = sha256(args.stellarium_index)
    lines = [
        "# Normalized Stellarium modern constellation source lines.",
        f"# commit={STELLARIUM_COMMIT}",
        f"# source_sha256={source_sha}",
        "# columns: constellationId<TAB>comma-separated star ids",
    ]
    for constellation in constellations:
        for line in constellation.lines:
            lines.append(f"{constellation.id}\t{','.join(line)}")
    return "\n".join(lines) + "\n"


def render_hipparcos_stars(stars: dict[int, SourceStar]) -> str:
    lines = [
        "# Hipparcos coordinates for every star used by Stellarium modern constellation lines.",
        "# source=CDS I/239 hip_main.dat",
        f"# source_sha256={HIPPARCOS_SOURCE_SHA256}",
        "# columns: starId<TAB>raHours<TAB>decDegrees",
    ]
    for hip in sorted(stars):
        star = stars[hip]
        lines.append(f"{star.id}\t{star.ra_hours:.8f}\t{star.dec_degrees:.8f}")
    return "\n".join(lines) + "\n"


def ascii_name(value: str) -> str:
    return unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode("ascii")


def star_sort_key(star_id: str) -> int:
    return int(star_id.removeprefix("hip_"))


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def normalize_degrees(value: float) -> float:
    return value % 360.0


def clamp(value: float) -> float:
    return max(-1.0, min(1.0, value))


if __name__ == "__main__":
    main()
