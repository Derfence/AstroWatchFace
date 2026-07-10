# ADR 0007: Modes d'affichage au clic central

## Statut

Accepté

## Contexte

AstroFace doit proposer trois lectures de la même face : le cadran complet existant, un mode constellations seules en vision nocturne, et un mode système solaire vu de dessus. La face `watch-face` est actuellement une Watch Face Format sans code, tandis que `wear-app` fournit les couches dynamiques sous forme de complications image plein écran.

Les deux nouveaux modes doivent masquer la minuterie native et la trotteuse sans migrer toute la face vers une implémentation Kotlin.

## Décision

Conserver `watch-face` sans code et ajouter une complication plein écran supérieure, `ModeOverlayDataSourceService`, fournie par `wear-app`.

Un état persistant `DisplayMode` est stocké dans `wear-app` avec trois valeurs : `FULL_DIAL`, `CONSTELLATIONS_NIGHT` et `SOLAR_SYSTEM`. Un élément central de la face WFF lance `ModeCycleActivity`, qui passe au mode suivant et demande la mise à jour de toutes les complications.

En mode `FULL_DIAL`, l'overlay de mode rend un bitmap entièrement transparent. En modes `CONSTELLATIONS_NIGHT` et `SOLAR_SYSTEM`, il rend un bitmap noir opaque qui recouvre les aiguilles natives et toutes les couches inférieures.

Le mode constellations réutilise la position actuelle des constellations, mais force les étoiles et les traits en rouge opaque. Le mode système solaire utilise un modèle séparé basé sur des coordonnées héliocentriques, avec fond étoilé, Soleil au centre, planètes Mercure à Neptune et Terre incluse. Il remplace les cercles d'orbite par les mêmes queues colorées que l'overlay céleste principal, avec une queue dédiée pour la Terre. Il ne réutilise pas `CelestialBody`, afin de ne pas mélanger le rendu directionnel géocentrique avec le rendu orbital vu de dessus.

## Conséquences

- Le cycle des modes peut avoir une courte latence liée au rafraîchissement des complications.
- Les aiguilles minutes et secondes restent déclarées dans WFF, mais sont visuellement couvertes en modes astro.
- Le rendu des icônes planétaires est partagé entre l'overlay céleste existant et le mode système solaire.
- Le rendu des queues d'orbite est partagé entre l'overlay céleste existant et le mode système solaire.
- La Terre existe seulement dans le modèle solaire, pas dans l'overlay céleste géocentrique.
