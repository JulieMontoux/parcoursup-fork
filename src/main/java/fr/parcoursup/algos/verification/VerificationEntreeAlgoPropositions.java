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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        verifierGroupesetInternatsDesVoeux(entree);

        LOGGER.log(Level.INFO, "V\u00e9rification des {0} groupes d''affectation", entree.groupesAffectations.size());
        for (GroupeAffectation g : entree.groupesAffectations.values()) {
            verifierIntegriteGroupe(g);
        }

        LOGGER.log(Level.INFO, "V\u00e9rification des {0} internats", entree.internats.size());
        for (GroupeInternat internat : entree.internats.values()) {
            verifierIntegriteInternat(internat);
        }

        VerificationAlgoRepondeurAutomatique.verifier(
                entree.voeux,
                entree.candidatsAvecRepondeurAutomatique);

    }

    static void verifierGroupesetInternatsDesVoeux(AlgoPropositionsEntree entree) throws VerificationException {
        LOGGER.info("Vérification: tous les voeux des internats dans la file de voeux");
        for (GroupeInternat internat : entree.internats.values()) {
            for (Voeu v : internat.voeux()) {
                if (!entree.voeux.contains(v)) {
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_PROPOSITIONS_VOEU_NON_RECENSE);
                }
            }
        }

        LOGGER.info("Vérification: tous les voeux des groupes dans la file de voeux");
        for (GroupeAffectation g : entree.groupesAffectations.values()) {
            for (Voeu v : g.voeuxEnAttente) {
                if (!entree.voeux.contains(v)) {
                    throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_PROPOSITIONS_VOEU_NON_RECENSE);
                }
            }
        }

        LOGGER.info("Vérification: tous les voeux avec groupe non nul, "
                + " id cohérent et id internat cohérent");
        for (Voeu v : entree.voeux) {
            if (v.getGroupeAffectation() == null) {
                alerter(v + " avec groupe nul");
            }
            if (v.getGroupeAffectation().id.gTaCod != v.id.gTaCod) {
                alerter(v + " avec id inconsistent");
            }
            if (v.getInternat() != null
                    && v.getInternat().id.gTaCod != 0
                    && v.getInternat().id.gTaCod != v.id.gTaCod) {
                alerter(v + " avec id inconsistent");
            }
        }
    }

    public static void verifierIntegriteGroupe(GroupeAffectation g) throws VerificationException {
        /* intégrité des classements: un classement == un candidat */
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();
        Set<VoeuUID> voeuxVus = new HashSet<>();

        for (Voeu v : g.voeuxEnAttente) {

            if (voeuxVus.contains(v.id)) {
                alerter("b) deux voeux avec la même id " + v.id);
            }

            voeuxVus.add(v.id);

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
                alerter("e) ordre appel formation négatif ou nul dans le groupe " + v.getGroupeAffectation());
            }

            /* remarque le voeu peut-être marqué "avecInternat"
                et en même temps internat==null car c'est un internat sans classement
                (obligatoire ou non-sélectif) */
            if (v.avecInternatAClassementPropre()
                    && !v.getInternat().voeux().contains(v)) {
                alerter("intégrité données dans internat " + v.getInternat());
            }

        }
    }

    public static void verifierIntegriteInternat(GroupeInternat internat) throws VerificationException {
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();

        /* intégrité des classements: un classement == un candidat */
        for (Voeu v : internat.voeux()) {

            if (v.getInternat() != internat) {
                alerter("intégrité données dans internat " + internat);
            }

            if (v.rangInternat <= 0) {
                alerter("e) classement internat négatif");
            }

            Integer gCnCod = ordreVersCandidat.get(v.rangInternat);
            if (gCnCod == null) {
                ordreVersCandidat.put(v.rangInternat, v.id.gCnCod);
            } else if (gCnCod != v.id.gCnCod) {
                alerter("c) candidats distincts avec le même classement");
            }

            Integer ordre = candidatVersOrdre.get(v.id.gCnCod);
            if (ordre == null) {
                candidatVersOrdre.put(v.id.gCnCod, (v.rangInternat));
            } else if (ordre != v.rangInternat) {
                alerter("d) candidats distincts avec le même classement");
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
