# Projet AstroFace

## Résumé

AstroFace est une face de montre pour Samsung Galaxy Watch4 Classic affichant l'heure avec des aiguilles analogiques et des informations astronomiques calculées pour un point GPS fixe :

- Latitude : 45°39'56.5"N, soit 45.665694° N
- Longitude : 2°56'39.1"E, soit 2.944194° E
- Lieu de référence : point fixe en France métropolitaine, fuseau horaire civil attendu Europe/Paris
- Montre cible : Samsung Galaxy Watch4 Classic 46 mm, résolution de conception 450 x 450 px

La face ne doit pas afficher de météo. Les données astronomiques doivent être calculées localement autant que possible, sans dépendance Internet permanente. L'objectif est de produire une face lisible au poignet, utile pour l'observation du ciel, avec une esthétique astronomique claire plutôt qu'une surcharge d'informations.

La solution cible recommandée est hybride :

- Watch Face Studio / Watch Face Format pour le rendu principal de la face.
- Android Studio pour les calculs astronomiques, les tests, et une app Wear OS légère exposant les données à la face via complications ou données précalculées.
- App téléphone non retenue en première intention, car le point GPS est fixe, il n'y a pas de météo et les réglages utilisateur sont limités.

## Objectif utilisateur

L'utilisateur veut consulter rapidement, depuis sa montre :

- L'heure actuelle avec une lecture analogique classique.
- Les moments importants de la journée astronomique : lever/coucher du Soleil, lever/coucher de la Lune, début/fin de nuit astronomique.
- La phase de la Lune.
- La position utile de la Polaire sur l'anneau 12h.
- La position des planètes lorsqu'on regarde vers le Sud.
- Les constellations autour du zénith à minuit.

## Décisions actées

- La montre cible est une Galaxy Watch4 Classic 46 mm.
- Le cadran horaire principal reste analogique en 12h, avec aiguilles classiques.
- Les barres astronomiques utilisent un cadran extérieur en 24h.
- Les barres astronomiques représentent des périodes continues.
- Les heures exactes des événements ne sont pas affichées en texte ; elles se lisent uniquement via l'anneau 24h.
- Sur le cadran 24h, minuit est placé à 12h.
- Le cadran 24h garde une échelle civile absolue, mais les barres astronomiques représentent les 24 prochaines heures à partir du moment de rendu.
- Un séparateur visuel "maintenant" distingue le début de la fenêtre glissante du retour à +24h.
- Le cadran 24h affiche des repères horaires simples : petites barres perpendiculaires à chaque heure, chiffres toutes les 3h.
- Les icônes astronomiques peuvent être placées entre le cadran analogique 12h et le cadran extérieur 24h si le rendu est plus esthétique.
- Le "lever/coucher astronomique" désigne le passage du Soleil à -18° sous l'horizon.
- La position de la Polaire est une indication visuelle simple et esthétique, pas un outil de mise en station précis.
- La Polaire est représentée par un élément graphique simple, cercle ou étoile, placé sur l'anneau 12h.
- Uranus et Neptune doivent être affichées avec les autres planètes.
- Toutes les planètes restent visibles sur le cadran, même lorsqu'elles sont sous l'horizon.
- Les planètes ne changent pas d'apparence lorsqu'elles sont sous l'horizon.
- Les positions planétaires sont calculées en temps réel pour l'instant d'affichage.
- Les planètes sont positionnées uniquement par azimut ; leur hauteur n'est pas représentée.
- Le Soleil et la Lune sont ajoutés aux icônes de position avec les planètes.
- Les constellations affichées sont celles autour du zénith à minuit, avec le zénith placé au centre de la montre.
- Le rayon angulaire autour du zénith est fixé à 30° pour la première version.
- Toutes les constellations présentes dans ce rayon de 30° sont affichées.
- Les constellations sont affichées par leur tracé d'étoiles uniquement, sans nom.
- La phase de la Lune commence par un symbole simple.
- La date et la batterie de la montre sont affichées à l'intérieur de l'horloge.
- La date, la batterie et la phase de la Lune sont affichées au-dessus des constellations.
- La palette de base est noir, rouge et blanc.
- Les icônes astronomiques utilisent la couleur principale de l'objet représenté.
- Les icônes astronomiques sont de petites icônes graphiques, pas seulement des symboles textuels.
- L'anneau céleste est un cercle complet avec 12h = Sud et 3h = Ouest.
- La trotteuse est visible en mode actif.
- Le cadran 24h affiche des chiffres toutes les 3h en première intention.
- Le format de date retenu est de type "sam. 04 juil.".
- La batterie est affichée avec une icône et un pourcentage.
- Le style global visé est celui d'un instrument scientifique.
- La publication reste possible plus tard.
- En Always-On Display, le cadran conserve la parité fonctionnelle avec le mode actif, trotteuse et overlay astronomique compris.
- L'architecture cible doit privilégier une face + une app Wear OS associée, sans app téléphone en première version.

