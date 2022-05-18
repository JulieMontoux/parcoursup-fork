
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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.ClassCastExceptionMessage;

import java.io.Serializable;
import java.util.Objects;

/** Classe comprenant les caractéristiques
identifiant de manière unique un voeu 
dans la base de données */
public class VoeuUID implements Serializable {

    /** l'identifiant unique du candidat dans la base de données */
    public final int gCnCod;

    /** l'identifiant unique de la formation d'affectation dans la base de données.
        Positionné à -1 pour les internats commun à plusieurs formations.*/
    public final int gTaCod;

    /** indique si le voeu comprend une demande d'internat */
    public final boolean iRhCod;

    public VoeuUID(
            int gCnCod,
            int gTaCod,
            boolean avecInternat) {
        this.gCnCod = gCnCod;
        this.gTaCod = gTaCod;
        this.iRhCod = avecInternat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException(ClassCastExceptionMessage.GLOBAL_TEST_EGALITE_IMPREVU.getMessage());
        }
        VoeuUID voeuUID = (VoeuUID) o;
        return gCnCod == voeuUID.gCnCod && gTaCod == voeuUID.gTaCod && iRhCod == voeuUID.iRhCod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gCnCod, gTaCod, iRhCod);
    }

    @Override
    public String toString() {
        return "G_CN_COD=" + gCnCod
                + " AND g_ta_cod=" + gTaCod
                + " AND I_RH_COD=" + (iRhCod ? "1" : "0");
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    private VoeuUID() {
        this.gCnCod = 0;
        this.gTaCod = 0;
        this.iRhCod = false;
    }
   
    
}
