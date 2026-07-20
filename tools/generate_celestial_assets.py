#!/usr/bin/env python3
"""Generate WFF-ready celestial icons and orbit tails from AstroFace painter specs.

The values in this file mirror CelestialBodyIconPainter, OrbitTailPainter and
CelestialOrbitGeometry. Assets are rendered at 8x and downsampled to preserve
the anti-aliased appearance of Android Canvas.
"""

from __future__ import annotations

import argparse
import json
import math
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
DRAWABLE_DIR = ROOT / "watch-face/src/main/res/drawable-nodpi"
LAYOUT_PATH = ROOT / "watch-face/src/main/res/raw/celestial_asset_layout.json"
PREVIEW_PATH = ROOT / "docs/images/celestial_assets_preview.png"

SUPERSAMPLE = 8
VIEWPORT_SIZE = 340
VIEWPORT_CENTER = 170.0
ICON_CANVAS_SIZE = 24
ICON_CENTER = ICON_CANVAS_SIZE / 2.0
TAIL_SWEEP_DEGREES = 100.0
TAIL_STROKE_WIDTH = 2.0


@dataclass(frozen=True)
class BodySpec:
    name: str
    radius: float
    tail_rgb: tuple[int, int, int]


BODIES = (
    BodySpec("sun", 110.0, (255, 204, 82)),
    BodySpec("moon", 115.0, (235, 238, 242)),
    BodySpec("mercury", 120.0, (166, 145, 119)),
    BodySpec("venus", 125.0, (255, 188, 48)),
    BodySpec("mars", 130.0, (214, 68, 48)),
    BodySpec("jupiter", 135.0, (229, 169, 98)),
    BodySpec("saturn", 140.0, (238, 215, 142)),
    BodySpec("uranus", 145.0, (104, 211, 210)),
    BodySpec("neptune", 150.0, (69, 113, 225)),
)


def scaled(value: float) -> float:
    return value * SUPERSAMPLE


def rgba_image(width: int, height: int) -> Image.Image:
    return Image.new("RGBA", (width * SUPERSAMPLE, height * SUPERSAMPLE), (0, 0, 0, 0))


