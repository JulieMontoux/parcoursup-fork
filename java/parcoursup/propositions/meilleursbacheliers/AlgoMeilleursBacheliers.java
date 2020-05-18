/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
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
package parcoursup.propositions.meilleursbacheliers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.algo.VoeuUID;

public class AlgoMeilleursBacheliers {

    private static final Logger LOGGER = Logger.getLogger(AlgoMeilleursBacheliers.class.getSimpleName());

    /* Cette fonction de comparaison suivante est le pivot
    de l'algorithme des cmeilleurs bacheliers.
    Elle permet de trier les voeux d'un groupe d'affectation en donnant la priorité aux
    MB éligibles au dispositif dans la formation ce jour là.
    Ces MB sont interclassés par note au Bac et ordre d'appel.
    Plus le résultat est négatif, plus le voeu v1 est prioritaire sur le voeu v2.
    
    C'est le pré-ordre sur les voeux induit par
    l'ordre lexicographique sur les candidats obtenu en appliquant d'abord
    l'ordre induit par les moyennes et ensuite la position dans l'ordre d'appel.
     */
    static int comparerVoeuxSelonCritereMB(
            Voeu v1,
            Voeu v2
    ) {

        if (v1.groupe != v2.groupe) {
            throw new IllegalArgumentException("Ce comparateur ne compare "
                    + "que les voeux d'un même groupe d'affectation");
        }

        /* Regle0: on compare deux non-MB sur la base de leur ordre d'appel */
        if (!v1.getEligibleDispositifMB() && !v2.getEligibleDispositifMB()) {
            return v1.ordreAppelInitial - v2.ordreAppelInitial;
        }

        /* Regle1: les MB eligibles passent avant les autres */
        if (v1.getEligibleDispositifMB() && !v2.getEligibleDispositifMB()) {
            return -1;
        } else if (!v1.getEligibleDispositifMB() && v2.getEligibleDispositifMB()) {
            return 1;
        }

        if (!v1.estMeilleurBachelier() || !v2.estMeilleurBachelier()) {
            throw new IllegalArgumentException("Etat inconsistent de l'algo meilleur bachelier");
        }
        //a ce stade les deux voeux sont pour des MB éligibles
        /* Regle2: deux MB éligibles sont comparés par leurs moyenne au bac */
        double moy1 = v1.moyenneBac();
        double moy2 = v2.moyenneBac();

        if (moy1 > moy2) {
            return -1;
        } else if (moy1 < moy2) {
            return 1;
        }

        /* Regle3: deux MB avec la même moyenne au Bac sont comparés par leurs
            rangs dans l'ordre d'appel (valeur initiale)
         */
        return v1.ordreAppelInitial - v2.ordreAppelInitial;

    }

    /* Cette fonction met à jour l'ordre d'appel dans un groupe d'affectation */
    static void appliquerDispositifMeilleursBacheliersDansGroupeAffectation(
            GroupeAffectation groupe
    ) {
        /* réordonne les voeux selon le critère MB */
        groupe.voeuxEnAttente.sort(AlgoMeilleursBacheliers::comparerVoeuxSelonCritereMB);

        /* on tient compte de la possibilité d'avoir deux voeux consécutifss pour le même candidat */
        int dernierCandidat = -1;
        int rang = 0;
        for (Voeu v : groupe.voeuxEnAttente) {
            int gCnCod = v.id.gCnCod;
            /* on ne met à jour le rang que lorsque qu'on arrive sur un nouveau candidat */
            if (gCnCod != dernierCandidat) {
                rang++;
                dernierCandidat = gCnCod;
            }
            v.setOrdreAppel(rang);
        }
    }

