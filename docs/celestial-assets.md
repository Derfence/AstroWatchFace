# Ressources célestes WFF

Les neuf astres disposent chacun de deux PNG transparents générés depuis les
spécifications des peintres Android existants :

- `celestial_<astre>_icon.png` contient uniquement le symbole de l'astre sur un
  fond transparent ;
- `celestial_<astre>_tail.png` contient la queue colorée de 100°, avec son
  dégradé d'alpha et son rayon orbital propre.

Le fond autour de chaque astre reste transparent. En revanche, les parties non
éclairées de la Lune et de Vénus sont noires et opaques : elles représentent
une surface dans l'ombre, et non une ouverture à travers l'astre.

Les deux ressources sont séparées afin que la queue puisse tourner autour du
centre du cadran sans modifier l'orientation visuelle de l'icône. Cela préserve
notamment l'inclinaison des anneaux de Saturne et l'orientation des croissants.

Les queues sont recadrées au plus près de leurs pixels visibles, mais leur cadre
contient toujours le centre de rotation. Les positions, dimensions et pivots
WFF sont consignés dans
`watch-face/src/main/res/raw/celestial_asset_layout.json`, dans le repère local
`340 × 340` de la couche céleste.

## Mouvement WFF

Les positions sont calculées par `wear-app` toutes les dix minutes. Trois
complications `RANGED_VALUE` transportent chacune trois couples « angle initial
+ déplacement circulaire ». Les valeurs sont quantifiées par pas de `0,2°`,
puis WFF interpole leur angle avec `[MINUTE] % 10`.

Chaque queue et son icône appartiennent à un groupe orbital pivotant autour du
centre du viewport. L'icône applique simultanément la rotation opposée : sa
position suit donc l'orbite tandis que son dessin reste droit.

## Régénération

```bash
python3 tools/generate_celestial_assets.py
python3 tools/generate_celestial_assets.py --check
```

Le script utilise Pillow uniquement comme outil de développement. Il ne crée
aucune dépendance de production. Il reprend les constantes de :

- `CelestialBodyIconPainter` ;
- `OrbitTailPainter` ;
- `CelestialOrbitGeometry`.

Toute modification de ces peintres doit être répercutée dans le générateur,
puis validée par `--check` et par les tests du module `watch-face`.
