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
     */
    public static void verifier(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws Exception {

        /* Vérification P7.1 */
        for (Voeu v : voeux) {
            if ((v.estDemissionAutomatique() || v.estAcceptationAutomatique())
                    && !candidatsAvecRepondeurAutomatique.contains(v.id.G_CN_COD)) {
                throw new RuntimeException("Inconsistence logique: invaidation de P7.1");
            }
        }

        /* Vérification P7.2 */
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : voeux) {
            if(!candidatsAvecRepondeurAutomatique.contains(v.id.G_CN_COD) 
                    || v.estAffecteHorsPP()) {
                continue;
            }
            int G_CN_COD = v.id.G_CN_COD;
            if (v.estAcceptationAutomatique() || v.estAffecteJoursPrecedents()) {
                if (propositionsAuxCandidatsAvecRepAuto.containsKey(G_CN_COD)) {
                    throw new RuntimeException("RepAuto Violation P7.2");
                }
                propositionsAuxCandidatsAvecRepAuto.put(G_CN_COD, v);
            }
        }

        /* Vérification P7.3 */
        for (Voeu v : voeux) {
            int G_CN_COD = v.id.G_CN_COD;
            if (v.estDemissionAutomatique()) {
                Voeu proposition = propositionsAuxCandidatsAvecRepAuto.getOrDefault(G_CN_COD, null);
                if (proposition == null) {
                    throw new RuntimeException("Inconsistence logique: invaidation  P7.3");
                }
                if (!proposition.estAcceptationAutomatique() || proposition.rangRepondeur <= 0) {
                    throw new RuntimeException("Inconsistence logique: invaidation  P7.3");
                }
                if (v.estDemissionAutomatiqueVoeuAttente() 
                        && proposition.rangRepondeur > v.rangRepondeur) {
                    throw new RuntimeException("Inconsistence logique: invalidation  P7.3");
                }
            }
        }
    }

    private VerificationAlgoRepondeurAutomatique() {
    }

}
