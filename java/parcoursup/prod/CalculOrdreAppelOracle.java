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
package parcoursup.prod;

import parcoursup.exceptions.AccesDonneesException;
import parcoursup.exceptions.VerificationException;
import parcoursup.ordreappel.CalculOrdreAppel;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppel;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppelOracle;

import java.util.logging.Logger;

public class CalculOrdreAppelOracle {

    private static final Logger LOGGER = Logger.getLogger(CalculOrdreAppelOracle.class.getSimpleName());

    public static void main(String[] args) throws AccesDonneesException, VerificationException {

        if (args.length < 3) {
            LOGGER.info("Usage: CalculOrdreAppelOracle TNSAlias login password");
            System.exit(0);
        }

        ConnecteurDonneesAppel conn = new ConnecteurDonneesAppelOracle(
                args[0],
                args[1],
                args[2]
        );

        CalculOrdreAppel calcul = new CalculOrdreAppel(conn);

        calcul.execute();

    }

}