## Périmètre fonctionnel

### Inclus

- Face analogique avec aiguilles heures, minutes et secondes.
- Calculs astronomiques pour un seul point GPS fixe.
- Affichage des événements astronomiques sur le contour de l'horloge sous forme de barres colorées.
- Affichage d'icônes astronomiques sur le contour.
- Affichage interne de la phase de la Lune.
- Affichage interne des constellations autour du zénith à minuit.
- Mode Always-On Display à parité fonctionnelle avec le mode actif.
- Préparation d'une architecture permettant de tester les calculs indépendamment de l'interface.

### Exclus

- Météo.
- Géolocalisation dynamique.
- Choix de ville.
- Synchronisation Internet obligatoire.
- Compte utilisateur.
- Données communautaires ou sociales.
- Affichage cartographique complet du ciel.

### À décider plus tard

- Détail d'intégration entre la face et l'app Wear OS associée.
- Ajout éventuel d'une app Android téléphone dans une version ultérieure.
- Modalités exactes d'une publication future.
- Niveau de précision astronomique attendu.
- Stratégie exacte de calcul rapide des positions planétaires entre deux recalculs complets.

## Données astronomiques à calculer

### Soleil

Données souhaitées :

- Heure de lever du Soleil.
- Heure de coucher du Soleil.
- Début de la nuit astronomique, lorsque le Soleil passe sous -18° après le coucher.
- Fin de la nuit astronomique, lorsque le Soleil remonte au-dessus de -18° avant le lever.

Points d'attention :

- Les événements doivent être calculés pour la date locale.
- Les heures doivent être affichées en heure locale Europe/Paris, avec gestion automatique heure d'été / heure d'hiver.
- Il faut définir si l'altitude solaire utilisée pour lever/coucher inclut la réfraction atmosphérique standard et le rayon apparent du Soleil, généralement autour de -0.833°.

### Lune

Données souhaitées :

- Heure de lever de la Lune.
- Heure de coucher de la Lune.
- Phase de la Lune.
- Position de la Lune en azimut pour l'icône céleste.
- Éventuellement pourcentage d'illumination.
- Éventuellement âge de la Lune depuis la nouvelle lune.

Points d'attention :

- Certains jours, il peut ne pas y avoir de lever ou de coucher de Lune dans la journée civile locale.
- La Lune peut être déjà levée au début de la journée ou rester visible après minuit.
- Le design doit prévoir les cas sans événement dans la journée.

### Polaire

Donnée souhaitée :

- Position de la Polaire sur l'anneau 12h.

Interprétation fonctionnelle proposée :

- Calculer l'angle horaire de la Polaire autour du pôle céleste nord.
- Afficher cette position comme un cercle ou une petite étoile sur l'anneau 12h.

Points d'attention :

- Le rendu doit rester simple et graphique.
- L'affichage ne doit pas être traité comme un outil de mise en station d'une monture réelle.
- Le niveau de précision attendu est indicatif et esthétique.

### Planètes

Donnée souhaitée :

- Position des planètes, du Soleil et de la Lune lorsqu'on regarde vers le Sud.
- Convention demandée : 12h = Sud, 3h = Ouest.

Interprétation fonctionnelle proposée :

- Afficher toutes les planètes, le Soleil et la Lune sur le cadran.
- Convertir leur azimut en position autour d'un anneau de ciel.
- Ne pas représenter leur hauteur.
- Ne pas distinguer les objets sous l'horizon.
- Utiliser la convention suivante :
  - 12h : Sud
  - 3h : Ouest
  - 6h : Nord, probablement masqué ou secondaire si l'affichage représente seulement le ciel vers le Sud
  - 9h : Est

Objets à afficher :

- Soleil
- Lune
- Mercure
- Vénus
- Mars
- Jupiter
- Saturne
- Uranus
- Neptune

Points d'attention :

- Les objets sous l'horizon restent affichés exactement comme les autres.
- Les icônes utilisent la couleur principale de l'objet représenté.
- Les positions doivent être mises à jour pour l'instant courant.
- Une optimisation pourra être étudiée : calcul complet quotidien à heure fixe, puis calcul rapide au moment de l'affichage si cette approximation est pertinente et réellement différente en coût d'un calcul complet.

