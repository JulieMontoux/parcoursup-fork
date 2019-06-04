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

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import parcoursup.carte.ExportDonneesCarte;
import parcoursup.donnees.ConnecteurOracle;

public class ExporterDonneesCarteOracle {

    private static final Logger LOGGER = Logger.getLogger(ExporterDonneesCarteOracle.class.getSimpleName());

    public static void main(String[] args) throws SQLException, IOException, JAXBException {

        if (args.length < 3) {
            LOGGER.info("Usage: envoiPropositions TNSAlias login password");
            System.exit(0);
        }

        ConnecteurOracle conn = new ConnecteurOracle(
                args[0],
                args[1],
                args[2],
                false
        );

        ExportDonneesCarte export = new ExportDonneesCarte(conn);
        
        export.exporterDonnees(true, true, false);

    }

}
