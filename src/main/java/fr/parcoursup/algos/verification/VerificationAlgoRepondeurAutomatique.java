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
import fr.parcoursup.algos.propositions.algo.StatutVoeu;
import fr.parcoursup.algos.propositions.algo.Voeu;

import java.util.*;

/**
 *
 * Cette classe implémente les vérifications d'intégrité des données et de respect de la spécification
 * concernant le répondeur automatique.
 * <p>
 * P7 (répondeur automatique)
 * <p>
 * P7.1 Si un candidat a accepté automatiquement une proposition ou renoncé automatiquement à un voeu
 * alors son répondeur automatique est activé.
 * <p>
 * P7.2 Si un candidat a activé son répondeur automatique
 * alors il a au plus une proposition en PP,
 * et cette proposition est acceptée.
 * <p>
 * P7.3 Si un candidat a renoncé automatiquement à une proposition ou un voeu en attente
 * alors le même jour il a reçu une nouvelle proposition sur un voeu mieux classé
 * et l'a acceptée automatiquement.
 * <p>
 * P7.4 Si un candidat a activé son RA alors tous ses voeux sont classés dans le RA,
 * excepté éventuellement les propositions des jours précédents.
 * <p>
 * P7.5 Si un candidat a activé son RA alors tous ses voeux ont un rang différent dans le RA.
 * <p>
 * P7.6 (ajout 17/03/21 suite à retour thierry@catie). Si un voeu du RA est accepté alors
 * les voeux du RA encore en attente ont un meilleur rang dans le RA.
 *
 */

public class VerificationAlgoRepondeurAutomatique {

    public static void verifier(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        verifierP71etP74(voeux, candidatsAvecRepondeurAutomatique);
        verifierP72(voeux, candidatsAvecRepondeurAutomatique);
        verifierP73(voeux, candidatsAvecRepondeurAutomatique);
        verifierP75(voeux, candidatsAvecRepondeurAutomatique);
        verifierP76(voeux, candidatsAvecRepondeurAutomatique);
    }

    public static void verifierP71etP74(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        /* Vérification P7.1 */
        for (Voeu v : voeux) {
            if ((StatutVoeu.estDemissionAutomatiqueParRepondeurAutomatique(v.statut)
                    || StatutVoeu.estAcceptationAutomatique(v.statut))
                    && !candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_1, v);
            }
            if (candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                    && !StatutVoeu.aEteProposeJoursPrecedents(v.statut)
                    && v.statut != StatutVoeu.REP_AUTO_REFUS_PROPOSITION
                    && (v.getRangPreferencesCandidat() <= 0)
            ) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_4, v);
            }
        }
    }

    public static void verifierP72(Collection<Voeu> voeux, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        /* Vérification P7.2 */
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : voeux) {
            int gCnCod = v.id.gCnCod;
            if ( candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod) && StatutVoeu.estProposition(v.statut)  &&!v.estAffecteHorsPP()) {
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
            if (StatutVoeu.estProposition(v.statut)) {
                propositionsAuxCandidatsAvecRepAuto.put(v.id.gCnCod, v);
            }
        }
        /* Vérification P7.3 */
        for (Voeu v : voeux) {
            int gCnCod = v.id.gCnCod;
            if (StatutVoeu.estDemissionAutomatiqueParRepondeurAutomatique(v.statut)) {
                Voeu proposition = propositionsAuxCandidatsAvecRepAuto.get(gCnCod);
                if (proposition == null
                        || (!StatutVoeu.estAcceptationAutomatique(proposition.statut) || proposition.getRangPreferencesCandidat() <= 0)
                        || (StatutVoeu.estDemissionAutomatiqueVoeuAttenteParRepondeurAutomatique(v.statut) && proposition.getRangPreferencesCandidat() > v.getRangPreferencesCandidat())) {
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_3, v);
                }
            }
        }
    }

    public static void verifierP75(Collection<Voeu> voeux,Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        /* Vérification P7.5 */
        Map<Integer, Set<Integer>> candidatsVersRangs = new HashMap<>();
        for (Voeu v : voeux) {
            int gCnCod = v.id.gCnCod;
            if (candidatsAvecRepondeurAutomatique.contains(gCnCod)) {
                if(StatutVoeu.estEnAttenteDeProposition(v.statut) && v.getRangPreferencesCandidat() <= 0) {
                    throw new VerificationException(VerificationExceptionMessage.REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG, v);
                }
                if (v.getRangPreferencesCandidat() > 0) {
                    Set<Integer> s = candidatsVersRangs.computeIfAbsent(gCnCod, k -> new HashSet<>());
                    if (s.contains(v.getRangPreferencesCandidat())) {
                        throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_5, v);
                    }
                    s.add(v.getRangPreferencesCandidat());
                }
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
            if(v.getRangPreferencesCandidat() > 0) {
                int gCnCod = v.id.gCnCod;
                if (StatutVoeu.estProposition(v.statut)) {
                    /* vérification normalement déjà effectuée par verifierP72 */
                    if (candidatsVersRangProposition.containsKey(gCnCod)) {
                        throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_2, v);
                    }
                    candidatsVersRangProposition.put(gCnCod, v.getRangPreferencesCandidat());
                }
                if(StatutVoeu.estEnAttenteDeProposition(v.statut)) {
                    int rangActuel = candidatsVersRangMaxEnAttente.getOrDefault(gCnCod, Integer.MAX_VALUE);
                    candidatsVersRangMaxEnAttente.put(gCnCod, Math.min(v.getRangPreferencesCandidat(), rangActuel));
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
