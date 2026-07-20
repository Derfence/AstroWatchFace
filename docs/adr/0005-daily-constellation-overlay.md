# ADR 0005: Constellations quotidiennes en fond de cadran

## Statut

Accepté

## Contexte

Le projet demande d'afficher les constellations autour du zénith à minuit, sans noms. Le premier incrément limitait le rendu à la zone centrale de rayon `110f`, mais le rendu souhaité devient un fond astronomique sur toute la montre.

## Décision

Les constellations sont calculées dans `wear-app`, avec un catalogue d'astérismes embarqué et Astronomy Engine pour convertir les étoiles en coordonnées horizontales.

Le catalogue embarqué est généré depuis un snapshot verrouillé de la culture de ciel moderne de Stellarium et des coordonnées Hipparcos/CDS nécessaires aux tracés. Le filtre de construction retient toutes les constellations dont au moins une étoile de tracé est visible depuis le point GPS fixe du projet au cours d'un balayage annuel des minuits locaux. Les tests relisent les sources normalisées et vérifient que les étoiles, les segments et les constellations retenues correspondent exactement au snapshot filtré.

Le rendu forme un arrière-plan complet, limité au cercle de la montre. ADR 0009 extrait ce fond du bitmap du cadran 24 h vers une complication quotidienne placée sous celui-ci. Le rayon céleste de sélection est porté à `90°` autour du zénith.

Les constellations utilisent une orientation de carte du ciel, différente de celle des planètes : `12h = Nord`, `3h = Ouest`, `6h = Sud`, `9h = Est`, comme lorsqu'on regarde le ciel allongé dans l'herbe.

Le rafraîchissement logique des constellations a lieu une fois par jour au lever du Soleil. À partir du lever du Soleil local, les constellations représentent le minuit de la nuit suivante. Avant le lever du Soleil, elles conservent la cible calculée au lever précédent.

## Conséquences

- La face WFF assemble le fond quotidien dans un slot distinct sous le cadran 24 h.
- Les constellations peuvent rester stables toute la journée, même si l'overlay est redemandé plus souvent pour les planètes.
- Le snapshot est partagé en mémoire avec le mode nocturne ; les styles normal et rouge possèdent chacun leur bitmap rasterisé.
- Le rendu utilise un clip circulaire à l'échelle complète de la montre.
- Le catalogue embarqué est plus volumineux que la V1 initiale, mais il reste hors réseau, généré de manière déterministe et sans changement d'interface de rendu.
