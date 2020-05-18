/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.carte.algo;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/* 
    Une recommandation.
 */
public class Recommandation {

    /* recommandation de la filiere2 sur la fiche de la filiere 1 */
    public final Filiere filiere1;

    public final Filiere filiere2;

    public final int proximite;

    public final int pctVoeuxCommuns;

    public final int proximiteSemantique;

    /* nombre minimum de voeux communs pour que deux filieres soient
    considérées comme proches */
    public static final int NB_CANDIDATS_COMMUNS_MIN = 50;
    static final int PCT_CANDIDATS_COMMUNS_MIN = 5;
    static final int PROXIMITE_MIN = 5;

    Recommandation(Filiere filiere1, Filiere filiere2, Map<String, Integer> nbOccurencesMotsCles) {

        this.filiere1 = filiere1;

        this.filiere2 = filiere2;

        int nbVoeuxCommuns = filiere1.candidatsCommuns.getOrDefault(filiere2, 0);
        int nbCandidats = filiere1.nbCandidatsAnneePrecedente();
        if (filiere1 == filiere2) {
            proximite = Integer.MAX_VALUE;
            pctVoeuxCommuns = 100;
            proximiteSemantique = 100;
        } else if ( //filiere nouvellement apparue sur Parcoursup
                nbCandidats == 0
                //trop peu de candidats en commun
                || nbVoeuxCommuns < NB_CANDIDATS_COMMUNS_MIN
                || 100 * nbVoeuxCommuns
                < PCT_CANDIDATS_COMMUNS_MIN * nbCandidats) {
            proximite = 0;
            pctVoeuxCommuns = 0;
            proximiteSemantique = 0;
        } else {

            // calcul de la proximité observée dans les voeux des candidats de l'année n-1
            // pourcentage de candidats ayant choisi une autre filiere2 parmi tous les candidats
            // ayant choisi filiere1 
            // pour les autres filières, on calcule un pourcentage des candidats
            pctVoeuxCommuns = 100 * nbVoeuxCommuns / nbCandidats;

            Set<String> motsCle = filiere1.getMotsClesOnisep();
            Set<String> motsCle2 = filiere2.getMotsClesOnisep();

            /* Proximité sémantique:
            somme des poids des différents mots clés Onisep en commun avec une autre filière
            un mot clé a poids 100 si il taggue 2 filières
            un mot clé a poids 66 si il taggue 3 filières
            un mot clé a poids 50 si il taggue 4 filières 
            ...
             */
            int proximiteScore = 0;
            Set<String> intersection = new TreeSet<>((String s1, String s2) -> s1.compareTo(s2));
            intersection.addAll(motsCle);
            intersection.retainAll(motsCle2);
            for (String motCle : intersection) {
                int nbOccurences = nbOccurencesMotsCles.getOrDefault(motCle, Integer.MAX_VALUE);
                proximiteScore += (2 * 100 / nbOccurences);
            }
            proximiteSemantique = proximiteScore;

            proximite = pctVoeuxCommuns + proximiteSemantique;
        }
    }

}
