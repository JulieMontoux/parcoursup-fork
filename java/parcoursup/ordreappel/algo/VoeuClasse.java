
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
package parcoursup.ordreappel.algo;

public class VoeuClasse implements Comparable<VoeuClasse> {

    /* les différents types de candidats */
    public enum TypeCandidat {
        BoursierDuSecteur,
        BoursierHorsSecteur,
        NonBoursierDuSecteur,
        NonBoursierHorsSecteur
    };

    /* le type du candidat */
    public final TypeCandidat typeCandidat;

    /* code identifiant le candidat dans la base de données */
    public final int G_CN_COD;

    /* le rang du voeu transmis par la commission de classement des voeux */
    public final int rang;

    /* le rang du voeu dans l'ordre d'appel, caculé par l'algorithme */
    public int rangAppel = 0;
    
    public VoeuClasse(
            int G_CN_COD,
            int rang,
            boolean estBoursier,
            boolean estDuSecteur) {
        this.G_CN_COD = G_CN_COD;
        this.rang = rang;
        this.typeCandidat
                = estBoursier
                        ? (estDuSecteur ? TypeCandidat.BoursierDuSecteur : TypeCandidat.BoursierHorsSecteur)
                        : (estDuSecteur ? TypeCandidat.NonBoursierDuSecteur : TypeCandidat.NonBoursierHorsSecteur);
    }

    public boolean estBoursier() {
        return typeCandidat == TypeCandidat.BoursierDuSecteur
                || typeCandidat == TypeCandidat.BoursierHorsSecteur;
    }

    public boolean estDuSecteur() {
        return typeCandidat == TypeCandidat.BoursierDuSecteur
                || typeCandidat == TypeCandidat.NonBoursierDuSecteur;
    }

    /* comparateur permettant de trier les voeux par ordre du groupe de classement */    
    @Override
    public int compareTo(VoeuClasse o) {
        return rang - o.rang;
    }

}
