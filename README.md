# AstroFace

AstroFace est un squelette Wear OS en deux APK :

- `wear-app` calcule et fournit le cadran 24 h et l'aiguille des heures 24 h via deux sources de complication `PHOTO_IMAGE`. Le cadran 24 h calcule localement les événements Soleil/Lune pour le point GPS fixe documenté.
- `watch-face` est une Face WFF sans code, responsable de l'assemblage visuel, de l'aiguille des minutes et de la trotteuse.
- L'aiguille des heures fait un tour en 24 h, l'aiguille des minutes fait un tour en 60 minutes, et la trotteuse avance par sauts d'une seconde.

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
