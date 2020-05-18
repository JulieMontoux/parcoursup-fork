/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.carte.algo;

import java.util.HashMap;
import java.util.Map;
import parcoursup.exceptions.VerificationException;

/* Un domaine Onisep permet d'identifier le domaine thématique d'un diplôme.
Ce domaine est renseigné dans le champ "TFR".
Par exemple TFR="menuiserie".

Le domaine est placé dans une arborescence identifiées par les noeuds 
n+1,n+2,n+3 et n+4 (du plus particulier au plus général).

Par exemple le domaine "menuiserie"  se place dans l'arborescence sous les domaines intermédiaires 

    n1 "aménagement du bâtiment"
    n2 "bâtiment - second oeuvre"
    n3 "bâtiment - construction"
    
Ce domaine est identifié en base Onisep par la clé cleOnisep=2075

 */
public class DomaineOnisep {

    public static DomaineOnisep creerDomaineOnisep(
            int cleOnisep,
            String libelle,
            int cleOnisepSup) throws VerificationException {
        if (domaines.containsKey(cleOnisep)) {
            throw new VerificationException("DomaineOnisep: duplication de DKEY");
        }
        if(cleOnisep == NO_DKEY_SUP) {
            throw new VerificationException("DomaineOnisep: clé réservée");            
        }
        DomaineOnisep domaine = new DomaineOnisep(libelle, cleOnisepSup);
        domaines.put(cleOnisep, domaine);
        return domaine;
    }

    private DomaineOnisep(String libelle, int cleOnisepSup ) {
        this.libelle = libelle;
        this.cleOnisepSup = cleOnisepSup;
    }

    /* domaine thématique. G_KO_LIB dans G_KEY_ONI.*/
    final String libelle;

    /* clé du domaine supérieur. G_KO_COD_SUP dans G_KEY_ONI. */
    final int cleOnisepSup;
    
    /* clé utilisée en cas d'absence de domaine supérieur */
    public static final int NO_DKEY_SUP = -1;
    
    /* renvoie le domaine supérieur dans la hiérarchie, ou null si il n'y en a pas */
    DomaineOnisep getDomaineSuperieur() {
        return domaines.get(cleOnisepSup);
    }
    
    /* utilisé pour controler l'absence de duplications */
    private static final Map<Integer, DomaineOnisep> domaines = new HashMap<>();
    
    public static DomaineOnisep getDomaine(int cleOnisep) {
        return domaines.get(cleOnisep);
    }
    
}
