/* 
    Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.meilleursbacheliers.AlgoMeilleursBacheliersDonnees;

public class VerificationResultatsAlgoMeilleurBachelier {

    /* on vérifie les propriétés suivantes.
    P1. Dans chaque groupe, les voeux éligibles au dispositif MB 
        sont en tête de l'ordre d'appel
    P2. Dans chaque groupe, les moyennes au Bac des voeux éligibles
        MB sont décroissantes dans l'ordre d'appel
    P3. Dans un même groupe les voeux éligibles au dispositif MB avec la même moyenne au Bac
        sont départagés par ordre d'appel  
    P4. Dans une même formation, si un voeu est éligible alors tout voeu
        d'un MB avec une moyenne stictement meilleure est éligible
    P5. Dans chaque formation, si le nombre de candidats éligibles MB
        est strictement inférieur au nombre de places réservées
        duquel on a soustrait le nombre de candidats avec une proposition
        au titre des MB, alors c'est que tous les MB sont éligibles.
    P6. Dans chaque formation, si le nombre de candidat éligibles MB
        excède le nombre de places alors les derniers éligibles de chaque groupe ont la
        même moyenne au Bac.
    
     */
    public static void verifier(AlgoMeilleursBacheliersDonnees donnees) {

        /* calcul de la liste des groupesAVerifier et verif de propriétés staiques */
        Set<GroupeAffectation> groupesAVerifier = new HashSet<>();
        Map<Integer, Collection<Voeu>> voeuxParFormation = new HashMap<>();

        for (GroupeAffectation groupe : donnees.groupes) {
            for (Voeu v : groupe.voeuxEnAttente) {
                if (v.eligibleDispositifMB && !v.estMeilleurBachelier()) {
                    alerter("Voeu éligibles au dispositif MB non formulé par un MB");
                }
                if (v.estMeilleurBachelier()) {
                    groupesAVerifier.add(v.groupe);
                }
            }
        }

        for (GroupeAffectation groupe : groupesAVerifier) {
            verifierP1(groupe);
            verifierP2(groupe);
            verifierP3(groupe);
        }

        for (GroupeAffectation groupe : groupesAVerifier) {
            for (Voeu v : groupe.voeuxEnAttente) {
                int G_TA_COD = v.id.G_TA_COD;
                if (!voeuxParFormation.containsKey(G_TA_COD)) {
                    voeuxParFormation.put(G_TA_COD, new ArrayList<>());
                }
                voeuxParFormation.get(G_TA_COD).add(v);
            }
        }

        for (Entry<Integer, Collection<Voeu>> entry : voeuxParFormation.entrySet()) {
            int G_TA_COD = entry.getKey();
            Collection<Voeu> voeux = entry.getValue();

            verifierP4(voeux);

            Integer nbPlacesVacantes = donnees.nbPlacesMeilleursBacheliersVacantes.get(G_TA_COD);
            if (nbPlacesVacantes == null) {
                continue;
            }

            verifierP5EtP6(voeux, nbPlacesVacantes);

        }

    }

    /* 
    P1. Dans chaque groupe, les voeux éligibles au dispositif MB 
        sont en tête de l'ordre d'appel
     */
    static void verifierP1(GroupeAffectation groupe) {

        boolean vuUnNonMB = false;

        for (Voeu v : groupe.voeuxTriesParOrdreAppel()) {
            if (vuUnNonMB && v.eligibleDispositifMB) {
                alerter("Les MB ne sont pas tous en tête de l'ordre d'appel du groupe " + groupe);
            }
            vuUnNonMB |= !v.eligibleDispositifMB;
        }

    }

    /* 
    P2. dans chaque groupe, les moyennes au Bac des voeux éligibles
        MB sont décroissantes dans l'ordre d'appel    
     */
    static void verifierP2(GroupeAffectation groupe) {

        double derniereMoyenne = Double.MAX_VALUE;
        for (Voeu v : groupe.voeuxTriesParOrdreAppel()) {
            if (!v.eligibleDispositifMB) {
                /* sachant P1 on peut sortir de la boucle: il n'y aura plus de MB éligibles */
                break;
            }
            double moyenne = v.moyenneBac();
            if (moyenne > derniereMoyenne) {
                alerter("L'ordre d'apppel des MB éligibles n'est pas consistent"
                        + "avec les moyennes");
            }
            derniereMoyenne = moyenne;
        }

    }

    /* 
    P3. dans un même groupe les meilleurs bacheliers 
     avec la même moyenne au Bac sont départagées par leur ordre d'appel  
     */
    static void verifierP3(GroupeAffectation groupe) {

        Collection<Voeu> voeux = groupe.voeuxTriesParOrdreAppel();
        for (Voeu v1 : voeux) {
            if (!v1.eligibleDispositifMB) {
                /* sachant P1 on peut sortir de la boucle: il n'y aura plus de MB éligibles */
                break;
            }
            for (Voeu v2 : voeux) {
                if (v2.estMeilleurBachelier()) {
                    if ((v1.moyenneBac() == v2.moyenneBac())
                            && (v1.ordreAppelInitial > v2.ordreAppelInitial)
                            && (v1.ordreAppel < v2.ordreAppel)) {
                        alerter("Violation MB P3");
                    }
                }
            }
        }
    }

    /*
    P4. Dans une même formation, si un voeu est éligible alors tout voeu
        d'un MB avec une moyenne strictement meilleure est éligible
     */
    static void verifierP4(Collection<Voeu> voeux) {

        for (Voeu v1 : voeux) {
            if (v1.eligibleDispositifMB) {
                for (Voeu v2 : voeux) {
                    if (v2.estMeilleurBachelier()) {
                        if (!v2.eligibleDispositifMB
                                && (v2.moyenneBac() > v1.moyenneBac())) {
                            alerter("Violation P4 MB");
                        }
                    }
                }
            }
        }
    }

    /*
    P5. dans chaque formation, si le nombre de candidats éligibles MB
        est strictement inférieur au nombre de places réservées
        duquel on a soustrait le nombre de candidats avec une proposition
        au titre des MB, alors c'est que tous les MB sont éligibles.
    
    P6. Dans chaque formation, si le nombre de candidat éligibles MB
        excède le nombre de places alors les derniers éligibles de chaque groupe ont la
        même moyenne au Bac et sont de groupesAVerifier différents
     */
    static void verifierP5EtP6(Collection<Voeu> voeux, int nbPlacesVacantes) {

        Set<Integer> candidatsEligibles = new HashSet<>();
        List<Voeu> voeuxEligibles = new ArrayList<>();

        boolean auMoinsUnMBNonEligible = false;
        for (Voeu v1 : voeux) {
            if (v1.eligibleDispositifMB) {
                candidatsEligibles.add(v1.id.G_CN_COD);
                voeuxEligibles.add(v1);
            } else if (v1.estMeilleurBachelier()) {
                auMoinsUnMBNonEligible = true;
            }
        }
        if (auMoinsUnMBNonEligible
                && (candidatsEligibles.size() < nbPlacesVacantes)) {
            alerter("Violation MB P5");
        }

        /* rare cas de surcapacité */
        if (candidatsEligibles.size() > nbPlacesVacantes) {
            /* les pires moyennes en tête deliste */
            voeuxEligibles.sort((Voeu v1, Voeu v2)
                    -> (int) (10000.0 * (v1.moyenneBac() - v2.moyenneBac()))
            );

            Voeu v0 = voeuxEligibles.get(0);
            int G_CN_COD0 = v0.id.G_CN_COD;
            double moyenne0 = v0.moyenneBac();

            if (candidatsEligibles.size() < 2) {
                alerter("MB P6: En cas de surcapacité "
                        + "il devrait y avoir au moins deux candidats eligibles");
            }

            /* on itère sur les voeux à la recherche d'un cas d'égalité 
            avec moyenne 0 depuis un group différent */
            for (Voeu v1 : voeuxEligibles) {
                if (v1.id.G_CN_COD == G_CN_COD0) {
                    continue;
                }
                double moyenne1 = v1.moyenneBac();

                if (moyenne0 != moyenne1) {
                    alerter("Violation MB P6");
                }

                if (v1.groupe.id.C_GP_COD != v0.groupe.id.C_GP_COD) {
                    /* on a bien trouvé un cas d'égalité */
                    break;
                }
            }

        }
    }

    private static void alerter(String message) {
        throw new RuntimeException("Données d'entrée non intègres: " + message);
    }

    private VerificationResultatsAlgoMeilleurBachelier() {
    }
}