### Constellations

Donnée souhaitée :

- Une ou plusieurs constellations visibles à l'intérieur de l'horloge.

Interprétations possibles :

- Afficher les constellations autour du zénith à minuit.
- Placer le zénith au centre de la montre.
- Limiter l'affichage aux constellations situées dans un rayon angulaire de 30°.
- Afficher toutes les constellations trouvées dans ce rayon.
- Afficher uniquement le tracé d'étoiles, sans nom.

Recommandation initiale :

- Pour une première version, calculer les constellations proches du zénith à minuit et afficher toutes celles qui se trouvent dans le rayon de 30°.
- Utiliser un rayon initial de 30° autour du zénith, puis ajuster selon le rendu si nécessaire.

Points d'attention :

- Une vraie carte des constellations demande beaucoup de données stellaires.
- Le rendu doit rester lisible sur 450 px.
- L'objectif est à la fois informatif et esthétique, mais la lisibilité doit primer.

## Représentation visuelle proposée

### Structure générale

La face pourrait être organisée en couches :

1. Fond sombre, très sobre, compatible Always-On Display.
2. Cadran extérieur 24h pour les événements de temps.
3. Zone intermédiaire pour les icônes astronomiques, entre le 24h et le 12h si le rendu le permet.
4. Cadran analogique 12h avec index horaires.
5. Zone centrale avec constellations en arrière-plan.
6. Zone centrale supérieure avec phase de Lune, date et batterie.
7. Aiguilles analogiques au-dessus des informations de fond.

### Barres de couleurs sur le contour

Données affichées :

- Lever/coucher de la Lune.
- Lever/coucher du Soleil.
- Début/fin de nuit astronomique.

Recommandation :

- Utiliser un anneau temporel sur 24h.
- Chaque angle du contour correspond à une heure de la journée.
- Placer minuit à 12h.
- Afficher un repère court à chaque heure.
- Afficher des chiffres toutes les 3h en première intention.
- Les informations astronomiques sont affichées sous forme de périodes continues.
- Les périodes sont affichées comme arcs colorés.
- Les heures exactes ne sont pas affichées en texte ; elles se lisent par correspondance avec l'anneau 24h.

Exemple de couleurs à valider :

- Jour solaire : doré ou ambre.
- Nuit astronomique : bleu profond ou violet sombre.
- Lune visible : blanc cassé ou argent.
- Crépuscule astronomique : bleu moyen.

Décision :

- Le cadran extérieur est un cadran 24h absolu.
- 00h est placé en haut, à 12h.
- Le cadran analogique principal reste en 12h classique.

### Icônes sur le contour

Données affichées :

- Position de la Polaire sur l'anneau 12h.
- Position des planètes, du Soleil et de la Lune lorsqu'on regarde vers le Sud.

Point critique :

- Ces icônes ne représentent pas la même dimension que les barres :
  - Les barres représentent le temps.
  - Les planètes et la Polaire représentent une position dans le ciel.

Recommandation :

- Ne pas mélanger temps et azimut sur le même anneau sans distinction.
- Prévoir deux anneaux visuels :
  - Anneau temporel externe pour les événements de la journée.
  - Anneau céleste interne ou semi-anneau pour les positions dans le ciel.
- Étudier une disposition des icônes entre le cadran 12h et le cadran 24h, car cela peut mieux séparer les informations tout en gardant un rendu compact.
- Utiliser une couleur spécifique par objet : Soleil jaune/blanc chaud, Lune blanc froid, Mars rouge, Vénus blanc/ivoire, Jupiter orangé clair, Saturne jaune pâle, Uranus bleu-vert, Neptune bleu.
- Utiliser de petites icônes graphiques plutôt que des symboles astronomiques textuels seuls.
- Placer ces icônes sur un cercle céleste complet.
- Appliquer explicitement la convention d'orientation : 12h = Sud, 3h = Ouest, 6h = Nord, 9h = Est.

### Centre de l'horloge

Données affichées :

- Phase de la Lune.
- Date.
- Batterie de la montre.
- Constellations visibles.
- Éventuellement texte court : illumination lunaire ou heure de prochaine nuit astronomique.

Recommandation :

