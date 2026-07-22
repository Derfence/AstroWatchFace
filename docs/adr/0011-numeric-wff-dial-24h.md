# ADR 0011 : Cadran 24 h numérique rendu par WFF

## Statut

Accepté, en attente de validation visuelle physique avant suppression de l'oracle bitmap.

## Contexte

Le slot 1 produisait douze bitmaps dynamiques `450 × 450` pour une timeline de
deux heures. Les arcs astronomiques variaient, mais le cercle, les graduations,
les libellés et les styles des marqueurs étaient redessinés dans chaque image.
Cette représentation consommait 9,72 Mo bruts par requête et sollicitait
théoriquement le provider douze fois par jour.

WFF peut alimenter ses primitives de dessin avec les champs numériques d'une
complication `RANGED_VALUE`. L'observateur reste fixe, les calculs restent
locaux et le fuseau civil reste `Europe/Paris`.

## Décision

Le slot `1` et `Dial24hDataSourceService` conservent leur identité, mais leur
contrat devient `RANGED_VALUE`. Trois nombres entiers exactement représentables
en `Float` transportent deux bornes de 11 bits chacun :

- `minimum` encode l'aube astronomique et le lever du Soleil ;
- `value`, décalé de `4 194 304`, encode le coucher du Soleil et le crépuscule
  astronomique ;
- `maximum`, décalé de `8 388 608`, encode le début et la fin de visibilité de
  la Lune.

Les codes `0..1439` représentent les minutes civiles, `2045` une journée
complète, `2046` l'instant courant et `2047` une borne absente. Le codec impose
`minimum < value < maximum`. La quantification à la minute limite l'erreur
angulaire à `0,125°`.

La timeline est événementielle et couvre dix heures. Elle change lorsqu'un
événement entre dans la fenêtre glissante de 24 h ou en sort, sans cadence
artificielle entre deux changements. Wear OS est invité à renouveler les
données toutes les huit heures, ce qui conserve deux heures de chevauchement.
Les payloads identiques partagent la même instance de
`RangedValueComplicationData`.

WFF décode les six bornes avec `floor`, `% 2048` et les décalages de bande. Il
dessine les arcs du jour, des crépuscules, de la nuit astronomique et de la
visibilité lunaire, les six marqueurs, le cercle extérieur, 24 graduations
horaires et 12 points correspondant aux pas de cinq minutes de l'aiguille des
minutes. Les graduations horaires multiples de trois restent plus longues et
portent les libellés civils sauf `00`, réservé à la batterie. `NOW` utilise
`[HOUR_0_23_MINUTE] * 15` sans marqueur, `ABSENT` masque l'arc et `FULL_DAY`
dessine un cercle complet. Aucune expression ne dépend de `[SECOND]`.

`AstroWindowCalculator` calcule l'altitude solaire une fois et l'altitude
lunaire une fois par projection. Les événements proviennent du cache
éphéméride partagé existant. Aucune permission, dépendance de production,
persistance ou donnée réseau n'est ajoutée.

## Déploiement

Le codec, la projection, le planner et leurs tests constituent la première
étape. La deuxième étape bascule simultanément le manifeste de `wear-app` et le
slot WFF. `Dial24hRenderer` reste temporairement disponible uniquement comme
oracle de comparaison. Il sera supprimé dans une troisième étape après une
observation de 24 à 48 h sur la montre réelle, couvrant midi, nuit, événements
solaires et lunaires, minuit et changement d'heure.

Si Wear OS ne réassocie pas automatiquement le provider après le changement de
type, la face doit être retirée puis ajoutée une fois. La publication reste
bloquée tant que la comparaison physique avec l'ancien rendu n'est pas validée.

## Conséquences

- Le slot 1 ne crée plus de bitmap dynamique ; la métrique attendue est
  `dial24hBitmapBytes=0`.
- Les sollicitations planifiées passent théoriquement de douze à trois par
  jour.
- La précision civile est d'une minute et la couverture de sécurité est de
  deux heures.
- Les deux APK doivent être mis à jour ensemble.
- Les décisions bitmap du slot 1 dans les ADR 0002, 0008 et 0009 sont
  remplacées par cette décision.
