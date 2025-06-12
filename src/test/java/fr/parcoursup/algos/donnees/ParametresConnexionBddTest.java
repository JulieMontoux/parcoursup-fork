package fr.parcoursup.algos.donnees;

import java.util.Objects;

/**
 * Paramètres de connexion utilisés uniquement pour les tests d’intégration.
 *
 * <p>Les valeurs sont lues :
 * <ul>
 *   <li>en priorité dans les propriétés JVM (-DCLÉ=valeur),</li>
 *   <li>puis dans les variables d’environnement (même nom).</li>
 * </ul>
 *
 * <p>Si l’une des valeurs obligatoires est absente ou vide,
 * le constructeur lève immédiatement une {@link IllegalStateException}.
 */
public final class ParametresConnexionBddTest {

    // -----------------------------------------------------------------------
    // Constantes (noms des propriétés/variables d’environnement)
    // -----------------------------------------------------------------------
    private static final String PROP_DRIVER       = "DRIVER_BDD_TEST";
    private static final String PROP_URL          = "URL_BDD_TEST";
    private static final String PROP_UTILISATEUR  = "UTILISATEUR_BDD_TEST";
    private static final String PROP_MDP          = "MDP_BDD_TEST";

    // -----------------------------------------------------------------------
    // Champs exposés en lecture seule
    // -----------------------------------------------------------------------
    private final String driver;
    private final String urlBddJdbc;
    private final String nomUtilisateur;
    private final String mdp;

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------
    public ParametresConnexionBddTest() {
        this.driver         = getRequiredProperty(PROP_DRIVER);
        this.urlBddJdbc     = normaliseUrl(getRequiredProperty(PROP_URL));
        this.nomUtilisateur = getRequiredProperty(PROP_UTILISATEUR);
        this.mdp            = getRequiredProperty(PROP_MDP);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------
    public String getDriver()        { return driver; }
    public String getUrlBddJdbc()    { return urlBddJdbc; }
    public String getNomUtilisateur(){ return nomUtilisateur; }
    public String getMdp()           { return mdp; }

    // -----------------------------------------------------------------------
    // Méthodes utilitaires privées
    // -----------------------------------------------------------------------
    /** Récupère la propriété/variable et garantit qu’elle n’est pas vide. */
    private static String getRequiredProperty(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);       // fallback env var
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Le paramètre « " + key + " » est manquant. "
                            + "Passe-le en option JVM (-D" + key + "=...) ou variable d’environnement."
            );
        }
        return value.trim();
    }

    /** Unifie la forme de l’URL JDBC pour éviter les erreurs de parsing. */
    private static String normaliseUrl(String rawUrl) {
        // Remplace les \ Windows par des /, si jamais on utilise un chemin local (H2, SQLite, etc.).
        return rawUrl.replace('\\', '/');
    }
}