- Afficher une phase de Lune sous forme de symbole simple pour commencer.
- Placer les constellations en arrière-plan, avec un tracé rouge sombre ou blanc très atténué.
- Placer la phase de Lune, la date et la batterie au-dessus des constellations.
- Donner à ces trois informations des zones stables, avec contraste plus fort que les constellations.
- Éviter une image lunaire trop détaillée si elle devient illisible.
- Afficher au maximum une ou deux informations textuelles courtes en plus de la date et de la batterie.

### Hiérarchie visuelle centrale

Proposition :

- Les constellations forment une couche de fond, très fine, peu lumineuse, sans texte.
- La phase de Lune occupe une petite zone dédiée, par exemple en haut du centre ou à 6h interne selon l'équilibre avec les aiguilles.
- La date est affichée au format "sam. 04 juil.", en texte blanc compact, dans un cartouche discret ou sur une ligne isolée.
- La batterie est affichée avec une petite icône et un pourcentage, en blanc avec accent rouge si le niveau est bas.
- Les aiguilles passent au-dessus de tout, mais leur centre et leur largeur doivent éviter de masquer durablement la date, la batterie et la phase de Lune.
- Le rouge sert d'accent et de langage astronomique, mais le blanc reste réservé aux informations à lire rapidement.
- Les éléments centraux importants peuvent recevoir un léger masque noir semi-transparent derrière eux pour rester lisibles sans créer de gros pavés visuels.

## Mode Always-On Display

Objectif :

- Conserver les mêmes fonctionnalités qu'en mode actif.
- Garder l'overlay astronomique, le cadran 24h, la phase de Lune, la date, la batterie et la trotteuse visibles.
- Vérifier sur appareil réel que Wear OS ne force pas une limitation ambient non déclarée par le WFF.

## Architecture technique envisagée

### Option 1 : Watch Face Studio seulement

Avantages :

- Mise en place rapide.
- Bon outil visuel.
- Adapté à la composition graphique.

Limites :

- Calculs astronomiques complexes difficiles ou impossibles à maintenir proprement.
- Peu adapté aux tests automatisés.
- Peu adapté à une logique métier riche.

Conclusion :

- Insuffisant pour l'ambition complète du projet.

### Option 2 : Watch Face Studio + calculs préparés

Principe :

- La face reste construite avec Watch Face Studio.
- Les données sont calculées ailleurs puis intégrées sous forme de ressources ou de valeurs.

Avantages :

- Permet de garder WFS pour le design.
- Plus simple qu'une app complète.

Limites :

- Pas idéal si les données doivent être mises à jour automatiquement chaque jour.

Conclusion :

- Possible pour un prototype ou une version statique, pas idéal pour un usage quotidien.

### Option 3 : Projet Android Studio hybride

Principe :

- Module de calcul astronomique testable.
- App Wear OS légère pour calculer, mettre en cache et exposer les données.
- Watch face WFF pour l'affichage.
- App téléphone optionnelle, non prévue en V1.

Avantages :

- Architecture propre.
- Calculs testables.
- Évolutif.
- Pas besoin d'Internet pour les données astronomiques.

Limites :

- Mise en place plus longue.
- Demande plus de rigueur technique.

Conclusion :

- Recommandation cible pour le projet : une face + une app Wear OS associée, structurées dans un projet Android Studio avec un module de calcul partagé.

## Stratégie d'hybridation retenue

La recommandation actuelle est une architecture en deux morceaux côté produit installé :

1. Watch face
2. App Wear OS associée

L'app téléphone n'est pas nécessaire en V1.

Raison principale :

- Les coordonnées GPS sont fixes.
- La météo est exclue.
- Les données astronomiques peuvent être calculées hors ligne.
- Il n'y a pas encore de besoin fort de configuration depuis le téléphone.

L'app Wear OS associée aurait un rôle discret :

- Calculer les données astronomiques pour aujourd'hui et éventuellement les prochains jours.
- Mettre les résultats en cache.
- Fournir les données à la face.
- Servir de support technique pour des calculs plus avancés que ce que la face WFF sait faire seule.

L'app téléphone deviendrait pertinente seulement si un besoin apparaît :

- Configuration graphique confortable.
- Choix de plusieurs lieux d'observation.
- Profils de thèmes.
- Import/export de réglages.
- Aide utilisateur ou documentation embarquée.
- Synchronisation de données externes.

## Modules techniques recommandés

### astro-core

Responsabilités :

- Calculer les événements solaires.
- Calculer les événements lunaires.
- Calculer la phase de la Lune.
- Calculer la position azimutale du Soleil et de la Lune.
- Calculer la position de la Polaire.
- Calculer les positions planétaires.
- Calculer les constellations dans un rayon de 30° autour du zénith à minuit.
- Fournir des modèles de données simples et testables.

