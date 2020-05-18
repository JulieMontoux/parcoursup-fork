
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
package parcoursup.propositions.algo;

public class GroupeAffectationUID {

    /*l'identifiant unique du groupe de classement pédagogique dans la base de données */
    public final int cGpCod;

    /*l'identifiant unique de la formation d'inscription dans la base de données.*/
    public final int gTiCod;

    /*l'identifiant unique de la formation d'affectation dans la base de données.*/
    public final int gTaCod;

    public GroupeAffectationUID(
            int cGpCod,
            int gTiCod,
            int gTacod) {
        this.cGpCod = cGpCod;
        this.gTiCod = gTiCod;
        this.gTaCod = gTacod;
    }

    public GroupeAffectationUID(GroupeAffectationUID o) {
        this(o.cGpCod, o.gTiCod, o.gTaCod);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof GroupeAffectationUID) {
            GroupeAffectationUID ta = (GroupeAffectationUID) obj;
            return this.cGpCod == ta.cGpCod
                    && this.gTiCod == ta.gTiCod
                    && this.gTaCod == ta.gTaCod;
        } else {
            throw new ClassCastException("Test d'égalité imprévu");
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cGpCod ^ (gTiCod << 10) ^ (gTaCod << 20));
    }

    @Override
    public String toString() {
        return "C_GP_COD=" + cGpCod + " AND G_TI_COD=" + gTiCod + " AND g_ta_cod=" + gTaCod;
    }

    private GroupeAffectationUID() {
        this.cGpCod = 0;
        this.gTiCod = 0;
        this.gTaCod = 0;
    }

}
