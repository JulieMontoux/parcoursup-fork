
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
package parcoursup.prod;

import java.sql.SQLException;
import java.util.Scanner;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppelOracle;
import parcoursup.ordreappel.modifications.ModifClassement;

/* Prise en compte des remontée tardives de classements par les formations,
            après la date de calcul de l'ordre d'appel (effectué une unique fois).
            Dans ce cas on recalcule uniquement l'ordre d'appel de la formation
            concernée.
 */
public class ModifClassementProd {

    /* récupère les données d'insertion et renvoie le nouveau rang dans l'ordre d'appel */
    public static void main(String[] args) throws SQLException, Exception {
        
        if (args.length < 3) {
            System.out.println(
                    "Usage: calculeNouveauRangOrdreAppel serveur login password");
            return;
        }
        
        String url = args[0];
        String login = args[1];
        String password = args[2];
        
        Scanner reader = new Scanner(System.in);
        System.out.println("G_CN_COD? ");
        int G_CN_COD = Integer.parseInt(reader.nextLine());
        
        System.out.println("C_GP_COD? ");
        int C_GP_COD = Integer.parseInt(reader.nextLine());
        
        System.out.println("nouveauClassement? ");
        int nouveauClassement = Integer.parseInt(reader.nextLine());
        
        System.out.println("Calcul du nouveau rang dans l'ordre d'appel pour le candidat "
                + G_CN_COD + " dans le groupe " + C_GP_COD + " et classement " + nouveauClassement);
        
        ConnecteurDonneesAppelOracle acces = 
                new ConnecteurDonneesAppelOracle(url,login,password,false);
        
        int nouvelOrdreAppel = ModifClassement.calculeNouveauRangOrdreAppel(
                acces,
                G_CN_COD,                
                C_GP_COD,
                nouveauClassement);
        
        System.out.println("Nouveau rang dans l'ordre d'appel " + nouvelOrdreAppel);
        
    }
 
}
