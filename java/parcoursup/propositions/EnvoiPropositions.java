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

import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.algo.GroupeAffectation;
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

    public void execute(boolean logDonnees) throws Exception {

        LOGGER.info("Récupération des données");
        AlgoPropositionsEntree entree = acces.recupererDonnees();

        if (logDonnees) {
            LOGGER.info("Sauvegarde locale de l'entrée");
            entree.serialiser(null);
        }

        LOGGER.info("Calcul des propositions");
        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        LOGGER.info("Verification des resultat");
        String logFile = "EnvoiPropositionsErrors.log";
        VerificationsResultatsAlgoPropositions verificationsResultats
                = new VerificationsResultatsAlgoPropositions(logFile, false);
        verificationsResultats.verifier(entree, sortie);

        if (sortie.alerte) {
            LOGGER.info("La vérification a déclenché une alerte. Les groupes suivants seront ignorés lors de l'exportation: ");
            for (GroupeAffectation grp : sortie.groupesNonExportes) {
                LOGGER.info(grp.id.toString());
            }
            LOGGER.log(Level.INFO, "Veuillez consulter le fichier de log {0} pour plus de d\u00e9tails.", logFile);
        }

        if (sortie.avertissement) {
            LOGGER.info("La vérification a déclenché un avertissement.");
        }

        if (logDonnees) {
            LOGGER.info("Sauvegarde locale de la sortie");
            sortie.serialiser(null);
        }

        LOGGER.info("Export des données");
        acces.exporterDonnees(sortie);

    }

}
