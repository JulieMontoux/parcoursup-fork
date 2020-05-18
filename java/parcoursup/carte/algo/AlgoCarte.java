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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlgoCarte {

    public static AlgoCarteSortie calculerDonneesCarte(AlgoCarteEntree entree) {

            //on précalcule l'ensemble des mots clés de chaque filière
        Map<String, Integer> nbOccurencesMotCle = new HashMap<>();
        for (Filiere filiere : entree.filieres) {
            for (String mot : filiere.getMotsClesOnisep()) {
                int filieres = nbOccurencesMotCle.getOrDefault(mot, 0);
                filieres++;
                nbOccurencesMotCle.put(mot, filieres);
            }
        }

        ArrayList<Recommandation> recommandations = new ArrayList<>();

        for (Filiere filiere : entree.filieres) {
            /* les scores sont entre 1 et 5 */
            for (Filiere filiere2 : filiere.candidatsCommuns.keySet()) {
                Recommandation reco = new Recommandation(filiere, filiere2, nbOccurencesMotCle);
                if (reco.proximite >= Recommandation.PROXIMITE_MIN) {
                    recommandations.add(reco);
                }
            }
        }

        AlgoCarteSortie sortie = new AlgoCarteSortie();
        sortie.filieres.addAll(entree.filieres);

        /* on trie les recommandations, les moins proches en premier */
        recommandations.sort((Recommandation r1, Recommandation r2) -> r2.proximite - r1.proximite);
        int compteur = 0;
        for (Recommandation reco : recommandations) {
            //score entre 1 et 5 en fonction de la position dans la liste triée
            int score = 1 + 5 * compteur / recommandations.size();
            sortie.recommandations.put(reco, score);
            compteur++;
        }

        return sortie;
    }

    private AlgoCarte() {}
    
}

