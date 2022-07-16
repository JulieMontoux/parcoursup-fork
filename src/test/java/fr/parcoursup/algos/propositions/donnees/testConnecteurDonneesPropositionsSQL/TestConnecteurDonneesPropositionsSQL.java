package fr.parcoursup.algos.propositions.donnees.testConnecteurDonneesPropositionsSQL;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.donnees.ParametresConnexionBddTest;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionSQLConfig;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestConnecteurDonneesPropositionsSQL extends DBTestCase {

    protected final JeuDonneesBaseTestPropositions jeuDonneesBase;

    protected final String contenuFichierFlatXml;

    public TestConnecteurDonneesPropositionsSQL(String name) {

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

        this.jeuDonneesBase = new JeuDonneesBaseTestPropositions();
        
        this.contenuFichierFlatXml = this.jeuDonneesBase.getFlatXml();
                
    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {

        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

    }

    @Override
    protected DatabaseOperation getSetUpOperation() {

        return DatabaseOperation.CLEAN_INSERT;

    }

    @Override
    protected DatabaseOperation getTearDownOperation() {

        return DatabaseOperation.DELETE;

    }

    @Override
    protected IDataSet getDataSet() throws Exception {

        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);

        InputStream inputStream = new ByteArrayInputStream(this.contenuFichierFlatXml.getBytes());
        IDataSet dataSet = builder.build(inputStream);

        return dataSet;

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests / connexion BDD
    //
    ///////////////////////////////////////////////////////////////////////////
    @Test(expected = Test.None.class /* no exception expected */)
    public void test_connexion_bdd_doit_reussir_avec_constructeur_proto1_et_parametres_connexion_valides() throws Exception {
        try (ConnecteurSQL connecteurSQL = new ConnecteurSQL(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD))) {
                ConnecteurDonneesPropositionsSQL connecteur
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
                assertNotNull(connecteur);
        }
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_connexion_bdd_doit_reussir_avec_constructeur_proto2_et_parametres_connexion_valides() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
        )) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(conn);
        }
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_connexion_bdd_doit_reussir_avec_constructeur_proto3_et_parametres_connexion_valides() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
        )) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(
                    conn,
                    new ConnecteurDonneesPropositionSQLConfig(false)
            );
        }
    }



    ///////////////////////////////////////////////////////////////////////////
    //
    // Helpers : récupération des connecteurs utilisés
    // pour les tests d'importation / exportation
    //
    ///////////////////////////////////////////////////////////////////////////
    public static ConnecteurSQL getConnecteurDonneesProd() throws Exception {
        return new ConnecteurSQL(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
        );
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Helpers : accès et modification des valeurs enregistrées dans le schéma
    // de test
    //
    ///////////////////////////////////////////////////////////////////////////
    protected static final int INDEX_FLAG_INTERRUP_DONNEES = 31;

    protected static final int INDEX_FLAG_ALERTE = 34;

    protected static final int INDEX_PARAMETRE_DATE_DEBUT_CAMPAGNE = 35;

    protected static final int INDEX_PARAMETRE_DATE_DEBUT_GDD = 316;

    protected static final int INDEX_PARAMETRE_DATE_FIN_ORD_GDD = 437;

    protected static final int INDEX_PARAMETRE_DATE_OUV_COMP_INTERNATS = 334;

    protected String getBddChaineDate(int indiceParametre) throws Exception {

        PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                "SELECT g_pr_val FROM g_par WHERE g_pr_cod = ?"
        );
        ps.setInt(1, indiceParametre);

        ResultSet rs = ps.executeQuery();
        rs.next();

        String chaineDateFormatee = rs.getString(1);

        return chaineDateFormatee;

    }

    protected void setBddChaineDate(String chaineDate, int indiceParametre) throws Exception {

        PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                "UPDATE G_PAR SET g_pr_val = ? WHERE g_pr_cod = ?"
        );

        ps.setString(1, chaineDate);
        ps.setInt(2, indiceParametre);
        ps.executeUpdate();

    }

    protected LocalDateTime getDateFromChaineDate(String chaineDate) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy:HHmm");
        LocalDateTime date = LocalDateTime.parse(chaineDate, dtf);
        return date;

    }

    protected LocalDateTime getBddDateDebutCampagne() throws Exception {
        String chaineDateDebutCampagne = this.getBddChaineDate(INDEX_PARAMETRE_DATE_DEBUT_CAMPAGNE);
        LocalDateTime dateDebutCampagne = this.getDateFromChaineDate(chaineDateDebutCampagne);
        return dateDebutCampagne;
    }

    protected LocalDateTime getBddDateDebutGDD() throws Exception {
        String chaine = this.getBddChaineDate(INDEX_PARAMETRE_DATE_DEBUT_GDD);
        LocalDateTime date = this.getDateFromChaineDate(chaine);
        return date;
    }

    protected LocalDateTime getBddDateFinOrdGDD() throws Exception {
        String chaine = this.getBddChaineDate(INDEX_PARAMETRE_DATE_FIN_ORD_GDD);
        LocalDateTime date = this.getDateFromChaineDate(chaine);
        return date;
    }

    protected void setBddDateDebutCampagne(String chaineDateDebutCampagne) throws Exception {
        this.setBddChaineDate(chaineDateDebutCampagne, INDEX_PARAMETRE_DATE_DEBUT_CAMPAGNE);
    }

    protected void setBddDateDebutGDD(String chaineDateDebutCampagne) throws Exception {
        this.setBddChaineDate(chaineDateDebutCampagne, INDEX_PARAMETRE_DATE_DEBUT_GDD);
    }

    protected void setBddDateFinOrdGDD(String chaineDateDebutCampagne) throws Exception {
        this.setBddChaineDate(chaineDateDebutCampagne, INDEX_PARAMETRE_DATE_FIN_ORD_GDD);
    }

    protected LocalDateTime getBddDateOuvertureCompleteInternats() throws Exception {

        String chaineDateOuvertureCompleteInternats = this.getBddChaineDate(INDEX_PARAMETRE_DATE_OUV_COMP_INTERNATS);
        LocalDateTime dateOuvertureCompleteInternats = this.getDateFromChaineDate(chaineDateOuvertureCompleteInternats);
        return dateOuvertureCompleteInternats;

    }

    protected void setBddDateOuvertureCompleteInternats(String chaineDateOuvertureCompleteInternats) throws Exception {

        this.setBddChaineDate(chaineDateOuvertureCompleteInternats, INDEX_PARAMETRE_DATE_OUV_COMP_INTERNATS);

    }

    protected int recupereBddNombreJoursEcoulesDepuisDebutCampagne() throws Exception {

        LocalDateTime maintenant = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime dateDebutCampagne = this.getBddDateDebutCampagne().toLocalDate().atStartOfDay();
        long nombreJoursEcoules = Duration.between(dateDebutCampagne, maintenant).toDays() + 1;
        return Math.toIntExact(nombreJoursEcoules);

    }

    protected int recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats() throws Exception {

        LocalDateTime dateDebutCampagne = this.getBddDateDebutCampagne().toLocalDate().atStartOfDay();
        LocalDateTime dateOuvertureCompleteInternats = this.getBddDateOuvertureCompleteInternats().toLocalDate().atStartOfDay();
        long nombreJoursEcoules = 1 + Duration.between(dateDebutCampagne, dateOuvertureCompleteInternats).toDays();
        return Math.toIntExact(nombreJoursEcoules);

    }

    protected int recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD() throws Exception {

        LocalDateTime dateDebutCampagne = this.getBddDateDebutCampagne().toLocalDate().atStartOfDay();
        LocalDateTime dateDebutGDD = this.getBddDateDebutGDD().toLocalDate().atStartOfDay();
        long nombreJoursEcoules = 1 + Duration.between(dateDebutCampagne, dateDebutGDD).toDays();
        return Math.toIntExact(nombreJoursEcoules);

    }

    protected int recupereBddNombreTotalJoursEntreDebutCampagneEtFinOrdVoeuxGDD() throws Exception {

        LocalDateTime dateDebutCampagne = this.getBddDateDebutCampagne().toLocalDate().atStartOfDay();
        LocalDateTime finOrdGDD = this.getBddDateFinOrdGDD().toLocalDate().atStartOfDay();
        long nombreJoursEcoules = 1 + Duration.between(dateDebutCampagne, finOrdGDD).toDays();
        return Math.toIntExact(nombreJoursEcoules);

    }

    protected boolean getBddValeurFlag(int indiceParametre) throws Exception {

        PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                "SELECT g_pr_val FROM g_par WHERE g_pr_cod = ?"
        );
        ps.setInt(1, indiceParametre);

        ResultSet rs = ps.executeQuery();
        rs.next();

        boolean valeurFlag = rs.getBoolean(1);
        return valeurFlag;

    }

    protected void setValeurFlag(int valeurFlag, int indiceParametre) throws Exception {

        PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                "UPDATE G_PAR SET G_PR_VAL= ? WHERE G_PR_COD = ?"
        );

        ps.setInt(1, valeurFlag);
        ps.setInt(2, indiceParametre);
        ps.executeUpdate();

    }

    protected boolean getBddValeurFlagInterruptionFluxDonnees() throws Exception {

        return this.getBddValeurFlag(INDEX_FLAG_INTERRUP_DONNEES);

    }

    protected void setValeurFlagInterruptionFluxDonnees(int valeurFlag) throws Exception {

        this.setValeurFlag(valeurFlag, INDEX_FLAG_INTERRUP_DONNEES);

    }

    protected boolean getBddValeurFlagAlerte() throws Exception {

        return this.getBddValeurFlag(INDEX_FLAG_ALERTE);

    }

    protected void setBddValeurFlagAlerte(int valeurFlag) throws Exception {

        this.setValeurFlag(valeurFlag, INDEX_FLAG_ALERTE);

    }

}
