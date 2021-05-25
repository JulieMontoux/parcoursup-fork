package fr.parcoursup.algos.donnees;


public class ParametresConnexionBddTest {

    public final String driver;
    public final String urlBddJdbc;
    public final String nomUtilisateur;
    public final String mdp;


    public ParametresConnexionBddTest() {

        this.driver = System.getProperty("DRIVER_BDD_TEST");
        this.urlBddJdbc = System.getProperty("URL_BDD_TEST");
        this.nomUtilisateur = System.getProperty("UTILISATEUR_BDD_TEST");
        this.mdp = System.getProperty("MDP_BDD_TEST");

    }

}
