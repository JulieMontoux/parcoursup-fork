package fr.parcoursup.algos.bacasable.propositions;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;
import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.propositions.algo.AlgoPropositions;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import org.javalite.activejdbc.DB;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DemoPropositionsBdd {
    
    private static final Logger LOGGER = Logger.getLogger(DemoPropositionsBdd.class.getSimpleName());
    
    public static void main(String[] args) throws Exception {

        
        ///////////////////////////////////////////////////////////////////////////////////////////
        //
        // Exemple de mise en oeuvre de l'algorithme de sélection / envoi de nouvelles propositions.
        // 
        // Le scénario proposé ici est très basique, n'incluant que 2 formations, 2 candidats et
        // 5 voeux.
        //
        // Son objectif est essentiellement d'illustrer les modalités d'enregistrement des conditions
        // initiales d'un scénario en BDD (nature des informations à enregistrer, paramétrage à
        // effectuer, etc.), d'appel / exécution de l'algorithme puis d'extraction du résultat avant
        // envoi effectif des nouvelles propositions aux candidats.
        // 
        // Les données correspondant aux conditions initiales de ce scénario sont enregistrées en
        // base de données avec des jeux de valeurs similaires à ceux utilisés en production.
        // Cet enregistrement est effectué par l'intermédiaire d'un composant ORM (PeuplementBdd)
        // destiné à faciliter l'insertion séquentielle de l'ensemble des données requises.
        // 
        // Les nouvelles propositions d'admission sont enregistrées, après exécution de l'algorithme,
        // dans la table A_ADM_PROP d'où elles sont par la suite extraites et traitées.
        //
        // Programme à lancer avec :
        // 
        // mvn exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsBdd"
        //
        // ou bien directement :
        //
        // mvn clean activejdbc-instrumentation:instrument exec:java -Dexec.mainClass="fr.parcoursup.algos.bacasable.propositions.DemoPropositionsBdd"
        //
        ///////////////////////////////////////////////////////////////////////////////////////////
        
        

        InputStream is = DemoPropositionsBdd.class.getResourceAsStream("/properties-from-pom.properties");
    
        Properties properties = new Properties();
        properties.load(is);
    
        
        // Les données sont injectées dans le base de données "bac à sable" dont les tables concernées doivent
        // être préalablement vidées
        
        String driver = properties.getProperty("DRIVER_BDD_BAC_A_SABLE");
        String urlBddJdbc = properties.getProperty("URL_BDD_BAC_A_SABLE").replace('\\','/');
        String nomUtilisateur = properties.getProperty("UTILISATEUR_BDD_BAC_A_SABLE");
        String mdp = properties.getProperty("MDP_BDD_BAC_A_SABLE");

        DB db = new DB();
        
        db.open(driver, urlBddJdbc, nomUtilisateur, mdp);
    
        Candidat.deleteAll();
        Candidature.deleteAll();
        EtatBarresAdmission.deleteAll();
        EtatDemissionsAutomatiques.deleteAll();
        EtatPredictionsDernierRangAppele.deleteAll();
        EtatVoeuxEnAttente.deleteAll();
        Filiere.deleteAll();
        Formation.deleteAll();
        FormationAffectation.deleteAll();
        FormationInscription.deleteAll();
        GroupeAffectationFormation.deleteAll();
        GroupeAffectationInternat.deleteAll();
        GroupeClassementPedagogique.deleteAll();
        JuryPedagogique.deleteAll();
        NouvellePropositionAdmission.deleteAll();
        ParametreApplication.deleteAll();
        PositionCandidatDansGroupeClassementInternat.deleteAll();
        PositionCandidatDansGroupeClassementPedagogique.deleteAll();
        PropositionAdmission.deleteAll();
        SituationVoeu.deleteAll();
        TypeFormation.deleteAll();
        Voeu.deleteAll();
  
        
        //
        // Les données requises pour la formalisation du scénario sont enregistrées
        // séquentiellement par l'intermédiaire d'objets facilitant le paramétrage
        // et la définition des liens entre concepts / entités enregistrés en base
        // de données. La séquence suivie découle des relations liant les différentes
        // tables entre elles.
        //
        
        //
        // Paramétrage et enregistrement des données relatives à la mise en oeuvre de l'application
        //
        
        new ParametreApplication(
            31,                 // indexFlagInterruptionDonneesEntrantes
            "1"                 // 0 = flag off
        ).insert();
        new ParametreApplication(
            34,                // indexFlagAlerte
            "0"
        ).insert();
        new ParametreApplication(
            35,                // indexDateDebutDeCampagne
            "20/05/2020:0000"  // date et heure du début de la campagne
        ).insert();
        new ParametreApplication(
            334,                // indexDateOuvertureCompleteInternats
            "01/07/2020:0000"   // date et heure de l'ouverture complète des internats
        ).insert();
        new ParametreApplication(
                316,                // indexDateDebutGDD
                "15/07/2020:0000"   // date et heure de l'ouverture complète des internats
        ).insert();
        new ParametreApplication(
                437,                // indexFinOrdonancementVoeuxGDD
                "18/07/2020:0000"   // date et heure de la fin d'ordonnacement des voeux GDD
        ).insert();
        
        //
        // Paramétrage et enregistrement des données relatives aux candidats
        //
        
        HashMap<String, Object> parametrageCandidat1 = new HashMap<>();
        
        parametrageCandidat1.put(
            Candidat.CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE,
            0    // 0 = n'a pas activé son répondeur automatique
        );
        
        parametrageCandidat1.put(Candidat.ETIQUETTE, "Candidat 1");
        

        Candidat candidat1 = new Candidat(
            17_644,            // identifiant numérique du candidat
            100,               // 100 = candidat inscrit (valeur requise dans ce contexte)
            parametrageCandidat1
        );

        candidat1.insert();

        
        HashMap<String, Object> parametrageCandidat2 = new HashMap<>();
        
        parametrageCandidat2.put(
            Candidat.CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE,
            0    // 0 = n'a pas activé son répondeur automatique
        );
        
        parametrageCandidat2.put(
            Candidat.ETIQUETTE,
            "Candidat 2"
        );

        Candidat candidat2 = new Candidat(
            23_455,          // identifiant numérique du candidat
            100,             // 100 = candidat inscrit (valeur requise dans ce contexte)
            parametrageCandidat2
        );

        candidat2.insert();
    

        //
        // Paramétrage et enregistrement des données relatives aux filières de formation
        //

        Filiere filiere1 = new Filiere(
            679              // identifiant numérique de la filière
        );

        filiere1.insert();


        Filiere filiere2 = new Filiere(
            983              // identifiant numérique de la filière
        );

        filiere2.insert();

        //
        // Paramétrage et enregistrement des données relatives aux types de formation
        //

        TypeFormation typeFormation1 = new TypeFormation(
            23               // identifiant numérique du type de formation
        );

        typeFormation1.insert();


        TypeFormation typeFormation2 = new TypeFormation(
            45               // identifiant numérique du type de formation
        );

        typeFormation2.insert();


        //
        // Paramétrage relatif aux établissements
        //

        Etablissement etablissement1 = new Etablissement(
            "e1"             // identifiant de l'établissement
        );      


        Etablissement etablissement2 = new Etablissement(
            "e2"             // identifiant de l'établissement
        );


        //
        // Paramétrage et enregistrement des données relatives à la formation d'inscription
        //
        // Une formation d'inscription est liée à une filière, un type de formation et
        // un établissement
        //

        HashMap<String, Object> parametrageFormationInscription1 = new HashMap<>();
        
        parametrageFormationInscription1.put(
            FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
            1    // 1 = paramétrage de la formation effectué (valeur requise dans ce contexte)
        );
        
        parametrageFormationInscription1.put(
            FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2    // 2 = classement du dossier terminé (valeur requise dans ce contexte)
        );           

        parametrageFormationInscription1.put(
            FormationInscription.CODE_TYPE_INTERNAT_ASSOCIE,
            1    // 1 = internat commun à plusieurs formations de l'établissement
        );        

        parametrageFormationInscription1.put(
            FormationInscription.ETIQUETTE,
            "Formation 1"
        );

        FormationInscription formationInscription1 = new FormationInscription(
            329,                        // identifiant numérique de la formation d'inscription
            etablissement1,             // établissement
            typeFormation1,             // type de formation
            filiere1,                   // filière
            parametrageFormationInscription1
        );

        formationInscription1.insert();


        HashMap<String, Object> parametrageFormationInscription2 = new HashMap<>();
        
        parametrageFormationInscription2.put(
            FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
            1    // 1 = paramétrage de la formation effectué (valeur requise dans ce contexte)
        );        
        
        parametrageFormationInscription2.put(
            FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2    // 2 = classement du dossier terminé (valeur requise dans ce contexte)
        );
       
        parametrageFormationInscription2.put(
            FormationInscription.CODE_TYPE_INTERNAT_ASSOCIE,
            0    // 0 = l'internat associé est uniquement associé à cette formation
        );
        
        parametrageFormationInscription2.put(
            FormationInscription.ETIQUETTE,
            "Formation 2"
        );

        FormationInscription formationInscription2 = new FormationInscription(
            567,                        // identifiant numérique de la formation d'inscription
            etablissement2,             // établissement
            typeFormation2,             // type de formation
            filiere2,                   // filière
            parametrageFormationInscription2
        );

        formationInscription2.insert();


        //
        // Paramétrage et enregistrement des données relatives aux jurys pédagogiques
        //
        // Un jury pédagogique est lié à une formation d'inscription
        //
        
        JuryPedagogique juryPedagogique1 = new JuryPedagogique(
            17,                         // identifiant numérique du jury
            formationInscription1       // formation d'inscription
        );

        juryPedagogique1.insert();


        JuryPedagogique juryPedagogique2 = new JuryPedagogique(
            23,                         // identifiant numérique du jury
            formationInscription2       // formation d'inscription
        );

        juryPedagogique2.insert();


        //
        // Paramétrage et enregistrement des données relatives aux formations d'affectation
        //

        FormationAffectation formationAffectation1 = new FormationAffectation(
            560                         // identifiant numérique de la formation d'affectation

        );

        formationAffectation1.insert();


        FormationAffectation formationAffectation2 = new FormationAffectation(
            899                         // identifiant numérique de la formation d'affectation
        );

        formationAffectation2.insert();

        //
        // Paramétrage et enregistrement des données relatives aux formations
        //
        // Une formation établit un lien entre une formation d'inscription
        // et une formation d'affectation
        //

        HashMap<String, Object> parametrageFormation1 = new HashMap<>();
        
        parametrageFormation1.put(
            Formation.ETIQUETTE,
            "Formation 1"
        );

        Formation formation1 = new Formation(
            formationInscription1,      // formation d'inscription
            formationAffectation1,      // formation d'affectation
            parametrageFormation1
        );

        formation1.insert();


        HashMap<String, Object> parametrageFormation2 = new HashMap<>();
        parametrageFormation2.put(Formation.ETIQUETTE, "Formation 2");
        
        Formation formation2 = new Formation(
            formationInscription2,      // formation d'inscription
            formationAffectation2,      // formation d'affectation
            parametrageFormation2
        );

        formation2.insert();

        //
        // Paramétrage et enregistrement des données relatives aux groupes d'affectation
        //
        // Un groupe d'affectation est lié à une formation (ex. : groupe d'étudiants
        // issus de classes préparatoires)
        //

        HashMap<String, Object> parametrageGroupeAffectationFormation1 = new HashMap<>();
        
        parametrageGroupeAffectationFormation1.put(
            GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
            1
        );

        parametrageGroupeAffectationFormation1.put(
            GroupeAffectationFormation.RANG_LIMITE_APPEL,
            1
        );

        GroupeAffectationFormation groupeAffectationFormation1 = new GroupeAffectationFormation(
            2_391,                           // identifiant numérique du groupe d'affectation / formation
            formation1,                      // formation
            parametrageGroupeAffectationFormation1
        );

        groupeAffectationFormation1.insert();


        HashMap<String, Object> parametrageGroupeAffectationFormation2 = new HashMap<>();
        
        parametrageGroupeAffectationFormation2.put(
            GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
            1
        );
        
        parametrageGroupeAffectationFormation2.put(
            GroupeAffectationFormation.RANG_LIMITE_APPEL,
            1
        );
        
        GroupeAffectationFormation groupeAffectationFormation2 = new GroupeAffectationFormation(
            5_687,                           // identifiant numérique du groupe d'affectation / formation
            formation2,                      // formation
            parametrageGroupeAffectationFormation2
        );

        groupeAffectationFormation2.insert();

        
        //
        // Paramétrage et enregistrement des données relatives aux groupes de classement pédagogiques
        //
        // Un groupe de classement pédagogique est lié à un groupe d'affectation
        //

        HashMap<String, Object> parametrageGroupeClassementPedagogique1 = new HashMap<>();
        
        parametrageGroupeClassementPedagogique1.put(
            GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2    // 2 = classement finalisé (valeur requise dans ce contexte)
        );
        
        parametrageGroupeClassementPedagogique1.put(
            GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
            0
        );      // code 0 = formation établissant un classement (valeur requise dans ce contexte)

        GroupeClassementPedagogique groupeClassementPedagogique1 = new GroupeClassementPedagogique(
            groupeAffectationFormation1,     // groupe d'affectation / formation
            parametrageGroupeClassementPedagogique1
        );

        groupeClassementPedagogique1.insert();

        
        HashMap<String, Object> parametrageGroupeClassementPedagogique2 = new HashMap<>();
        
        parametrageGroupeClassementPedagogique2.put(
            GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
            2    // 2 = classement finalisé (valeur requise dans ce contexte)
        );  

        parametrageGroupeClassementPedagogique2.put(
            GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
            0    // 0 = formation établissant un classement (valeur requise dans ce contexte)
        );       

        GroupeClassementPedagogique groupeClassementPedagogique2 = new GroupeClassementPedagogique(
            groupeAffectationFormation2,     // groupe d'affectation / formation
            parametrageGroupeClassementPedagogique2
        );

        groupeClassementPedagogique2.insert();

        //
        // Paramétrage et enregistrement des données relatives aux groupes d'affectation en internat
        //
        // Un groupe d'affectation en internat peut éventuellement être associé,
        // de manière exclusive, à une formation
        //

        HashMap<String, Object> parametrageGroupeAffectationInternat1 = new HashMap<>();
        
        parametrageGroupeAffectationInternat1.put(
            "formation",
            formation1
        );
        
        parametrageGroupeAffectationInternat1.put(
            GroupeAffectationInternat.NOMBRE_RECRUTEMENTS_SOUHAITE,
            10
        );
        
        GroupeAffectationInternat groupeAffectationInternat1 = new GroupeAffectationInternat(
            5_674,                           // identifiant numérique du groupe d'affectation en internat
            parametrageGroupeAffectationInternat1
        );

        groupeAffectationInternat1.insert();


        HashMap<String, Object> parametrageGroupeAffectationInternat2 = new HashMap<>();
        
        parametrageGroupeAffectationInternat2.put(
            "formation",
            formation2
        );
        
        parametrageGroupeAffectationInternat2.put(
            GroupeAffectationInternat.NOMBRE_RECRUTEMENTS_SOUHAITE,
            10
        );
        
        GroupeAffectationInternat groupeAffectationInternat2 = new GroupeAffectationInternat(
            8_196,                           // identifiant numérique du groupe d'affectation en internat
            parametrageGroupeAffectationInternat2
        );

        groupeAffectationInternat2.insert();

        
        //
        // Paramétrage et enregistrement des données relatives aux groupes de classement en internat
        //
        // Un groupe de classement en internat est associé à un groupe d'affectation en internat
        //

        GroupeClassementInternat groupeClassementInternat1 = new GroupeClassementInternat(
            groupeAffectationInternat1       // groupe d'affectation / internat
        );


        GroupeClassementInternat groupeClassementInternat2 = new GroupeClassementInternat(
            groupeAffectationInternat2       // groupe d'affectation / internat
        );


        //
        // Paramétrage et enregistrement des données relatives aux candidatures
        //
        // Une candidature est liée à un candidat et à une formation d'inscription
        //

        Candidature candidature1Formation1 = new Candidature(
            candidat1,                       // candidat
            formationInscription1            // formation d'inscription
        );

        candidature1Formation1.insert();

            
        Candidature candidature1Formation2 = new Candidature(
            candidat1,                       // candidat
            formationInscription2            // formation d'inscription
        );

        candidature1Formation2.insert();


        Candidature candidature2Formation1 = new Candidature(
            candidat2,                       // candidat
            formationInscription1            // formation d'inscription
        );

        candidature2Formation1.insert();


        Candidature candidature2Formation2 = new Candidature(
            candidat2,                       // candidat
            formationInscription2            // formation d'inscription
        );

        candidature2Formation2.insert();


        //
        // Paramétrage et enregistrement des données relatives aux positions des candidats dans les groupes de classement pédagogiques
        // 
        // Une position est liée à un candidat et un groupe de classement pédagogique
        //

        HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementPedagogique1 = new HashMap<>();
        
        parametragePositionCandidat1DansGroupeClassementPedagogique1.put(
            PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );              
        
        parametragePositionCandidat1DansGroupeClassementPedagogique1.put(
            PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
            1
        );
        
        PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique1 = new PositionCandidatDansGroupeClassementPedagogique(
            groupeClassementPedagogique1,    // groupe de classement pédagogique
            candidat1,                       // candidat
            parametragePositionCandidat1DansGroupeClassementPedagogique1
        );

        positionCandidat1DansGroupeClassementPedagogique1.insert();


        HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementPedagogique1 = new HashMap<>();
        
        parametragePositionCandidat2DansGroupeClassementPedagogique1.put(
            PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
            5     // 5 = candidat classé (valeur requise dans ce contexte)
        );
        
        parametragePositionCandidat2DansGroupeClassementPedagogique1.put(
            PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
            2
        );

        PositionCandidatDansGroupeClassementPedagogique positionCandidat2DansGroupeClassementPedagogique1 = new PositionCandidatDansGroupeClassementPedagogique(
            groupeClassementPedagogique1,    // groupe de classement pédagogique
            candidat2,                       // candidat
            parametragePositionCandidat2DansGroupeClassementPedagogique1
        );

        positionCandidat2DansGroupeClassementPedagogique1.insert();


        HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementPedagogique2 = new HashMap<>();
        
        parametragePositionCandidat1DansGroupeClassementPedagogique2.put(
            PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // code 5 = candidat classé (valeur requise dans ce contexte)
        );             
        
        parametragePositionCandidat1DansGroupeClassementPedagogique2.put(
            PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
            2
        );

        PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique2 = new PositionCandidatDansGroupeClassementPedagogique(
            groupeClassementPedagogique2,    // groupe de classement pédagogique
            candidat1,                       // candidat
            parametragePositionCandidat1DansGroupeClassementPedagogique2
        );

        positionCandidat1DansGroupeClassementPedagogique2.insert();


        HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementPedagogique2 = new HashMap<>();
        
        parametragePositionCandidat2DansGroupeClassementPedagogique2.put(
            PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );

        parametragePositionCandidat2DansGroupeClassementPedagogique2.put(
            PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
            1
        );

        PositionCandidatDansGroupeClassementPedagogique positionCandidat2DansGroupeClassementPedagogique2 = new PositionCandidatDansGroupeClassementPedagogique(
            groupeClassementPedagogique2,    // groupe de classement pédagogique
            candidat2,                       // candidat
            parametragePositionCandidat2DansGroupeClassementPedagogique2
        );

        positionCandidat2DansGroupeClassementPedagogique2.insert();


        //
        // Paramétrage relatif aux régimes d'hébergement possibles
        //

        RegimeHebergement regimeHebergementSansInternat  = new RegimeHebergement(
            0    // 0 = pas d'hébergement en internat
        );


        RegimeHebergement regimeHebergementAvecInternat  = new RegimeHebergement(
            1    // 1 = hébergement en internat
        );


        //
        // Paramétrage et enregistrement des données relatives aux positions des candidats dans les groupes de classement internat
        // 
        // Une position est liée à un candidat et un groupe de classement en internat
        //
        
        HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementInternat1 = new HashMap<>();
        
        parametragePositionCandidat1DansGroupeClassementInternat1.put(
            PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );  

        parametragePositionCandidat1DansGroupeClassementInternat1.put(
            PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
            40
        );

        
        PositionCandidatDansGroupeClassementInternat positionCandidat1DansGroupeClassementInternat1 = new PositionCandidatDansGroupeClassementInternat(
            groupeClassementInternat1,            // groupe de classement / internat
            candidat1,                            // candidat
            parametragePositionCandidat1DansGroupeClassementInternat1
        );

        positionCandidat1DansGroupeClassementInternat1.insert();


        HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementInternat1 = new HashMap<>();
        
        parametragePositionCandidat2DansGroupeClassementInternat1.put(
            PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );        
        
        parametragePositionCandidat2DansGroupeClassementInternat1.put(
            PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
            50
        );
        
        PositionCandidatDansGroupeClassementInternat positionCandidat2DansGroupeClassementInternat1 = new PositionCandidatDansGroupeClassementInternat(
            groupeClassementInternat1,            // groupe de classement / internat
            candidat2,                            // candidat
            parametragePositionCandidat2DansGroupeClassementInternat1
            );
    
        positionCandidat2DansGroupeClassementInternat1.insert();
        
        
        HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementInternat2  = new HashMap<>();
        
        parametragePositionCandidat1DansGroupeClassementInternat2.put(
            PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );        
        
        parametragePositionCandidat1DansGroupeClassementInternat2.put(
            PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
            45
        );       
        
        PositionCandidatDansGroupeClassementInternat positionCandidat1DansGroupeClassementInternat2 = new PositionCandidatDansGroupeClassementInternat(
            groupeClassementInternat2,            // groupe de classement / internat
            candidat1,                            // candidat
            parametragePositionCandidat1DansGroupeClassementInternat2
        );

        positionCandidat1DansGroupeClassementInternat2.insert();


        HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementInternat2  = new HashMap<>();
        
        parametragePositionCandidat2DansGroupeClassementInternat2.put(
            PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
            5    // 5 = candidat classé (valeur requise dans ce contexte)
        );

        parametragePositionCandidat2DansGroupeClassementInternat2.put(
            PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
            55
        );
        
        PositionCandidatDansGroupeClassementInternat positionCandidat2DansGroupeClassementInternat2 = new PositionCandidatDansGroupeClassementInternat(
            groupeClassementInternat2,            // groupe de classement / internat
            candidat2,                            // candidat
            parametragePositionCandidat2DansGroupeClassementInternat2
        );

        positionCandidat2DansGroupeClassementInternat2.insert();


        //
        // Paramétrage et enregistrement des données relatives aux situations possibles des voeux
        //

        SituationVoeu situationVoeuEnAttente = new SituationVoeu(
            1,     // identifiant numérique
            1,     // code 1 = voeu en attente 
            0,     // code 0 = voeu non affecte 
            0,      // code 0 = voeu non clôturé
                0
        );

        situationVoeuEnAttente.insert();


        SituationVoeu situationVoeuAffecte = new SituationVoeu(
            2,     // identifiant numérique
            0,     // code 0 = voeu en attente 
            1,     // code 1 = voeu affecte 
            0,      // code 0 = voeu non clôturé
                0
        );

        situationVoeuAffecte.insert();

        //
        // Paramétrage et enregistrement des données relatives aux voeux formulés par les candidats
        //
        // Un voeu est lié à un candidat, un groupe d'affectation, un régime et une situation
        // (en attente, affecté...)
        //

        Voeu voeuCandidat1Formation1AvecInternat = new Voeu(
            candidat1,                        // candidat
            groupeAffectationFormation1,      // groupe d'affectation/ formation
            regimeHebergementAvecInternat,    // régime d'hébergement demandé
            situationVoeuEnAttente            // situation du voeu

        );

        voeuCandidat1Formation1AvecInternat.insert();


        Voeu voeuCandidat1Formation2AvecInternat = new Voeu(
            candidat1,                         // candidat
            groupeAffectationFormation2,       // groupe d'affectation/ formation
            regimeHebergementAvecInternat,     // régime d'hébergement demandé
            situationVoeuEnAttente               // situation du voeu         

        );

        voeuCandidat1Formation2AvecInternat.insert();


        Voeu voeuCandidat2Formation1AvecInternat = new Voeu(
            candidat2,                         // candidat
            groupeAffectationFormation1,       // groupe d'affectation/ formation
            regimeHebergementAvecInternat,     // régime d'hébergement demandé
            situationVoeuEnAttente             // situation du voeu

        );

        voeuCandidat2Formation1AvecInternat.insert();

        
        Voeu voeuCandidat2Formation2AvecInternat = new Voeu(
            candidat2,                         // candidat
            groupeAffectationFormation2,       // groupe d'affectation/ formation
            regimeHebergementAvecInternat,     // régime d'hébergement demandé
            situationVoeuEnAttente             // situation du voeu
        );

        voeuCandidat2Formation2AvecInternat.insert();

        Voeu voeuCandidat2Formation2SansInternat = new Voeu(
            candidat2,                        // candidat
            groupeAffectationFormation2,      // groupe d'affectation/ formation
            regimeHebergementSansInternat,    // régime d'hébergement demandé
            situationVoeuEnAttente              // situation du voeu
        );

        voeuCandidat2Formation2SansInternat.insert();
   
        db.close();
            
            
        // 
        // Application de l'algorithme
        // (nécessite une connexion au travers du connecteur de données)
        // 
        

        try (
                ConnecteurSQL connecteurSQL
                = new ConnecteurSQL(
                        urlBddJdbc,
                        nomUtilisateur,
                        mdp
                )) {
           
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions  = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            
            LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
            // Désactivation temporairement les logs d'erreur INFO
            
            AlgoPropositionsEntree entree = connecteurDonneesPropositions.recupererDonnees();
            AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);
            connecteurDonneesPropositions.exporterDonnees(sortie);
                    
            LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
            
            // 
            // Récupération du résultat depuis la base de données, table A_ADM_PROP et tables liées
            // et affichage
            // 
            
            StringBuilder rapportSortie = new StringBuilder();
            
            db.open(driver, urlBddJdbc, nomUtilisateur, mdp);
        
            List<NouvellePropositionAdmission> nouvellesPropositionsAdmission = NouvellePropositionAdmission.findAll();
            
            int nombreNouvellesPropositionsAdmission = nouvellesPropositionsAdmission.size();
                        
            rapportSortie.append("\n\n*** Nouvelles propositions d'admissions :\n\n");
            
            for(int i=0; i<nombreNouvellesPropositionsAdmission; i++) {
                
                NouvellePropositionAdmission nouvellePropositionsAdmission = nouvellesPropositionsAdmission.get(i);
                Voeu voeu = nouvellePropositionsAdmission.getVoeu();
                String nomCandidat = (String) voeu.getCandidat().getValeurChamp(Candidat.ETIQUETTE);
                String nomFormation = (String) voeu.getFormation().getValeurChamp(Formation.ETIQUETTE);
                String regimeHebergement = voeu.getRegimeHebergement().toString();
                String info = (i+1) + "/ ";
                info += nomCandidat + " - ";
                info += nomFormation + " - ";
                info += regimeHebergement + "\n";            
                rapportSortie.append(info);

            }
            
            List<EtatVoeuxEnAttente> voeuxEnAttente = EtatVoeuxEnAttente.findAll();
            
            int nombreVoeuxEnAttente = voeuxEnAttente.size();
            
            rapportSortie.append("\n*** Voeux en attente, n'ayant pas encore reçu de proposition :\n\n");
                       
            for(int i=0; i<nombreVoeuxEnAttente; i++) {
                EtatVoeuxEnAttente voeuEnAttente = voeuxEnAttente.get(i);
                Voeu voeu = voeuEnAttente.getVoeu();
                String nomCandidat = (String) voeu.getCandidat().getValeurChamp(Candidat.ETIQUETTE);
                String nomFormation = (String) voeu.getFormation().getValeurChamp(Formation.ETIQUETTE);
                String regimeHebergement = voeu.getRegimeHebergement().toString();

                String info = (i + 1) + "/ " +
                        nomCandidat + " - " +
                        nomFormation + " - " +
                        regimeHebergement + "\n";
                rapportSortie.append(info);
            }
            
            rapportSortie.append("\n");

            LOGGER.log(Level.INFO, "{0}", rapportSortie);
                     
            db.close();                
                     
        }
                        
        System.exit(0);
    
    }
    
    
}
