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
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.algo.Voeu;

import java.util.*;

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

    P7.5 Si un candidat a activé son RA alors tous ses voeux ont un rang différent dans le RA.

    P7.6 (ajout 17/03/21 suite à retour thierry@catie). Si un voeu du RA est accepté alors
        les voeux du RA encore en attente ont un meilleur rang dans le RA

     */
    public static void verifier(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        verifierP71(voeux, candidatsAvecRepondeurAutomatique);
        verifierP72(voeux, candidatsAvecRepondeurAutomatique);
        verifierP73(voeux, candidatsAvecRepondeurAutomatique);
        verifierP75(voeux);
        verifierP76(voeux, candidatsAvecRepondeurAutomatique);
    }

    public static void verifierP71(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        /* Vérification P7.1 */
        for (Voeu v : voeux) {
            if ((v.estDemissionAutomatique() || v.estAcceptationAutomatique())
                    && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_1, v);
            }
            if (candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    && (v.rangRepondeur <= 0)
                    && v.estEnAttenteDeProposition()) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_4, v);
            }
        }
    }

    public static void verifierP72(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        /* Vérification P7.2 */
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : voeux) {
            if (!candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    || v.estAffecteHorsPP()) {
                continue;
            }
            int gCnCod = v.id.gCnCod;
            if (v.estProposition()) {
                if (propositionsAuxCandidatsAvecRepAuto.containsKey(gCnCod)) {
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_2, v.id.gCnCod);
                }
                propositionsAuxCandidatsAvecRepAuto.put(gCnCod, v);
            }
        }
    }

    public static void verifierP73(
            Collection<Voeu> voeux,
            Set<Integer> candidatsAvecRepondeurAutomatique
    ) throws VerificationException {
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : voeux) {
            if (!candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    || v.estAffecteHorsPP()) {
                continue;
            }
            if (v.estAcceptationAutomatique() || v.estAffecteJoursPrecedents()) {
                propositionsAuxCandidatsAvecRepAuto.put(v.id.gCnCod, v);
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
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_3, v);
                }
            }
        }
    }

    public static void verifierP75(Collection<Voeu> voeux) throws VerificationException {
        /* Vérification P7.5 */
        Map<Integer, Set<Integer>> candidatsVersRangs = new HashMap<>();
        for (Voeu v : voeux) {
            if (v.rangRepondeur > 0) {
                Set<Integer> s = candidatsVersRangs.computeIfAbsent(v.id.gCnCod, k -> new HashSet<>());
                if (s.contains(v.rangRepondeur)) {
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_5, v);
                }
                s.add(v.rangRepondeur);
            }
        }
    }

    public static void verifierP76(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique)
            throws VerificationException {
        /* Vérification P7.6 */
        Map<Integer, Integer> candidatsVersRangProposition = new HashMap<>();
        Map<Integer, Integer> candidatsVersRangMaxEnAttente = new HashMap<>();
        for (Voeu v : voeux) {
            if (!candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    || v.estAffecteHorsPP()) {
                continue;
            }
            if(v.rangRepondeur > 0) {
                int gCnCod = v.id.gCnCod;
                if (v.estProposition()) {
                    /* vérification normalement déjà effectuée par verifierP72 */
                    if (candidatsVersRangProposition.containsKey(gCnCod)) {
                        throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_2, v);
                    }
                    candidatsVersRangProposition.put(gCnCod, v.rangRepondeur);
                }
                if(v.estEnAttenteDeProposition()) {
                    int rangActuel = candidatsVersRangMaxEnAttente.getOrDefault(gCnCod, Integer.MAX_VALUE);
                    candidatsVersRangMaxEnAttente.put(gCnCod, Math.min(v.rangRepondeur, rangActuel));
                }
            }
        }
        for(Map.Entry<Integer,Integer> e : candidatsVersRangMaxEnAttente.entrySet()) {
            int gCnCod = e.getKey();
            int rangEnAttente = e.getValue();
            int rangProposition = candidatsVersRangProposition.getOrDefault(gCnCod, Integer.MAX_VALUE);
            if(rangEnAttente >= rangProposition) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_6, gCnCod);
            }
        }

    }

    private VerificationAlgoRepondeurAutomatique() {
    }

}
