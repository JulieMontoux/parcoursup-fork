Ce dépôt est utilisé par le Ministère de l'Enseignement Supérieur et de la Recherche (MESRI) afin de publier les algorithmes utilisés par la plateforme Parcoursup dans le cadre de la procédure nationale de préinscription pour l'accès aux formations initiales du premier cycle de l'enseignement supérieur https://www.parcoursup.fr.

Le dépôt contient les algorithmes et le code Java permettant

      - le calcul de l'ordre d'appel
      - le calcul des propositions de formations
      - le calcul des propositions d'hébergement en internat
      - l'application du dispositif "Meilleurs Bacheliers"
      - l'application du répondeur automatique.

Un document synthétique de présentation est accessible sur le site du MESRI : http://www.enseignementsup-recherche.gouv.fr/

Le dépôt contient également le code PL/SQL permettant
la vérification de certains des calculs effectués par l'implémentation Java.

Le dépôt est organisé en plusieurs dossiers:

    doc/presentation_algorithmes.pdf		présentation des algorithmes
    doc/implementation.txt			description synthétique de l'implémentation des algorithmes
    doc/exemples/				exemples au format XML
    java/					code Java
    plsql/					code PL/SQL

Calcul de l'ordre d'appel:

    java/parcoursup/prod/CalculOrdreAppelProd.java		procédure principale (main) utilisée dans Parcoursup
    java/parcoursup/ordreappel/algo/				implémentation Java de l'algorithme de calcul de l'ordre d'appel
    java/parcoursup/ordreappel/exemples/			exemples
    java/parcoursup/ordreappel/donnees/				accès aux données (Oracle ou XML)

Calcul des propositions à envoyer:

    java/parcoursup/prod/EnvoiPropositionsProd.java    	        procédure principale (main) utilisée dans Parcoursup
    java/parcoursup/propositions/algo/				implémentation Java de l'algorithme de calcul des propositions à envoyer
    java/parcoursup/propositions/meilleursbacheliers/		implémentation Java du dispositif "Meilleurs Bacheliers"
    java/parcoursup/propositions/repondeur/		        implémentation Java du répondeur automatique
    java/parcoursup/propositions/exemples/			exemples, y compris le générateur d'exemples aléatoires
    java/parcoursup/propositions/donnees/			accès aux donnees (Oracle ou XML)

Vérification des calculs:

    java/parcoursup/verification

Génération des données de la carte des formations:

    java/parcoursup/carte
    
Compilation:

    1. MAJ dépôt Maven : mvn install:install-file -Dfile=/opt/instantclient_12_2/ojdbc8.jar -DgroupId=com.oracle -DartifactId=ojdbc8 -Dversion=12.2.0.1 -Dpackaging=jar
    2. mvn package -P oracle

Usage:

    java -cp [ORACLE_INSTANT_CLIENT_PATH]/ojdbc8.jar:target/parcourssup-algo-[YEAR].[VERSION].jar parcoursup/prod/EnvoiPropositionsProd {TNSNAME} {USER} {PASSWD} {PATH_LOG_FILE}

Contact: parcoursup@enseignementsup.gouv.fr
