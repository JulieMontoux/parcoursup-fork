/* Copyright 2018, 2018 Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

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
package parcoursup.propositions;

import java.util.logging.Logger;

import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.donnees.ConnecteurDonneesPropositions;

/* Le calcul des propositions à envoyer est effectué par le code suivant.
Ce code est exécuté de manière quotidienne.
 */
public class EnvoiPropositions {

    private static final Logger LOGGER = Logger.getLogger(EnvoiPropositions.class.getName());

    private final ConnecteurDonneesPropositions acces;

    public EnvoiPropositions(ConnecteurDonneesPropositions acces) {
        this.acces = acces;
    }

    public void execute() throws Exception {

        LOGGER.info("Récupération des données");
        AlgoPropositionsEntree entree = acces.recupererDonnees();

        LOGGER.info("Sauvegarde locale de l'entrée");
        entree.serialiser(null);

        LOGGER.info("Calcul des propositions");
        AlgoPropositionsSortie sortie = AlgoPropositions.calculePropositions(entree);

        LOGGER.info("Sauvegarde locale de la sortie");
        sortie.serialiser(null);

        LOGGER.info("Export des données");
        acces.exporterDonnees(sortie);

    }
}
