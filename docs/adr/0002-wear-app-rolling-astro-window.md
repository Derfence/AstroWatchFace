# ADR 0002: Fenêtre astro glissante dans wear-app

## Statut

Accepté

## Contexte

Le projet cible recommande un module `astro-core`, mais le dépôt actuel ne contient que `wear-app` et `watch-face`. Le cadran 24 h doit afficher les événements Soleil/Lune sans ajouter de logique dans la Face WFF, et sans attendre une extraction de module complète.

## Décision

Implémenter le premier incrément de calcul astronomique dans `wear-app`, dans un package Kotlin pur `astro`.

Le cadran 24 h conserve une échelle civile absolue : `00` reste en haut et les libellés restent les heures locales Europe/Paris. Les arcs affichent toutefois une fenêtre glissante de 24 h à partir du moment du rendu, avec un séparateur visuel fort à l'heure courante.

Astronomy Engine est utilisé comme dépendance locale pour les levers/couchers du Soleil et de la Lune, ainsi que les passages solaires à -18°.

## Conséquences

- `watch-face` reste sans code et continue seulement d'assembler les complications.
- Les calculs astro sont testables sans Android et pourront être déplacés vers `astro-core` plus tard.
- La complication 24 h demande une mise à jour toutes les 15 minutes, sous réserve des décisions de planification de Wear OS.
- L'application n'a pas besoin de réseau ni de géolocalisation dynamique pour cet incrément.
