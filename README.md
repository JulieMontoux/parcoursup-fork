Ce dépôt est utilisé par le Ministère de l'Enseignement Supérieur et de la Recherche (MESRI) afin de publier les algorithmes utilisés par la plateforme Parcoursup dans le cadre de la procédure nationale de préinscription pour l'accès aux formations initiales du premier cycle de l'enseignement supérieur https://www.parcoursup.fr.

### Description de la base de code

Le dépôt contient les algorithmes et le code Java permettant

      - le calcul de l'ordre d'appel
      - le calcul des propositions de formations
      - le calcul des propositions d'hébergement en internat
      - l'application du répondeur automatique.

Un document synthétique de présentation est accessible sur le site du MESRI : http://www.enseignementsup-recherche.gouv.fr/

Le dépôt contient également le code PL/SQL permettant
la vérification de certains des calculs effectués par l'implémentation Java.

Le dépôt est organisé en plusieurs dossiers :

    doc/presentation_algorithmes.pdf		présentation des algorithmes
    doc/implementation.txt			description synthétique de l'implémentation des algorithmes
    doc/exemples/				exemples au format XML
    src/main/java/					code Java
    src/test/java/					tests du code Java
    plsql/					code PL/SQL

Calcul de l'ordre d'appel:

    CalculOrdreAppelProd		procédure principale (main) utilisée dans Parcoursup
    fr.parcoursup.algos.ordreappel.algo.*		implémentation Java de l'algorithme de calcul de l'ordre d'appel
    fr.parcoursup.algos.ordreappel.exemples.*			exemples
    fr.parcoursup.algos.ordreappel.donnees.*				accès aux données (Oracle ou XML)

Calcul des propositions à envoyer :

    EnvoiPropositionsProd    	        procédure principale (main) utilisée dans Parcoursup
    fr.parcoursup.algos.propositions.algo.*				implémentation Java de l'algorithme de calcul des propositions à envoyer
    fr.parcoursup.algos.propositions.repondeur.*		        implémentation Java du répondeur automatique
    fr.parcoursup.algos.propositions.exemples.*			exemples, y compris le générateur d'exemples aléatoires
    fr.parcoursup.algos.propositions.donnees.*			accès aux donnees (Oracle ou XML)

Vérification des calculs:

    fr.parcoursup.algos.verification.*

Ce dépôt intègre également le code d'outils permettant de tester les algorithmes en mode "bac à sable" ainsi qu'une suite de tests unitaires.

### Paquets logiciels requis

Les paquets logiciels suivants sont requis pour la compilation du projet et la mise en oeuvre du bac à sable et de la suite de tests :

- java8
- maven

Un accès à un terminal graphique est également recommandé pour la consultation des rapports relatifs à la couverture de code.


### Base de données

Le code peut être mis en oeuvre dans un contexte "bac à sable"
permettant de tester localement le fonctionnement des algorithmes de Parcoursup.

Une partie de ces algorithmes nécessite un accès à des bases de données pouvant être déployées localement sous la forme
de bases **[H2](https://www.h2database.com)** ou **Oracle**, et dont les schémas sont disponibles dans le répertoire `./db-setup/`.

La **configuration proposée par défaut utilise le moteur H2** et ne nécessite donc pas de manipulations particulières pour la génération
des bases qui sont automatiquement créées ou mises à jour à l'exécution du code affairant. Celles-ci sont alors enregistrées sur le chemin
`./h2/bacasable.mv.db` et `./h2/tests.mv.db` et on se référera au contenu du fichier pom.xml pour en récupérer les identifiants de connexion,
pour un accès direct via la console web H2 par exemple.

Il est également possible d'utiliser un backend BDD alternatif basé sur le serveur gratuit *Oracle Database 18c Express Edition* pouvant
être exécuté localement au travers d'un service Docker. Les utilisateurs intéressés par cette solution sont invités à consulter le fichier
`./db-setup/setup-oracle.md`.


### Compilation du projet

Le projet peut être compilé dans sa configuration par défaut (pour des interactions, donc, avec des bases de données **H2** générées
automatiquement), via la commande :

```
mvn clean package
```

qui est équivalente à :

```
mvn clean package -P bdd-h2
```

À noter : la génération du package prend en compte ici, par défaut, le résultat des **tests unitaires associés** (il y aura donc échec global
si au moins l'un des tests échoue). Pour une génération plus rapide sans exécution des tests, on effectuera :

```
mvn clean package -DskipTests=true
```

La procédure à suivre pour l'utilisation du SGBD **Oracle** passe quant à elle par les étapes suivantes :

1/ récupérer le fichier [https://maven.xwiki.org/externals/com/oracle/jdbc/ojdbc8/12.2.0.1/ojdbc8-12.2.0.1.jar](https://maven.xwiki.org/externals/com/oracle/jdbc/ojdbc8/12.2.0.1/ojdbc8-12.2.0.1.jar) (driver JDBC Oracle) puis le déposer à la racine du projet.

2/ installer ce fichier dans le dépôt local :

```
mvn install:install-file -Dfile=./ojdbc8-12.2.0.1.jar -DgroupId=com.oracle -DartifactId=ojdbc8 -Dversion=12.2.0.1 -Dpackaging=jar
```

(note : le fichier est alors "mis en cache" par Maven à un emplacement qui peut varier mais qui est typiquement, sous Linux : `~/.m2/repository/com/oracle/ojdbc8/`)

3/ compiler le projet (ici sans exécution des tests unitaires associés) :

```
mvn clean package -P bdd-oracle -DskipTests=true
```

Pour compiler le projet sous le profil `bdd-oracle` en prenant en compte cette fois-ci le résultat des tests unitaires, ne pas oublier
de lancer préalablement le service Docker associé (se référer à la [documentation](./db-setup/setup-oracle.md) liée)
puis effectuer :

```
mvn clean package -P bdd-oracle
```

### Tests unitaires

Le projet intègre une suite de tests relatifs au fonctionnement des algorithmes au coeur de l'application (ordreappel, propositions)
ainsi qu'aux couches d'accès aux données BDD.

Ces tests sont automatiquement exécutés lors de la compilation du projet avec :

```
mvn clean package -P bdd-h2
```

ou bien :

```
mvn clean package -P bdd-oracle
```

Pour n'intégrer qu'un sous-ensemble de tests donnés (associés à un package particulier par exemple) :

```
mvn clean package "-Dtest=fr.parcoursup.algos.ordreappel.algo.**"
```

ou bien :

```
mvn clean package -Dtest=fr.parcoursup.algos.propositions.donnees.** -P bdd-oracle
```

Pour le test d'une classe particulière :

```
mvn clean package -Dtest=fr.parcoursup.algos.ordreappel.algo.TestOrdreAppel
```

Pour obtenir et consulter les métriques relatives à la couverture de code :

```
mvn clean package -P bdd-h2,metriques-clover
```

ou bien :

```
mvn clean package -P bdd-oracle,metriques-clover
```

Le rapport généré est alors disponible dans le répertoire `target/site/clover/` (fichier `index.html`).


### Bac à sable

Le dépôt comprend un espace d'expérimentation nommé "**bac à sable**", visant à fournir un moyen simple de tester et d'appréhender le fonctionnement des algorithmes de Parcoursup.

Celui-ci comprend des **exemples de programmes illustrant l'utilisation des APIs** permettant le **calcul de l'ordre d'appel** et de **l'ordre d'envoi des propositions de formation**.

Le fichier [bac-a-sable.md](bac-a-sable.md) fournit plus d'informations à ce sujet.


Contact: parcoursup@enseignementsup.gouv.fr
