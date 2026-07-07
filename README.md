# AstroFace

AstroFace est un squelette Wear OS en deux APK :

- `wear-app` calcule et fournit les cadrans 24 h et 12 h via deux sources de complication `PHOTO_IMAGE`. Le cadran 24 h calcule localement les événements Soleil/Lune pour le point GPS fixe documenté.
- `watch-face` est une Face WFF sans code, responsable de l'assemblage visuel et des aiguilles.

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
