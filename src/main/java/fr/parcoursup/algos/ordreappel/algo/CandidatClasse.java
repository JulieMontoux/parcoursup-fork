
/* Copyright 2019 © Ministère de l'Enseignement Supérieur, 
de la Recherche et de l'Innovation, Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr) 

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
package fr.parcoursup.algos.ordreappel.algo;

import fr.parcoursup.algos.exceptions.ClassCastExceptionMessage;

import java.io.Serializable;
import java.util.Objects;

public class CandidatClasse implements Serializable {
   
    /* identifiant unique du candidat dans la base */
    public final int gCnCod;
    
    /* rangAppel dans l'ordre d'appel */
    public final int rangAppel;
    
    public CandidatClasse(int gCnCod, int rang) {
        this.gCnCod = gCnCod;
        this.rangAppel = rang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            throw new ClassCastException(ClassCastExceptionMessage.GLOBAL_TEST_EGALITE_IMPREVU.getMessage());
        CandidatClasse that = (CandidatClasse) o;
        return gCnCod == that.gCnCod && rangAppel == that.rangAppel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gCnCod,rangAppel);
    }

    @Override
    public String toString() {
        return "Candidat " + gCnCod + " rang " + rangAppel;
    }
}
