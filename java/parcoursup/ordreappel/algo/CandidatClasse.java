
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
package parcoursup.ordreappel.algo;


public class CandidatClasse {
   
    /* identifiant unique du candidat dans la base */
    public final int G_CN_COD;
    
    /* rangAppel dans l'ordre d'appel */
    public final int rangAppel;
    
    public CandidatClasse(int G_CN_COD, int rang) {
        this.G_CN_COD = G_CN_COD;
        this.rangAppel = rang;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CandidatClasse) {
            CandidatClasse ta = (CandidatClasse) obj;
            return (this.G_CN_COD == ta.G_CN_COD)
                    && (this.rangAppel == ta.rangAppel);
        } else {
            throw new RuntimeException("Test d'égalité imprévu");
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(G_CN_COD)^Integer.hashCode(rangAppel);
    }

    @Override
    public String toString() {
        return "Candidat " + G_CN_COD + " rang " + rangAppel;
    }
}
