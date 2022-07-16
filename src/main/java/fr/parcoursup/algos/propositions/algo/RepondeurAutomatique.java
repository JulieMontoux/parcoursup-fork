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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Cette classe contient l'implémentation du répondeur automatique.
 *
 * Un candidat ayant activé son répondeur automatique
 * et recevant une nouvelle proposition démissionne automatiquement des propositions
 * antérieures et des voeux en attente qui ont un rang plus élevé dans son ordre de préférence.
 *
 * Voir le document de présetation des algorithmes pour plus de détails.
 */
public class RepondeurAutomatique {

    private static final Logger LOGGER = Logger.getLogger(RepondeurAutomatique.class.getSimpleName());

    /**
     * Applique le répondeur automatique.
     *
     * @param entree les données d'entrée
     * @return le nombre de places libérées par le repondeur automatique
     * @throws VerificationException en cas de problème d'intégrité des données d'entrée
     */
    static long appliquerRepondeurAutomatique(AlgoPropositionsEntree entree) throws VerificationException {
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

    /**
     * Applique le répondeur automatique pour les candidats l'ayant activé.
     *
     * @param voeux                             tous les voeux de tous les candidats, ayant activé ou non leur répondeur
     * @param candidatsAvecRepondeurAutomatique liste des candidats ayant activé leur répondeur
     * @return le nombre de places libérées par le répondeur
     * @throws VerificationException en cas de problème d'intégrité des données d'entrée
     */
    static long appliquerRepondeurAutomatique(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {

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
                        .filter(Voeu::estPropositionDuJour)
                        .collect(Collectors.groupingBy(v -> v.id.gCnCod))
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream().mapToInt(Voeu::getRangPreferencesCandidat).min().orElse(Integer.MAX_VALUE)
                        ));

        Set<Integer> candidatsAvecRepAutoEtPropCeJour = rangPreferenceMeilleurePropositionDuJour.keySet();

        long placesLiberees = 0;
        /* démission automatique des voeux moins bien classés qu'une nouvelle proposition
        et des anciennes propositions */
        for (Voeu v : voeuxDesCandidatsAvecRepAuto) {
            int gCnCod = v.id.gCnCod;
            if (candidatsAvecRepAutoEtPropCeJour.contains(gCnCod)) {
                int rangMeilleureProposition = rangPreferenceMeilleurePropositionDuJour.get(gCnCod);
                if (v.aEteProposeJoursPrecedents() || v.getRangPreferencesCandidat() > rangMeilleureProposition) {
                    if (v.estProposition()) {
                        placesLiberees++;
                    }
                    v.refuserAutomatiquementParApplicationRepondeurAutomatique();
                }
            }
        }

        return placesLiberees;
    }

    private RepondeurAutomatique() {
    }


}
