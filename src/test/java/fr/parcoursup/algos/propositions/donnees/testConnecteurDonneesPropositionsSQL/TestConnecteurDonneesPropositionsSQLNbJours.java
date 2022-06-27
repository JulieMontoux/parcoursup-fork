package fr.parcoursup.algos.propositions.donnees.testConnecteurDonneesPropositionsSQL;

import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConnecteurDonneesPropositionsSQLNbJours {

    @Test
    public void test_nb_jours_depuis_debut_campagne_doit_retourner_valeurs_correctes() throws Exception {

        Timestamp debutCampagne = Timestamp.valueOf("2021-05-20 10:15:30.00");
        Timestamp instant0_1 = Timestamp.valueOf("2021-05-20 01:00:00");
        Timestamp instant0_2 = Timestamp.valueOf("2021-05-20 23:00:00");
        Timestamp instant1_1 = Timestamp.valueOf("2021-05-21 01:00:00");
        Timestamp instant1_2 = Timestamp.valueOf("2021-05-21 23:00:00");
        Timestamp instant31_1 = Timestamp.valueOf("2021-06-20 01:00:00");
        Timestamp instant31_2 = Timestamp.valueOf("2021-06-20 23:00:00");
        Timestamp instant365_1 = Timestamp.valueOf("2022-05-20 01:00:00");
        Timestamp instant365_2 = Timestamp.valueOf("2022-05-20 23:00:00");
        Timestamp instantneg_1 = Timestamp.valueOf("2021-05-19 01:00:00");
        Timestamp instantneg_2 = Timestamp.valueOf("2021-05-19 23:00:00");

        assertEquals((int) 0,
                (int) Whitebox.invokeMethod(
                ConnecteurDonneesPropositionsSQL.class,
                "nbJoursEntre",
                debutCampagne,
                instant0_1));
        assertEquals((int) 0,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant0_2));
        assertEquals((int) 1,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant1_1));
        assertEquals((int) 1,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant1_2));
        assertEquals((int) 31,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant31_1));
        assertEquals((int) 31,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant31_2));
        assertEquals((int) 365,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant365_1));
        assertEquals((int) 365,
                (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instant365_2));
        assertTrue( (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instantneg_1) < 0);
        assertTrue( (int) Whitebox.invokeMethod(
                        ConnecteurDonneesPropositionsSQL.class,
                        "nbJoursEntre",
                        debutCampagne,
                        instantneg_2) < 0);

    }
}
