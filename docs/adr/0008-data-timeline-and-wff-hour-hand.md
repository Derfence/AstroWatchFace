# ADR 0008: Timelines de complications et aiguille 24h WFF

## Statut

Accepté

## Contexte

Les complications image peuvent être rafraîchies avec plusieurs secondes ou plusieurs heures de retard, même quand l'application demande une période de mise à jour régulière. Cela rend le cadran principal, les positions célestes, le statut central et les modes astro trop dépendants du bon vouloir du système.

Les modes constellations et système solaire sont des modes de passage : l'utilisateur les consulte brièvement, puis revient au mode montre complet. Ils doivent donc rester peu coûteux à maintenir.

## Décision

Les complications image fournies par `wear-app`, sauf l'aiguille 24h, renvoient une `ComplicationDataTimeline`.

Le cadran principal, les positions célestes et le statut central utilisent une cadence de 10 minutes avec un horizon de 6 heures. Les modes constellations et système solaire utilisent une cadence de 24 heures avec un horizon de 48 heures.

L'aiguille 24h n'est plus une complication image. Elle est déclarée dans la WFF avec un `PartImage` utilisant le PNG transparent `hour_hand_bitmap`.

Les essais sur montre réelle ont isolé le défaut initial : le VectorDrawable `hour_hand` ne s'affichait pas dans un `PartImage`, tandis que sa version PNG s'affichait aux mêmes position et dimensions. Un second essai a confirmé que l'attribut statique `angle` et `Transform target="angle"` fonctionnent avec le PNG.

Le validateur WFF officiel a révélé que les attributs `isCustomizable` utilisaient les booléens Android en minuscules. WFF version 1 exige `TRUE` ou `FALSE` ; les quatre slots utilisent désormais `FALSE`.

Le `PartImage` final utilise un pivot `(0.5, 0.97)` placé au centre du cadran et applique `Transform target="angle" value="[HOUR_0_23_MINUTE] * 15"`. La source WFF fournit l'heure locale 0–23 combinée aux minutes ; le facteur 15 convertit les 24 heures en 360 degrés avec la précision à la minute.

La batterie ne fait plus partie du bitmap de statut central. Elle est rendue directement en WFF avec `[BATTERY_PERCENT]`, sous l'overlay de mode pour être masquée par les modes constellations et système solaire.

## Conséquences

- Les informations prévisibles restent lisibles même si Wear OS retarde le prochain appel au provider.
- Les modes astro de passage ne génèrent que deux frames quotidiennes par requête.
- Le slot et le service `Hour24hHandDataSourceService` disparaissent.
- Le VectorDrawable `hour_hand` est remplacé par un PNG transparent 18 x 140 compatible avec `PartImage` sur la montre testée.
- L'aiguille suit directement l'heure locale dans le runtime WFF, sans attendre de rafraîchissement de complication.
- Le build de diagnostic est supprimé après validation sur montre réelle.
