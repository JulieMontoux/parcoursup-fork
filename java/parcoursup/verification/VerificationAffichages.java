/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.verification;

import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.Voeu;

public class VerificationAffichages {

    /*
    P8 (rangs sur liste d'attente)

    Si un candidat C1 précède un candidat C2 
    dans l'ordre d'appel d'un groupe F 
    et si tous deux ont des voeuxEnAttente en attente pour F 
    alors C1 a un rang sur liste d'attente <= à celui de C2. 
     */
    public static void verifierRangsSurListeAttente(GroupeAffectation groupe) throws VerificationException {

        /* on trie les voeuxEnAttente, le meilleur rang sur liste attente en tête de liste */
        for (Voeu v1 : groupe.voeuxTriesParOrdreAppel()) {
            if (v1.estEnAttenteDeProposition()
                    && !v1.avecInternatAClassementPropre()) {
                for (Voeu v2 : groupe.voeuxEnAttente) {
                    if (v2.estEnAttenteDeProposition()
                            && v2.getOrdreAppel() > v1.getOrdreAppel()
                            && v2.getRangListeAttente() < v1.getRangListeAttente()) {

                        throw new VerificationException(
                                "Violation respect ordre liste attente pour"
                                + " les voeux sans demande internat"
                                + " v1 floué par v2 où v1 est "
                                + v1 + " et v2 est " + v2);
                    }
                }
                break;//il suffit de vérifier pour un seul v1, puisque la liste est triée
            }
        }
    }

    VerificationAffichages() {
    }

}
