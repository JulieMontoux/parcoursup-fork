/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation,
    Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

    This file is part of Algorithmes-de-parcoursup.

    Algorithmes-de-parcoursup is free software: you can redistribute it and/or modify
    it under the terms of the Affero GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Algorithmes-de-parcoursup is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Affero GNU General Public License for more details.

    You should have received a copy of the Affero GNU General Public License
    along with Algorithmes-de-parcoursup.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.parcoursup.algos.propositions;

import fr.parcoursup.algos.donnees.Serialisation;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.AlgoPropositions;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositions;

import java.util.logging.Logger;

/* Le calcul des propositions à envoyer est effectué par le code suivant.
Ce code est exécuté de manière quotidienne.
 */
public class EnvoiPropositions {

    private static final Logger LOGGER = Logger.getLogger(EnvoiPropositions.class.getSimpleName());

    private final ConnecteurDonneesPropositions input;

    private final ConnecteurDonneesPropositions output;

    public EnvoiPropositions(ConnecteurDonneesPropositions input, ConnecteurDonneesPropositions output) {
        this.input = input;
        this.output = output;
    }

    public void execute(boolean logDonnees) throws VerificationException, AccesDonneesException {
        execute(logDonnees, true);
    }

    public void execute(boolean logDonnees, boolean exporterResultats) throws VerificationException, AccesDonneesException {

        LOGGER.info("Récupération des données");
        AlgoPropositionsEntree entree = input.recupererDonnees();

        if (logDonnees) {
            LOGGER.info("Sauvegarde locale de l'entrée");
            new Serialisation<AlgoPropositionsEntree>().serialiserEtCompresser(
                    entree,
                    AlgoPropositionsEntree.class);
        }

        LOGGER.info("Calcul des propositions");
        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        if (sortie.getAlerte()) {
            LOGGER.info(sortie.getAlerteMessage());
        }

        if (sortie.getAvertissement()) {
            LOGGER.info("La vérification a déclenché un avertissement.");
        }

        if (logDonnees) {
            LOGGER.info("Sauvegarde locale de la sortie");
            new Serialisation<AlgoPropositionsSortie>().serialiserEtCompresser(
                    sortie,
                    AlgoPropositionsSortie.class);
        }

        if (exporterResultats) {
            LOGGER.info("Export des données");
            output.exporterDonnees(sortie);
        }
    }

}
