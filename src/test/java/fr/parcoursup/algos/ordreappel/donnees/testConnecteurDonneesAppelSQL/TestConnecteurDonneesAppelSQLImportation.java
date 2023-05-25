package fr.parcoursup.algos.ordreappel.donnees.testConnecteurDonneesAppelSQL;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelEntree;
import fr.parcoursup.algos.ordreappel.algo.GroupeClassement;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;
import org.junit.Test;
import org.powermock.reflect.Whitebox;


import java.util.Optional;

import static org.junit.Assert.assertThrows;

public class TestConnecteurDonneesAppelSQLImportation extends TestConnecteurDonneesAppelSQL {

    public TestConnecteurDonneesAppelSQLImportation(String name) {

        super(name);

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ///////////////////////////////////////////////////////////////////////////
    @Test(expected = Test.None.class /* no exception expected */)
    public void test_recuperation_DonneesOrdreAppel_doit_reussir() throws Exception {

        try (ConnecteurSQL connecteurSQL = this.getConnecteurSQL()) {
            ConnecteurDonneesAppelSQL connecteurDonneesAppel = new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
            AlgoOrdreAppelEntree entree = connecteurDonneesAppel.recupererDonneesOrdreAppel();

            assertEquals(entree.groupesClassements.size(), 1);
        }

    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_recuperation_verification_DonneesOrdreAppelGroupe_existant_doit_reussir() throws Exception {

        try (ConnecteurSQL connecteurSQL = this.getConnecteurSQL()) {
            ConnecteurDonneesAppelSQL connecteurDonneesAppel = new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
            GroupeClassement groupeClassement = connecteurDonneesAppel.recupererDonneesOrdreAppelGroupe(4_584);
            assertEquals(groupeClassement.voeuxClasses.size(), 2);
        }

    }

    @Test
    public void test_recuperation_donneesOrdreAppelGroupe_inexistant_doir_renvoyer_exception() {

        Throwable exception = assertThrows(AccesDonneesException.class, () -> {

            try (ConnecteurSQL connecteurSQL = this.getConnecteurSQL()) {
                ConnecteurDonneesAppelSQL connecteurDonneesAppel = new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
                connecteurDonneesAppel.recupererDonneesOrdreAppelGroupe(999_999);
            }

        });

        assertTrue(exception.getMessage().contains("Pas de groupe"));

    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_recuperation_verification_donnees_GroupeClassement_existant_doit_reussir() throws Exception {

        try (ConnecteurSQL connecteurSQL = this.getConnecteurSQL()) {
            ConnecteurDonneesAppelSQL connecteurDonneesAppel = new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
            AlgoOrdreAppelEntree entree = Whitebox.invokeMethod(connecteurDonneesAppel, "recupererDonnees", 4_584);
            assertEquals(entree.groupesClassements.size(), 1);
            Optional<GroupeClassement> groupeClassement1 = entree.groupesClassements.stream().findFirst();
            assertEquals(groupeClassement1.get().voeuxClasses.size(), 2);
        }

    }

}
