package fr.parcoursup.algos.donnees;

import java.sql.*;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.exceptions.AccesDonneesException;
import org.junit.Test;


public class TestConnecteurSQL {

    private final ParametresConnexionBddTest parametresConnexion;

    public TestConnecteurSQL() {

        this.parametresConnexion = new ParametresConnexionBddTest();

    }


    @Test
    public void test_creation_connecteur_avec_objet_connexion_valide() throws Exception {

        Connection conn = DriverManager.getConnection(
            this.parametresConnexion.urlBddJdbc,
            this.parametresConnexion.nomUtilisateur,
            this.parametresConnexion.mdp
        );

        try (ConnecteurSQL connecteurSQL = new ConnecteurSQL(conn)) {
            connecteurSQL.connection();
        }

    }


    @Test
    public void creation_connecteur_avec_objet_connexionNull_doit_echouer() {

        Throwable exception = assertThrows(AccesDonneesException.class, () -> {

            ConnecteurSQL connecteurSQL = new ConnecteurSQL(null);
            connecteurSQL.close();

        });

        assertTrue(exception.getMessage().contains("Impossible de créer un ConnecteurSQL"));

    }


    @Test
    public void test_creation_connecteur_avec_url_jdbc_valide() throws Exception {

        try (ConnecteurSQL connecteurSQL = new ConnecteurSQL(
                this.parametresConnexion.urlBddJdbc,
                this.parametresConnexion.nomUtilisateur,
                this.parametresConnexion.mdp
        )) {
            connecteurSQL.connection();
        }

    }


}
