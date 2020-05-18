
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
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
package parcoursup.propositions.algo;

public class GroupeInternatUID {

    /*l'identifiant unique de l'internat dans la base de données */
    public final int cGiCod;

    /*l'identifiant unique de la formation d'affectation dans la base de données.
        Positionné à 0 pour un internat commun à plusieurs formations.*/
    public final int gTaCod;

    public GroupeInternatUID(
            int cGiCod,
            int gTaCod) {
        this.cGiCod = cGiCod;
        this.gTaCod = gTaCod;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupeInternatUID) {
            GroupeInternatUID ta = (GroupeInternatUID) obj;
            return this.cGiCod == ta.cGiCod
                    && this.gTaCod == ta.gTaCod;
        } else {
            throw new ClassCastException("Test d'égalité imprévu");
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cGiCod ^ (gTaCod << 16));
    }

    @Override
    public String toString() {
        return "C_GI_COD=" + cGiCod + ((gTaCod != 0) ? (" AND g_ta_cod=" + gTaCod) : "");
    }

    private GroupeInternatUID() {
        this.cGiCod = 0;
        this.gTaCod = 0;
    }

}
