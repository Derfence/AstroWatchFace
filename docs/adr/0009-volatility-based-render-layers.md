# ADR 0009 : Couches de rendu selon leur volatilité

## Statut

Accepté — les décisions bitmap du slot 1 sont remplacées par l'ADR 0011.

## Contexte

Les timelines de deux heures produisent douze images à dix minutes d'intervalle. Le fond de constellations et les azimuts de lever/coucher étaient pourtant redessinés dans chaque image, tandis que le statut central utilisait un bitmap transparent plein écran. La première image d'une timeline était également générée une seconde fois comme donnée par défaut.

## Décision

La Face WFF assemble huit complications, ordonnées selon leur volatilité : constellations quotidiennes, cadran glissant 24 h, horizon céleste quotidien, trois groupes de positions célestes, statut central et mode astro.

- Les constellations sont valides d'un lever du Soleil au suivant.
- Les marqueurs d'horizon sont valides jusqu'au prochain minuit local.
- Le cadran suit la timeline numérique événementielle de l'ADR 0011. ADR 0010 confie l'interpolation des positions à WFF.
- Le statut change au prochain minuit ou à l'expiration de la phase lunaire.
- Le mode complet renvoie `EMPTY` au lieu d'un bitmap transparent.

Les événements Soleil/Lune sont recherchés une fois pour une éphéméride de trois jours, indexée par observateur et date locale. Les fenêtres glissantes de 24 h filtrent ensuite cette éphéméride. Les caches sont bornés, synchronisés et uniquement en mémoire ; un redémarrage du processus provoque donc un recalcul normal.

Les marqueurs d'horizon utilisent un bitmap `340 × 340` placé à `(55, 55)` et le statut un bitmap `200 × 190` placé à `(125, 120)`. Les positions et queues sont des PNG statiques transformés par WFF selon trois complications numériques. Le séparateur « maintenant » est déplacé dans WFF sous forme de PNG pivotant avec `[HOUR_0_23_MINUTE]`.

## Conséquences

- Les données quotidiennes ne sont plus recalculées ni redessinées dans chaque frame dynamique.
- L'ancien renderer du cadran reste temporairement un oracle visuel et n'est plus appelé par le slot 1.
- Le slot 1 ne produit plus aucun bitmap dynamique.
- Les timelines de positions ne produisent plus aucun bitmap dynamique.
- Les changements d'heure sont respectés grâce aux bornes basées sur les dates et fuseaux locaux.
- La perte du processus invalide tous les caches ; aucune donnée astronomique ou position n'est écrite sur disque.
- La validation visuelle finale des nouveaux viewports et du séparateur WFF doit être réalisée sur Galaxy Watch4 Classic, notamment en AOD.
