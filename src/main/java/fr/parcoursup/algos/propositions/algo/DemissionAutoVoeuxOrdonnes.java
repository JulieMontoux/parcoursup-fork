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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe contient l'implémentation de la démission automatique des voeux archivés en GDD.
 * Cela s'applique aux candidats qui n'ont pas activé leur répondeur automatique.
 * <p>
 * Si un candidat qui n'a pas activé son répondeur automatique
 * reçoit une proposition sur un voeu clôturé,
 * et si ce voeu a un rang de préférence
 * alors le candidat démissionne automatiquement de tous ses autres voeux
 * en attente et de rang strictement supérieurs dans l'ordre de préférence du candidat,
 * ainsi que des propositions auxquelles il n'a pas encore donné de réponse.
 * <p>
 * Voir le document de proésenations des algorithmes pour plus de détails.
 */
public class DemissionAutoVoeuxOrdonnes {


    private static final Logger LOGGER = Logger.getLogger(DemissionAutoVoeuxOrdonnes.class.getSimpleName());

    /**
     * Applique la démission auto des voeux ordonnés.
     *
     * @param statuts                           les statuts des voeux
     * @param rangMeilleurePropositionDuJour    les rangs minimaux des propositions du jour, par candidat
     * @param candidatsAvecRepondeurAutomatique liste des candidats ayant activé leur répondeur
     * @return nombre de places libérées
     */
    static List<Voeu> appliquerDemissionAutomatiqueVoeuOrdonnes(
            StatutsVoeux statuts,
            Map<Integer, Integer> rangMeilleurePropositionDuJour,
            Set<Integer> candidatsAvecRepondeurAutomatique) {

        statuts.refuserAutomatiquementVoeuEnAttenteParApplicationDemissionVoeuxOrdonnesDesCandidats(
                v -> {
                    Integer rangMeilleurProposition = rangMeilleurePropositionDuJour.get(v.id.gCnCod);
                    return rangMeilleurProposition != null
                            && rangMeilleurProposition < v.getRangPreferencesCandidat()
                            && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod);
                }
        );

        List<Voeu> placesLiberees = statuts.refuserAutomatiquementPropositionsDuJourParApplicationDemissionVoeuxOrdonnes(
                v -> {
                    Integer rangMeilleurProposition = rangMeilleurePropositionDuJour.get(v.id.gCnCod);
                    return rangMeilleurProposition != null
                            && rangMeilleurProposition < v.getRangPreferencesCandidat()
                            && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod);
                }
        );

        if (placesLiberees.isEmpty()) {
            LOGGER.info("Aucune place libérée par la démission automatique en GDD");
        } else {
            LOGGER.log(Level.INFO, "La démission automatique en GDD a libéré {0} places", placesLiberees.size());
        }

        return placesLiberees;
    }


    private DemissionAutoVoeuxOrdonnes() {

    }

}
