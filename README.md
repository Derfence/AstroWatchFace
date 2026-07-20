# AstroFace

AstroFace est une face Wear OS en deux APK :

- `wear-app` calcule localement les événements Soleil/Lune et fournit six couches de complication : constellations, cadran 24 h, horizon, positions célestes, statut et modes astro.
- `watch-face` est une Face WFF sans code, responsable de l'assemblage des couches, du séparateur « maintenant », des aiguilles et de la batterie.
- L'aiguille des heures fait un tour en 24 h, l'aiguille des minutes fait un tour en 60 minutes, et la trotteuse avance par sauts d'une seconde.

Les couches quotidiennes utilisent des caches bornés uniquement en mémoire. Le cadran et les positions gardent une précision de dix minutes sur une timeline de deux heures ; le statut suit ses échéances réelles.

## Build

```bash
./gradlew :wear-app:assembleDebug :watch-face:assembleDebug
```

## Installation montre

Installe d'abord l'app Wear OS, puis la Face :

```bash
adb install wear-app/build/outputs/apk/debug/wear-app-debug.apk
adb install watch-face/build/outputs/apk/debug/watch-face-debug.apk
```

Ensuite, sélectionne AstroFace comme face active sur la montre. L'app Wear OS affiche un diagnostic avec les deux services et la dernière demande reçue par la Face.
