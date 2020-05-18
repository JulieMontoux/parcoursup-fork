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
package parcoursup.verification;

import parcoursup.exceptions.VerificationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import parcoursup.propositions.algo.Voeu;

public class VerificationAlgoRepondeurAutomatique {

    /*
    P7 (répondeur automatique)

    P7.1 Si un candidat a accepté automatiquement une proposition ou renoncé automatiquement à un voeu
    alors son répondeur automatique est activé.
    
    P7.2 Si un candidat a activé son répondeur automatique 
    alors il a au plus une proposition en PP,
    et cette proposition est acceptée.
    
    P7.3 Si un candidat a renoncé automatiquement à une proposition ou un voeu en attente
    alors le même jour il a reçu une nouvelle proposition sur un voeu mieux classé 
    et l'a acceptée automatiquement.

    P7.4 Si un candidat a activé son RA alors tous ses voeux sont classés dans le RA,
    excepté éventuellement les propositions des jours précédents.
    */
    
    public static void verifier(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {

        /* Vérification P7.1 */
        for (Voeu v : voeux) {
            if ((v.estDemissionAutomatique() || v.estAcceptationAutomatique())
                    && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)) {
                throw new VerificationException("RepAuto violation P7.1: "
                        + "'Si un candidat a accepté automatiquement une proposition "
                        + "ou renoncé automatiquement à un voeu " 
                        + " alors son répondeur automatique est activé'. Voeu en cause: "
                        + v);
            }
            if(candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    && (v.rangRepondeur <=0) 
                    && !v.estAffecteJoursPrecedents()
                    && !v.estDemissionAutomatiqueProposition()
                    ) {
                throw new VerificationException("RepAuto violation P7.4: "
                        + "'Si un candidat a activé son RA alors tous ses voeux sont classés dans le RA,"
                        + " excepté éventuellement les propositions des jours précédents'. Voeu en cause: "
                        + v);                
            }
        }

        /* Vérification P7.2 */
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : voeux) {
            if (!candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    || v.estAffecteHorsPP()) {
                continue;
            }
            int gCnCod = v.id.gCnCod;
            if (v.estAcceptationAutomatique() || v.estAffecteJoursPrecedents()) {
                if (propositionsAuxCandidatsAvecRepAuto.containsKey(gCnCod)) {
                    throw new VerificationException("RepAuto Violation P7.2"
                            + "'Si un candidat a activé son répondeur automatique "
                            + " alors il a au plus une proposition en PP,"
                            + " et cette proposition est acceptée'. Candidat en cause " + gCnCod);
                }
                propositionsAuxCandidatsAvecRepAuto.put(gCnCod, v);
            }
        }

        /* Vérification P7.3 */
        for (Voeu v : voeux) {
            int gCnCod = v.id.gCnCod;
            if (v.estDemissionAutomatique()) {
                Voeu proposition = propositionsAuxCandidatsAvecRepAuto.getOrDefault(gCnCod, null);
                if (proposition == null
                        || (!proposition.estAcceptationAutomatique() || proposition.rangRepondeur <= 0)
                        || (v.estDemissionAutomatiqueVoeuAttente() && proposition.rangRepondeur > v.rangRepondeur)) {
                    throw new VerificationException("Viloation propriété  P7.3"
                            + "'Si un candidat a renoncé automatiquement à une proposition "
                            + "ou un voeu en attente alors il a reçu une nouvelle proposition"
                            + " sur un voeu mieux classé et l'a acceptée automatiquement'."
                            + "Voeu en cause " + v);
                }
            }
        }
    }

    private VerificationAlgoRepondeurAutomatique() {
    }

}
