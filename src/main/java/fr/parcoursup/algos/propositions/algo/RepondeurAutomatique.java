/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RepondeurAutomatique {

    private static final Logger LOGGER = Logger.getLogger(RepondeurAutomatique.class.getSimpleName());

    /**
     *
     * @param voeux tous les voeux de tous les candidats, ayant activé ou non leur répondeur
     * @param candidatsAvecRepondeurAutomatique liste des candidats ayant activé leur répondeur
     * @return le nombre de places libérées par le répondeur
     * @throws VerificationException en cas de problème de cohérence des données
     */
    public static long appliquerRepondeurAutomatique(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {

        Collection<Voeu> voeuxDesCandidatsAvecRepAuto =
                voeux.stream()
                        .filter(v ->
                                candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                                        && !v.estAffecteHorsPP()
                                        && (v.estEnAttenteDeProposition() || v.estProposition())
                        ).collect(Collectors.toList());

        /* L'entrée ne contient que des voeux de candidats
        ayant activé le répondeur automatique. Cette vérification reprend 7.4.
         */
        for (Voeu v : voeuxDesCandidatsAvecRepAuto) {
            if ((v.estEnAttenteDeProposition() && !v.getRepondeurActive())) {
                throw new VerificationException(VerificationExceptionMessage.REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG, v);
            }
        }

        /* Liste, indexée par id candidat, des voeux en attente et des propositions
        des candidats ayant activé le répondeur automatique. */
        Map<Integer, Integer> rangPreferenceMeilleurePropositionDuJour =
                voeuxDesCandidatsAvecRepAuto.stream()
                        //.filter(v -> v.estEnAttenteDeProposition() || v.estProposition())
                        .filter(Voeu::estPropositionDuJour)
                        .collect(Collectors.groupingBy(v -> v.id.gCnCod))
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream().mapToInt(v -> v.rangPreferencesCandidat).min().orElse(Integer.MAX_VALUE)
                        ));

        long placesLiberees = 0;
        /* candidat par candidat, on applique le répondeur automatique */
        for (Voeu v : voeuxDesCandidatsAvecRepAuto) {
            int rangMeilleureProposition = rangPreferenceMeilleurePropositionDuJour.getOrDefault(v.id.gCnCod, Integer.MAX_VALUE);
            if(v.rangPreferencesCandidat > rangMeilleureProposition) {
                if (v.estProposition()) {
                    placesLiberees++;
                }
                v.refuserAutomatiquementParApplicationRepondeurAutomatique();
            }
        }

        return placesLiberees;
    }

    private RepondeurAutomatique() {
    }

    /**
     *
     * @param entree les données d'entrée
     * @return le nombre de places liberees par le repondeur automatique
     * @throws VerificationException en cas d'incohérence des données
     */
    public static long appliquerRepondeurAutomatique(AlgoPropositionsEntree entree) throws VerificationException {
        if (!entree.candidatsAvecRepondeurAutomatique.isEmpty()) {
            LOGGER.log(Level.INFO, "{0} candidats ont activé le répondeur automatique",
                    entree.candidatsAvecRepondeurAutomatique.size()
            );
            final long placesLibereesParRepAuto
                    = appliquerRepondeurAutomatique(entree.voeux, entree.candidatsAvecRepondeurAutomatique);

            if (placesLibereesParRepAuto == 0) {
                LOGGER.info("Aucune place libérée par le répondeur automatique");
            } else {
                LOGGER.log(Level.INFO, "Le répondeur automatique a libéré {0} places",
                        placesLibereesParRepAuto);
            }

            return placesLibereesParRepAuto;
        } else {
            LOGGER.info("Aucun candidat n'a activé le répondeur automatique");
            return 0;
        }
    }

}
