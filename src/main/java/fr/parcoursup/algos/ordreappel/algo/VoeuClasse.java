
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
package fr.parcoursup.algos.ordreappel.algo;

import fr.parcoursup.algos.exceptions.ClassCastExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;

import java.io.Serializable;

public class VoeuClasse implements Serializable, Comparable<VoeuClasse> {

    /* les différents types de candidats */
    public enum TypeCandidat {
        BOURSIER_DU_SECTEUR,
        BOURSIER_HORS_SECTEUR,
        NON_BOURSIER_DU_SECTEUR,
        NON_BOURSIER_HORS_SECTEUR
    }

    /* le type du candidat */
    public final TypeCandidat typeCandidat;

    /* code identifiant le candidat dans la base de données */
    public final int gCnCod;

    /* le rang du voeu transmis par la commission de classement des voeux */
    public final int rang;

    /* le rang du voeu dans l'ordre d'appel, caculé par l'algorithme */
    private Integer rangAppel = null;
    
    public int getRangAppel() { 
        return rangAppel; 
    }

    public void setRangAppel(int rangAppel) { 
        this.rangAppel = rangAppel;
    }
    
    public VoeuClasse(
            int gCnCod,
            int rang,
            boolean estBoursier,
            boolean estDuSecteur) throws VerificationException {
        this.gCnCod = gCnCod;
        if(rang <= 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.gCnCod);
        }
        this.rang = rang;
        if(estBoursier) {
            this.typeCandidat = (estDuSecteur ? TypeCandidat.BOURSIER_DU_SECTEUR : TypeCandidat.BOURSIER_HORS_SECTEUR);
        } else {
            this.typeCandidat = (estDuSecteur ? TypeCandidat.NON_BOURSIER_DU_SECTEUR : TypeCandidat.NON_BOURSIER_HORS_SECTEUR);
        }
    }

    public boolean estBoursier() {
        return typeCandidat == TypeCandidat.BOURSIER_DU_SECTEUR
                || typeCandidat == TypeCandidat.BOURSIER_HORS_SECTEUR;
    }

    public boolean estDuSecteur() {
        return typeCandidat == TypeCandidat.BOURSIER_DU_SECTEUR
                || typeCandidat == TypeCandidat.NON_BOURSIER_DU_SECTEUR;
    }

    /* comparateur permettant de trier les voeux par ordre du groupe de classement */    
    @Override
    public int compareTo(VoeuClasse o) {
        return rang - o.rang;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VoeuClasse) {
            VoeuClasse o = (VoeuClasse) obj;
            return this.rang == o.rang; 
        } else {
            throw new ClassCastException(ClassCastExceptionMessage.VOEU_CLASSE_TEST_EGALITE_IMPREVU.getMessage());
        }
    }

    @Override
    public int hashCode() {
        return this.gCnCod;
    }
    
    @Override
    public String toString() {
        return "gCnCod=" + gCnCod;
    }
    

}
