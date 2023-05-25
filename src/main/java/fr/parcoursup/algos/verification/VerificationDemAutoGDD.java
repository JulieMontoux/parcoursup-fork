/*
    Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package fr.parcoursup.algos.verification;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.Voeu;

import java.util.*;
import java.util.stream.Collectors;

import static fr.parcoursup.algos.exceptions.VerificationExceptionMessage.*;

/**
 *
 * Cette classe implémente les vérifications d'intégrité des données et de respect de la spécification
 * concernant la démission automatique en GDD.
 *
 * La vérifs ci-dessous sont effectuée uniquement après le début de la GDD.
 *
 * P8 Si un voeu archivé a été démissionné automatiquement le jour même, alors ce voeu a un rang dans l'ordre de préférence du candidat.
 * De plus le candidat concerné a une nouvelle proposition du jour, de rang strictement inférieur.
 *
 */
public class VerificationDemAutoGDD {

    public static void verifier(Collection<Voeu> voeux, Parametres parametres) throws VerificationException {

        Map<Integer, List<Voeu>> voeuxParCandidat = voeux.stream().collect(Collectors.groupingBy(v -> v.id.gCnCod));


        boolean demAutoGDDCommencee = parametres.nbJoursCampagne >= parametres.nbJoursCampagneDateDebutGDD;
        if(demAutoGDDCommencee) {

            //on se concentre uniquement sur les candidats participant à la GDD
            Set<Integer> candidatsAvecAuMoinsUnVoeuEnAttente
                    = voeux.stream().filter( v-> v.estEnAttenteDeProposition()).map(v -> v.id.gCnCod).collect(Collectors.toSet());
            voeuxParCandidat.keySet().retainAll(candidatsAvecAuMoinsUnVoeuEnAttente);

            verifierP8(voeuxParCandidat);
        }
    }

    /**
     * @param voeux tous les voeux, groupés par numéro de candidat.
     * @throws VerificationException si la propriété 8 n'est pas vérifiée.
     */
    public static void verifierP8(Map<Integer, List<Voeu>> voeux) throws VerificationException {
        for (Map.Entry<Integer, List<Voeu>> e : voeux.entrySet()) {
            int gCnCod = e.getKey();
            OptionalInt demissionnes = e.getValue().stream().filter(Voeu::estDemissionGDD).mapToInt(Voeu::getRangPreferencesCandidat).min();
            if(demissionnes.isPresent()) {
                OptionalInt pireRangProp
                        = e.getValue().stream()
                        .filter(Voeu::estPropositionDuJour)
                        .mapToInt(Voeu::getRangPreferencesCandidat)
                        .filter(r -> (r > 0))
                        .max();
                if(!pireRangProp.isPresent() || pireRangProp.getAsInt() > demissionnes.getAsInt()) {
                    throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8, gCnCod);
                }
            }
        }
    }

    private VerificationDemAutoGDD() {
    }

}
