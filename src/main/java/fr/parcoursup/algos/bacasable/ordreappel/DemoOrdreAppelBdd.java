package fr.parcoursup.algos.bacasable.ordreappel;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;
import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppel;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelEntree;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelSortie;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;
import org.javalite.activejdbc.DB;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class DemoOrdreAppelBdd {
    
    private static final Logger LOGGER = Logger.getLogger("");

    public static void main(String[] args) throws Exception {

        ///////////////////////////////////////////////////////////////////////////////////////////
        //
        // Exemple de calcul d'ordre d'appel.
        //
        // Le scénario proposé ici reprend l'exemple décrit dans le document de présentation des
        // algorithmes de Parcoursup 2019, point 4.1, "Calcul de l'ordre d'appel pour les
        // formations soumises au seul taux minimum boursiers".
        //
        // Les données correspondant aux conditions initiales de ce scénario (paramétrage de
        // la formation, des candidatures, etc.) sont enregistrées en base de données avec des
        // jeux de valeurs similaires à ceux utilisés en production. Cet enregistrement est
        // effectué par l'intermédiaire d'un composant ORM (PeuplementBdd) destiné à faciliter
        // l'insertion séquentielle de l'ensemble des données requises.
        // 
        // L'ordre d'appel calculé après application de l'algorithme est enregistré
        // dans la table C_CAN_GRP, d'où les informations sont ensuite extraites pour l'affichage
        // des résultats.
        //
        // Programme à lancer avec :
        // 
        // mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ExempleTestOrdreAppelBdd"
        //
        // ou bien  :
        //
        // mvn clean activejdbc-instrumentation:instrument exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.ExempleTestOrdreAppelBdd"
        //
        ///////////////////////////////////////////////////////////////////////////////////////////
        
        InputStream is = DemoOrdreAppelBdd.class.getResourceAsStream("/properties-from-pom.properties");

        Properties properties = new Properties();
        properties.load(is);
        

        // Les données sont injectées dans le base de données "bac à sable" dont les tables concernées doivent
        // être préalablement vidées

        String driver = properties.getProperty("DRIVER_BDD_BAC_A_SABLE");
        String urlBddJdbc = properties.getProperty("URL_BDD_BAC_A_SABLE");
        String nomUtilisateur = properties.getProperty("UTILISATEUR_BDD_BAC_A_SABLE");
        String mdp = properties.getProperty("MDP_BDD_BAC_A_SABLE");

        DB db = new DB();
        
        db.open(driver, urlBddJdbc, nomUtilisateur, mdp);

        ParametreApplication.deleteAll();
        Filiere.deleteAll();
        Candidat.deleteAll();
        TypeFormation.deleteAll();
        FormationInscription.deleteAll();
        JuryPedagogique.deleteAll();
        FormationAffectation.deleteAll();
        Formation.deleteAll();
        GroupeAffectationFormation.deleteAll();
        GroupeClassementPedagogique.deleteAll();
        GroupeAffectationInternat.deleteAll();
        Candidature.deleteAll();
        PositionCandidatDansGroupeClassementPedagogique.deleteAll();
        PositionCandidatDansGroupeClassementInternat.deleteAll();
        SituationVoeu.deleteAll();
        Voeu.deleteAll();
        PropositionAdmission.deleteAll();
        EtatBarresAdmission.deleteAll();

        
        //
        // Les données requises pour la formalisation du scénario sont enregistrées
        // séquentiellement par l'intermédiaire d'objets facilitant le paramétrage
        // et la définition des liens entre concepts / entités enregistrés en base
        // de données. La séquence suivie découle des relations liant les différentes
        // tables entre elles.
        //
        
        
        //
        // Paramétrage et enregistrement des données relatives à la filière de formation
        //
        
        Filiere filiere = new Filiere(
            453           // identifiant numérique de la filière
        );

        filiere.insert();

        
        //
        // Paramétrage et enregistrement des données relatives au type de formation
        //
        
        TypeFormation typeFormation = new TypeFormation(
            12            // identifiant numérique du type de formation

        );

        typeFormation.insert();

        
        //
        // Paramétrage relatif à l'établissement
        //
        
        Etablissement etablissement = new Etablissement(
            "e1"          // identifiant de l'établissement
        );

        
        //
        // Paramétrage et enregistrement des données relatives à la formation d'inscription
        //
        // Une formation d'inscription est liée à une filière, un type de formation et
        // un établissement
        //
        
        HashMap<String, Object> parametrageFormationInscription = new HashMap<>();
        
        parametrageFormationInscription.put(
            FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
            1   // 1 = paramétrage de la formation effectué (valeur requise dans ce contexte)
        );
        
        parametrageFormationInscription.put(
            FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2  // 2 = classement du dossier terminé (valeur requise dans ce contexte)
        );    

        FormationInscription formationInscription = new FormationInscription(
            435,                          // identifiant numérique de la formation d'inscription
            etablissement,                // établissement associé
            typeFormation,                // type de formation
            filiere,                      // filière
            parametrageFormationInscription        
        );

        formationInscription.insert();

        
        //
        // Paramétrage et enregistrement des données relatives au jury pédagogique
        //
        // Un jury pédagogique est lié à une formation d'inscription
        //

        JuryPedagogique juryPedagogique = new JuryPedagogique(
            127,                    // identifiant numérique du jury
            formationInscription    // formation d'inscription
        );

        juryPedagogique.insert();

        
        //
        // Paramétrage et enregistrement des données relatives à la formation d'affectation
        //
        
        FormationAffectation formationAffectation = new FormationAffectation(
            729   // identifiant numérique de la formation d'affectation
        );

        formationAffectation.insert();

        
        //
        // Paramétrage et enregistrement des données relatives à la formation
        //
        // Une formation établit un lien entre une formation d'inscription
        // et une formation d'affectation
        //

        int tauxMinimumBoursiers = 25;

        HashMap<String, Object> parametrageFormation = new HashMap<>();
        
        parametrageFormation.put(
            Formation.CAPACITE_TOTALE_FORMATION,
            10
        );
        
        parametrageFormation.put(
            Formation.CODE_TAUX_MINIMUM_BOURSIERS_APPLIQUE,
            1     // 1 = un taux minimum de boursiers doit être appliqué
        );
        
        parametrageFormation.put(
            Formation.TAUX_MINIMUM_BOURSIERS,
            tauxMinimumBoursiers
        );

        Formation formation = new Formation(
            formationInscription, // formation d'inscription
            formationAffectation, // formation d'affectation
            parametrageFormation
        );

        formation.insert();

        
        //
        // Paramétrage et enregistrement des données relatives au groupe d'affectation
        //
        // Un groupe d'affectation est lié à une formation (ex. : groupe d'étudiants
        // issus de classes préparatoires)
        //
        
        HashMap<String, Object> parametrageGroupeAffectationFormation = new HashMap<>();
        
        parametrageGroupeAffectationFormation.put(
            GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
            15
        );

        GroupeAffectationFormation groupeAffectationFormation = new GroupeAffectationFormation(
            4_584, // identifiant numérique de la formation d'affectation
            formation, // formation
            parametrageGroupeAffectationFormation
        );

        groupeAffectationFormation.insert();

        
        //
        // Paramétrage et enregistrement des données relatives au groupe de classement pédagogique
        //
        // Un groupe de classement pédagogique est lié à un groupe d'affectation
        //

        HashMap<String, Object> parametrageGroupeClassementPedagogique = new HashMap<>();

        parametrageGroupeClassementPedagogique.put(
            GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2    // 2 = classement finalisé (valeur requise dans ce contexte)
        );

        parametrageGroupeClassementPedagogique.put(
            GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
            0    // 0 = formation établissant un classement (valeur requise dans ce contexte)
        );     

        GroupeClassementPedagogique groupeClassementPedagogique = new GroupeClassementPedagogique(
            groupeAffectationFormation, // groupe d'affectation / formation
            parametrageGroupeClassementPedagogique
        );

        groupeClassementPedagogique.insert();

        
        //
        // Paramétrage relatif aux régimes d'hébergement
        //
        
        RegimeHebergement regimeHebergementSansInternat = new RegimeHebergement(
            0 // code  0 = pas d'hébergement en internat
        );

        //
        // Situations possibles des voeux
        //
        
        SituationVoeu situationVoeuEnAttente = new SituationVoeu(
            1, // identifiant numérique
            1, // code 1 = voeu en attente 
            0, // code 0 = voeu non affecte 
            0,  // code 0 = voeu non clôturé
                0
        );

        situationVoeuEnAttente.insert();

        
        //
        // Paramétrage et enregistrement des données relatives aux candidats, candidatures, voeux et classements
        //
        
        int nombreTotalCandidats = 20;

        List<Integer> identifiantsCandidatsBoursiers = new ArrayList<>();
        identifiantsCandidatsBoursiers.add(3);
        identifiantsCandidatsBoursiers.add(7);
        identifiantsCandidatsBoursiers.add(17);

        int compteurCandidatsBoursiers = 1;
        int compteurCandidatsNonBoursiers = 1;

        HashMap<String, Object> parametrageCandidat;
        HashMap<String, Object> parametragePositionCandidatDansGroupeClassementPedagogique;

        for (int i = 1; i <= nombreTotalCandidats; i++) {

            Candidat candidat;

            if (identifiantsCandidatsBoursiers.contains(i)) {

                String nomCandidat = "B" + compteurCandidatsBoursiers;

                parametrageCandidat = new HashMap<>();

                parametrageCandidat.put(
                    Candidat.CODE_BOURSIER,
                    1    // 1 = candidat boursier
                );

                parametrageCandidat.put(
                    Candidat.CODE_VALIDATION_STATUT_BOURSIER,
                    1    // 1 = certification boursier établie par SIEGE (valeur requise dans ce contexte)
                );

                parametrageCandidat.put(Candidat.ETIQUETTE, nomCandidat);

                candidat = new Candidat(
                    i, // identifiant numérique du candidat
                    100, // code 100 = candidat inscrit
                    parametrageCandidat
                );

                candidat.insert();

                compteurCandidatsBoursiers += 1;

            } else {

                String nomCandidat = "C" + compteurCandidatsNonBoursiers;

                parametrageCandidat = new HashMap<>();

                parametrageCandidat.put(
                    Candidat.ETIQUETTE,
                    nomCandidat
                );

                candidat = new Candidat(
                    i, // identifiant numérique du candidat
                    100, // 100 = candidat inscrit (valeur requise dans ce contexte)
                    parametrageCandidat
                );

                candidat.insert();

                compteurCandidatsNonBoursiers += 1;

            }

            Candidature candidature = new Candidature(
                    candidat, // candidat
                    formationInscription // formation d'inscription
            );

            candidature.insert();

            final int position = i;

            parametragePositionCandidatDansGroupeClassementPedagogique = new HashMap<>();

            parametragePositionCandidatDansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_INITIAL_CANDIDAT,
                position
            );
            
            parametragePositionCandidatDansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5     // 5 = candidat classé (valeur requise dans ce contexte)
            );             

            PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique, // groupe de classement pédagogique
                candidat, // candidat
                parametragePositionCandidatDansGroupeClassementPedagogique
            );

            positionCandidat1DansGroupeClassementPedagogique.insert();

            Voeu voeuCandidat = new Voeu(
                candidat, // candidat
                groupeAffectationFormation, // groupe d'affectation / formation
                regimeHebergementSansInternat, // régime d'hébergement demandé
                situationVoeuEnAttente // situation du voeu
            );

            voeuCandidat.insert();

        }

        db.close();

        // 
        // Application de l'algorithme
        // (nécessite une connexion au travers du connecteur de données)
        //
        // La méthode exporterDonneesOrdresAppel() enregistrant l'ordre d'appel
        // dans la table C_CAN_GRP (table liée à l'entité PositionCandidatDansGroupeClassementPedagogique)
        //
        
        // Désactivation de l'affichage des logs

        try (
                ConnecteurSQL connecteurSQL
                = new ConnecteurSQL(
                        urlBddJdbc,
                        nomUtilisateur,
                        mdp
                )) {
            
            ConnecteurDonneesAppelSQL connecteurDonneesAppel = new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
            
            LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
            // Désactivation temporairement les logs d'erreur INFO
            
            AlgoOrdreAppelEntree entree = connecteurDonneesAppel.recupererDonneesOrdreAppel();
            AlgoOrdreAppelSortie sortie = AlgoOrdreAppel.calculerOrdresAppels(entree);
            connecteurDonneesAppel.exporterDonneesOrdresAppel(sortie);
            
            LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
            
        }

        // 
        // Récupération du résultat depuis la base de données, table C_CAN_GRP et tables liées
        // 
        
        db.open(driver, urlBddJdbc, nomUtilisateur, mdp);

        List<Candidat> listeOrdonneeClassementPedagogique = groupeClassementPedagogique.getlisteOrdonneeClassementPedagogique();
        List<Candidat> listeOrdonneeOrdreAppel = groupeClassementPedagogique.getlisteOrdonneeOrdreAppel();

        db.close();

        // 
        // Affichage du résultat
        // 
 
        String output = "";
        
        output += "\n\nExemple basé sur le cas défini dans le document de présentation des algorithmes de Parcoursup 2019,\n";
        output += "point 4.1, calcul de l'ordre d'appel pour les formations soumises au seul taux minimum boursiers.\n\n";
        
        output += "Les candidats préfixés B correspondent à des candidats boursiers et les candidats C à des candidats non boursiers.\n";
        output += "Le taux minimum de boursiers spécifié pour cette formation est de " + tauxMinimumBoursiers + " %.\n";
        output += "La remontée des candidats boursiers dans l'ordre d'appel résulte de l'application des algorithmes décrits au point 4 ";
        output += "du document de référence.\n\n";

        String[] headers = {"Position du candidat", "Classement pédagogique", "Ordre d'appel"};

        String[][] data = new String[nombreTotalCandidats][3];

        for (int i = 1; i <= nombreTotalCandidats; i++) {
            data[i - 1][0] = String.valueOf(i);
            data[i - 1][1] = (String) listeOrdonneeClassementPedagogique.get(i - 1).getValeurChamp(Candidat.ETIQUETTE);
            data[i - 1][2] = (String) listeOrdonneeOrdreAppel.get(i - 1).getValeurChamp(Candidat.ETIQUETTE);
        }
        
        output += prepareTableau(headers, data, 4, "|");
                        
        LOGGER.info(output);
        
        System.exit(0);

    }
    
       
    
    public static String prepareTableau(
        String[] headers,
        String[][] data,
        int margeEspacesGauche,
        String separateurHorizontal
    ) {
        
        StringBuilder rapportSortie = new StringBuilder();
                
        int nombreColonnes = headers.length;
        int largeurTotale = 0;

        for (String header : headers) {
            largeurTotale += header.length() + margeEspacesGauche + (separateurHorizontal.length());
        }               
        
        String formatSeparateur = String.format("%%0%dd", largeurTotale);
        rapportSortie.append(String.format(formatSeparateur, 0).replace("0", "-"));
        rapportSortie.append("\n");

        for (String header : headers) {
            String f2 = String.format("%%%ds", header.length());
            rapportSortie.append(String.format(f2, header)).append(separateurHorizontal);
        }
    
        rapportSortie.append("\n");

        for (String[] datum : data) {
            for (int j = 0; j < nombreColonnes; j++) {
                String f3 = String.format("%%%ds", headers[j].length());
                rapportSortie.append(String.format(f3, (datum[j]))).append(separateurHorizontal);
            }
            rapportSortie.append("\n");

        }
        
        rapportSortie.append(String.format(formatSeparateur, 0).replace("0", "-"));
        rapportSortie.append("\n");
        
        return rapportSortie.toString(); 

    }

}
