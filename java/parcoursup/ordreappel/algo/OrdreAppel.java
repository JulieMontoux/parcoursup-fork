
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

import java.util.ArrayList;
import java.util.List;


public class OrdreAppel {

    /* la liste des candidats, en ordre d'appel croissant */
    public final List<CandidatClasse> candidats = new ArrayList<>();

    /* construit l'ordre d'appel à partir de la liste des voeux classés dans l'ordre d'appel */
    public OrdreAppel(List<VoeuClasse> voeux) {
        
        /* ajoute les candidats dans l'ordre d'appel */
        for(VoeuClasse voe : voeux) {
            candidats.add(new CandidatClasse(voe.gCnCod, voe.getRangAppel()));
        }

        candidats.sort((CandidatClasse c1,CandidatClasse c2) -> c1.rangAppel - c2.rangAppel);
        
    }
        
    
}
