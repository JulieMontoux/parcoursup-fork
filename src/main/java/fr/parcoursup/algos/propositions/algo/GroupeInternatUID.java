
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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.ClassCastExceptionMessage;
import java.io.Serializable;
import java.util.Objects;

public class GroupeInternatUID implements Serializable {

    /*l'identifiant unique de l'internat dans la base de données */
    public final int cGiCod;

    /*l'identifiant unique de la formation d'inscription dans la base de données.
       Positionné à 0 pour un internat d'établissement.*/
    public final int gTiCod;

    /*l'identifiant unique de la formation d'affectation dans la base de données.
        Positionné à 0 pour un internat commun à plusieurs formations d'affectation.*/
    public final int gTaCod;

    public GroupeInternatUID(
            int cGiCod,
            int gTiCod,
            int gTaCod) {
        this.cGiCod = cGiCod;
        this.gTiCod = gTiCod;
        this.gTaCod = gTaCod;
    }

    public GroupeInternatUID(
            int cGiCod,
            int gTaCod) {
        this.cGiCod = cGiCod;
        this.gTiCod = 0;
        this.gTaCod = gTaCod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException(ClassCastExceptionMessage.GLOBAL_TEST_EGALITE_IMPREVU.getMessage());
        }
        GroupeInternatUID that = (GroupeInternatUID) o;
        return cGiCod == that.cGiCod && gTiCod == that.gTiCod && gTaCod == that.gTaCod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cGiCod, gTiCod, gTaCod);
    }

    @Override
    public String toString() {

        return "C_GI_COD=" + cGiCod
                + ((gTiCod != 0) ? (" AND  g_ti_cod=" + gTiCod) : "")
                + ((gTaCod != 0) ? (" AND  g_ta_cod=" + gTaCod) : "");
    }

    private GroupeInternatUID() {
        this.cGiCod = 0;
        this.gTiCod = 0;
        this.gTaCod = 0;
    }

}
