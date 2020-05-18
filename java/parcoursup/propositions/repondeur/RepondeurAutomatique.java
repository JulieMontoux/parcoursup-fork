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
package parcoursup.propositions.repondeur;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.algo.Voeu;

public class RepondeurAutomatique {

    /* Comparateur de voeu, basé sur le principe suivant.
    
    Renvoie -1 ssi v1 est préféré à v2.
    
    Lorsqu'il active son répondeur automatique,
    un candidat conserve au plus une proposition, qu'il accepte,
    refuse une partie de ses voeux en attente et classe les voeux en attente
    de proposition par ordre de préférence. Le
    candidat  préfère nécessairement à la proposition précédemment acceptée
    tous les voeux actifs du répondeur.*/
    static int comparerVoeux(Voeu v1, Voeu v2) {

        if (!v1.estAffecteJoursPrecedents() && v2.estAffecteJoursPrecedents()) {
            /* v1 est préféré */
            return -1;
        }
        if (v1.estAffecteJoursPrecedents() && !v2.estAffecteJoursPrecedents()) {
            /* v2 est préféré */
            return 1;
        }
        if (v1.estAffecteJoursPrecedents() && v2.estAffecteJoursPrecedents()) {
            throw new IllegalStateException("Un candidat ayant activé le répondeut automatique "
                    + " ne peut avoir qu'une proposition active, problème avec les voeux "
                    + v1 + " et " + v2);
        }
        if (v1.rangRepondeur <= 0 || v2.rangRepondeur <= 0) {
            throw new IllegalStateException("Impossible de comparer les voeux "
                    + v1 + " et " + v2 + " dans l'ordre du rep auto.");
        }
        return v1.rangRepondeur - v2.rangRepondeur;
    }

    /* effectue les réponses automatiques des candidats
    ayant activé le répondeur automatique. 
    Retourne le nombre de places libérées par l'application du RA.*/
    public static int reponsesAutomatiques(Collection<Voeu> entree) throws VerificationException {

        /* L'entrée ne contient que des voeux de candidats
        ayant activé le répondeur automatique. Cette vérification reprend 7.4.
         */
        for (Voeu v : entree) {
            if ((v.estEnAttenteDeProposition() && !v.getRepondeurActive()) || v.estAffecteHorsPP()) {
                throw new VerificationException("Problème intégrité données:"
                        + "voeu en attente de proposition d'un candidat"
                        + "ayant activé son RA mais qui n'a pas de rang dans le RA: " + v);
            }
        }

        /* Liste, indexée par candidats, des voeux en attente et des propositions
        des candidats ayant activé le répondeur automatique. */
        final Map<Integer, List<Voeu>> voeux = new HashMap<>();

        for (Voeu v : entree) {
            int gCnCod = v.id.gCnCod;
            if (v.estEnAttenteDeProposition()
                    || v.estProposition()) {
                /* on crée la liste si le candidat n'a pas encore été rencontré */
                if (!voeux.containsKey(gCnCod)) {
                    voeux.put(gCnCod, new ArrayList<>());
                }
                List<Voeu> voeuxCandidat = voeux.get(gCnCod);
                voeuxCandidat.add(v);
            }
        }

        Set<Voeu> placesLiberees = new HashSet<>();

        /* candidat par candidat, on applique le répondeur automatique */
        for (List<Voeu> voeuxCandidat : voeux.values()) {

            /* on trie les voeux selon les préférences du candidat */
            voeuxCandidat.sort(RepondeurAutomatique::comparerVoeux);

            /* on parcourt les voeux par ordre de préférence du candidat
            et le candidat refuse automatiquement tous ceux qui viennent
            après une proposition.
             */
            Voeu demissionerDesVoeuxSuivants = null;
            for (Voeu voeu : voeuxCandidat) {
                if (demissionerDesVoeuxSuivants != null) {
                    if (voeu.estProposition()) {
                        placesLiberees.add(voeu);
                    }
                    voeu.refuserAutomatiquement();
                } else if (voeu.estProposition()) {
                    demissionerDesVoeuxSuivants = voeu;
                }
            }

        }

        return placesLiberees.size();

    }

    private RepondeurAutomatique() {
    }

}
