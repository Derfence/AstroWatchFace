# ADR 0005: Constellations quotidiennes en fond de cadran

## Statut

Accepté

## Contexte

Le projet demande d'afficher les constellations autour du zénith à minuit, sans noms. Le premier incrément limitait le rendu à la zone centrale de rayon `110f`, mais le rendu souhaité devient un fond astronomique sur toute la montre.

## Décision

Les constellations sont calculées dans `wear-app`, avec un catalogue d'astérismes embarqué et Astronomy Engine pour convertir les étoiles en coordonnées horizontales.

Le rendu est placé dans la couche du cadran 24 h, avant les arcs, les repères, l'overlay des planètes et les aiguilles. Les constellations forment donc un arrière-plan complet, limité au cercle de la montre. Le rayon céleste de sélection est porté à `90°` autour du zénith.

Les constellations utilisent une orientation de carte du ciel, différente de celle des planètes : `12h = Nord`, `3h = Ouest`, `6h = Sud`, `9h = Est`, comme lorsqu'on regarde le ciel allongé dans l'herbe.

Le rafraîchissement logique des constellations a lieu une fois par jour au lever du Soleil. À partir du lever du Soleil local, les constellations représentent le minuit de la nuit suivante. Avant le lever du Soleil, elles conservent la cible calculée au lever précédent.

## Conséquences

- La face WFF reste inchangée : le cadran 24 h porte le fond de constellations et l'overlay céleste continue de porter les positions planétaires.
- Les constellations peuvent rester stables toute la journée, même si l'overlay est redemandé plus souvent pour les planètes.
- Le rendu utilise un clip circulaire à l'échelle complète de la montre.
- Le catalogue embarqué est approximatif et volontairement léger pour la V1 ; il pourra être enrichi sans changer l'interface de rendu.
