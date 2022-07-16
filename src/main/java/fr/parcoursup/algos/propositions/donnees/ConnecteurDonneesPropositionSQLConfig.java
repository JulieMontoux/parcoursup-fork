package fr.parcoursup.algos.propositions.donnees;

public class ConnecteurDonneesPropositionSQLConfig {

    /**
     * flag activé en prod, désactivé en simulation
     */
    final boolean verifierInterruptionFluxDonneesEntrantes;

    /**
     * flag activé en simulation
     * Désactivé en prod.
     */
    final boolean recupererSeulementVoeuxEnAttente;

    /**
     * flag activé en simulation
     * Désactivé en prod.
     */
    final boolean inclurePropositionsRefusees;

    /**
     * flag activé en simulation, sur des données avec une remontée des classements partielle ou inexistante.
     * Désactivé en prod.
     */
    final boolean recupererSeulementVoeuxClasses;

    /**
     * paramètre permettant de récupérer une fraction des données, utilisé pour accélérer certains tests
     */
    final int sparseDataTestingMode;

    /**
     * flag activé dans certaines simulations pour pallier au manque de données sur les capacités
     * ou les ordres d'appel.
     * Désactivé en prod.
     */
    final boolean simulationAvantDebutCampagne;

    /**
     * flag activé en simulation, désactivé en prod (si nécessaires, les délétions sont effectuées par des scripts SQL)
     */
    final boolean effacerEntreesDuMemeJour;

    /**
     * flag activé dans certaines simulations, désactivé en prod.
     */
    final boolean ignorerSurbooking;

    /**
     * paramètre utilisé par certaines simulations.
     * Valaur par défaut -1 en prod.
     */
    final int simulerNbJrs;

    /**
     * Constructeuyr utilisé en prod, avec les valeurs par défaut
     */
    public ConnecteurDonneesPropositionSQLConfig() {
        this.verifierInterruptionFluxDonneesEntrantes = true;
        this.recupererSeulementVoeuxEnAttente = true;
        this.recupererSeulementVoeuxClasses = true;
        this.sparseDataTestingMode = 0;
        this.simulationAvantDebutCampagne = false;
        this.simulerNbJrs = -1;
        this.effacerEntreesDuMemeJour = false;
        this.ignorerSurbooking = false;
        this.inclurePropositionsRefusees = false;
    }

    /**
     * Constructeur utilisé en simulation et par certains tests
     */
    public ConnecteurDonneesPropositionSQLConfig(
            boolean recupererSeulementVoeuxEnAttente,
            boolean recupererSeulementVoeuxClasses,
            int sparseDataTestingMode,
            boolean simulationAvantDebutCampagne,
            int simulerNbJrs,
            boolean ignorerSurbooking
    ) {
        this.verifierInterruptionFluxDonneesEntrantes = false;
        this.recupererSeulementVoeuxEnAttente = recupererSeulementVoeuxEnAttente;
        this.inclurePropositionsRefusees = !recupererSeulementVoeuxEnAttente;
        this.recupererSeulementVoeuxClasses = recupererSeulementVoeuxClasses;
        this.sparseDataTestingMode = sparseDataTestingMode;
        this.simulationAvantDebutCampagne = simulationAvantDebutCampagne;
        this.simulerNbJrs = simulerNbJrs;
        this.effacerEntreesDuMemeJour = true;
        this.ignorerSurbooking = ignorerSurbooking;
    }

    /**
     * Constructeur utilisé par les tests unitaires
     */
    public ConnecteurDonneesPropositionSQLConfig(boolean verifierInterruptionFluxDonneesEntrantes) {
        this.verifierInterruptionFluxDonneesEntrantes = verifierInterruptionFluxDonneesEntrantes;
        this.recupererSeulementVoeuxEnAttente = true;
        this.recupererSeulementVoeuxClasses = true;
        this.sparseDataTestingMode = 0;
        this.simulationAvantDebutCampagne = false;
        this.simulerNbJrs = -1;
        this.effacerEntreesDuMemeJour = false;
        this.ignorerSurbooking = false;
        this.inclurePropositionsRefusees = false;
    }

}
