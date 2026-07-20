#!/usr/bin/env python3
"""Generate the deterministic WFF bitmap used by the rotating now separator."""

from pathlib import Path
import struct
import zlib


WIDTH = 12
HEIGHT = 222
OUTPUT = Path(__file__).parents[1] / "watch-face/src/main/res/drawable-nodpi/now_separator.png"


def chunk(kind: bytes, payload: bytes) -> bytes:
    checksum = zlib.crc32(kind + payload) & 0xFFFFFFFF
    return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", checksum)


def pixel(x: int, y: int) -> tuple[int, int, int, int]:
    if 4 <= y <= 30 and 5 <= x <= 6:
        return 255, 255, 255, 235
    if y <= 36 and 4 <= x <= 7:
        return 229, 57, 53, 240
    if y <= 38:
        return 0, 0, 0, 255
    return 0, 0, 0, 0


def main() -> None:
    raw = bytearray()
    for y in range(HEIGHT):
        raw.append(0)
        for x in range(WIDTH):
            raw.extend(pixel(x, y))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", WIDTH, HEIGHT, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), level=9))
    png += chunk(b"IEND", b"")
    OUTPUT.write_bytes(png)


if __name__ == "__main__":
    main()
