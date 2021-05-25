package fr.parcoursup.algos.ordreappel.donnees.testConnecteurDonneesAppelSQL;

import java.util.HashMap;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;


import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.javalite.activejdbc.DB;


public class JeuDonneesBaseTestOrdreAppel {
    
    
    protected final ScenarioTestDbUnit scenario;
    
    
    public JeuDonneesBaseTestOrdreAppel() {
        
        this.scenario = new ScenarioTestDbUnit();
        
        try(DB db = new DB()){
            
            db.open(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
                );      
            
            
            // Candidats
    
            
            HashMap<String, Object> parametrageCandidat1 = new HashMap<>();
    
            parametrageCandidat1.put(
                Candidat.CODE_BOURSIER,
                1
            );  

            parametrageCandidat1.put(
                Candidat.CODE_VALIDATION_STATUT_BOURSIER,
                1  // 1 = certification boursier établie par SIEGE    
            );      
            
            Candidat candidat1 = new Candidat(
                12_765,                             // identifiant numérique du candidat
                100,                               // code 100 = candidat inscrit
                parametrageCandidat1
            );
    
            
            this.scenario.ajouteEntite(candidat1);
    
    
            HashMap<String, Object> parametrageCandidat2 = new HashMap<>();
            
            parametrageCandidat2.put(
                Candidat.CODE_BOURSIER,
                0
            );  

            parametrageCandidat2.put(
                Candidat.CODE_VALIDATION_STATUT_BOURSIER,
                0
            );      
            
            Candidat candidat2 = new Candidat(
                65_322,                             // identifiant numérique du candidat
                100,                               // code 100 = candidat inscrit
                parametrageCandidat2
            );
    
            this.scenario.ajouteEntite(candidat2);
    
    
            // Filière
    
            Filiere filiere = new Filiere(
                453                                // identifiant numérique de la filière
            );
    
            this.scenario.ajouteEntite(filiere);
    
    
            // Type de formation
    
            TypeFormation typeFormation = new TypeFormation(
                24                                 // identifiant numérique du type de formation

            );
    
            this.scenario.ajouteEntite(typeFormation);
    
    
            // Etablissement
    
            Etablissement etablissement = new Etablissement(
                "e1"                              // identifiant de l'établissement
            );      
            

            // Formation d'inscription
    
            HashMap<String, Object> parametrageFormationInscription = new HashMap<>();
            
            parametrageFormationInscription.put(
                    FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
                    1    // 1 = paramétrage de la formation effectué
                );
            
            parametrageFormationInscription.put(
                    FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
                    2    // code 2 = classement du dossier terminé
                );
            
            FormationInscription formationInscription = new FormationInscription(
                435,                              // identifiant numérique de la formation d'inscription
                etablissement,                    // établissement associé
                typeFormation,                    // type de formation
                filiere,                          // filière
                parametrageFormationInscription
            );
    
            this.scenario.ajouteEntite(formationInscription);
    

            // Jury pédagogique
    
            JuryPedagogique juryPedagogique = new JuryPedagogique(
                839,                              // identifiant numérique du jury
                formationInscription              // formation d'inscription
            );
    
            this.scenario.ajouteEntite(juryPedagogique);
    

            // Formation d'affectation
    
            FormationAffectation formationAffectation = new FormationAffectation(
                279                              // identifiant numérique de la formation d'affectation

            );
    
            this.scenario.ajouteEntite(formationAffectation);
    
    
            // Formation
    
            HashMap<String, Object> parametrageFormation = new HashMap<>();
            
            parametrageFormation.put(
                Formation.CAPACITE_TOTALE_FORMATION,
                10
            );
            
            parametrageFormation.put(
                Formation.TAUX_MINIMUM_BOURSIERS,
                20
            );
            
            parametrageFormation.put(
                Formation.CODE_PREFERENCE_CANDIDATS_RESIDENTS_APPLIQUEE,
                1    // 1 = un taux maximum de non résidents doit être appliqué
            );
            
            parametrageFormation.put(
                Formation.TAUX_MAXIMUM_NON_RESIDENTS,
                20
                );
            
            
            Formation formation = new Formation(
                formationInscription,              // formation d'inscription
                formationAffectation,              // formation d'affectation
                parametrageFormation
            );
    
            this.scenario.ajouteEntite(formation);
    

            // Groupe d'affectation
    
            HashMap<String, Object> parametrageGroupeAffectationFormation = new HashMap<>();
            
            parametrageGroupeAffectationFormation.put(
                GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
                15
            );
            
            GroupeAffectationFormation groupeAffectationFormation = new GroupeAffectationFormation(
                4_584,                              // identifiant numérique de la formation d'affectation
                formation,                         // formation
                parametrageGroupeAffectationFormation
            );
    
            this.scenario.ajouteEntite(groupeAffectationFormation);
    

            // Groupe de classement pédagogique
            
            HashMap<String, Object> parametrageGroupeClassementPedagogique = new HashMap<>();

            parametrageGroupeClassementPedagogique.put(
                GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
                2    // 2 = classement finalisé
            );
            
            parametrageGroupeClassementPedagogique.put(
                GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
                0    // 0 = formation établissant un classement
            );
    
            GroupeClassementPedagogique groupeClassementPedagogique = new GroupeClassementPedagogique(
                groupeAffectationFormation,         // groupe d'affectation / formation
                parametrageGroupeClassementPedagogique
            );
    
            this.scenario.ajouteEntite(groupeClassementPedagogique);
    
    
            // Candidatures
            
            HashMap<String, Object> parametrageCandidature1 = new HashMap<>();

            parametrageCandidature1.put(
                Candidature.CODE_CANDIDAT_EST_DU_SECTEUR,
                1    // 1 = le candidat est considéré comme étant du secteur
            );
    
            Candidature candidature1 = new Candidature(
                candidat1,                         // candidat
                formationInscription,              // formation d'inscription
                parametrageCandidature1
            );
    
            this.scenario.ajouteEntite(candidature1);
    
            
            HashMap<String, Object> parametrageCandidature2 = new HashMap<>();
            
            parametrageCandidature2.put(
                    Candidature.CODE_CANDIDAT_EST_DU_SECTEUR,
                    0    // 0 = le candidat est considéré comme n'étant pas du secteur
                );
    
            Candidature candidature2 = new Candidature(
                candidat2,                         // candidat
                formationInscription,              // formation d'inscription
                parametrageCandidature2
            );
    
            this.scenario.ajouteEntite(candidature2);
    
    
            // Position des candidats dans le groupe de classement pédagogique
    
            HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementPedagogique = new HashMap<>();
            
            parametragePositionCandidat1DansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_INITIAL_CANDIDAT,
                1
            );  
            
            parametragePositionCandidat1DansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
            
            PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique,       // groupe de classement pédagogique
                candidat1,                         // candidat
                parametragePositionCandidat1DansGroupeClassementPedagogique
            );
    
