
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
package parcoursup.ordreappel.donnees;

import parcoursup.exceptions.AccesDonneesException;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.algo.GroupeClassement;

public interface ConnecteurDonneesAppel {

    /* utilisés pour le calcul de l'ordre d'appel */
    AlgoOrdreAppelEntree recupererDonneesOrdreAppel() throws AccesDonneesException;

    /* récupère les données d'appel d'un gorupe unique */
    public GroupeClassement recupererDonneesOrdreAppelGroupe(int gTaCod) throws AccesDonneesException;
    
    /* vérifie si un candidat est Boursier */
    public boolean estBoursier(int gCnCod) throws AccesDonneesException;

    /* vérifie si un candidat est considéré du secteur dans un groupe */
    public boolean estDuSecteur(int gCnCod, int cGpCod) throws AccesDonneesException;

    
    /* export des données */    
    public void exporterDonneesOrdresAppel(AlgoOrdreAppelSortie donnees) throws AccesDonneesException;

}
