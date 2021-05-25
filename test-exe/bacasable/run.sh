#!/bin/bash
# A lancer depuis le répertoire racine du projet avec : ./test-exe/bacasable/run.sh

set -e

mvn clean package -P bdd-h2 -DskipTests=true -P bdd-h2

mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelJson" -Dexec.args="./src/test/java/fr/parcoursup/algos/bacasable/algoOrdreAppelEntree.json /tmp/sortie.json"

mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsJson" -Dexec.args="./src/test/java/fr/parcoursup/algos/bacasable/algoPropositionsEntree.json /tmp/sortie.json"

mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelBdd"  -P bdd-h2

mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsBdd"  -P bdd-h2

mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsRepondeurBdd"  -P bdd-h2

echo "Tests exécutés avec succès."
