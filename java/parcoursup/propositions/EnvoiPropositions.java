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
package parcoursup.propositions;

import java.util.logging.Logger;
import parcoursup.donnees.Serialisation;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.donnees.ConnecteurDonneesPropositions;
import parcoursup.verification.VerificationsResultatsAlgoPropositions;

/* Le calcul des propositions à envoyer est effectué par le code suivant.
Ce code est exécuté de manière quotidienne.
 */
public class EnvoiPropositions {

    private static final Logger LOGGER = Logger.getLogger(EnvoiPropositions.class.getSimpleName());

    private final ConnecteurDonneesPropositions acces;

    public EnvoiPropositions(ConnecteurDonneesPropositions acces) {
        this.acces = acces;
    }

    public void execute(boolean logDonnees) throws VerificationException, AccesDonneesException {

        LOGGER.info("Récupération des données");
        AlgoPropositionsEntree entree = acces.recupererDonnees();

        if (logDonnees) {
            LOGGER.info("Sauvegarde locale de l'entrée");
            new Serialisation<AlgoPropositionsEntree>().serialiserEtCompresser(
                    entree,
                    AlgoPropositionsEntree.class);
        }

        LOGGER.info("Calcul des propositions");
        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        LOGGER.info("Verification des resultat");
        VerificationsResultatsAlgoPropositions verificationsResultats
                = new VerificationsResultatsAlgoPropositions();
        verificationsResultats.verifier(entree, sortie);

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

        LOGGER.info("Export des données");
        acces.exporterDonnees(sortie);

    }

}
