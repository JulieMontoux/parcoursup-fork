/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
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

package parcoursup.propositions.meilleursbacheliers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.VoeuUID;


public class AlgoMeilleursBacheliersDonnees {
    
    /* liste des meilleurs bacheliers */
    public final List<MeilleurBachelier> meilleursBacheliers = new ArrayList<>();
        
    /* liste des propositions faites dans le cadre du dispositif meilleurs bacheliers */
    public final Set<VoeuUID> propositionsMeilleursBacheliers = new HashSet<>();

    /* pour chaque formation, identifiée par son G_TA_COD,
    le nombre de places réservées pour les meilleurs bacheliers. */
    public final Map<Integer, Integer > nbPlacesMeilleursBacheliers = new HashMap<>();    
    
    /* pour chaque formation, identifiée par son G_TA_COD,
    le nombre de places vacantes pour le dispositif meilleurs bacheliers,
    apès soustraction des propositions actuelles.
    */
    public final Map<Integer, Integer > nbPlacesMeilleursBacheliersVacantes  = new HashMap<>();     
    
    /* liste des groupes d'affectation  */
    public final List<GroupeAffectation> groupes = new ArrayList<>();

}
