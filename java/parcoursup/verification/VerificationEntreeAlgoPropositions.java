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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.algo.VoeuUID;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

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
        h) les moyennes des MB sont >= 10
        i) nb de places meilleurs bacheliers < 0
        j) pas de duplication de meilleurs bacheliers
     */
    public static void verifierIntegrite(AlgoPropositionsEntree entree) throws VerificationException {

        LOGGER.info("Vérification: tous les voeux des internats sdans la file de voeux");
        for (GroupeInternat internat : entree.internats.values()) {
            for (Voeu v : internat.voeux()) {
                if (!entree.voeux.contains(v)) {
                    throw new VerificationException("Voeu non recensé en entrée");
                }
            }
        }

        LOGGER.info("Vérification: tous les voeux des groupes dans la file de voeux");
        for (GroupeAffectation g : entree.groupesAffectations.values()) {
            for (Voeu v : g.voeuxEnAttente) {
                if (!entree.voeux.contains(v)) {
                    throw new VerificationException("Voeu non recensé en entrée");
                }
            }
        }

        LOGGER.info("Vérification: tous les voeux avec groupe non nul, "
                + " id cohérent et id internat cohérent");
        for (Voeu v : entree.voeux) {
            if (v.groupe == null) {
                alerter(v + " avec groupe nul");
            }
            if (v.groupe.id.gTaCod != v.id.gTaCod) {
                alerter(v + " avec id inconsistent");
            }
            if (v.internat != null
                    && v.internat.id.gTaCod != 0
                    && v.internat.id.gTaCod != v.id.gTaCod) {
                alerter(v + " avec id inconsistent");
            }
        }

        Set<VoeuUID> voeuxVus = new HashSet<>();

        /* intégrité des classements: un classement == un candidat */
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();

        LOGGER.log(Level.INFO, "V\u00e9rification des {0} groupes d''affectation", entree.groupesAffectations.size());
        for (GroupeAffectation g : entree.groupesAffectations.values()) {

            ordreVersCandidat.clear();
            candidatVersOrdre.clear();
            voeuxVus.clear();

            for (Voeu v : g.voeuxEnAttente) {

                if (voeuxVus.contains(v.id)) {
                    alerter("b) deux voeux avec la même id " + v.id);
                }

                voeuxVus.add(v.id);

                Integer gCnCod = ordreVersCandidat.get(v.getOrdreAppel());
                if (gCnCod == null) {
                    ordreVersCandidat.put(v.getOrdreAppel(), v.id.gCnCod);
                } else if (gCnCod != v.id.gCnCod) {
                    alerter("c) candidats distincts avec le même classement dans le groupe " + g);
                }

                Integer ordre = candidatVersOrdre.get(v.id.gCnCod);
                if (ordre == null) {
                    candidatVersOrdre.put(v.id.gCnCod, (v.getOrdreAppel()));
                } else if (ordre != v.getOrdreAppel()) {
                    alerter("d) candidat" + v.id.gCnCod + " avec deux classements distincts " + v.getOrdreAppel() + " dans le groupe " + g);
                }

                if (v.getOrdreAppel() <= 0) {
                    alerter("e) ordre appel formation négatif ou nul dans le groupe " + v.groupe);
                }

                /* remarque le voeu peut-être marqué "avecInternat"
                et en même temps internat==null car c'est un internat sans classement
                (obligatoire ou non-sélectif) */
                if (v.avecInternatAClassementPropre()
                        && !v.internat.voeux().contains(v)) {
                    alerter("intégrité données dans internat " + v.internat);
                }

            }
        }

        LOGGER.log(Level.INFO, "V\u00e9rification des {0} internats", entree.internats.size());
        for (GroupeInternat internat : entree.internats.values()) {

            /* intégrité des classements: un classement == un candidat */
            ordreVersCandidat.clear();
            candidatVersOrdre.clear();

            for (Voeu v : internat.voeux()) {

                if (v.internat != internat) {
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

        LOGGER.log(Level.INFO, "V\u00e9rification des {0} meilleurs bacheliers", entree.meilleursBacheliers.size());
        VerificationAlgoRepondeurAutomatique.verifier(
                entree.voeux,
                entree.candidatsAvecRepondeurAutomatique);

        Set<Integer> mbVus = new HashSet<>();
        for (MeilleurBachelier mb : entree.meilleursBacheliers) {
            if (mbVus.contains(mb.gCnCod)) {
                alerter("j: MB " + mb.gCnCod + " avec deux moyennes aux bac");
            }
            if (mb.moyenne < 10) {
                alerter("h: MB sous la moyenne");
            }
            mbVus.add(mb.gCnCod);
        }

        for (Entry<Integer, Integer> entry : entree.nbPlacesMeilleursBacheliers.entrySet()) {
            int nbPlaces = entry.getValue();
            if (nbPlaces < 0) {
                alerter("i: nombre de places MB négatif dans la  formation: g_ta_cod=" + entry.getKey());
            }
        }

    }

    private static void alerter(String message) throws VerificationException {
        throw new VerificationException("Données d'entrée non intègres: " + message);
    }

    private VerificationEntreeAlgoPropositions() {
    }

    private static final Logger LOGGER = Logger.getLogger(VerificationEntreeAlgoPropositions.class.getSimpleName());

}
