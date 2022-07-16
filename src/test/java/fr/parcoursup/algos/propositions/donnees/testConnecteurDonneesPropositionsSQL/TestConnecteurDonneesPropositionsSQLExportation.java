package fr.parcoursup.algos.propositions.donnees.testConnecteurDonneesPropositionsSQL;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.propositions.Helpers;
import fr.parcoursup.algos.propositions.algo.*;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.*;

public class TestConnecteurDonneesPropositionsSQLExportation extends TestConnecteurDonneesPropositionsSQL {

    public TestConnecteurDonneesPropositionsSQLExportation(String name) {

        super(name);

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Helpers / création objets test
    //
    ///////////////////////////////////////////////////////////////////////////
    protected GroupeAffectation creeGroupeAffectation(
            Parametres parametres,
            int gTiCod,
            int gTacod,
            int cGpCod,
            int nbRecrutementsSouhaite,
            int rangLimite,
            int rangDernierAppele
    ) throws Exception {

        GroupeAffectation groupeAffectation = new GroupeAffectation(
                nbRecrutementsSouhaite,
                new GroupeAffectationUID(cGpCod, gTiCod, gTacod),
                rangLimite,
                rangDernierAppele,
                parametres
        );

        return groupeAffectation;

    }

    protected GroupeInternat creeGroupeInternat(
            Parametres parametres,
            int cGiCod,
            int gTacod,
            int nbPlacesTotal
    ) throws Exception {

        GroupeInternat groupeInternat = new GroupeInternat(
                new GroupeInternatUID(cGiCod, gTacod),
                nbPlacesTotal
        );

        return groupeInternat;

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ///////////////////////////////////////////////////////////////////////////
    protected void exporte_donnees_nouvelles_proposition_admission(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie
    ) throws Exception {

        connecteurDonneesPropositions.exporterNouvellesPropositionsAdmission(sortie);

    }

    @Test
    public void exportation_donnees_proposition_admission_avec_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();

        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats, 90);

        int gTiCod;
        int gTacod;
        int cGpCod;
        int cGiCod;
        int gCnCod;
        int nombreLignesTable_A_ADM_PROP_AvantExportation;
        int nombreLignesTable_A_ADM_PROP_ApresExportation;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );
            Whitebox.setInternalState(sortie, "parametres", parametres);
            gTiCod = 1;
            gTacod = 1;
            cGpCod = 1;
            cGiCod = 1;
            gCnCod = 1;
            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // paramètre rangDernierAppele
            );
            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );
            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.PROPOSITION_DU_JOUR,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    // paramètre affecteHorsPP
            );
            sortie.voeux.add(voeu);
            nombreLignesTable_A_ADM_PROP_AvantExportation = this.getConnection().getRowCount("A_ADM_PROP");
            this.exporte_donnees_nouvelles_proposition_admission(connecteurDonneesPropositions, sortie);
            nombreLignesTable_A_ADM_PROP_ApresExportation = this.getConnection().getRowCount("A_ADM_PROP");
        }

        assertEquals(nombreLignesTable_A_ADM_PROP_ApresExportation, nombreLignesTable_A_ADM_PROP_AvantExportation + 1);

        PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                "SELECT count(*) "
                + "FROM A_ADM_PROP "
                + "WHERE "
                + "G_CN_COD = ? "
                + "AND G_TA_COD = ? "
                + "AND C_GP_COD = ? "
                + "AND G_TI_COD = ? "
                + "AND C_GI_COD = ? "
                + "AND I_RH_COD = 1"
                + "AND NB_JRS = ?"
        );

        ps.setInt(1, gCnCod);
        ps.setInt(2, gTacod);
        ps.setInt(3, cGpCod);
        ps.setInt(3, gTiCod);
        ps.setInt(4, cGiCod);
        ps.setInt(5, gCnCod);
        ps.setInt(6, nbJoursCampagne);

        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        assertEquals(count, 1);

    }

    @Test
    public void exportation_donnees_proposition_admission_sans_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int gCnCod = 1;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // paramètre rangDernierAppele
            );

            Voeu voeu = Helpers.creeVoeuSansInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    Voeu.StatutVoeu.PROPOSITION_DU_JOUR,
                    10 // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    // paramètre affecteHorsPP
            );

            sortie.voeux.add(voeu);

            int nombreLignesTable_A_ADM_PROP_AvantExportation = this.getConnection().getRowCount("A_ADM_PROP");

            this.exporte_donnees_nouvelles_proposition_admission(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_ADM_PROP_ApresExportation = this.getConnection().getRowCount("A_ADM_PROP");

            assertEquals(nombreLignesTable_A_ADM_PROP_ApresExportation, nombreLignesTable_A_ADM_PROP_AvantExportation + 1);

            PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                    "SELECT count(*) "
                    + "FROM A_ADM_PROP "
                    + "WHERE "
                    + "G_CN_COD = ? "
                    + "AND G_TA_COD = ? "
                    + "AND G_TI_COD = ? "
                    + "AND C_GP_COD = ? "
                    + "AND C_GI_COD = 0 "
                    + "AND I_RH_COD = 0 "
                    + "AND NB_JRS = ?"
            );

            ps.setInt(1, gCnCod);
            ps.setInt(2, gTacod);
            ps.setInt(3, gTiCod);
            ps.setInt(4, cGpCod);
            ps.setInt(5, nbJoursCampagne);

            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            assertEquals(count, 1);
        }

    }

    protected void exporte_donnees_demission_automatique(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie) throws Exception {

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "exporterDemissionsAutomatiques",
                sortie
        );

    }

    @Test
    public void exportation_donnees_demission_automatique_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int cGiCod = 1;
            int gCnCod = 1;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // rangDernierAppele
            );

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.REP_AUTO_REFUS_PROPOSITION,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    //affecteHorsPP
            );

            sortie.voeux.add(voeu);

            int nombreLignesTable_A_ADM_DEM_AvantExportation = this.getConnection().getRowCount("A_ADM_DEM");

            this.exporte_donnees_demission_automatique(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_ADM_DEM_ApresExportation = this.getConnection().getRowCount("A_ADM_DEM");

            assertEquals(nombreLignesTable_A_ADM_DEM_ApresExportation, nombreLignesTable_A_ADM_DEM_AvantExportation + 1);

            PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                    "SELECT count(*) "
                    + "FROM A_ADM_DEM "
                    + "WHERE "
                    + "G_CN_COD = ? "
                    + "AND G_TA_COD = ? "
                    + "AND C_GP_COD = ? "
                    + "AND G_TI_COD = ? "
                    + "AND C_GI_COD = ? "
                    + "AND I_RH_COD = 1"
                    + "AND NB_JRS = ?"
            );

            ps.setInt(1, gCnCod);
            ps.setInt(2, gTacod);
            ps.setInt(3, cGpCod);
            ps.setInt(4, gTiCod);
            ps.setInt(5, cGiCod);
            ps.setInt(6, nbJoursCampagne);

            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            assertEquals(count, 1);
        }

    }

    protected void exporte_donnees_predicteur_derniers_appeles_date_pivot(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Collection<GroupeAffectation> groupesAffectation,
            Parametres parametres) throws Exception {

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "exporterPredicteurRangDernierAppele",
                groupesAffectation,
                parametres
        );

    }

    @Test
    public void exportation_donnees_predicteur_derniers_appeles_date_pivot_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite,
                    10, // paramètre rangLimite,
                    10 // paramètre rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();

            groupesAffectation.add(groupeAffectation);

            Whitebox.setInternalState(
                    groupeAffectation,
                    "estimationRangDernierAppeleADatePivot",
                    20
            );

            int nombreLignesTable_A_ADM_PRED_DER_APP_AvantExportation = this.getConnection().getRowCount("A_ADM_PRED_DER_APP");

            this.exporte_donnees_predicteur_derniers_appeles_date_pivot(connecteurDonneesPropositions, groupesAffectation, parametres);

            int nombreLignesTable_A_ADM_PRED_DER_APP_ApresExportation = this.getConnection().getRowCount("A_ADM_PRED_DER_APP");

            assertEquals(nombreLignesTable_A_ADM_PRED_DER_APP_ApresExportation, nombreLignesTable_A_ADM_PRED_DER_APP_AvantExportation + 1);
        }

    }

    protected void exporte_barres_affichees_voeux_avec_internat(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie
    ) throws Exception {

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "exporterBarresAfficheesVoeuxAvecInternat",
                sortie
        );

    }

    @Test
    public void test_exportation_barres_affichees_voeux_avec_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int cGiCod = 1;

            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite,
                    10, // paramètre rangLimite,
                    10 // paramètre rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();

            groupesAffectation.add(groupeAffectation);

            Whitebox.setInternalState(sortie, "groupes", groupesAffectation);

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Map<GroupeAffectationUID, Integer> barresInternatAffichees = new HashMap<>();
            Map<GroupeAffectationUID, Integer> barresAppelAffichees = new HashMap<>();

            //on ajoute un candidat en attente pour déclencher l'exportation
            Helpers.creeVoeuAvecInternatEtInjecteDependances(0, groupeAffectation, groupeInternat, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1, 1);

            groupeInternat.barresInternatAffichees.put(
                    groupeAffectation.id,
                    10
            );

            groupeInternat.barresAppelAffichees.put(
                    groupeAffectation.id,
                    10
            );

            Collection<GroupeInternat> groupesInternat = Collections.singletonList(groupeInternat);

            Whitebox.setInternalState(sortie, "internats", groupesInternat);

            int nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation = this.getConnection().getRowCount("A_REC_GRP_INT_PROP");

            this.exporte_barres_affichees_voeux_avec_internat(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation = this.getConnection().getRowCount("A_REC_GRP_INT_PROP");

            assertEquals(nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation + 1, nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation);
        }

    }

    protected void exporte_barres_affichees_voeux_sans_internat(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie) throws Exception {

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "exporterBarresAfficheesVoeuxSansInternat",
                sortie
        );

    }

    @Test
    public void test_exportation_barres_affichees_voeux_sans_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int cGiCod = 1;

            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite,
                    10, // paramètre rangLimite,
                    10 // paramètre rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();

            groupesAffectation.add(groupeAffectation);

            Whitebox.setInternalState(sortie, "groupes", groupesAffectation);

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Map<GroupeAffectationUID, Integer> barresInternatAffichees = new HashMap<>();
            Map<GroupeAffectationUID, Integer> barresAppelAffichees = new HashMap<>();

            groupeInternat.barresInternatAffichees.put(
                    groupeAffectation.id,
                    10
            );

            groupeInternat.barresAppelAffichees.put(
                    groupeAffectation.id,
                    10
            );

            Collection<GroupeInternat> groupesInternat = new ArrayList<>();

            groupesInternat.add(groupeInternat);

            Whitebox.setInternalState(sortie, "internats", groupesInternat);

            int nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation = this.getConnection().getRowCount("A_REC_GRP_INT_PROP");

            this.exporte_barres_affichees_voeux_sans_internat(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation = this.getConnection().getRowCount("A_REC_GRP_INT_PROP");

            assertEquals(nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation, nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation + 1);
        }

    }

    protected void exporte_rang_sur_liste_attente(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie) throws Exception {

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "exporterRangsSurListeAttente",
                sortie
        );

    }

    @Test
    public void test_exportation_rang_sur_liste_attente_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int cGiCod = 1;
            int gCnCod = 1;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // rangDernierAppele
            );

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    //affecteHorsPP
            );

            Whitebox.setInternalState(voeu, "rangListeAttente", 3);

            sortie.voeux.add(voeu);

            int nombreLignesTable_A_VOE_PROP_AvantExportation = this.getConnection().getRowCount("A_VOE_PROP");

            this.exporte_rang_sur_liste_attente(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_VOE_PROP_ApresExportation = this.getConnection().getRowCount("A_VOE_PROP");

            assertEquals(nombreLignesTable_A_VOE_PROP_ApresExportation, nombreLignesTable_A_VOE_PROP_AvantExportation + 1);
        }

    }

    protected void exporte_affichages(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie) throws Exception {

        connecteurDonneesPropositions.exporterAffichages(sortie);

    }

    @Test
    public void test_exportation_affichages_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL co
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(co.connection());
            co.connection().setAutoCommit(false);

            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 761;
            int gTacod = 761;
            int cGpCod = 761;
            int cGiCod = 761;
            int gCnCod = 761;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();
            groupesAffectation.add(groupeAffectation);
            Whitebox.setInternalState(sortie, "groupes", groupesAffectation);

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Collection<GroupeInternat> groupesInternat = new ArrayList<>();
            groupesInternat.add(groupeInternat);
            Whitebox.setInternalState(sortie, "internats", groupesInternat);

            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    //affecteHorsPP
            );

            Whitebox.setInternalState(voeu, "rangListeAttente", 3);

            sortie.voeux.add(voeu);

            int nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation = this.getConnection().getRowCount(A_REC_GRP_INT_PROP);
            int nombreLignesTable_A_VOE_PROP_AvantExportation = this.getConnection().getRowCount(A_VOE_PROP);

            this.exporte_affichages(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation = this.getConnection().getRowCount(A_REC_GRP_INT_PROP);
            int nombreLignesTable_A_VOE_PROP_ApresExportation = this.getConnection().getRowCount(A_VOE_PROP);

            assertEquals(nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation + 2, nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation);
            assertEquals(nombreLignesTable_A_VOE_PROP_AvantExportation + 1, nombreLignesTable_A_VOE_PROP_ApresExportation);
        }

    }

    protected void exporte_donnees(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            AlgoPropositionsSortie sortie
    ) throws Exception {

        connecteurDonneesPropositions.exporterDonnees(sortie);

    }

    @Test
    public void test_exportation_donnees_avec_connecteur_prod_doit_reussir() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL co
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(co.connection());
            co.connection().setAutoCommit(false);

            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 111;
            int gTacod = 111;
            int cGpCod = 232;
            int cGiCod = 354;
            int gCnCod = 15678;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();

            groupesAffectation.add(groupeAffectation);

            Whitebox.setInternalState(sortie, "groupes", groupesAffectation);

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Collection<GroupeInternat> groupesInternat = new ArrayList<>();

            groupesInternat.add(groupeInternat);

            Whitebox.setInternalState(sortie, "internats", groupesInternat);

            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    //affecteHorsPP
            );

            Whitebox.setInternalState(voeu, "rangListeAttente", 3);

            sortie.voeux.add(voeu);

            int nombreLignesTable_A_ADM_PRED_DER_APP_AvantExportation = this.getConnection().getRowCount(A_ADM_PRED_DER_APP);
            int nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation = this.getConnection().getRowCount(A_REC_GRP_INT_PROP);
            int nombreLignesTable_A_VOE_PROP_AvantExportation = this.getConnection().getRowCount(A_VOE_PROP);

            this.exporte_donnees(connecteurDonneesPropositions, sortie);

            int nombreLignesTable_A_ADM_PRED_DER_APP_ApresExportation = this.getConnection().getRowCount(A_ADM_PRED_DER_APP);
            int nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation = this.getConnection().getRowCount(A_REC_GRP_INT_PROP);
            int nombreLignesTable_A_VOE_PROP_ApresExportation = this.getConnection().getRowCount(A_VOE_PROP);

            assertEquals(nombreLignesTable_A_ADM_PRED_DER_APP_AvantExportation + 1, nombreLignesTable_A_ADM_PRED_DER_APP_ApresExportation);
            assertEquals(nombreLignesTable_A_REC_GRP_INT_PROP_AvantExportation + 2, nombreLignesTable_A_REC_GRP_INT_PROP_ApresExportation);
            assertEquals(nombreLignesTable_A_VOE_PROP_AvantExportation + 1, nombreLignesTable_A_VOE_PROP_ApresExportation);
        }

    }

    @Test
    public void test_exportation_donnees_doit_modifier_valeur_flag_alerte_si_erreur_detectee() throws Exception {

        int nbJoursCampagne = this.recupereBddNombreJoursEcoulesDepuisDebutCampagne();
        int nbJoursCampagneDatePivotInternats = this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats();
        int nbJoursCampagneDebutGDD = this.recupereBddNombreTotalJoursEntreDebutCampagneEtDebutGDD();
        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats,nbJoursCampagneDebutGDD);

        try (ConnecteurSQL co
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(co.connection());
            co.connection().setAutoCommit(false);

            AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(
                    AlgoPropositionsSortie.class
            );

            Whitebox.setInternalState(sortie, "parametres", parametres);

            int gTiCod = 1;
            int gTacod = 1;
            int cGpCod = 1;
            int cGiCod = 1;
            int gCnCod = 1;

            GroupeAffectation groupeAffectation = creeGroupeAffectation(
                    parametres,
                    gTiCod,
                    gTacod,
                    cGpCod,
                    10, // paramètre nbRecrutementsSouhaite
                    10, // paramètre nbRecrutementsSouhaite
                    10 // rangDernierAppele
            );

            Collection<GroupeAffectation> groupesAffectation = new ArrayList<>();

            groupesAffectation.add(groupeAffectation);

            Whitebox.setInternalState(sortie, "groupes", groupesAffectation);

            GroupeInternat groupeInternat = creeGroupeInternat(
                    parametres,
                    cGiCod,
                    gTacod,
                    10 // parametre nbPlacesTotal
            );

            Collection<GroupeInternat> groupesInternat = new ArrayList<>();

            groupesInternat.add(groupeInternat);

            Whitebox.setInternalState(sortie, "internats", groupesInternat);

            Voeu voeu = Helpers.creeVoeuAvecInternatEtInjecteDependances(
                    gCnCod,
                    groupeAffectation,
                    groupeInternat,
                    Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                    10, // parametre ordreAppel,
                    // paramètre ordreAppelAffiche,
                    5 // paramètre rangInternat,
                    // paramètre rangRepondeur,
                    //affecteHorsPP
            );

            Whitebox.setInternalState(voeu, "rangListeAttente", 3);

            sortie.voeux.add(voeu);

            Whitebox.setInternalState(sortie, "alerte", true);

            this.exporte_donnees(connecteurDonneesPropositions, sortie);
            // doit provoquer la mise à jour du flag "alerte" dans la table G_PAR

            PreparedStatement ps = this.getConnection().getConnection().prepareStatement(
                    "SELECT count(*) "
                    + "FROM G_PAR "
                    + "WHERE "
                    + "G_PR_COD = ? "
                    + "AND G_PR_VAL = 1"
            );

            ps.setInt(1, INDEX_FLAG_ALERTE);

            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            assertEquals(count, 1);
        }

    }

}
