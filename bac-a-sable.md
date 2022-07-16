Le dépôt comprend un espace d'expérimentation nommé "**bac à sable**", visant à fournir un moyen simple de tester et d'appréhender le fonctionnement des algorithmes de Parcoursup.

Le bacasable inclut quelques exemples de programmes **illustrant l'utilisation des APIs à différents niveaux**, de la **mise en oeuvre des algorithmes "coeur"** avec entrées / sorties sérialisées dans des fichiers JSON, à une **interaction de plus haut niveau avec la base de données**.

## Exemples avec entrées / sorties sérialisées dans des fichiers JSON

La mise en oeuvre des algorithmes "coeur" de calcul de l'ordre d'appel et de détermination de la liste des propositions à envoyer quotidiennement, durant la campagne, est illustrée au travers des programmes `DemoOrdreAppelJson` et `DemoPropositionsJson`.
Ces programmes prennent en entrée des **données sérialisées dans un fichier JSON** et enregistrent leur sortie également dans un fichier JSON.

Le programme [DemoOrdreAppelJson](src/main/java/fr/parcoursup/algos/bacasable/ordreappel/DemoOrdreAppelJson.java) se base sur un exemple décrit dans le **[document de présentation des algorithmes de Parcoursup 2019](doc/presentation_algorithmes_parcoursup_2019.pdf)**, **point 4.1**.

Il prend en argument le chemin d'un fichier JSON ([exemple](src/test/java/fr/parcoursup/algos/bacasable/algoOrdreAppelEntree.json)) On le lance en effectuant :

```bash
mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelJson" -Dexec.args="./src/test/java/fr/parcoursup/algos/bacasable/algoOrdreAppelEntree.json test-exe/tmp/sortie.json"
cat test-exe/tmp/sortie.json
```

Le programme [DemoPropositionsJson](src/main/java/fr/parcoursup/algos/bacasable/propositions/DemoPropositionsJson.java) donne un aperçu du modèle de données mis en oeuvre pour les calculs liés à l'envoi des propositions.

Il prend en argument le chemin d'un fichier JSON ([exemple](src/test/java/fr/parcoursup/algos/bacasable/algoPropositionsEntree.json)), on le lance en effectuant :

```bash
mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsJson" -Dexec.args="./src/test/java/fr/parcoursup/algos/bacasable/algoPropositionsEntree.json test-exe/tmp/sortie.json"
cat test-exe/tmp/sortie.json
```

## Exemples avec entrées / sorties en base de données

Les programmes mis en en production prennent leurs entrées et enregistrent leurs sorties dans une base de données.

Ceux-ci prennent appui sur une API permettant **l'extraction des données et l'exportation des résultats**, 
dans un contexte incluant de multiples informations liées au **paramétrage des candidatures, formations, internats**, etc.

Trois programmes donnent ici un aperçu du modèle de données sous-jacent et des modalités de mise en oeuvre des algorithmes dans ce contexte précis.

Le programme [DemoOrdreAppelBdd](src/main/java/fr/parcoursup/algos/bacasable/ordreappel/DemoOrdreAppelBdd.java) reproduit le scénario décrit par `DemoPropositionsJson`, avec une interaction possible, au choix et en sélectionnant le bon profil Maven, avec une base de données **H2** (natif java) ou **Oracle** (via Docker, voir les fichiers [README](README.md) et [setup-oracle.md](db-setup/setup-oracle.md)).

Avec la base de données H2 proposée par défaut, on lance le programme, après avoir préalablement compilé le projet avec `mvn clean package`, en effectuant :

```
mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelBdd"
```

ou bien directement (sans compilation préalable du projet) :

```
mvn clean activejdbc-instrumentation:instrument exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelBdd"
```

Ce programme effectue séquentiellement :

- l'enregistrement des données d'entrée correspondant au scénario décrit
- le calcul de l'ordre d'appel
- la récupération et l'affichage du résultat

Les données d'entrée sont **enregistrées séquentiellement par l'intermédiaire d'objets ORM** mappant les concepts et relations entre entités représentées en base de données. 
La séquence suivie découle des relations liant les tables entre elles, l'ordre d'insertion devant être respecté pour aboutir à un jeu d'entrée cohérent et complet.
Les commentaires figurant dans le code donnent plus de précisions à ce sujet.

À noter : la bibliothèque de mappage objet relationnel proposée dans le package `fr.parcoursup.algos.bacasable.peuplementbdd` est principalement destinée à faciliter l'enregistrement d'informations en base de données (objets -> BDD). Les fonctionnalités liées à l'extraction de données (BDD -> objets) ne sont en revanche que partiellement implémentées.

Deux autres programmes d'exemples, codés selon le même principe, permettent de tester l'**algorithme de calcul d'envoi des propositions de formation**.

Le programme [DemoPropositionsBdd](src/main/java/fr/parcoursup/algos/bacasable/propositions/DemoPropositionsBdd.java) décrit un scénario très basique, n'incluant que 2 formations, 2 candidats et 5 voeux. Son objectif principal est d'illustrer les modalités d'enregistrement des conditions
initiales, d'appel / exécution de l'algorithme puis d'extraction du résultat avant envoi des nouvelles propositions aux candidats.

On le lance directement en effectuant :

```
mvn clean activejdbc-instrumentation:instrument exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsBdd"

```

Le programme [DemoPropositionsRepondeurBdd](src/main/java/fr/parcoursup/algos/bacasable/propositions/DemoPropositionsRepondeurBdd.java) décrit les conditions d'utilisation du répondeur automatique, selon un scénario là encore très basique n'incluant qu'un candidat, deux formations et 4 voeux.

À lancer directement avec :

```
mvn clean activejdbc-instrumentation:instrument exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsRepondeurBdd"

```

Ce programme illustre les modalités de peuplement de la base de données et de récupération des résultats, en fournissant un modèle utilisable pour des programmes de test / démonstration plus ambitieux. On peut se référer aux commentaires pour plus de précisions sur son implémentation.

En complément de ces éléments la documentation relative à la [structure de la base de données](doc/structure-bdd.md) explicite la structure des tables utilisées par les algorithmes.
