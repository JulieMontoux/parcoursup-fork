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
package parcoursup.ordreappel.exemples;

import java.util.Random;
import parcoursup.ordreappel.algo.GroupeClassement;
import parcoursup.ordreappel.algo.VoeuClasse;

public class ExempleAleatoire extends ExempleOrdreAppel {

    final int nbCandidats;

    public ExempleAleatoire(int nbCandidats) {
        this.nbCandidats = nbCandidats;
    }

    @Override
    String nom() {
        return "exemple_aleatoire";
    }

    @Override
    GroupeClassement initialise() {

        Random r = new Random();

        int tauxMinBoursier = r.nextInt(50);

        boolean seulementTauxBoursiers = r.nextBoolean();
        int tauxMinResident = (seulementTauxBoursiers ? 0 : r.nextInt(99));

        GroupeClassement groupe = new GroupeClassement(r.nextInt(), tauxMinBoursier, tauxMinResident);

        /* C1 C2 C3 C4 C5 B6 B7 C8 */
        for (int i = 1; i <= nbCandidats; i++) {
            groupe.ajouterVoeu(
                    new VoeuClasse(
                            i,
                            i,
                            r.nextBoolean(),
                            seulementTauxBoursiers || r.nextBoolean())
            );
        }

        return groupe;
    }

}
