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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.algo.VoeuUID;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

public class VerificationEntreeAlgoPropositions {

    /* vérifie l'intégrité des données d'entrée et lève une exception si nécessaire.
    Propriétés:
        a) tous les voeuxEnAttente dans les groupes sont en attente
        b) pas deux voeuxEnAttente distincts avec la même id
        c) pas deux candidats distincts avec le même classement, formation et internat
        d) pas le même candidat avec deux classements distincts, formation et internat
        e) classements strictement positifs
        f) chaque voeu avec internat se retrouve dans l'internat correspondant
        g) un candidat avec répondeur automatique a au plus une proposition en PP
        h) les moyennes des MB sont >= 10
        i) nb de places meilleurs bacheliers < 0
        j) pas de duplication de meilleurs bacheliers
     */
    public static void verifierIntegrite(AlgoPropositionsEntree entree) throws Exception {

        for (GroupeInternat internat : entree.internats.values()) {
            for (Voeu v : internat.voeux()) {
                if (!entree.voeux.contains(v)) {
                    throw new RuntimeException();
                }
            }
        }

        for (GroupeAffectation g : entree.groupesAffectations.values()) {
            for (Voeu v : g.voeuxEnAttente) {
                if (!entree.voeux.contains(v)) {
                    throw new RuntimeException();
                }
            }
        }

        for (Voeu v : entree.voeux) {
            if (v.groupe == null) {
                alerter("Voeu " + v + " avec groupe nul");
            }
            if (v.groupe.id.G_TA_COD != v.id.G_TA_COD) {
                alerter("Voeu " + v + " avec id inconsistent");
            }
            if (v.internat != null
                    && v.internat.id.G_TA_COD != 0
                    && v.internat.id.G_TA_COD != v.id.G_TA_COD) {
                alerter("Voeu " + v + " avec id inconsistent");
            }
        }

        Set<VoeuUID> voeuxVus = new HashSet<>();

        /* intégrité des classements: un classement == un candidat */
        Map<Integer, Integer> ordreVersCandidat
                = new HashMap<>();
        Map<Integer, Integer> candidatVersOrdre
                = new HashMap<>();

        for (GroupeAffectation g : entree.groupesAffectations.values()) {

            ordreVersCandidat.clear();
            candidatVersOrdre.clear();
            voeuxVus.clear();

            for (Voeu v : g.voeuxEnAttente) {

                if (voeuxVus.contains(v.id)) {
                    alerter("b) deux voeux avec la même id " + v.id);
                }

                voeuxVus.add(v.id);

                if (v.internatDejaObtenu() 
                        && v.formationDejaObtenue()
                        && !v.estAnnulationDemission()) {
                    //alerter("a) le voeu "  + v + " a le flag attente mais l'internat et la formation"
                   //         + " sont déjà obtenus par le canddat");
                }

                Integer G_CN_COD = ordreVersCandidat.get(v.ordreAppel);
                if (G_CN_COD == null) {
                    ordreVersCandidat.put(v.ordreAppel, v.id.G_CN_COD);
                } else if (G_CN_COD != v.id.G_CN_COD) {
                    alerter("c) candidats distincts avec le même classement dans le groupe " + g);
                }

                Integer ordre = candidatVersOrdre.get(v.id.G_CN_COD);
                if (ordre == null) {
                    candidatVersOrdre.put(v.id.G_CN_COD, (v.ordreAppel));
                } else if (ordre != v.ordreAppel) {
                    alerter("d) candidat" + v.id.G_CN_COD + " avec deux classements distincts " + v.ordreAppel + " dans le groupe " + g);
                }

                if (v.ordreAppel <= 0) {
                    alerter("e) ordre appel formation négatif dans le groupe " + v.groupe);
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

                Integer G_CN_COD = ordreVersCandidat.get(v.rangInternat);
                if (G_CN_COD == null) {
                    ordreVersCandidat.put(v.rangInternat, v.id.G_CN_COD);
                } else if (G_CN_COD != v.id.G_CN_COD) {
                    alerter("c) candidats distincts avec le même classement");
                }

                Integer ordre = candidatVersOrdre.get(v.id.G_CN_COD);
                if (ordre == null) {
                    candidatVersOrdre.put(v.id.G_CN_COD, (v.rangInternat));
                } else if (ordre != v.rangInternat) {
                    alerter("d) candidats distincts avec le même classement");
                }

            }
        }

        VerificationAlgoRepondeurAutomatique.verifier(
                entree.voeux,
                entree.candidatsAvecRepondeurAutomatique);

        Set<Integer> MBvus = new HashSet<>();
        for (MeilleurBachelier mb : entree.meilleursBacheliers) {
            if (MBvus.contains(mb.G_CN_COD)) {
                alerter("j: MB " + mb.G_CN_COD + " avec deux moyennes aux bac");
            }
            if (mb.moyenne < 10) {
                alerter("h: MB sous la moyenne");
            }
            MBvus.add(mb.G_CN_COD);
        }

        for (Entry<Integer, Integer> entry : entree.nbPlacesMeilleursBacheliers.entrySet()) {
            int nbPlaces = entry.getValue();
            if (nbPlaces < 0) {
                alerter("i: nombre de places MB négatif dans la  formation: G_TA_COD=" + entry.getKey());
            }
        }

    }

    private static void alerter(String message) {
        throw new RuntimeException("Données d'entrée non intègres: " + message);
    }

    private VerificationEntreeAlgoPropositions() {
    }

}
