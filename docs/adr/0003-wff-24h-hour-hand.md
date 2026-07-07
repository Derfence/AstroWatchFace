# ADR 0003: Aiguille des heures 24h par complication image

## Statut

Accepté

## Contexte

Le besoin de lecture horaire évolue : l'aiguille des heures doit faire un tour complet en 24 heures, tandis que l'aiguille des minutes reste classique et que la trotteuse doit avancer par sauts d'une seconde.

Le module `watch-face` reste une Face Watch Face Format sans code. L'élément WFF `HourHand` représente une aiguille d'heures standard qui fait un tour en 12 heures, donc il ne convient pas pour le nouveau contrat visuel.

Un essai avec une `PartImage` WFF pilotée par `Transform target="angle"` compile, mais l'aiguille n'apparaît pas sur la montre réelle. Le rendu de l'aiguille 24h doit donc passer par un chemin déjà validé sur l'appareil : les complications image générées par `wear-app`.

## Décision

Remplacer l'aiguille `HourHand` visible par un troisième slot de complication plein écran, non personnalisable, fourni par `Hour24hHandDataSourceService`.

Le renderer `Hour24hHandRenderer` dessine une image transparente 450 x 450 contenant uniquement l'aiguille des heures. Son angle est calculé côté `wear-app` avec la même convention que le cadran 24h : 00:00 en haut, 06:00 à droite, 12:00 en bas et 18:00 à gauche. La précision retenue est la minute.

Conserver `MinuteHand` pour l'aiguille des minutes, car son comportement WFF correspond au besoin : un tour en 60 minutes.

Remplacer la trotteuse lisse `Sweep` par `Tick`, afin que `SecondHand` avance par mouvements discrets à chaque seconde.

## Conséquences

- `watch-face` reste sans code et ne reçoit pas de dépendance de production.
- L'aiguille des heures dépend d'une source de complication supplémentaire dans `wear-app`, à réinstaller avec la Face.
- Le contrat visuel devient : 00:00 en haut, 06:00 à droite, 12:00 en bas, 18:00 à gauche pour l'aiguille des heures.
- Les services `Dial12h...` restent stables techniquement, mais leurs libellés utilisateur deviennent des repères analogiques afin de ne plus suggérer une lecture horaire principale en 12h.
- Un test JVM vérifie la structure XML WFF attendue pour éviter une régression vers `HourHand`, `Transform` ou `Sweep`.
