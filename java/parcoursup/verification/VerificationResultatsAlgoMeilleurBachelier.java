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

import parcoursup.exceptions.VerificationException;
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
    P4bis. Dans une formation, si un voeu v1 d'un candidat c1 est éligible au dispositif MBC,
        alors dans tout groupe d'appel de la formation,
        si au moins un meilleur bachelier a la même moyenne que c1,
        ce groupe contient au moins un voeu éligible au dispositif MBC avec la même moyenne.
    P5. Dans chaque formation, si le nombre de candidats éligibles MB
        est strictement inférieur au nombre de places réservées
        duquel on a soustrait le nombre de candidats avec une proposition
        au titre des MB, alors c'est que tous les MB sont éligibles.
    P6. Dans chaque formation, si le nombre de candidat éligibles MB
        excède le nombre de places alors il y a au moins deux candidats éligibles
	qui ont la même moyenne au bac et cette moyenne est la plus petite des moyennes
	au bac de tous les candidats éligibles du groupe et ces deux candidats sont dans deux groupes de
	classement différents.
    
     */
    public static void verifier(AlgoMeilleursBacheliersDonnees donnees) throws VerificationException {

        /* calcul de la liste des groupesAVerifier et verif de propriétés staiques */
        Set<GroupeAffectation> groupesAVerifier = new HashSet<>();
        Map<Integer, Collection<Voeu>> voeuxParFormation = new HashMap<>();

        for (GroupeAffectation groupe : donnees.groupes) {
            for (Voeu v : groupe.voeuxEnAttente) {
                if (v.getEligibleDispositifMB() && !v.estMeilleurBachelier()) {
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
                int gTaCod = v.id.gTaCod;
                if (!voeuxParFormation.containsKey(gTaCod)) {
                    voeuxParFormation.put(gTaCod, new ArrayList<>());
                }
                voeuxParFormation.get(gTaCod).add(v);
            }
        }

        for (Entry<Integer, Collection<Voeu>> entry : voeuxParFormation.entrySet()) {
            int gTaCod = entry.getKey();
            Collection<Voeu> voeux = entry.getValue();

            verifierP4etP4bis(voeux);

            Integer nbPlacesVacantes = donnees.nbPlacesMeilleursBacheliersVacantes.get(gTaCod);
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
    static void verifierP1(GroupeAffectation groupe) throws VerificationException {

        boolean vuUnNonMB = false;

        for (Voeu v : groupe.voeuxTriesParOrdreAppel()) {
            if (vuUnNonMB && v.getEligibleDispositifMB()) {
                alerter("Violation propriété P1 des Meilleurs Bacheliers 'Dans chaque groupe,"
                        + "  les voeux éligibles au dispositif MB \n"
                        + " sont en tête de l'ordre d'appel' pour le groupe " + groupe);
            }
            vuUnNonMB |= !v.getEligibleDispositifMB();
        }

    }

    /* 
    P2. dans chaque groupe, les moyennes au Bac des voeux éligibles
        MB sont décroissantes dans l'ordre d'appel    
     */
    static void verifierP2(GroupeAffectation groupe) throws VerificationException {

        double derniereMoyenne = Double.MAX_VALUE;
        for (Voeu v : groupe.voeuxTriesParOrdreAppel()) {
            if (!v.getEligibleDispositifMB()) {
                /* sachant P1 on peut sortir de la boucle: il n'y aura plus de MB éligibles */
                break;
            }
            double moyenne = v.moyenneBac();
            if (moyenne > derniereMoyenne) {
                alerter("Violation propriété P2 des Meilleurs Bacheliers "
                        + "'dans chaque groupe, les moyennes au Bac des voeux éligibles "
                        + " MB sont décroissantes dans l'ordre d'appel " + groupe);
            }
            derniereMoyenne = moyenne;
        }

    }

    /* 
    P3. dans un même groupe les meilleurs bacheliers 
     avec la même moyenne au Bac sont départagées par leur ordre d'appel  
     */
    static void verifierP3(GroupeAffectation groupe) throws VerificationException {

        Collection<Voeu> voeux = groupe.voeuxTriesParOrdreAppel();
        for (Voeu v1 : voeux) {
            if (!v1.getEligibleDispositifMB()) {
                /* sachant P1 on peut sortir de la boucle: il n'y aura plus de MB éligibles */
                break;
            }
            for (Voeu v2 : voeux) {
                if (v2.estMeilleurBachelier()
                        && (Double.compare(v1.moyenneBac(), v2.moyenneBac()) == 0)
                        && (v1.ordreAppelInitial > v2.ordreAppelInitial)
                        && (v1.getOrdreAppel() < v2.getOrdreAppel())) {
                    alerter("Violation propriété P3 des Meilleurs Bacheliers "
                            + "'dans un même groupe les meilleurs bacheliers "
                            + " avec la même moyenne au Bac sont départagées par leur ordre d'appel."
                            + " Voeux " + v1 + " et " + v2);
                }
            }
        }
    }

    /*
    P4. Dans une même formation, si un voeu est éligible alors tout voeu
        d'un MB avec une moyenne strictement meilleure est éligible
    
    P4bis. Dans une formation, si un voeu v1 d'un candidat c1 est éligible au dispositif MBC,
    alors dans tout groupe d'appel de la formation,
    si au moins un meilleur bachelier a la même moyenne que c1,
    ce groupe contient au moins un voeu éligible au dispositif MBC avec la même moyenne.

     */
    static void verifierP4etP4bis(Collection<Voeu> voeux) throws VerificationException {

        for (Voeu v1 : voeux) {
            if (v1.getEligibleDispositifMB()) {
                for (Voeu v2 : voeux) {
                    if (v2.estMeilleurBachelier()
                            && !v2.getEligibleDispositifMB()
                            && (v2.moyenneBac() > v1.moyenneBac())) {
                        alerter("Violation propriété P4 des Meilleurs Bacheliers "
                                + "'Dans une même formation, si un voeu est éligible alors tout voeu "
                                + " d'un MB avec une moyenne strictement meilleure est éligible "
                                + "Voeux " + v1 + " et " + v2 + ".");
                    }

                }
            }
        }

        Map<Double, Set<GroupeAffectation>> moyennesMBeligiblesVersGroupes = new HashMap<>();
        Map<Double, Set<GroupeAffectation>> moyennesMBversGroupes = new HashMap<>();

        for (Voeu v1 : voeux) {
            if (v1.estMeilleurBachelier()) {
                double moyenne = v1.moyenneBac();

                if (!moyennesMBversGroupes.containsKey(moyenne)) {
                    moyennesMBversGroupes.put(moyenne, new HashSet<>());
                }
                moyennesMBversGroupes.get(moyenne).add(v1.groupe);

                if (v1.getEligibleDispositifMB()) {
                    if (!moyennesMBeligiblesVersGroupes.containsKey(moyenne)) {
                        moyennesMBeligiblesVersGroupes.put(moyenne, new HashSet<>());
                    }
                    moyennesMBeligiblesVersGroupes.get(moyenne).add(v1.groupe);
                }
            }
        }

        for (Entry<Double, Set<GroupeAffectation>> entry : moyennesMBeligiblesVersGroupes.entrySet()) {
            double moyenne = entry.getKey();
            Set<GroupeAffectation> groupesEligibles = entry.getValue();
            Set<GroupeAffectation> groupesEligiblesOuNon = moyennesMBversGroupes.get(moyenne);
            /* chaque groupe ayant au moins un MB avec cette moyenne 
            doit avoir au moins un MB eligible avec cette moyenne.
            PAs forcément tous car à l'intérieur d'un même groupe 
            ils sont discriminés par rang dans l'ordre d'appel. */
            if (groupesEligiblesOuNon != null) {
                groupesEligiblesOuNon.removeAll(groupesEligibles);
                if (!groupesEligiblesOuNon.isEmpty()) {
                    alerter("Violation propriété P4bis des Meilleurs Bacheliers "
                            + "'P4bis. Dans une formation, si un voeu v1 d'un candidat c1 est éligible au dispositif MBC,\n"
                            + " alors dans tout groupe d'appel de la formation,\n"
                            + " si au moins un meilleur bachelier a la même moyenne que c1,\n"
                            + " ce groupe contient au moins un voeu éligible au dispositif MBC avec la même moyenne.\n"
                            + ".'. "
                            + "Groupe " + groupesEligiblesOuNon.stream().findAny().toString() + ".");
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
        excède le nombre de places alors il y a au moins deux candidats éligibles
	qui ont la même moyenne au bac et cette moyenne est la plus petite des moyennes
	au bac de tous les candidats éligibles du groupe et ces deux candidats sont dans deux groupes de
	classement différents.
     */
    static void verifierP5EtP6(Collection<Voeu> voeux, int nbPlacesVacantes) throws VerificationException {

        Set<Integer> candidatsEligibles = new HashSet<>();
        List<Voeu> voeuxEligibles = new ArrayList<>();

        boolean auMoinsUnMBNonEligible = false;
        for (Voeu v1 : voeux) {
            if (v1.getEligibleDispositifMB()) {
                candidatsEligibles.add(v1.id.gCnCod);
                voeuxEligibles.add(v1);
            } else if (v1.estMeilleurBachelier()) {
                auMoinsUnMBNonEligible = true;
            }
        }
        if (auMoinsUnMBNonEligible
                && (candidatsEligibles.size() < nbPlacesVacantes)) {
            alerter("Violation propriété P5 des Meilleurs Bacheliers."
                    + "dans chaque formation, si le nombre de candidats éligibles MB "
                    + "est strictement inférieur au nombre de places réservées "
                    + "duquel on a soustrait le nombre de candidats avec une proposition "
                    + "au titre des MB, alors c'est que tous les MB sont éligibles.");
        }

        /* rare cas de surcapacité */
        if (candidatsEligibles.size() > nbPlacesVacantes) {
            /* les pires moyennes en tête deliste */
            voeuxEligibles.sort((Voeu v1, Voeu v2)
                    -> (int) (10000.0 * (v1.moyenneBac() - v2.moyenneBac()))
            );

            Voeu v0 = voeuxEligibles.get(0);
            int gCnCod0 = v0.id.gCnCod;
            double moyenne0 = v0.moyenneBac();

            if (candidatsEligibles.size() < 2) {
                alerter("MB P6: En cas de surcapacité "
                        + "il devrait y avoir au moins deux candidats eligibles.");
            }

            /* on itère sur les voeux à la recherche d'un cas d'égalité 
            avec moyenne 0 depuis un group différent */
            for (Voeu v1 : voeuxEligibles) {
                if (v1.id.gCnCod == gCnCod0) {
                    continue;
                }
                double moyenne1 = v1.moyenneBac();

                if (Double.compare(moyenne0, moyenne1) != 0) {
                    alerter("Violation propriété P6 des Meilleurs Bacheliers."
                            + "'Dans chaque formation, si le nombre de candidat éligibles MB "
                            + " excède le nombre de places alors il y a au moins deux candidats éligibles"
                            + "	qui ont la même moyenne au bac et cette moyenne est la plus petite des moyennes"
                            + "	au bac de tous les candidats éligibles du groupe et ces deux candidats "
                            + " sont dans deux groupes de classement différents'. Voeu " + v1 + " moyenne " + moyenne0);
                }

                if (v1.groupe.id.cGpCod != v0.groupe.id.cGpCod) {
                    /* on a bien trouvé un cas d'égalité */
                    break;
                }
            }

        }
    }

    private static void alerter(String message) throws VerificationException {
        throw new VerificationException("Données d'entrée non intègres: " + message);
    }

    private VerificationResultatsAlgoMeilleurBachelier() {
    }
}
