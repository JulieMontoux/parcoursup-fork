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

public class DemoOrdreAppel {

    public static void main(String[] args) throws Exception {

        ExempleA1 exempleA1 = new ExempleA1();
        exempleA1.execute(true);

        ExempleA2 exempleA2 = new ExempleA2();
        exempleA2.execute(true);

        ExempleA3 exempleA3 = new ExempleA3();
        exempleA3.execute(true);

        ExempleA4 exempleA4 = new ExempleA4();
        exempleA4.execute(true);

        ExempleA5 exempleA5 = new ExempleA5();
        exempleA5.execute(true);

        ExempleA6 exempleA6 = new ExempleA6();
        exempleA6.execute(true);
        
        while(true) {
            Random r = new Random();
            ExempleAleatoire e = new ExempleAleatoire(1 + r.nextInt(1000));
            e.execute(false);
        }

    }
}