Contraintes :

- Aucun accès Android si possible.
- Tests unitaires faciles.
- Entrées explicites : date, heure, latitude, longitude, fuseau horaire.

État d'implémentation :

- Différé pour le premier incrément d'affichage Soleil/Lune.
- La logique de calcul vit provisoirement dans `wear-app`, dans un package Kotlin pur `astro`, afin de pouvoir être extraite plus tard sans changer le rendu.

### wear-app

Responsabilités :

- Planifier les recalculs quotidiens.
- Stocker un cache local.
- Exposer les données à la watch face.
- Gérer éventuellement les préférences simples.
- En première implémentation, calculer à chaque rendu du cadran 24h une fenêtre glissante de 24h pour lever/coucher du Soleil, passages à -18° et visibilité lunaire.

### watch-face

Responsabilités :

- Rendu analogique.
- Rendu des anneaux temporels.
- Rendu des icônes astronomiques.
- Rendu AOD.
- Lecture des données précalculées ou complications.

### mobile-app, optionnelle

Responsabilités possibles :

- Présenter une configuration confortable sur téléphone.
- Afficher une aide.
- Permettre des variantes de thème.
- Synchroniser les préférences avec la montre.

Non nécessaire en première version car le point GPS est fixe, la météo est exclue et les réglages sont encore limités.

## Besoin de calcul et précision

Niveau de précision recommandé pour une première version :

- Événements Soleil/Lune : précision à la minute.
- Phase de Lune : précision visuelle suffisante, pourcentage arrondi.
- Planètes, Soleil et Lune : azimut à l'instant courant, sans représentation de hauteur.
- Polaire : précision suffisante pour usage indicatif et esthétique.
- Constellations : affichage de toutes les constellations autour du zénith à minuit dans un rayon de 30°.

Questions à clarifier :

- Le calcul rapide des positions planétaires entre deux recalculs complets apporte-t-il un gain réel par rapport à un calcul complet à l'affichage ?
- Quelle disposition centrale choisir pour date, batterie et phase de Lune ?

## Critères d'acceptation proposés

- L'heure analogique est lisible en moins d'une seconde.
- Les informations astronomiques principales restent lisibles sur une Galaxy Watch4 Classic.
- Les calculs utilisent exclusivement le point GPS fixe fourni.
- Aucune donnée météo n'est affichée.
- Les heures sont affichées en heure locale Europe/Paris.
- Le mode Always-On Display reste lisible et conserve la parité fonctionnelle avec le mode actif.
- En Always-On Display, l'heure, la date, la batterie, la trotteuse, le cadran 24h, l'overlay astronomique et la phase de Lune restent affichés.
- Les cas sans lever/coucher de Lune dans la journée sont gérés sans erreur visuelle.
- Toutes les planètes, y compris Uranus et Neptune, sont présentes sur le cadran.
- Le Soleil et la Lune sont présents avec les icônes de position céleste.
- Les objets sous l'horizon n'ont pas de différence d'affichage.
- Les positions célestes utilisent l'azimut uniquement.
- Les constellations sont limitées à 30° autour du zénith à minuit et affichées par tracé uniquement.
- Toutes les constellations dans le rayon prédéfini de 30° sont affichées.
- Les barres astronomiques représentent des périodes continues.
- Les heures exactes des événements astronomiques ne sont pas affichées en texte.
- Le cadran extérieur 24h place minuit à 12h et affiche des repères horaires.
- Les chiffres du cadran 24h sont affichés toutes les 3h en première intention.
- La date utilise le format "sam. 04 juil.".
- La batterie affiche une icône et un pourcentage.
- Les icônes célestes sont de petites icônes graphiques colorées.
- L'anneau céleste est un cercle complet avec 12h = Sud, 3h = Ouest, 6h = Nord et 9h = Est.
- Le style visuel général évoque un instrument scientifique.
- Les calculs principaux sont couverts par des tests unitaires.
- La face reste utilisable sans connexion Internet.
- Le projet reste compatible avec une publication future.

## Risques et ambiguïtés

### Surcharge visuelle

Le projet combine heure analogique, arcs temporels, icônes planétaires, Polaire, phase lunaire et constellations. Sur un écran de 450 px, le risque principal est une face trop dense.

Mitigation :

- Prioriser les informations.
- Utiliser plusieurs niveaux de détails.
- Rendre certains éléments optionnels.

