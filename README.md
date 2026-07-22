# AstroFace

AstroFace est une face Wear OS en deux APK :

- `wear-app` calcule localement les événements Soleil/Lune et fournit huit complications : constellations, cadran 24 h, horizon, trois groupes de positions célestes, statut et modes astro.
- `watch-face` est une Face WFF sans code, responsable du rendu numérique du cadran 24 h, de l'assemblage des couches, du séparateur « maintenant », des aiguilles, de la batterie et de la position de mise en station de la Polaire.
- L'aiguille des heures fait un tour en 24 h, l'aiguille des minutes fait un tour en 60 minutes, et la trotteuse avance par sauts d'une seconde.

Les ressources des astres rendues et interpolées directement par WFF sont décrites
dans [docs/celestial-assets.md](docs/celestial-assets.md). Elles peuvent être
régénérées avec `python3 tools/generate_celestial_assets.py`.
Le marqueur de viseur polaire se régénère avec
`python3 tools/generate_polaris_marker.py`.

Les couches quotidiennes utilisent des caches bornés uniquement en mémoire. Le slot 1 transporte six bornes civiles à la minute dans une timeline événementielle couvrant 10 h, renouvelée toutes les 8 h ; WFF dessine directement ses arcs, marqueurs, graduations et libellés, sans bitmap dynamique. Les positions célestes sont calculées aux bornes de dix minutes, transportées sous forme numérique puis interpolées à la minute par WFF ; le statut suit ses échéances réelles.

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

Ensuite, sélectionne AstroFace comme face active sur la montre. L'app Wear OS affiche un diagnostic des services et la dernière demande reçue par la Face.
