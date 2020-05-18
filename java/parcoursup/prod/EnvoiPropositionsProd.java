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

import oracle.jdbc.pool.OracleDataSource;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.donnees.ConnecteurDonneesPropositionsOracle;
import parcoursup.verification.VerificationsResultatsAlgoPropositions;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import parcoursup.donnees.Serialisation;

public class EnvoiPropositionsProd {

    private static final Logger LOGGER = Logger.getLogger(EnvoiPropositionsProd.class.getSimpleName());

    /**
     * @param args the command line arguments
     * @throws parcoursup.exceptions.AccesDonneesException
     */
    public static void main(String[] args) throws AccesDonneesException {

        try {
            if (args.length < 4) {
                log("Usage: envoiPropositions TNSAlias login password output_dir [--no-interactive]");
                System.exit(1);
            }

            String tnsAlias = args[0];
            String login = args[1];
            String password = args[2];
            boolean interactif = args.length < 5 || !args[4].contentEquals("--no-interactive");

            String outputDir = args[3];
            String fileSuffix = LocalDateTime.now().toString();

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

            try (
                    ConnecteurDonneesPropositionsOracle acces
                    = new ConnecteurDonneesPropositionsOracle(
                            ods.getConnection())) {

                log("Récupération des données");
                AlgoPropositionsEntree entree = acces.recupererDonnees();

                log("Sauvegarde locale de l'entrée");
                new Serialisation<AlgoPropositionsEntree>().serialiserEtCompresser(
                        outputDir + "/" + "entree_" + fileSuffix + ".xml",
                        entree,
                        AlgoPropositionsEntree.class
                );

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
                        = new VerificationsResultatsAlgoPropositions();
                verificationsResultats.verifier(entree, sortie);

                if (sortie.getAlerte()) {
                    log(sortie.getAlerteMessage());
                }

                if (sortie.getAvertissement()) {
                    log("La vérification a déclenché un avertissement.");
                }

                if (interactif) {
                    boolean quitter = attendreMot("exporter", "exporter les propositions");
                    if (quitter) {
                        System.exit(0);
                    }
                }

                log("Sauvegarde locale de la sortie");
                new Serialisation<AlgoPropositionsSortie>().serialiserEtCompresser(
                        outputDir + "/" + "sortie_" + fileSuffix + ".xml",
                        sortie,
                        AlgoPropositionsSortie.class
                );

                log("Export des données");
                acces.exporterDonnees(sortie);

                if (sortie.getAlerte()) {
                    System.exit(1);
                } else if (sortie.getAvertissement()) {
                    System.exit(2);
                } else {
                    System.exit(0);
                }
            }

        } catch (Exception e) {
            log("envoiPropositions a échoué suite à l'erreur suivante.");
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    static void log(String msg) {
        LOGGER.info(msg);
    }

    /* renvoie true si l'utilisateur a demandé à quitter  */
    static boolean attendreMot(String mot, String but) {
        Scanner reader = new Scanner(System.in, "utf-8");
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
