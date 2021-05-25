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
package fr.parcoursup.algos.propositions.repondeur;

import fr.parcoursup.algos.exceptions.IllegalStateExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.algo.Voeu;

import java.util.*;

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
            throw new IllegalStateException(IllegalStateExceptionMessage.REPONDEUR_AUTOMATIQUE_MULTIPLES_PROPOSITIONS.getMessage(v1, v2));
        }
        if (v1.rangRepondeur <= 0 || v2.rangRepondeur <= 0) {
            throw new IllegalStateException(IllegalStateExceptionMessage.REPONDEUR_AUTOMATIQUE_COMPARAISON_IMPOSSIBLE.getMessage(v1, v2));
        }
        return v1.rangRepondeur - v2.rangRepondeur;
    }

    /* effectue les réponses automatiques des candidats
    ayant activé le répondeur automatique. 
    Retourne le nombre de places libérées par l'application du RA.*/
    public static long reponsesAutomatiques(Collection<Voeu> entree) throws VerificationException {
        /* L'entrée ne contient que des voeux de candidats
        ayant activé le répondeur automatique. Cette vérification reprend 7.4.
         */
        for (Voeu v : entree) {
            if ((v.estEnAttenteDeProposition() && !v.getRepondeurActive()) || v.estAffecteHorsPP()) {
                throw new VerificationException(VerificationExceptionMessage.REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG, v);
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

        long placesLiberees = 0;
        /* candidat par candidat, on applique le répondeur automatique */
        for (List<Voeu> voeuxCandidat : voeux.values()) {
            placesLiberees += reponsesAutomatiquesCandidat(voeuxCandidat);
        }

        return placesLiberees;
    }

    public static long reponsesAutomatiquesCandidat(List<Voeu> voeuxCandidat) throws VerificationException {

        long placesLiberees = 0;
        /* on trie les voeux selon les préférences du candidat */
        voeuxCandidat.sort(RepondeurAutomatique::comparerVoeux);

        /* on parcourt les voeux par ordre de préférence du candidat
            et le candidat refuse automatiquement tous ceux qui viennent
            après une proposition.
         */
        boolean demissionerDesVoeuxSuivants = false;
        for (Voeu voeu : voeuxCandidat) {
            if (demissionerDesVoeuxSuivants) {
                if (voeu.estProposition()) {
                    placesLiberees++;
                }
                voeu.refuserAutomatiquement();
            } else if (voeu.estProposition()) {
                demissionerDesVoeuxSuivants = true;
            }
        }
        return placesLiberees;
    }

    private RepondeurAutomatique() {
    }

}
