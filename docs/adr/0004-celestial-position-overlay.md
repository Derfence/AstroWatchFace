# ADR 0004: Overlay des positions célestes

## Statut

Remplacé par l'ADR 0010

## Contexte

AstroFace doit afficher les positions du Soleil, de la Lune et des planètes sur un anneau céleste complet, avec la convention `12h = Sud`, `3h = Ouest`, `6h = Nord` et `9h = Est`.

Ces positions représentent une direction dans le ciel, tandis que le cadran 24 h existant représente des périodes et événements temporels. Mélanger les deux lectures dans le même renderer rendrait la responsabilité du cadran 24 h moins claire et compliquerait les évolutions visuelles.

## Décision

Ajouter une complication plein écran dédiée dans `watch-face`, fournie par `CelestialOverlayDataSourceService` dans `wear-app`.

Le service génère un bitmap transparent 450 x 450 contenant uniquement l'overlay céleste et les icônes des objets. Le calcul reste dans le package Kotlin pur `astro`, avec Astronomy Engine déjà présent dans le projet.

L'anneau céleste est rendu comme un cercle blanc discret, sans lettres cardinales, afin de réduire la densité visuelle de l'overlay.

Les corps célestes sont placés sur des orbites de montre concentriques, séparées par un espacement radial fixe. L'ordre retenu est Soleil, Lune, puis les planètes dans l'ordre de leur distance au Soleil : Mercure, Vénus, Mars, Jupiter, Saturne, Uranus et Neptune.

Chaque corps affiche une petite queue colorée le long de son orbite. La queue utilise un dégradé angulaire linéaire : transparente à son extrémité éloignée, puis progressivement plus opaque jusqu'au corps céleste.

Chaque orbite affiche aussi les marqueurs de lever/coucher du corps correspondant. Ces marqueurs sont de petits traits radiaux opaques, dans la couleur du corps, perpendiculaires à l'orbite, dont la longueur est calibrée sur l'espacement inter-orbites. De courts arcs opaques dans la couleur du corps partent de ces traits vers la portion d'altitude négative, avant le lever et après le coucher ; ils s'estompent rapidement et sont dessinés sous les queues.

Les icônes restent de petites formes graphiques colorées plutôt que des symboles astronomiques textuels. Vénus est représentée par un croissant ambré, Mars par un disque rouge avec une petite calotte, Mercure par un disque brun-gris minéral, Neptune par un disque bleu simple, tandis que Soleil, Lune, Jupiter, Saturne et Uranus conservent leur rendu dédié.

La fréquence de mise à jour déclarée est de 15 minutes. Le mouvement apparent des planètes ne justifie pas un rafraîchissement plus fréquent pour cette première version, et le Soleil/la Lune restent suffisamment proches visuellement entre deux mises à jour.

## Conséquences

- `watch-face` reste une Face Watch Face Format sans code.
- Le cadran temporel 24 h et les positions célestes peuvent évoluer indépendamment.
- Le cercle céleste appartient à l'overlay des positions, pas au cadran temporel.
- L'espacement radial des orbites améliore la séparation visuelle des icônes sans représenter la hauteur des objets.
- Les positions sont calculées hors ligne, sans météo, réseau ou géolocalisation dynamique.
- Les icônes des objets sous l'horizon sont affichées comme les autres, mais leurs levers/couchers sont indiqués par des marqueurs d'horizon propres à chaque orbite.
