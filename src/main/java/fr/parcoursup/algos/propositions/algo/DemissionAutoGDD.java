/* Copyright 2022 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation, Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Cette classe contient l'implémentation de la démission automatique des voeux archivés en GDD.
 * Cela s'applique aux candidats qui n'ont pas activé leur répondeur automatique.
 *
 * Si un candidat qui n'a pas activé son répondeur automatique
 * reçoit une proposition sur un voeu clôturé,
 * et si ce voeu a un rang de préférence
 * alors le candidat démissionne automatiquement de tous ses autres voeux
 * en attente et de rang strictement supérieurs dans l'ordre de préférence du candidat,
 * ainsi que des propositions auxquelles il n'a pas encore donné de réponse.
 *
 * Voir le document de proésenations des algorithmes pour plus de détails.
 */
public class DemissionAutoGDD {

    private static final Logger LOGGER = Logger.getLogger(DemissionAutoGDD.class.getSimpleName());

    /**
     * Applique la démission auto des voeux archivés en GDD.
     * @param entree les données d'entrée
     * @return le nombre de places libérées
     * @throws VerificationException en cas de problème d'intégrité des données d'entrée
     */
    static long appliquerDemissionAutomatiqueGDD(AlgoPropositionsEntree entree) throws VerificationException {

        long nbCandidatsAvecVoeuxArchives =
                entree.voeux.stream()
                        .filter(Voeu::getEstArchive)
                        .mapToInt(v -> v.id.gCnCod).distinct().count();
        if (nbCandidatsAvecVoeuxArchives > 0) {
            LOGGER.log(Level.INFO, "Préparation des données e la démission automatique en GDD,"
                            + "{0} candidats ont au moins un voeu archivé.",
                    nbCandidatsAvecVoeuxArchives
            );

            final long placesLibereesParDemissionAuto
                    = appliquerDemissionAutomatiqueGDD(entree.voeux, entree.candidatsAvecRepondeurAutomatique);

            if (placesLibereesParDemissionAuto == 0) {
                LOGGER.info("Aucune place libérée par la démission automatique en GDD");
            } else {
                LOGGER.log(Level.INFO, "La démission automatique en GDD a libéré {0} places",
                        placesLibereesParDemissionAuto);
            }
            return placesLibereesParDemissionAuto;
        } else {
            LOGGER.info("Pas d'application de la démission auto en GDD car " +
                    "aucun candidat n'a de voeu archivé.");
            return 0;
        }
    }

    /**
     * Applique la démission auto des voeux archivés en GDD.
     *
     * @param voeux la liste des voeux archivés
     * @param candidatsAvecRepondeurAutomatique candidats ayant activé le répondeur automatique
     * @return nombre de places libérées
     * @throws VerificationException en cas de problème d'intégrité des données d'entrée
     */
    static long appliquerDemissionAutomatiqueGDD(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {

        /* associe à chaque candidat (indexé par son gCnCod) le meilleur rang des propositions sur des voeux archivés
        parmi celles générées le jour mêmes */
        Map<Integer, Integer> rangMeilleurePropositionDuJour =
                voeux.stream()
                        .filter(v -> v.estPropositionDuJour()
                                && v.getEstArchive()
                                && v.getRangPreferencesCandidat() > 0
                                && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod))
                        .collect(Collectors.groupingBy(v -> v.id.gCnCod))
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream().mapToInt(Voeu::getRangPreferencesCandidat).min().orElse(Integer.MAX_VALUE))
                        );

        long placesLiberees = 0;

        for (Voeu v : voeux) {
            int rangMeilleureProposition = rangMeilleurePropositionDuJour.getOrDefault(v.id.gCnCod, Integer.MAX_VALUE);
            //On conserve une proposition à laquelle le candidat a déjà répondu.
            // Parmi les voeux moins bien classés dans l'ordre de préférence du candidat,
            // on en conserve que les (en fait la) proposition dèja accepté, si il en existe.
            // On refuse automatiquement les voeux moins bien classés dans l'ordre de préférence du candidat:
            //    - les voeux en attente de proposition
            //    - les propositions en attente de réponse
            //    - les propositions du jour.
            if (v.getRangPreferencesCandidat() > rangMeilleureProposition
                    && (v.estEnAttenteDeProposition()
                        || v.estPropositionDuJour()
                        || ( v.estPropJoursPrecedentsEnAttenteDeReponseCandidat() && !v.estAffecteHorsPP() ) )
            ) {
                if(v.estProposition()) {
                    placesLiberees++;
                }
                v.refuserAutomatiquementParApplicationDemissionGdd();
            }
        }

        return placesLiberees;
    }


    private DemissionAutoGDD() {

    }

}
