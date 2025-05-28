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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe contient l'implémentation du répondeur automatique.
 * <p>
 * Un candidat ayant activé son répondeur automatique
 * et recevant une nouvelle proposition démissionne automatiquement des propositions
 * antérieures et des voeux en attente qui ont un rang plus élevé dans son ordre de préférence.
 * <p>
 * Voir le document de présetation des algorithmes pour plus de détails.
 */
public class RepondeurAutomatique {

    private static final Logger LOGGER = Logger.getLogger(RepondeurAutomatique.class.getSimpleName());

    /**
     * Applique le répondeur automatique pour les candidats l'ayant activé.
     *
     * @param voeuxDesCandidatsAvecRepAuto                tous les voeux des candidats ayant activéleur répondeur, qui étaient initialement en attente ou proposition
     * @param candidatsAvecRepondeurAutomatique           liste des candidats ayant activé leur répondeur
     * @param statuts                                     les statuts des voeux
     * @param rangPreferenceMeilleurePropositionDuJour    les rangs minimaux des propositions du jour, par candidat
     * @return la liste des voeux démissionnés automatiquement
     * @throws VerificationException en cas de problème d'intégrité des données d'entrée
     */
    static List<Voeu> appliquerRepondeurAutomatique(
            Collection<Voeu> voeuxDesCandidatsAvecRepAuto,
            Set<Integer> candidatsAvecRepondeurAutomatique,
            StatutsVoeux statuts,
            Map<Integer, Integer> rangPreferenceMeilleurePropositionDuJour
    ) throws VerificationException {

        if (!candidatsAvecRepondeurAutomatique.isEmpty()) {
            LOGGER.log(Level.INFO, "{0} candidats ont activé le répondeur automatique",
                    candidatsAvecRepondeurAutomatique.size()
            );

            Set<Integer> candidatsAvecRepAutoEtPropCeJour = rangPreferenceMeilleurePropositionDuJour.keySet();

            List<Voeu> placesLiberees = new ArrayList<>();

        /* démission automatique des voeux moins bien classés qu'une nouvelle proposition
        et des anciennes propositions */
            for (Voeu v : voeuxDesCandidatsAvecRepAuto) {
                int gCnCod = v.id.gCnCod;
                if (candidatsAvecRepAutoEtPropCeJour.contains(gCnCod)
                        && statuts.estPropositionOuEnAttente(v)
                ){
                    int rangMeilleureProposition = rangPreferenceMeilleurePropositionDuJour.get(gCnCod);
                    if (StatutVoeu.aEteProposeJoursPrecedents(v.statut) || v.getRangPreferencesCandidat() > rangMeilleureProposition) {
                        if (statuts.estProposition(v)) {
                            placesLiberees.add(v);
                        }
                        statuts.refuserAutomatiquementParApplicationRepondeurAutomatique(v);
                    }
                }
            }

            if (placesLiberees.isEmpty()) {
                LOGGER.info("Aucune place libérée par le répondeur automatique");
            } else {
                LOGGER.log(Level.INFO, "Le répondeur automatique a libéré {0} places", placesLiberees.size());
            }

            return placesLiberees;
        } else {
            LOGGER.info("Aucun candidat n'a activé le répondeur automatique");
            return List.of();
        }
    }

    private RepondeurAutomatique() {}


}
