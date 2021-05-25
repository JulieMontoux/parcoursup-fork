#!/bin/bash

set -e

mvn clean package -DskipTests=true -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.classpathScope=test -Dexec.mainClass="fr.parcoursup.algos.prod.PreparationBddOracleOrdreAppel" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="fr.parcoursup.algos.prod.CalculOrdreAppelOracle" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.classpathScope=test -Dexec.mainClass="fr.parcoursup.algos.prod.PreparationBddOracleOrdreAppel" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="fr.parcoursup.algos.prod.CalculOrdreAppelProd" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.classpathScope=test -Dexec.mainClass="fr.parcoursup.algos.prod.PreparationBddOraclePropositions" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="fr.parcoursup.algos.prod.EnvoiPropositionsOracle" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.classpathScope=test -Dexec.mainClass="fr.parcoursup.algos.prod.PreparationBddOraclePropositions" -P bdd-oracle

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="fr.parcoursup.algos.prod.EnvoiPropositionsProd" -P bdd-oracle
