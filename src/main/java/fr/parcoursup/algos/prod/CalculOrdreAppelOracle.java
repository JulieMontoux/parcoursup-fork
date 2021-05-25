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
package fr.parcoursup.algos.prod;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.ordreappel.CalculOrdreAppel;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;

public class CalculOrdreAppelOracle {

    public static void main(String[] args) throws AccesDonneesException, VerificationException, SQLException, IOException, JAXBException {
        ExecutionParams params = ExecutionParams.fromEnv();
        try (
                ConnecteurSQL connecteurOracle = new ConnecteurSQL(
                        params.url,
                        params.user,
                        params.password
                )) {
            ConnecteurDonneesAppelSQL connecteurDonnesAppel = new ConnecteurDonneesAppelSQL(connecteurOracle.connection());
            CalculOrdreAppel calcul = new CalculOrdreAppel(connecteurDonnesAppel);
            calcul.execute();
        }
    }

    private CalculOrdreAppelOracle() {
    }

}
