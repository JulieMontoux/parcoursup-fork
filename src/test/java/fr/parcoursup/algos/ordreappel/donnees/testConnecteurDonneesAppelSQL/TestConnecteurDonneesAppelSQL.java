package fr.parcoursup.algos.ordreappel.donnees.testConnecteurDonneesAppelSQL;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.donnees.ParametresConnexionBddTest;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class TestConnecteurDonneesAppelSQL extends DBTestCase {

    protected final JeuDonneesBaseTestOrdreAppel jeuDonneesBase;

    protected final String contenuFichierFlatXml;

    public TestConnecteurDonneesAppelSQL(String name) {

        super(name);

        ParametresConnexionBddTest parametresConnexion = new ParametresConnexionBddTest();

        System.setProperty(
                PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS,
                parametresConnexion.driver
        );
        System.setProperty(
                PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL,
                parametresConnexion.urlBddJdbc
        );
        System.setProperty(
                PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME,
                parametresConnexion.nomUtilisateur
        );
        System.setProperty(
                PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD,
                parametresConnexion.mdp
        );

        this.jeuDonneesBase = new JeuDonneesBaseTestOrdreAppel();
        this.contenuFichierFlatXml = this.jeuDonneesBase.getFlatXml();

    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {

        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

    }

    @Override
    protected IDataSet getDataSet() throws Exception {

        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);

        InputStream inputStream = new ByteArrayInputStream(this.contenuFichierFlatXml.getBytes());
        IDataSet dataSet = builder.build(inputStream);

        return dataSet;

    }

    @Override
    protected DatabaseOperation getSetUpOperation() {

        return DatabaseOperation.CLEAN_INSERT;

    }

    @Override
    protected DatabaseOperation getTearDownOperation() {

        return DatabaseOperation.DELETE;

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests / connexion BDD
    //
    ///////////////////////////////////////////////////////////////////////////
    @Test(expected = Test.None.class /* no exception expected */)
    public void test_connexion_bdd_doit_reussir_avec_constructeur_proto1_et_parametres_connexion_valides() throws Exception {

        try (
                ConnecteurSQL connecteurSQL = new ConnecteurSQL(
                        System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                        System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                        System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
                )) {
            new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
        }
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_connexion_bdd_doit_reussir_avec_constructeur_proto2_et_parametres_connexion_valides() throws Exception {

        try (Connection conn = DriverManager.getConnection(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
        )) {
            new ConnecteurDonneesAppelSQL(conn);
        }

    }
    ///////////////////////////////////////////////////////////////////////////
    //
    // Helpers : récupération des connecteurs utilisés
    // pour les tests d'importation / exportation
    //
    ///////////////////////////////////////////////////////////////////////////

    protected ConnecteurSQL getConnecteurSQL() throws Exception {

        ConnecteurSQL connecteurSQL = new ConnecteurSQL(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
        );
        return connecteurSQL;

    }

    protected Connection getConnectionSQL(ConnecteurDonneesAppelSQL connecteurDonneesAppel) {

        Connection connection = Whitebox.getInternalState(connecteurDonneesAppel, "connection");
        return connection;

    }

}