    /* met à jour l'ordre d'appel dans chacun des groupes d'une formation */
    static void appliquerDispositifMeilleursBacheliersDansFormation (
            int nbPlacesVacantes,
            Set<GroupeAffectation> groupes
    ) {

        /* Deux phases. Tout d'abord on va calculer l'ensemble des
        MB destinataires des places réservées en marquant leurs voeux avec le flag
        eligiblesDispositifMB.
        Ensuite on va faire remonter ces MB dans les ordres d'appels.
         */
        for (GroupeAffectation groupe : groupes) {

            /* on fait un marquage qui remonte tous les MB en tête
            par ordre de moyenne au bac puis ordre appel */
            for (Voeu v : groupe.voeuxEnAttente) {
                v.setEligibleDispositifMB(v.estMeilleurBachelier());
            }
            groupe.voeuxEnAttente.sort(AlgoMeilleursBacheliers::comparerVoeuxSelonCritereMB);
        }

        /* on réinitialise les flags MB, qui seront mis à jour dans la prochaine boucle */
        for (GroupeAffectation groupe : groupes) {
            for (Voeu v : groupe.voeuxEnAttente) {
                v.setEligibleDispositifMB(false);
            }
        }

        /* on va descendre en // dans les listes des différents groupes,
        en marquant les voeux éligibles, et en traitant en priorité les
        meileurs moyennes. Les pointeurs permettent cette descente en //.
         */
        Map<GroupeAffectation, Integer> pointeurs = new HashMap<>();
        for (GroupeAffectation groupe : groupes) {
            pointeurs.put(groupe, 0);
        }

        Set<Integer> mbEligibles = new HashSet<>();
        while (mbEligibles.size() < nbPlacesVacantes) {

            /* la meilleur moyenne des MB en tête de chaque groupe */
            double meilleureMoyenne = -1;

            /* la liste des voeux actuellement pointés pour lesquels
            on trouve cette meilleure moyenne */
            List<Voeu> meilleursVoeux = new ArrayList<>();

            for (Entry<GroupeAffectation, Integer> entry : pointeurs.entrySet()) {
                GroupeAffectation groupe = entry.getKey();
                int index = entry.getValue();
                if (index < groupe.voeuxEnAttente.size()) {
                    Voeu v = groupe.voeuxEnAttente.get(index);
                    if (!v.estMeilleurBachelier()) {
                        /* le pointeur a atteint les non-MB
                            -> fin de la descente pour ce groupe */
                        entry.setValue(Integer.MAX_VALUE);
                    } else {
                        /* le pointeur est sur un MB. Est-il meilleur que
                        le meilleur MB des pointeurs précédents? */
                        double moyenne = v.moyenneBac();
                        int comparaison = Double.compare(moyenne, meilleureMoyenne);
                        if (comparaison > 0) {
                            /* oui, strictement, on oublie les précédents */
                            meilleureMoyenne = moyenne;
                            meilleursVoeux.clear();
                            meilleursVoeux.add(v);
                        } else if ( comparaison == 0) {
                            /* ni mieux ni moins bien: les deux MB sont incomparables,
                            on les garde tous deux
                             */
                            meilleursVoeux.add(v);
                        } else {
                            /* non, on passe au suivant */
                        }
                    }
                }
            }

            /* on a épuisé les MB de chaque groupe */
            if (meilleursVoeux.isEmpty()) {
                break;
            }

            /* on marque le voeu comme éligible et on met à jour l'ensemble des candidats éligibles
            (qui donne le nombre de places MB actuellement utilisées sur le contingent */
            for (Voeu v : meilleursVoeux) {

                v.setEligibleDispositifMB(true);
                mbEligibles.add(v.id.gCnCod);

                /* mise à jour du pointeur de la descente */
                int pointeur = pointeurs.get(v.groupe) + 1;
                pointeurs.put(v.groupe, pointeur);

                /* on vérifie si le prochain voeu dans le groupe
                est du même candidat (internat),
                si oui on descend d'un cran supplémentaire */
                if (pointeur < v.groupe.voeuxEnAttente.size()) {
                    Voeu nextv = v.groupe.voeuxEnAttente.get(pointeur);
                    if (nextv.id.gCnCod == v.id.gCnCod) {
                        nextv.setEligibleDispositifMB(true);
                        pointeurs.put(v.groupe, pointeur + 1);
                    }
                }

            }

        }

        /* on réordonne les voeux des groupes
        en mettant à jour les ordre d'appels */
        for (GroupeAffectation groupe : groupes) {
            appliquerDispositifMeilleursBacheliersDansGroupeAffectation(groupe);
        }

    }

