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

import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.pool.OracleDataSource;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.exceptions.VerificationException;
import parcoursup.ordreappel.algo.AlgoOrdreAppel;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppelOracle;
import static parcoursup.prod.EnvoiPropositionsProd.log;

public class CalculOrdreAppelProd {

    /**
     * @param args the command line arguments
     * @throws parcoursup.exceptions.AccesDonneesException
     * @throws java.sql.SQLException
     * @throws parcoursup.exceptions.VerificationException
     */
    public static void main(String[] args) throws AccesDonneesException, SQLException, VerificationException {

        if (args.length < 4) {
            LOGGER.info("Usage: envoiPropositions TNSAlias login password logfile [--no-interactive]");
            System.exit(1);
        }

        String tnsAlias = args[0];
        String login = args[1];
        String password = args[2];
        boolean interactif = args.length < 5 || !args[4].contentEquals("--no-interactive");

        if (interactif) {
            LOGGER.info("Mode interactif activé");
            boolean quitter = attendreMot("continuer", "continuer");
            if (quitter) {
                System.exit(0);
            }
        }

        LOGGER.info("Connection à la BD");

        /* utilisé pour renseigner le chemin vers le fichier de config  tnsnames.ora
        "When using TNSNames with the JDBC Thin driver,
        you must set the oracle.net.tns_admin property
        to the directory that contains your tnsnames.ora file."        
         */
        String tnsAdmin = System.getenv("TNS_ADMIN");
        if (tnsAdmin == null) {
            throw new AccesDonneesException("La variable d'environnement TNS_ADMIN n'est pas positionnée.");
        }

        log("Connexion à la base Oracle en utilisant les paramètres de connexion du dossier TNS " + tnsAdmin);
        System.setProperty("oracle.net.tns_admin", tnsAdmin);
        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:@" + tnsAlias);
        ods.setUser(login);
        ods.setPassword(password);

        try (ConnecteurDonneesAppelOracle acces = new ConnecteurDonneesAppelOracle(ods.getConnection())) {

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
        Scanner reader = new Scanner(System.in, "utf-8");
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
