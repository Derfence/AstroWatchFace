# ADR 0012 : Position de la Polaire pour viseur polaire

## Statut

Accepté

## Contexte

AstroFace doit indiquer où placer la Polaire dans un viseur polaire standard représenté comme un cadran 12 h, avec 12 en haut. Cette information est une aide réelle de mise en station et non une simple représentation décorative. Le calcul utilise le point fixe du projet, à 45,665694° N et 2,944194° E.

La face WFF utilise déjà huit `ComplicationSlot`, maximum autorisé par le format. Les trois champs de chaque complication numérique existante sont occupés, et fusionner des complications image augmenterait fortement la mémoire ou réduirait leur cadence de mise à jour.

## Décision

Calculer directement l'angle du marqueur dans WFF à partir de `[UTC_TIMESTAMP]`, sans ajouter de complication ni de service. La convention du viseur est :

`position 12 h = normaliser12h(6 h - angle horaire local de la Polaire / 2)`

L'angle horaire de référence est obtenu avec Astronomy Engine à partir des coordonnées J2000 de la Polaire et de l'observateur fixe. L'expression WFF utilise :

- l'époque `2026-01-01T00:00:00Z`, soit `1767225600000` ms ;
- l'angle de référence `122,754941129°` ;
- un jour sidéral arrondi à `86164091` ms, afin que le modulo reste entier et précis ;
- une correction séculaire de `0,001125874°` par jour.

La calibration est validée contre Astronomy Engine tous les quatorze jours entre 2020 et 2040, avec une erreur maximale autorisée de `0,5°`, équivalente à une minute du cadran 12 h.

Le marqueur est un PNG transparent de `20 × 20` px : étoile rouge opaque à quatre branches avec contour noir. Son centre est placé à un rayon de `217` px, contre `211` px pour les marqueurs solaires. Son extrémité extérieure dépasse volontairement de `2` px le cercle de découpe de `225` px. Le groupe tourne selon la position calculée et l'image reçoit la rotation inverse pour rester droite. Le marqueur est placé sous l'overlay de mode ; il est donc visible uniquement sur le cadran complet.

## Conséquences

- La fonctionnalité n'ajoute ni permission, ni accès réseau, ni dépendance de production.
- Le nombre de complications reste fixé à huit.
- La précision couvre l'usage visuel du réticule sur la période 2020–2040 ; une nouvelle calibration devra être validée avant d'étendre cette plage.
- La formule WFF du marqueur et sa contre-rotation dupliquent volontairement la même expression, faute de variable ou de référence réutilisable en WFF version 1.
