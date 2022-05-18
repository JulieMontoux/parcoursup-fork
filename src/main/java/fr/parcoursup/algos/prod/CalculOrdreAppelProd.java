package fr.parcoursup.algos.prod;

import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppel;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelEntree;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelSortie;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;
import oracle.jdbc.pool.OracleDataSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class CalculOrdreAppelProd {

    /**
     * @param args
     * @throws AccesDonneesException
     * @throws java.sql.SQLException
     * @throws VerificationException
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    public static void main(String[] args) throws AccesDonneesException, SQLException, VerificationException, IOException, JAXBException {

        ExecutionParams params = ExecutionParams.fromEnv();

        LOGGER.info("Connection à la BD");

        /* utilisé pour renseigner le chemin vers le fichier de config  tnsnames.ora
        "When using TNSNames with the JDBC Thin driver,
        you must set the oracle.net.tns_admin property
        to the directory that contains your tnsnames.ora file."        
         */
        String tnsAdmin = System.getenv("TNS_ADMIN");
        if (tnsAdmin == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CALCUL_ORDRE_APPEL_PROD_TNS_ADMIN);
        }

        log("Connexion à la base Oracle en utilisant les paramètres de connexion du dossier TNS " + tnsAdmin);
        System.setProperty("oracle.net.tns_admin", tnsAdmin);
        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:@" + params.tnsAlias);
        ods.setUser(params.user);
        ods.setPassword(params.password);

        try (Connection connection = ods.getConnection()) {
            ConnecteurDonneesAppelSQL acces = new ConnecteurDonneesAppelSQL(connection);

            LOGGER.info("Récupération des données");
            AlgoOrdreAppelEntree entree = acces.recupererDonneesOrdreAppel();

            LOGGER.info("Calcul des ordres d'appel");
            AlgoOrdreAppelSortie sortie = AlgoOrdreAppel.calculerOrdresAppels(entree);

            LOGGER.info("Export des données");
            acces.exporterDonneesOrdresAppel(sortie);
        }

        System.exit(0);
    }

    private static final Logger LOGGER = Logger.getLogger(CalculOrdreAppelProd.class.getSimpleName());

    static void log(String msg) {
        LOGGER.info(msg);
    }

    private CalculOrdreAppelProd() {
    }

}
