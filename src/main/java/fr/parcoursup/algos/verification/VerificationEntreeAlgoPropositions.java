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
import fr.parcoursup.algos.propositions.algo.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class VerificationEntreeAlgoPropositions {

    /* vérifie l'intégrité des données d'entrée et lève une exception si nécessaire.
    Propriétés:
        a) tous les voeux dans les groupes sont en attente
        b) pas deux voeux distincts avec la même id
        c) pas deux candidats distincts avec le même classement, formation et internat
        d) pas le même candidat avec deux classements distincts, formation et internat
        e) classements strictement positifs
        f) chaque voeu avec internat se retrouve dans l'internat correspondant
        g) un candidat avec répondeur automatique a au plus une proposition en PP
     */
    public static void verifierIntegrite(AlgoPropositionsEntree entree) throws VerificationException {

        verifierGroupesEtInternatsDesVoeux(entree);

        LOGGER.log(Level.INFO, "Vérification des {0} groupes d''affectation", entree.groupesAffectations.size());
        Map<GroupeAffectationUID, List<Voeu>> voeuxParGroupe = entree.voeux.stream().collect(Collectors.groupingBy(v -> v.groupeUID));
        for (GroupeAffectation g : entree.groupesAffectations.values()) {
            verifierIntegriteGroupe(g, voeuxParGroupe.getOrDefault(g.id, List.of()));
        }

        LOGGER.log(Level.INFO, "Vérification des {0} internats", entree.internats.size());
        Map<GroupeInternatUID, List<Voeu>> voeuxParInternat = entree.getVoeuxParInternat();
        for (GroupeInternat internat : entree.internats.values()) {
            verifierIntegriteInternat(internat, voeuxParInternat.getOrDefault(internat.id, List.of()));
        }

        LOGGER.info("Vérification des propriétés du répondeur automatique");
        VerificationAlgoRepondeurAutomatique.verifier(
                entree.voeux,
                entree.candidatsAvecRepondeurAutomatique);

        LOGGER.info("Vérification des démissions auto en GDD");
        VerificationDemAutoGDD.verifier(entree.voeux, entree.getParametres(), entree.candidatsAvecRepondeurAutomatique);

    }

    static void verifierGroupesEtInternatsDesVoeux(AlgoPropositionsEntree entree) throws VerificationException {

        LOGGER.info("Vérification: tous les voeux avec groupe non nul, "
                + " id cohérent et id internat cohérent");
        for (Voeu v : entree.voeux) {
            if (v.internatUID != null
                    && v.internatUID.gTaCod != 0
                    && v.internatUID.gTaCod != v.id.gTaCod) {
                alerter(v + " avec id inconsistent");
            }
        }
    }

    public static void verifierIntegriteGroupe(GroupeAffectation g, List<Voeu> voeux) throws VerificationException {
        /* intégrité des classements: un classement == un candidat */
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();
        Set<VoeuUID> voeuxVus = new HashSet<>();

        for (Voeu v : voeux) {

            if (voeuxVus.contains(v.id)) {
                alerter("b) deux voeux avec la même id " + v.id);
            }

            voeuxVus.add(v.id);

            if (StatutVoeu.estEnAttenteDeProposition(v.statut)) {
                Integer gCnCod = ordreVersCandidat.get(v.ordreAppel);
                if (gCnCod == null) {
                    ordreVersCandidat.put(v.ordreAppel, v.id.gCnCod);
                } else if (gCnCod != v.id.gCnCod) {
                    alerter("c) candidats distincts avec le même classement dans le groupe " + g);
                }

                Integer ordre = candidatVersOrdre.get(v.id.gCnCod);
                if (ordre == null) {
                    candidatVersOrdre.put(v.id.gCnCod, (v.ordreAppel));
                } else if (ordre != v.ordreAppel) {
                    alerter("d) candidat" + v.id.gCnCod + " avec deux classements distincts " + v.ordreAppel + " dans le groupe " + g);
                }

                if (v.ordreAppel <= 0) {
                    alerter("e) ordre appel formation négatif ou nul pour le voeu en attente " + v.id);
                }
            }

            /* remarque le voeu peut-être marqué "avecInternat"
                et en même temps internat==null car c'est un internat sans classement
                (obligatoire ou non-sélectif) */
            if (v.avecInternatAClassementPropre() && v.internatUID == null) {
                alerter("intégrité données dans internat " + v.id);
            }

        }
    }

    public static void verifierIntegriteInternat(GroupeInternat internat, List<Voeu> voeuxDansCetInternat) throws VerificationException {
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();

        /* intégrité des classements: un classement == un candidat */
        for (Voeu v : voeuxDansCetInternat) {

            if (!internat.id.equals(v.internatUID)) {
                alerter("intégrité données dans internat " + internat);
            }

            if (StatutVoeu.estEnAttenteDeProposition(v.statut)) {

                if (v.rangInternat <= 0) {
                    alerter("e) classement internat négatif dans l'internat " + internat.id);
                }

                Integer gCnCod = ordreVersCandidat.get(v.rangInternat);
                if (gCnCod == null) {
                    ordreVersCandidat.put(v.rangInternat, v.id.gCnCod);
                } else if (gCnCod != v.id.gCnCod) {
                    alerter("c) candidats distincts avec le même classement dans l'internat " + internat.id);
                }

                Integer ordre = candidatVersOrdre.get(v.id.gCnCod);
                if (ordre == null) {
                    candidatVersOrdre.put(v.id.gCnCod, (v.rangInternat));
                } else if (ordre != v.rangInternat) {
                    alerter("d) candidats distincts avec le même classement dans l'internat " + internat.id);
                }
            }

        }
    }

    private static void alerter(String message) throws VerificationException {
        throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_PROPOSITIONS_DONNEES_NON_INTEGRES, message);
    }

    private VerificationEntreeAlgoPropositions() {
    }

    private static final Logger LOGGER = Logger.getLogger(VerificationEntreeAlgoPropositions.class.getSimpleName());

}