def downsample(image: Image.Image) -> Image.Image:
    return image.resize(
        (image.width // SUPERSAMPLE, image.height // SUPERSAMPLE),
        Image.Resampling.LANCZOS,
    )


def downsample_icon(image: Image.Image) -> Image.Image:
    target_size = (image.width // SUPERSAMPLE, image.height // SUPERSAMPLE)
    red, green, blue, alpha = image.split()
    alpha_small = alpha.resize(target_size, Image.Resampling.LANCZOS)
    premultiplied = [
        ImageChops.multiply(channel, alpha).resize(target_size, Image.Resampling.LANCZOS)
        for channel in (red, green, blue)
    ]
    alpha_values = list(alpha_small.getdata())
    color_values = [list(channel.getdata()) for channel in premultiplied]
    straight_channels = []
    for values in color_values:
        channel = Image.new("L", target_size)
        channel.putdata([
            min(255, round(value * 255 / alpha_value)) if alpha_value > 0 else 0
            for value, alpha_value in zip(values, alpha_values)
        ])
        straight_channels.append(channel)
    result = Image.merge("RGBA", (*straight_channels, alpha_small))
    result.putdata([
        (0, 0, 0, 0) if alpha <= 1 else (red, green, blue, alpha)
        for red, green, blue, alpha in result.getdata()
    ])
    return result


def ellipse_box(cx: float, cy: float, rx: float, ry: float) -> tuple[float, float, float, float]:
    return (
        scaled(cx - rx),
        scaled(cy - ry),
        scaled(cx + rx),
        scaled(cy + ry),
    )


def fill_circle(draw: ImageDraw.ImageDraw, x: float, y: float, radius: float, color: tuple[int, ...]) -> None:
    draw.ellipse(ellipse_box(x, y, radius, radius), fill=color)


def fill_oval(
    draw: ImageDraw.ImageDraw,
    left: float,
    top: float,
    right: float,
    bottom: float,
    color: tuple[int, ...],
) -> None:
    draw.ellipse(tuple(scaled(value) for value in (left, top, right, bottom)), fill=color)


def stroke_oval(
    draw: ImageDraw.ImageDraw,
    left: float,
    top: float,
    right: float,
    bottom: float,
    color: tuple[int, ...],
    width: float,
) -> None:
    draw.ellipse(
        tuple(scaled(value) for value in (left, top, right, bottom)),
        outline=color,
        width=max(1, round(scaled(width))),
    )


def round_line(
    draw: ImageDraw.ImageDraw,
    start: tuple[float, float],
    end: tuple[float, float],
    color: tuple[int, ...],
    width: float,
) -> None:
    line_width = max(1, round(scaled(width)))
    points = [(scaled(start[0]), scaled(start[1])), (scaled(end[0]), scaled(end[1]))]
    draw.line(points, fill=color, width=line_width)
    radius = line_width / 2.0
    for x, y in points:
        draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=color)


def point_around(cx: float, cy: float, radius: float, angle_degrees: float) -> tuple[float, float]:
    radians = math.radians(angle_degrees)
    return cx + math.sin(radians) * radius, cy - math.cos(radians) * radius


def draw_icon(name: str) -> Image.Image:
    image = rgba_image(ICON_CANVAS_SIZE, ICON_CANVAS_SIZE)
    draw = ImageDraw.Draw(image)
    x = y = ICON_CENTER

    if name == "sun":
        for index in range(8):
            angle = index * 45.0
            round_line(
                draw,
                point_around(x, y, 5.4, angle),
                point_around(x, y, 7.4, angle),
                (255, 204, 82, 255),
                1.4,
            )
        fill_circle(draw, x, y, 4.8, (255, 224, 112, 255))
    elif name == "moon":
        fill_circle(draw, x, y, 5.4, (235, 238, 242, 255))
        fill_circle(draw, x + 2.5, y - 0.4, 5.1, (0, 0, 0, 255))
    elif name == "mercury":
        fill_circle(draw, x, y, 4.9, (166, 145, 119, 255))
        fill_circle(draw, x - 1.6, y - 1.0, 0.9, (108, 96, 84, 255))
        fill_circle(draw, x + 1.5, y + 1.1, 0.7, (108, 96, 84, 255))
    elif name == "venus":
        fill_circle(draw, x, y, 5.7, (255, 188, 48, 255))
        fill_circle(draw, x + 2.6, y - 0.2, 5.5, (0, 0, 0, 255))
        fill_circle(draw, x - 1.8, y - 1.8, 1.1, (255, 228, 104, 255))
    elif name == "mars":
        fill_circle(draw, x, y, 5.2, (214, 68, 48, 255))
        fill_circle(draw, x - 1.6, y + 1.1, 1.2, (122, 43, 34, 255))
        fill_oval(draw, x - 2.2, y - 5.1, x + 2.2, y - 2.8, (248, 230, 208, 255))
    elif name == "jupiter":
        fill_circle(draw, x, y, 5.8, (229, 169, 98, 255))
        round_line(draw, (x - 4.8, y - 1.6), (x + 4.8, y - 1.6), (255, 222, 166, 255), 1.2)
        round_line(draw, (x - 4.2, y + 1.8), (x + 4.2, y + 1.8), (255, 222, 166, 255), 1.2)
    elif name == "saturn":
        ring = rgba_image(ICON_CANVAS_SIZE, ICON_CANVAS_SIZE)
        ring_draw = ImageDraw.Draw(ring)
        stroke_oval(ring_draw, x - 8.5, y - 3.2, x + 8.5, y + 3.2, (238, 215, 142, 255), 1.4)
        ring = ring.rotate(-18.0, resample=Image.Resampling.BICUBIC, center=(scaled(x), scaled(y)))
        image.alpha_composite(ring)
        draw = ImageDraw.Draw(image)
        fill_circle(draw, x, y, 4.7, (227, 199, 112, 255))
    elif name == "uranus":
        fill_circle(draw, x, y, 5.1, (104, 211, 210, 255))
        stroke_oval(draw, x - 3.4, y - 6.2, x + 3.4, y + 6.2, (179, 244, 241, 255), 1.1)
    elif name == "neptune":
        fill_circle(draw, x, y, 5.2, (69, 113, 225, 255))
        fill_circle(draw, x - 1.8, y - 1.8, 1.3, (126, 166, 255, 255))
    else:
        raise ValueError(f"Unknown body: {name}")

    return downsample_icon(image)


def draw_tail(radius: float, rgb: tuple[int, int, int]) -> Image.Image:
    image = rgba_image(VIEWPORT_SIZE, VIEWPORT_SIZE)
    draw = ImageDraw.Draw(image)
    line_width = max(1, round(scaled(TAIL_STROKE_WIDTH)))
    steps = 800
    previous = point_around(VIEWPORT_CENTER, VIEWPORT_CENTER, radius, -TAIL_SWEEP_DEGREES)
    for index in range(1, steps + 1):
        fraction = index / steps
        angle = -TAIL_SWEEP_DEGREES + TAIL_SWEEP_DEGREES * fraction
        current = point_around(VIEWPORT_CENTER, VIEWPORT_CENTER, radius, angle)
        alpha = round(200 * fraction)
        draw.line(
            [(scaled(previous[0]), scaled(previous[1])), (scaled(current[0]), scaled(current[1]))],
            fill=(*rgb, alpha),
            width=line_width,
        )
        previous = current
    end = point_around(VIEWPORT_CENTER, VIEWPORT_CENTER, radius, 0.0)
    cap_radius = line_width / 2.0
    draw.ellipse(
        (
            scaled(end[0]) - cap_radius,
            scaled(end[1]) - cap_radius,
            scaled(end[0]) + cap_radius,
            scaled(end[1]) + cap_radius,
        ),
        fill=(*rgb, 200),
    )
    return downsample(image)


def crop_with_reference(
    image: Image.Image,
    reference_x: float,
    reference_y: float,
) -> tuple[Image.Image, tuple[int, int, int, int], float, float]:
    alpha = image.getchannel("A")
    box = alpha.getbbox()
    if box is None:
        raise ValueError("Generated image is empty")
    left, top, right, bottom = box
    left = min(left, math.floor(reference_x))
    top = min(top, math.floor(reference_y))
    right = max(right, math.ceil(reference_x) + 1)
    bottom = max(bottom, math.ceil(reference_y) + 1)
    cropped = image.crop((left, top, right, bottom))
    pivot_x = (reference_x - left) / cropped.width
    pivot_y = (reference_y - top) / cropped.height
    return cropped, (left, top, right, bottom), pivot_x, pivot_y


def crop_icon_canvas(image: Image.Image) -> tuple[Image.Image, tuple[int, int, int, int], float, float]:
    box = (1, 1, ICON_CANVAS_SIZE - 1, ICON_CANVAS_SIZE - 1)
    cropped = image.crop(box)
    pivot = (ICON_CENTER - box[0]) / cropped.width
    return cropped, box, pivot, pivot


def create_assets() -> tuple[dict[str, Image.Image], dict[str, object], Image.Image]:
    images: dict[str, Image.Image] = {}
    layout: dict[str, object] = {
        "schemaVersion": 1,
        "coordinateSpace": {"width": VIEWPORT_SIZE, "height": VIEWPORT_SIZE},
        "referenceAngleDegrees": 0,
        "iconBackground": "transparent",
        "opaqueShadowBodies": ["moon", "venus"],
        "orientation": "icons remain screen-upright; tails rotate around viewport center",
        "bodies": {},
    }

    preview = Image.new("RGBA", (450, 450), (0, 0, 0, 255))
    preview_draw = ImageDraw.Draw(preview)

    for index, body in enumerate(BODIES):
        icon_full = draw_icon(body.name)
        icon, icon_box, icon_pivot_x, icon_pivot_y = crop_icon_canvas(icon_full)
        icon_name = f"celestial_{body.name}_icon.png"
        images[icon_name] = icon

        tail_full = draw_tail(body.radius, body.tail_rgb)
        tail, tail_box, tail_pivot_x, tail_pivot_y = crop_with_reference(
            tail_full,
            VIEWPORT_CENTER,
            VIEWPORT_CENTER,
        )
        tail_name = f"celestial_{body.name}_tail.png"
        images[tail_name] = tail

        icon_left, icon_top, _, _ = icon_box
        tail_left, tail_top, _, _ = tail_box
        body_point_y = VIEWPORT_CENTER - body.radius
        layout["bodies"][body.name] = {
            "orbitRadius": body.radius,
            "icon": {
                "resource": icon_name.removesuffix(".png"),
                "x": round(VIEWPORT_CENTER - (ICON_CENTER - icon_left), 4),
                "y": round(body_point_y - (ICON_CENTER - icon_top), 4),
                "width": icon.width,
                "height": icon.height,
                "pivotX": round(icon_pivot_x, 8),
                "pivotY": round(icon_pivot_y, 8),
            },
            "tail": {
                "resource": tail_name.removesuffix(".png"),
                "x": tail_left,
                "y": tail_top,
                "width": tail.width,
                "height": tail.height,
                "pivotX": round(tail_pivot_x, 8),
                "pivotY": round(tail_pivot_y, 8),
            },
        }

        preview_angle = index * 360.0 / len(BODIES)
        draw_preview_tail(preview_draw, 225.0, 225.0, body.radius, preview_angle, body.tail_rgb)
        icon_x, icon_y = point_around(225.0, 225.0, body.radius, preview_angle)
        preview.alpha_composite(icon, (round(icon_x - icon_pivot_x * icon.width), round(icon_y - icon_pivot_y * icon.height)))

    return images, layout, preview


def validate_icon_backgrounds(images: dict[str, Image.Image]) -> None:
    opaque_shadow_bodies = {"moon", "venus"}
    for body in BODIES:
        image = images[f"celestial_{body.name}_icon.png"]
        black_alphas = [
            alpha
            for red, green, blue, alpha in image.getdata()
            if alpha > 0 and red == 0 and green == 0 and blue == 0
        ]
        if body.name in opaque_shadow_bodies:
            if 255 not in black_alphas:
                raise ValueError(f"Missing opaque shadow in {body.name} icon")
        elif black_alphas:
            raise ValueError(f"Visible black background in {body.name} icon")


def draw_preview_tail(
    draw: ImageDraw.ImageDraw,
    center_x: float,
    center_y: float,
    radius: float,
    angle_degrees: float,
    rgb: tuple[int, int, int],
) -> None:
    steps = 200
    previous = point_around(center_x, center_y, radius, angle_degrees - TAIL_SWEEP_DEGREES)
    for index in range(1, steps + 1):
        fraction = index / steps
        current = point_around(
            center_x,
            center_y,
            radius,
            angle_degrees - TAIL_SWEEP_DEGREES + TAIL_SWEEP_DEGREES * fraction,
        )
        draw.line((previous, current), fill=(*rgb, round(200 * fraction)), width=2)
        previous = current


def layout_json(layout: dict[str, object]) -> str:
    return json.dumps(layout, ensure_ascii=False, indent=2, sort_keys=True) + "\n"


def check_assets(images: dict[str, Image.Image], layout: dict[str, object], preview: Image.Image) -> None:
    errors: list[str] = []
    for name, expected in images.items():
        path = DRAWABLE_DIR / name
        if not path.exists():
            errors.append(f"missing {path.relative_to(ROOT)}")
            continue
        with Image.open(path) as actual:
            if actual.convert("RGBA").tobytes() != expected.tobytes() or actual.size != expected.size:
                errors.append(f"stale {path.relative_to(ROOT)}")
    if not LAYOUT_PATH.exists() or LAYOUT_PATH.read_text(encoding="utf-8") != layout_json(layout):
        errors.append(f"stale {LAYOUT_PATH.relative_to(ROOT)}")
    if not PREVIEW_PATH.exists():
        errors.append(f"missing {PREVIEW_PATH.relative_to(ROOT)}")
    else:
        with Image.open(PREVIEW_PATH) as actual_preview:
            if actual_preview.convert("RGBA").tobytes() != preview.tobytes():
                errors.append(f"stale {PREVIEW_PATH.relative_to(ROOT)}")
    if errors:
        raise SystemExit("Celestial assets check failed:\n- " + "\n- ".join(errors))
    print(f"Celestial assets are current: {len(images)} PNG files")


def write_assets(images: dict[str, Image.Image], layout: dict[str, object], preview: Image.Image) -> None:
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    LAYOUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    PREVIEW_PATH.parent.mkdir(parents=True, exist_ok=True)
    for name, image in images.items():
        image.save(DRAWABLE_DIR / name, format="PNG", optimize=True)
    LAYOUT_PATH.write_text(layout_json(layout), encoding="utf-8")
    preview.save(PREVIEW_PATH, format="PNG", optimize=True)
    print(f"Generated {len(images)} celestial PNG files")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="verify committed assets without rewriting them")
    args = parser.parse_args()
    images, layout, preview = create_assets()
    validate_icon_backgrounds(images)
    if args.check:
        check_assets(images, layout, preview)
    else:
        write_assets(images, layout, preview)


if __name__ == "__main__":
    main()
