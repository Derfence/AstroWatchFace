# ADR 0001: Two-APK Watch Face Skeleton

## Status

Accepted

## Context

AstroFace must keep the watch face rendering simple while allowing the Wear OS app to own generated dial content. The first skeleton must install two APKs on the watch and prove that the Face and the app communicate through official Wear OS complications.

## Decision

Use two Android application modules:

- `watch-face`: a Watch Face Format APK with `android:hasCode="false"`. It renders the black background, full-screen `PHOTO_IMAGE` complication slots, and the WFF-native analog hands.
- `wear-app`: a Wear OS app with `ComplicationDataSourceService` implementations. Each service renders one transparent 450 x 450 bitmap and returns it as `PhotoImageComplicationData`.

The watch face has no dial calculation logic. ADR 0008 supersedes the earlier hour-hand design: the 24 h hour hand, minute hand, and second hand are rendered directly by WFF, while generated dial content remains in complications.

## Consequences

- The generated 24 h dial can evolve independently from the WFF-rendered hands.
- The Face remains compatible with the WFF resource-only model.
- Future astronomical calculations belong in `wear-app` or a shared calculation module, not in `watch-face`.