            this.scenario.ajouteEntite(positionCandidat1DansGroupeClassementPedagogique);
    
            HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementPedagogique = new HashMap<>();
            
            parametragePositionCandidat2DansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_INITIAL_CANDIDAT,
                2
            );  
            
            parametragePositionCandidat2DansGroupeClassementPedagogique.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
    
            PositionCandidatDansGroupeClassementPedagogique positionCandidat2DansGroupeClassementPedagogique = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique,        // groupe de classement pédagogique
                candidat2,                          // candidat
                parametragePositionCandidat2DansGroupeClassementPedagogique
            );
    
            this.scenario.ajouteEntite(positionCandidat2DansGroupeClassementPedagogique);
    
    
            // Régimes d'hébergement
    
            RegimeHebergement regimeHebergementSansInternat  = new RegimeHebergement(
                0                               // code  0 = pas d'hébergement en internat
            );
    
    
            // Situations possibles des voeux
    
            SituationVoeu situationVoeuEnAttente = new SituationVoeu(
                1,                             // identifiant numérique
                1,                             // code 1 = voeu en attente 
                0,                             // code 0 = voeu non affecte 
                0                              // code 0 = voeu non clôturé 
            );
    
            this.scenario.ajouteEntite(situationVoeuEnAttente);
    
    
            // Voeux
    
    
            Voeu voeuCandidat1 = new Voeu(
                candidat1,                         // candidat
                groupeAffectationFormation,        // groupe d'affectation / formation
                regimeHebergementSansInternat,     // régime d'hébergement demandé
                situationVoeuEnAttente             // situation du voeu
            );
    
            this.scenario.ajouteEntite(voeuCandidat1);
    
    
    
            Voeu voeuCandidat2 = new Voeu(
                candidat2,                        // candidat
                groupeAffectationFormation,       // groupe d'affectation / formation
                regimeHebergementSansInternat,    // régime d'hébergement demandé
                situationVoeuEnAttente            // situation du voeu
            );
    
            this.scenario.ajouteEntite(voeuCandidat2);
             
        
        }
          
    }

    
    public String getFlatXml() {        
        
        return this.scenario.getFlatXml();

    }


}
