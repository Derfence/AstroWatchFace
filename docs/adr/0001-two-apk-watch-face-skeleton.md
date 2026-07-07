# ADR 0001: Two-APK Watch Face Skeleton

## Status

Accepted

## Context

AstroFace must keep the watch face rendering simple while allowing the Wear OS app to own generated dial content. The first skeleton must install two APKs on the watch and prove that the Face and the app communicate through official Wear OS complications.

## Decision

Use two Android application modules:

- `watch-face`: a Watch Face Format APK with `android:hasCode="false"`. It renders the black background, two full-screen `PHOTO_IMAGE` complication slots, and the analog hands.
- `wear-app`: a Wear OS app with two `ComplicationDataSourceService` implementations. Each service renders one transparent 450 x 450 bitmap and returns it as `PhotoImageComplicationData`.

The watch face has no dial calculation logic. Its only time-aware logic is the WFF analog hand rendering.

## Consequences

- The generated 24 h and 12 h dials can evolve independently from the Face.
- The Face remains compatible with the WFF resource-only model.
- Future astronomical calculations belong in `wear-app` or a shared calculation module, not in `watch-face`.