### Double lecture temps et direction

Le projet combine un cadran analogique 12h, un cadran extérieur 24h pour les barres, et des icônes dont la position correspond à une direction astronomique.

Mitigation :

- Séparer les anneaux.
- Utiliser des styles très distincts.
- Définir explicitement la convention de lecture.
- Placer les icônes dans une zone intermédiaire si cela améliore la lisibilité.

### Complexité astronomique

Les calculs précis peuvent devenir complexes.

Mitigation :

- Commencer avec un moteur simple mais testé.
- Définir le niveau de précision attendu.
- Éviter les dépendances Internet.

### Lisibilité AOD

Les visuels astronomiques détaillés peuvent mal se comporter en Always-On Display si Wear OS applique des contraintes ambient supplémentaires.

Mitigation :

- Garder la parité fonctionnelle dans le WFF.
- Valider sur appareil réel que l'overlay, la trotteuse et les informations astronomiques restent visibles en AOD.

## Questions de clarification

### Priorité produit

1. Quelle est l'information la plus importante après l'heure : Lune, Soleil, nuit astronomique, Polaire, planètes ou constellations ?
2. Entre usage pratique et esthétique, quel compromis visuel veux-tu si l'écran devient trop dense ?
3. Veux-tu une face dense en informations ou une face minimaliste avec seulement les signaux essentiels ?

### Matériel et usage

4. Question résolue : la montre cible est une Galaxy Watch4 Classic 46 mm.
5. Utilises-tu cette face surtout de jour, de nuit, ou pendant des sessions d'observation ?
6. La position fixe correspond-elle à ton lieu habituel d'observation ?

### Temps et anneaux

7. Question résolue : les barres utilisent un anneau 24h.
8. Question résolue : 00h est placé à 12h.
9. Souhaites-tu que l'anneau montre toute la journée civile ou seulement la période nuit/observation ?
10. Question résolue : les barres affichent des périodes continues.

### Soleil et nuit astronomique

11. Question résolue : "lever/coucher astronomique" correspond au passage du Soleil à -18°.
12. Veux-tu aussi afficher les crépuscules civil (-6°) et nautique (-12°), ou seulement astronomique (-18°) ?
13. Question résolue : les heures exactes se lisent uniquement via l'anneau 24h, sans texte dédié.

### Lune

14. Question résolue pour la V1 : phase de Lune sous forme de symbole simple.
15. Veux-tu afficher le pourcentage d'illumination ?
16. Veux-tu afficher l'âge de la Lune, par exemple J+7 après nouvelle lune ?
17. Si la Lune ne se lève ou ne se couche pas pendant la journée locale, comment veux-tu le représenter ?

### Polaire

18. Question résolue : la Polaire est un élément graphique simple, cercle ou étoile, sur l'anneau 12h.
19. Question résolue : l'affichage ne sert pas à aligner réellement une monture équatoriale.
20. Question résolue : la Polaire est affichée sur l'anneau 12h.

### Planètes

21. Question résolue : Uranus et Neptune doivent être affichées.
22. Question résolue : toutes les planètes restent visibles sur le cadran.
23. Question résolue : seule l'information d'azimut est affichée, pas la hauteur.
24. Question résolue : utiliser de petites icônes graphiques colorées, sans noms permanents.
25. Question résolue : utiliser un cercle complet avec 12h = Sud et 3h = Ouest.

### Constellations

26. Question résolue : constellations autour du zénith à minuit, zénith au centre de la montre.
27. Question résolue : afficher le tracé d'étoiles uniquement.
28. Question résolue : afficher toutes les constellations dans le rayon prédéfini de 30°.
29. Question résolue : rayon de 30° autour du zénith.

### Interface

30. Question résolue : esthétique d'instrument scientifique.
31. Question résolue : palette noir, rouge et blanc, avec icônes dans la couleur principale de l'objet.
32. Question résolue : trotteuse visible en mode actif.
33. Question résolue : afficher la date à l'intérieur de l'horloge.
34. Question résolue : afficher la batterie à l'intérieur de l'horloge.
35. Question résolue : date au format "sam. 04 juil.".
36. Question résolue : batterie affichée avec icône et pourcentage.

### Technique

37. Veux-tu que le projet reste modifiable principalement dans Watch Face Studio ?
38. Question résolue en intention : l'outil sera choisi selon l'architecture la plus adaptée.
39. Question résolue : publication possible plus tard.
40. Question résolue en première intention : les calculs doivent être faits hors ligne.
