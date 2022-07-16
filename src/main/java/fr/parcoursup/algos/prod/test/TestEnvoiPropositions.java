package fr.parcoursup.algos.prod.test;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.EnvoiPropositions;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionSQLConfig;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;

import java.io.IOException;
import java.sql.SQLException;

public class TestEnvoiPropositions {

    public static void main(String[] args) throws AccesDonneesException, VerificationException, SQLException, IOException {

        TestPropositionsConfig config = TestPropositionsConfig.fromFile();

        try (ConnecteurSQL connecteurOracleIn = new ConnecteurSQL(
                config.input.url,
                config.input.user,
                config.input.password);
             ConnecteurSQL connecteurOracleOut = new ConnecteurSQL(
                     config.output.url,
                     config.output.user,
                     config.output.password
             )
        ) {

            ConnecteurDonneesPropositionsSQL connecteurSQLIn
                    = new ConnecteurDonneesPropositionsSQL(
                    connecteurOracleIn.connection(),
                    new ConnecteurDonneesPropositionSQLConfig(false)
            );
            EnvoiPropositions envoiPropositions = new EnvoiPropositions(
                    connecteurSQLIn,
                    new ConnecteurDonneesPropositionsSQL(connecteurOracleOut.connection())
            );

            boolean logDonnees = false;
            envoiPropositions.execute(logDonnees);
        }
    }

    private TestEnvoiPropositions() {
    }

}
