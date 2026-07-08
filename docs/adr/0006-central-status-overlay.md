# ADR 0006: Couche centrale date, batterie et phase de Lune

## Statut

Accepté

## Contexte

Le projet demande d'afficher la date, le niveau de charge et la phase de la Lune à l'intérieur de l'horloge, au-dessus des constellations. Ces informations ne relèvent ni du cadran temporel 24 h, ni de l'overlay directionnel des positions célestes.

La batterie nécessite un accès Android local, alors que le cadran 24 h reste centré sur les calculs astronomiques et le fond de constellations.

## Décision

Ajouter une complication image plein écran dédiée, `StatusOverlayDataSourceService`, fournie par `wear-app` et assemblée par `watch-face`.

Cette couche rend un bitmap transparent 450 x 450 contenant uniquement les informations centrales immobiles :

- phase de Lune sous forme de disque à ombre continue ;
- date locale Europe/Paris au format compact français, par exemple `sam. 04 juil.` ;
- batterie sous forme d'icône seule, avec accent rouge à partir de 20 %.

La couche est placée dans `watchface.xml` après l'overlay des positions célestes et avant l'aiguille 24 h. Les aiguilles restent donc au-dessus de toutes les informations centrales.

La phase de Lune et la batterie sont placées de part et d'autre de l'indicateur `00` du cadran 24 h. La date est placée sous le centre de la montre, avec un décalage vertical initial de `60f`. Aucun fond semi-transparent n'est dessiné derrière ces informations.

La phase de Lune utilise Astronomy Engine déjà présent dans le projet. La phase affichée est celle du prochain lever de Lune suivant le dernier coucher de Lune connu. La donnée de complication est déclarée valide jusqu'au prochain coucher de Lune, afin que la cible bascule à ce moment vers le lever suivant. Le niveau de charge est lu localement via Android, sans permission supplémentaire et sans réseau.

## Conséquences

- `watch-face` reste sans code et continue d'assembler des complications image.
- La logique Android de batterie reste isolée dans `wear-app`.
- La date, la batterie et la phase de Lune peuvent évoluer visuellement sans mélanger les responsabilités du cadran 24 h.
- La fréquence de mise à jour déclarée reste de 15 minutes pour la fraîcheur de la date et de la batterie, tandis que la phase lunaire porte une validité jusqu'au prochain coucher de Lune.
- Le pourcentage d'illumination lunaire est calculé mais non affiché en V1, pour conserver un rendu central compact.
- Le pourcentage de batterie n'est plus affiché ; les futures variations de l'icône porteront l'information détaillée.