    static void calculerNombrePlacesVacantes(
            AlgoMeilleursBacheliersDonnees entree
    ) {

        /* Liste, indexée par formation (gTaCod) des meilleurs bacheliers
        déjà affectés sur un voeu du dispositif meilleurs bacheliers.
        Le nombre de paces réservées ce jour est obtenu en déduisant
        du nombre fixé par le recteur le nombre de mb déjà affectés.
         */
        Map<Integer, Set<Integer>> mbAffectes = new HashMap<>();
        for (VoeuUID v : entree.propositionsMeilleursBacheliers) {
            int gTaCod = v.gTaCod;
            int gCnCod = v.gCnCod;
            /* création de la liste pour cette formaiton
                    si pas encore rencontrée */
            if (!mbAffectes.containsKey(gTaCod)) {
                mbAffectes.put(gTaCod, new HashSet<>());
            }
            mbAffectes.get(gTaCod).add(gCnCod);
        }


        /* on met à jour le nombre de places vacantes dans chaque formation */
        int totalPlacesVacantes = 0;
        int totalFormationsAvecPlacesVacantes = 0;
        
        entree.nbPlacesMeilleursBacheliersVacantes.clear();
        for (Entry<Integer, Integer> entry : entree.nbPlacesMeilleursBacheliers.entrySet()) {
            int gTaCod = entry.getKey();
            int nbPlacesReservees = entry.getValue();
            Set<Integer> affectes = mbAffectes.get(gTaCod);
            int nbPlacesOccupees = (affectes == null) ? 0 : affectes.size();
            int nbPlacesVacantes = Math.max(0, nbPlacesReservees - nbPlacesOccupees);
            totalPlacesVacantes += nbPlacesVacantes;
            if(nbPlacesVacantes > 0) {
                totalFormationsAvecPlacesVacantes++;
            }
            entree.nbPlacesMeilleursBacheliersVacantes.put(gTaCod, nbPlacesVacantes);
        }
        
        LOGGER.log(Level.INFO, "{0} places vacantes MB dans {1} formations",
                new Object[]{totalPlacesVacantes, totalFormationsAvecPlacesVacantes}
        );


    }

    /* Cette fonction prépare les données d'entrée pour appliquerDispositifMeilleursBacheliersDansFormation:
           1) liste des groupes d'affecttaion de chaque formation qui contiennent au moins un MB en attente
           2) nombre de places MB disponibles
     */
    public static void appliquerDispositifMeilleursBacheliers(
            AlgoMeilleursBacheliersDonnees entree
    ) {

        LOGGER.log(Level.INFO, "Application du dispositif meilleurs bacheliers dans {0}"
                + " formations", entree.nbPlacesMeilleursBacheliers.size());

        /* Réinitialisation du flag eligibleDispositifMB pour les voeux en attente
        et constitution de la liste des groupes d'affectation de chaque formation (gTaCod)
        qui ont au moins un MB en attente */
        Map<Integer, Set<GroupeAffectation>> groupesFormation = new HashMap<>();
        for (GroupeAffectation groupe : entree.groupes) {
            for (Voeu v : groupe.voeuxEnAttente) {

                v.setEligibleDispositifMB(false);

                if (v.estMeilleurBachelier()) {
                    int gTaCod = v.id.gTaCod;
                    if (!groupesFormation.containsKey(gTaCod)) {
                        groupesFormation.put(gTaCod, new HashSet<>());
                    }
                    groupesFormation.get(gTaCod).add(v.groupe);
                }
            }
        }

        LOGGER.log(Level.INFO, "Calcul du nombre de places vacantes pour les MB dans les {0} formations concern\u00e9es.", entree.nbPlacesMeilleursBacheliers.size());
        /* calcul du nombre de places vacantes pour les MB dans
        chaque formation. Le calcul met à jour entree.nbPlacesMeilleursBacheliersVacantes */
        calculerNombrePlacesVacantes(entree);

        /* on met à jour l'ordre d'appel dans toutes les formations
        dans lesquelles il reste des places vacantes.
         */
        for (Entry<Integer, Integer> entry
                : entree.nbPlacesMeilleursBacheliersVacantes.entrySet()) {
            int gTaCod = entry.getKey();
            int nbPlacesVacantes = entry.getValue();
            if (nbPlacesVacantes > 0) {
                Set<GroupeAffectation> groupes = groupesFormation.get(gTaCod);
                if (groupes != null) {
                    appliquerDispositifMeilleursBacheliersDansFormation(
                            nbPlacesVacantes,
                            groupes);
                }
            }
        }
    }

    private AlgoMeilleursBacheliers() {
    }

}
