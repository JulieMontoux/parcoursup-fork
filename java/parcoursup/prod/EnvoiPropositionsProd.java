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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;
import javax.xml.bind.JAXBException;
import oracle.jdbc.pool.OracleDataSource;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.donnees.ConnecteurDonneesPropositionsOracle;
import parcoursup.verification.VerificationsResultatsAlgoPropositions;

public class EnvoiPropositionsProd {

    /**
     * @param args the command line arguments
     * @throws javax.xml.bind.JAXBException
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws JAXBException, SQLException, IOException, Exception {

        try {

            if (args.length < 4) {
                log("Usage: envoiPropositions TNSAlias login password logfile [--no-interactive]");
                System.exit(1);
            }

            String TNSAlias = args[0];
            String login = args[1];
            String password = args[2];
            boolean interactif = args.length < 5 || !args[4].contentEquals("--no-interactive");

            String logFile = args[3];

            if (interactif) {
                log("Mode interactif activé");
                boolean quitter = attendreMot("continuer", "continuer");
                if (quitter) {
                    System.exit(0);
                }
            }

            /* utilisé pour renseigner le chemin vers le fichier de config  tnsnames.ora
        "When using TNSNames with the JDBC Thin driver,
        you must set the oracle.net.tns_admin property
        to the directory that contains your tnsnames.ora file."        
             */
            String TNS_ADMIN = System.getenv("TNS_ADMIN");
            if (TNS_ADMIN == null) {
                throw new RuntimeException("La variable d'environnement TNS_ADMIN n'est pas positionnée.");
            }
            log("Connexion à la base Oracle en utilisant les paramètres de connexion du dossier TNS " + TNS_ADMIN);
            System.setProperty("oracle.net.tns_admin", TNS_ADMIN);

            OracleDataSource ods = new OracleDataSource();

            ods.setURL("jdbc:oracle:thin:@" + TNSAlias);
            ods.setUser(login);
            ods.setPassword(password);
            Connection conn = ods.getConnection();

            log("Création de l'accès à la BD");
            ConnecteurDonneesPropositionsOracle acces
                    = new ConnecteurDonneesPropositionsOracle(conn);

            log("Récupération des données");
            AlgoPropositionsEntree entree = acces.recupererDonnees();

            log("Sauvegarde locale de l'entrée");
            entree.serialiser(null);

            if (interactif) {
                boolean quitter = attendreMot("calculer", "calculer les propositions");
                if (quitter) {
                    System.exit(0);
                }
            }

            log("Calcul des propositions");
            AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

            log("Vérification des " + sortie.propositionsDuJour().count() + " propositions");
            VerificationsResultatsAlgoPropositions verificationsResultats
                    = new VerificationsResultatsAlgoPropositions(logFile, false);
            verificationsResultats.verifier(entree, sortie);

            if (sortie.alerte) {
                log("La vérification a déclenché une alerte. Les groupes suivants "
                        + " seront ignorés lors de l'exportation: " + sortie.groupesNonExportes);
                log("Veuillez consulter le fichier de log " + logFile
                        + " pour plus de détails.");
            }

            if (sortie.avertissement) {
                log("La vérification a déclenché un avertissement.");
            }

            if (interactif) {
                boolean quitter = attendreMot("exporter", "exporter les propositions");
                if (quitter) {
                    System.exit(0);
                }
            }

            log("Sauvegarde locale de la sortie");
            sortie.serialiser(null);

            log("Export des données");
            acces.exporterDonnees(sortie);

            if (sortie.alerte) {
                System.exit(1);
            } else if (sortie.avertissement) {
                System.exit(2);
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("envoiPropositions a échoué suite à l'erreur suivante.");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    static void log(String msg) {
        System.out.println(LocalDateTime.now().toLocalTime() + ": " + msg);
    }

    /* renvoie true si l'utilisateur a demandé à quitter  */
    static boolean attendreMot(String mot, String but) {
        Scanner reader = new Scanner(System.in);
        boolean ok = true;
        while (ok) {
            log("Veuillez taper '" + mot + "' pour " + but
                    + " ou 'quitter' pour quitter le programme.");
            String entree = reader.nextLine();
            if (entree.contentEquals("quitter")) {
                return true;
            }
            ok = !entree.contentEquals(mot);
        }
        return false;
    }

}
