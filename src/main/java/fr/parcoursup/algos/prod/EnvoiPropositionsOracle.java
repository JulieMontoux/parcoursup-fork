package fr.parcoursup.algos.prod;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.EnvoiPropositions;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;

public class EnvoiPropositionsOracle {

    public static void main(String[] args) throws AccesDonneesException, VerificationException, SQLException, IOException, JAXBException {

        ExecutionParams params = ExecutionParams.fromEnv();
        try (ConnecteurSQL connecteurOracle = new ConnecteurSQL(
                params.url,
                params.user,
                params.password
        )) {

            ConnecteurDonneesPropositionsSQL connecteurSQL
                    = new ConnecteurDonneesPropositionsSQL(connecteurOracle.connection());
            EnvoiPropositions envoiPropositions = new EnvoiPropositions(
                    connecteurSQL,
                    connecteurSQL
            );

            boolean logDonnees = true;
            envoiPropositions.execute(logDonnees);
        }
    }

    private EnvoiPropositionsOracle() {
    }

}
