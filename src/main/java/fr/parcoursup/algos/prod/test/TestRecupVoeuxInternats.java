package fr.parcoursup.algos.prod.test;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.prod.ExecutionParams;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.IndexInternats;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;

public class TestRecupVoeuxInternats {

    public static void main(String[] args) throws AccesDonneesException, VerificationException, SQLException, IOException, JAXBException {

        ExecutionParams params = ExecutionParams.fromEnv();
        try (ConnecteurSQL connecteurOracle = new ConnecteurSQL(
                params.url,
                params.user,
                params.password
        )) {

            ConnecteurDonneesPropositionsSQL connecteurSQL
                    = new ConnecteurDonneesPropositionsSQL(connecteurOracle.connection());

            connecteurSQL.initialiserAlgoPropositionsEntree();

            /* création de l'index internats */
            IndexInternats index = new IndexInternats();
            for (GroupeInternat internat : connecteurSQL.recupererInternats().values()) {
                index.indexer(internat.id);
            }

            /* récup desvoeux */
            connecteurSQL.recupererVoeuxAvecInternatsAClassementPropre(index, true, true);

        }
    }

    private TestRecupVoeuxInternats() {
    }

}
