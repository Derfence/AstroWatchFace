# ADR 0010 : Mouvement céleste interpolé dans WFF

## Statut

Accepté

## Contexte

La couche des positions célestes produisait douze bitmaps `340 × 340` pour une
timeline de deux heures. Seuls les azimuts variaient : les icônes, les queues et
leurs orbites étaient redessinées alors que leur apparence était statique.

WFF limite une face à huit slots de complication. Après retrait du slot bitmap,
trois slots restent disponibles pour transporter les neuf astres.

## Décision

Les positions sont calculées au début et à la fin de chaque intervalle de dix
minutes. Un cache LRU synchronisé, partagé par les trois providers, est indexé
par observateur et instant afin qu'une borne ne soit calculée qu'une fois.

Les astres sont répartis dans trois complications `RANGED_VALUE` : Soleil/Lune/
Mercure, Vénus/Mars/Jupiter et Saturne/Uranus/Neptune. Les champs minimum,
valeur et maximum transportent chacun un angle initial et le plus court delta
circulaire. Un pas de `0,2°` permet de conserver les trois valeurs sous `2²⁴`
tout en respectant l'ordre obligatoire `minimum < valeur < maximum`.

WFF décode ces valeurs et interpole l'angle avec `([MINUTE] % 10) / 10`. Le
groupe contenant la queue et l'icône pivote autour du centre ; l'icône applique
la rotation opposée pour conserver son orientation. Aucun accès à `[SECOND]`
n'est utilisé afin d'éviter une réévaluation chaque seconde.

## Conséquences

- La face utilise exactement huit complications, dont trois numériques pour
  les positions.
- Les timelines célestes ne créent plus de bitmap dynamique.
- Une requête alignée produit douze entrées et treize snapshots partagés ; une
  requête non alignée produit treize entrées et quatorze snapshots partagés.
- La quantification ajoute au plus `0,1°` par valeur. Les tests saisonniers au
  lieu d'observation par défaut maintiennent l'erreur interpolée sous `0,3°`.
- Le mouvement progresse à la minute en mode actif comme en AOD.
- Le changement de fuseau est pris en compte lors du prochain rafraîchissement
  des complications. Aucun cache ni bitmap dérivé n'est persisté.
