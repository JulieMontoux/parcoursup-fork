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
 * P8.1 Un candidat a soit tous ses voeux en attente archivès, soit aucun archivé.
 *
 * Les vérifs 8.x x>= 2 ci-dessous sont effectuée uniquement après le début de la GDD.
 *
 * P8.2 Tous les voeux en attente, les propositions du jour et les demissions auto du jour
 * ont un rang dans l'ordre de préférence du candidat. Chacun des voeux a un rang différent.
 *
 * P8.3 Un candidat a au plus deux propositions. Si il en a deux alors
 * une est acceptée et l'autre est en attente de réponse.
 *
 * P8.4 Si un candidat a une proposition dont l'ordre de préférence a été spécifié
 * alors les voeux encore en attente ont un rang strictement inférieur dans les préférences du candidat.
 *
 * P8.5 Si un voeu archivé a été démissionné automatiquement le jour même, alors ce voeu a un rang dans l'ordre de préférence du candidat.
 * De plus le candidat concerné a une nouvelle proposition du jour, de rang strictement inférieur.
 *
 */
public class VerificationDemAutoGDD {

    public static void verifier(Collection<Voeu> voeux, Parametres parametres) throws VerificationException {

        Map<Integer, List<Voeu>> voeuxParCandidat = voeux.stream().collect(Collectors.groupingBy(v -> v.id.gCnCod));

        verifierP81(voeuxParCandidat);

        boolean demAutoGDDCommencee = parametres.nbJoursCampagne >= parametres.nbJoursCampagneDateDebutGDD;
        if(demAutoGDDCommencee) {

            //on se concentre uniquement sur les candidats participant à la GDD
            Set<Integer> candidatsAvecAuMoinsUnVoeuEnAttente
                    = voeux.stream().filter( v-> v.estEnAttenteDeProposition()).map(v -> v.id.gCnCod).collect(Collectors.toSet());
            voeuxParCandidat.keySet().retainAll(candidatsAvecAuMoinsUnVoeuEnAttente);

            verifierP82(voeuxParCandidat);
            verifierP83(voeuxParCandidat);
            verifierP84(voeuxParCandidat);
            verifierP85(voeuxParCandidat);
        }
    }

    /**
     * @param voeux tous les voeux
     * @return true ssi au moins un voeu en attente est archivé
     * @throws VerificationException si la propriété 8.1 n'est pas respectée
     */
    public static void verifierP81(Map<Integer, List<Voeu>> voeuxParCandidat) throws VerificationException {
        for (Map.Entry<Integer, List<Voeu>> entry : voeuxParCandidat.entrySet()) {
            Integer gCnCod = entry.getKey();
            List<Voeu> voeux = entry.getValue();
            boolean auMoinsUnVoeuEnAttenteArchive
                    = voeux.stream().anyMatch(v -> v.estEnAttenteDeProposition() && v.getEstArchive());
            boolean auMoinsUnVoeuEnAttenteNonArchive
                    = voeux.stream().anyMatch(v -> v.estEnAttenteDeProposition() && !v.getEstArchive());
            if (auMoinsUnVoeuEnAttenteArchive && auMoinsUnVoeuEnAttenteNonArchive) {
                throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_1, gCnCod);
            }
        }
    }

    /**
     * @param voeux tous les voeux, groupés par numéro de candidat.
     * @throws VerificationException si la propriété 8.2 n'est pas vérifiée.
     */
    public static void verifierP82(Map<Integer, List<Voeu>> voeux) throws VerificationException {
        for (Map.Entry<Integer, List<Voeu>> e : voeux.entrySet()) {
            int gCnCod = e.getKey();
            Set<Integer> rangs = new HashSet<>();
            for(Voeu v : e.getValue()) {
                boolean voeuDoitAvoirUnRang = v.estEnAttenteDeProposition()
                        || v.estPropositionDuJour()
                        || (v.estDemissionAutomatique() && v.getStatut() != Voeu.StatutVoeu.REP_AUTO_REFUS_PROPOSITION);
                if(voeuDoitAvoirUnRang) {
                    if (v.getRangPreferencesCandidat() <= 0) {
                        throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_2_a, v);
                    }
                    if (rangs.contains(v.getRangPreferencesCandidat())) {
                        throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_2_b, v);
                    }
                    rangs.add(v.getRangPreferencesCandidat());
                }
            }
        }
    }

    /**
     * @param voeux tous les voeux, groupés par numéro de candidat.
     * @throws VerificationException si la propriété 8.3 n'est pas vérifiée.
     */
    public static void verifierP83(Map<Integer, List<Voeu>> voeux) throws VerificationException {
        for (Map.Entry<Integer, List<Voeu>> e : voeux.entrySet()) {
            int gCnCod = e.getKey();
            List<Voeu> propositions = e.getValue().stream()
                    .filter(v -> v.estProposition() && !v.estAffecteHorsPP())
                    .collect(Collectors.toList());
            if(propositions.size() > 2) {
                throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_3_a, gCnCod);
            }
            if(propositions.size() == 2) {
                Voeu v0 = propositions.get(0);
                Voeu v1 = propositions.get(1);
                boolean auMoinsUnePropositionEnAttenteDeReponse =
                        v0.estPropositionDuJour()
                                || v1.estPropositionDuJour()
                                || v0.estPropJoursPrecedentsEnAttenteDeReponseCandidat()
                                || v1.estPropJoursPrecedentsEnAttenteDeReponseCandidat()
                        ;
                boolean auMoinsUnePropositionAccepteePrecedemment =
                        (v0.getStatut() == Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE)
                        || (v1.getStatut() == Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE);
                if(!auMoinsUnePropositionEnAttenteDeReponse || ! auMoinsUnePropositionAccepteePrecedemment ) {
                    throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_3_b, gCnCod);
                }
            }
        }
    }

    /**
     * @param voeux tous les voeux, groupés par numéro de candidat.
     * @throws VerificationException si la propriété 8.4 n'est pas vérifiée.
     */
    public static void verifierP84(Map<Integer, List<Voeu>> voeux) throws VerificationException {
        for (Map.Entry<Integer, List<Voeu>> e : voeux.entrySet()) {
            int gCnCod = e.getKey();
            OptionalInt pireRangProp
                    = e.getValue().stream()
                    .filter(Voeu::estProposition)
                    .mapToInt(Voeu::getRangPreferencesCandidat)
                    .filter(r -> (r > 0))
                            .max();
            OptionalInt meilleurRangAttente
                    = e.getValue().stream()
                    .filter(Voeu::estEnAttenteDeProposition)
                    .mapToInt(Voeu::getRangPreferencesCandidat)
                    .filter(r -> (r > 0))
                    .max();
            if(pireRangProp.isPresent()
                    && meilleurRangAttente.isPresent()
                    && pireRangProp.getAsInt() <= meilleurRangAttente.getAsInt()) {
                throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_4, gCnCod);
            }
        }
    }

    /**
     * @param voeux tous les voeux, groupés par numéro de candidat.
     * @throws VerificationException si la propriété 8.5 n'est pas vérifiée.
     */
    public static void verifierP85(Map<Integer, List<Voeu>> voeux) throws VerificationException {
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
                    throw new VerificationException(VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8_5, gCnCod);
                }
            }
        }
    }

    private VerificationDemAutoGDD() {
    }

}
