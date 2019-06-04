
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.ordreappel.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class GroupeClassement {

    /*le code identifiant le groupe de classement dans la base de données 
        Remarque: un même groupe de classement peut être commun à plusieurs formations
     */
    public final int C_GP_COD;

    /* le taux minimum de boursiers dans ce groupe d'appel 
        (nombre min de boursiers pour 100 candidats) */
    public final int tauxMinBoursiersPourcents;

    /* le taux minimum de candidats du secteur dans ce groupe d'appel 
        (nombre min de candidats du secteur pour 100 candidats) */
    public final int tauxMinDuSecteurPourcents;

    /* la liste des candidats du groupe de classement */
    public final List<VoeuClasse> voeuxClasses = new ArrayList<>();

    public GroupeClassement(
            int C_GP_COD,
            int tauxMinBoursiersPourcents,
            int tauxMinResidentsPourcents) {
        this.C_GP_COD = C_GP_COD;
        this.tauxMinBoursiersPourcents = tauxMinBoursiersPourcents;
        this.tauxMinDuSecteurPourcents = tauxMinResidentsPourcents;
    }

    public void ajouterVoeu(VoeuClasse v) {
        voeuxClasses.add(v);
    }

    /* calcule de l'ordre d'appel */
    public OrdreAppel calculerOrdreAppel() {


        /* on crée autant de listes de candidats que de types de candidats, 
            triées par ordre de classement */
        Map<VoeuClasse.TypeCandidat, Queue<VoeuClasse>> filesAttente
                = new HashMap<>();

        for (VoeuClasse.TypeCandidat type : VoeuClasse.TypeCandidat.values()) {
            filesAttente.put(type, new LinkedList<>());
        }

        /* Chaque voeu classé est ventilé dans la liste correspondante, 
        en fonction du type du candidat. 
        Les quatre listes obtenues sont ordonnées par rang de classement, 
        comme l'est la liste voeuxClasses. */
        int nbBoursiersTotal = 0;
        int nbResidentsTotal = 0;

        /* on trie les candidats par classement, 
        les candidats les mieux classés en tête de liste  */
        voeuxClasses.sort((VoeuClasse v1, VoeuClasse v2) -> v1.rang - v2.rang);

        for (VoeuClasse voe : voeuxClasses) {

            /* on ajoute le voeu à la fin de la file (FIFO) correspondante */
            filesAttente.get(voe.typeCandidat).add(voe);

            if (voe.estBoursier()) {
                nbBoursiersTotal++;
            }
            if (voe.estDuSecteur()) {
                nbResidentsTotal++;
            }
        }

        int nbAppeles = 0;
        int nbBoursiersAppeles = 0;
        int nbResidentsAppeles = 0;

        /* la boucle ajoute les candidats un par un à la liste suivante,
            dans l'ordre d'appel */
        List<VoeuClasse> ordreAppel = new ArrayList<>();

        while (ordreAppel.size() < voeuxClasses.size()) {

            /* on calcule lequel ou lesquels des critères boursiers et candidats du secteur 
                contraignent le choix du prochain candidat dans l'ordre d'appel */
            boolean contrainteTauxBoursier
                    = (nbBoursiersAppeles < nbBoursiersTotal)
                    && (nbBoursiersAppeles * 100 < tauxMinBoursiersPourcents * (1 + nbAppeles));

            boolean contrainteTauxResident
                    = (nbResidentsAppeles < nbResidentsTotal)
                    && (nbResidentsAppeles * 100 < tauxMinDuSecteurPourcents * (1 + nbAppeles));

            /* on fait la liste des candidats satisfaisant
                les deux contraintes à la fois, ordonnée par rang de classement */
            PriorityQueue<VoeuClasse> eligibles = new PriorityQueue<>();

            for (Queue<VoeuClasse> queue : filesAttente.values()) {
                if (!queue.isEmpty()) {
                    VoeuClasse voe = queue.peek();
                    if ((voe.estBoursier() || !contrainteTauxBoursier)
                            && (voe.estDuSecteur() || !contrainteTauxResident)) {
                        eligibles.add(voe);
                    }
                }
            }

            /* stocke le meilleur candidat à appeler tout en respectant
            les deux contraintes si possible 
            ou à défaut seulement la contrainte sur le taux boursier */
            VoeuClasse meilleur = null;

            if (!eligibles.isEmpty()) {
                meilleur = eligibles.peek();
            } else {
                /* la liste peut être vide dans le cas où les deux contraintes 
                ne peuvent être satisfaites à la fois. 
                Dans ce cas nécessairement il y a une contrainte sur chacun des deux taux 
                (donc au moins un boursier non encore sélectionné) 
                et il ne reste plus de boursier du secteur, 
                donc il reste au moins un boursier hors-secteur */
                assert contrainteTauxBoursier && contrainteTauxResident;
                assert filesAttente.get(VoeuClasse.TypeCandidat.BoursierDuSecteur).isEmpty();
                assert !filesAttente.get(VoeuClasse.TypeCandidat.BoursierHorsSecteur).isEmpty();

                Queue<VoeuClasse> CandidatsBoursierNonResident
                        = filesAttente.get(VoeuClasse.TypeCandidat.BoursierHorsSecteur);

                meilleur = CandidatsBoursierNonResident.peek();
            }

            /* suppression du candidat choisi de sa file d'attente */
            Queue<VoeuClasse> queue = filesAttente.get(meilleur.typeCandidat);
            assert meilleur == queue.peek();
            queue.poll();

            /* ajout du meilleur candidat à l'ordre d'appel*/
            ordreAppel.add(meilleur);

            /* mise à jour des compteurs */
            nbAppeles++;

            if (meilleur.estBoursier()) {
                nbBoursiersAppeles++;
            }
            if (meilleur.estDuSecteur()) {
                nbResidentsAppeles++;
            }
        }

        /* mise à jour des ordres d'appel */
        int rangAppel = 1;
        for (VoeuClasse v : ordreAppel) {
            v.rangAppel = rangAppel;
            rangAppel++;
        }

        /* retourne les candidats classés dans l'ordre d'appel */
        return new OrdreAppel(ordreAppel);

    }

}
