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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import parcoursup.ordreappel.algo.AlgoOrdreAppel;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppelOracle;

public class CalculOrdreAppelProd {

    /**
     * @param args the command line arguments
     * @throws javax.xml.bind.JAXBException
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws JAXBException, SQLException, IOException, Exception {

        if (args.length < 4) {
            LOGGER.info("Usage: envoiPropositions TNSAlias login password logfile [--no-interactive]");
            System.exit(1);
        }

        String TNSAlias = args[0];
        String login = args[1];
        String password = args[2];
        boolean interactif = args.length < 5 || !args[4].contentEquals("--no-interactive");

        String logFile = args[3];

        if (interactif) {
            LOGGER.info("Mode interactif activé");
            boolean quitter = attendreMot("continuer", "continuer");
            if (quitter) {
                System.exit(0);
            }
        }

        LOGGER.info("Connection à la BD");

        try (ConnecteurDonneesAppelOracle acces = new ConnecteurDonneesAppelOracle(TNSAlias, login, password, true)) {

            LOGGER.info("Récupération des données");
            AlgoOrdreAppelEntree entree = acces.recupererDonneesOrdreAppel();

            if (interactif) {
                boolean quitter = attendreMot("calculer", "calculer l'ordre d'appel");
                if (quitter) {
                    System.exit(0);
                }
            }

            LOGGER.info("Calcul des ordres d'appel");
            AlgoOrdreAppelSortie sortie = AlgoOrdreAppel.calculerOrdresAppels(entree);

            if (interactif) {
                boolean quitter = attendreMot("exporter", "exporter les les ordres d'appel");
                if (quitter) {
                    System.exit(0);
                }
            }

            LOGGER.info("Export des données");
            acces.exporterDonneesOrdresAppel(sortie);
            
        }

        System.exit(0);

    }

    /* renvoie true si l'utilisateur a demandé à quitter  */
    static boolean attendreMot(String mot, String but) {
        Scanner reader = new Scanner(System.in);
        boolean ok = true;
        while (ok) {
            LOGGER.log(Level.INFO, "Veuillez taper ''{0}'' pour {1} ou ''quitter'' pour quitter le programme.", new Object[]{mot, but});
            String entree = reader.nextLine();
            if (entree.contentEquals("quitter")) {
                return true;
            }
            ok = !entree.contentEquals(mot);
        }
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(CalculOrdreAppelProd.class.getSimpleName());

}
