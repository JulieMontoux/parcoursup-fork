package fr.parcoursup.algos.prod;

import fr.parcoursup.algos.donnees.Serialisation;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.AlgoPropositions;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvoiPropositionsProd {

	private static final Logger LOGGER = Logger.getLogger(EnvoiPropositionsProd.class.getSimpleName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String user;
		String tnsAlias;
		String password;
		String outputDir;
		String fileSuffix = LocalDateTime.now().toString();
		boolean readOnly=true;

		try {

			ExecutionParams params = ExecutionParams.fromEnv();
			user=params.user;
			tnsAlias="";
			password=params.password;
			outputDir=params.outputDir;

		}catch (Exception e) {
			// set from cmd line

			user=args[1];
			tnsAlias=args[0];
			password=args[2];
			if(args.length<4) {
				outputDir="/tmp";
			}else {
				outputDir=args[3];

			}
		}
		try {	

			/* utilisé pour renseigner le chemin vers le fichier de config  tnsnames.ora
        "When using TNSNames with the JDBC Thin driver,
        you must set the oracle.net.tns_admin property
        to the directory that contains your tnsnames.ora file."        
			 */
			final String tnsAdmin = System.getenv("TNS_ADMIN");
			if (tnsAdmin == null) {
				throw new AccesDonneesException(AccesDonneesExceptionMessage.ENVOI_PROPOSITIONS_PROD_TNS_ADMIN);
			}
			log("Connexion à la base Oracle en utilisant les paramètres de connexion du dossier TNS " + tnsAdmin);
			System.setProperty("oracle.net.tns_admin", tnsAdmin);
			OracleDataSource ods = new OracleDataSource();
			ods.setURL("jdbc:oracle:thin:@" + tnsAlias);
			ods.setUser(user);
			ods.setPassword(password);

			try (Connection connection = ods.getConnection()) {

				ConnecteurDonneesPropositionsSQL acces
				= new ConnecteurDonneesPropositionsSQL(
						connection);
				log("Récupération des données");
				AlgoPropositionsEntree entree = acces.recupererDonnees();
				if(!readOnly) {
					log("Sauvegarde locale de l'entrée");
					new Serialisation<AlgoPropositionsEntree>().serialiserEtCompresser(
							outputDir + "/" + "entree_" + fileSuffix + ".xml",
							entree,
							AlgoPropositionsEntree.class
							);
				}
				log("Calcul des propositions");
				AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

				if(!readOnly) {
					log("Sauvegarde locale de la sortie");
					new Serialisation<AlgoPropositionsSortie>().serialiserEtCompresser(
							outputDir + "/" + "sortie_" + fileSuffix + ".xml",
							sortie,
							AlgoPropositionsSortie.class
							);
				}
				log("Export des données");
				acces.exporterDonnees(sortie);

				if (sortie.getAlerte()) {
					log(sortie.getAlerteMessage());
					System.exit(1);
				} else if (sortie.getAvertissement()) {
					log("La vérification a déclenché un avertissement.");
					System.exit(2);
				} else {
					System.exit(0);
				}
			}

		} catch (SQLException | AccesDonneesException | VerificationException e) {
			log("envoiPropositions a échoué suite à l'erreur suivante.");
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
	}

	static void log(String msg) {
		LOGGER.info(msg);
	}

	private EnvoiPropositionsProd() {
	}

}
