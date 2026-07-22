#!/usr/bin/env python3
"""Generate the deterministic four-point Polaris reticle marker."""

from pathlib import Path
import struct
import zlib


SIZE = 20
SUPERSAMPLING = 8
OUTPUT = Path(__file__).parents[1] / (
    "watch-face/src/main/res/drawable-nodpi/polaris_reticle_marker.png"
)
OUTER_STAR = (
    (10.0, 0.25),
    (12.2, 7.8),
    (19.75, 10.0),
    (12.2, 12.2),
    (10.0, 19.75),
    (7.8, 12.2),
    (0.25, 10.0),
    (7.8, 7.8),
)
INNER_STAR = (
    (10.0, 1.4),
    (11.5, 8.5),
    (18.6, 10.0),
    (11.5, 11.5),
    (10.0, 18.6),
    (8.5, 11.5),
    (1.4, 10.0),
    (8.5, 8.5),
)


def chunk(kind: bytes, payload: bytes) -> bytes:
    checksum = zlib.crc32(kind + payload) & 0xFFFFFFFF
    return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", checksum)


def contains(polygon: tuple[tuple[float, float], ...], x: float, y: float) -> bool:
    inside = False
    previous = polygon[-1]
    for current in polygon:
        x1, y1 = previous
        x2, y2 = current
        crosses = (y1 > y) != (y2 > y)
        if crosses and x < (x2 - x1) * (y - y1) / (y2 - y1) + x1:
            inside = not inside
        previous = current
    return inside


def sample(x: float, y: float) -> tuple[int, int, int, int]:
    if contains(INNER_STAR, x, y):
        return 255, 0, 0, 255
    if contains(OUTER_STAR, x, y):
        return 0, 0, 0, 255
    return 0, 0, 0, 0


def pixel(x: int, y: int) -> tuple[int, int, int, int]:
    x = min(x, SIZE - 1 - x)
    y = min(y, SIZE - 1 - y)
    center_pixels = (SIZE // 2 - 1, SIZE // 2)
    edge_pixels = (0, SIZE - 1)
    if x in center_pixels and y in center_pixels:
        return 255, 0, 0, 255
    if (x in center_pixels and y in edge_pixels) or (
        y in center_pixels and x in edge_pixels
    ):
        return 0, 0, 0, 255
    totals = [0, 0, 0, 0]
    for sample_y in range(SUPERSAMPLING):
        for sample_x in range(SUPERSAMPLING):
            color = sample(
                x + (sample_x + 0.5) / SUPERSAMPLING,
                y + (sample_y + 0.5) / SUPERSAMPLING,
            )
            for channel, value in enumerate(color):
                totals[channel] += value
    sample_count = SUPERSAMPLING * SUPERSAMPLING
    return tuple(round(value / sample_count) for value in totals)


def main() -> None:
    raw = bytearray()
    for y in range(SIZE):
        raw.append(0)
        for x in range(SIZE):
            raw.extend(pixel(x, y))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), level=9))
    png += chunk(b"IEND", b"")
    OUTPUT.write_bytes(png)


if __name__ == "__main__":
    main()
