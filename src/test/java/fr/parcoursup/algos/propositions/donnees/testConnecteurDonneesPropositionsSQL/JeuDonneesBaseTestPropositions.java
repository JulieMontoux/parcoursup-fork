package fr.parcoursup.algos.propositions.donnees.testConnecteurDonneesPropositionsSQL;

import java.util.HashMap;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.javalite.activejdbc.DB;




public class JeuDonneesBaseTestPropositions {

    protected final ScenarioTestDbUnit scenario;


    public JeuDonneesBaseTestPropositions() {

        this.scenario = new ScenarioTestDbUnit();


        try(DB db = new DB()){
        
            db.open(
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
                System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD)
            );   

        
            ParametreApplication parametreApplication1 = new ParametreApplication(
                31,                 // indexFlagInterruptionDonneesEntrantes
                "0"                 // 0 = flag off
            );

            this.scenario.ajouteEntite(parametreApplication1);



            ParametreApplication parametreApplication2 = new ParametreApplication(
                34,                // indexFlagAlerte
                "0"
            );

            this.scenario.ajouteEntite(parametreApplication2);



            ParametreApplication parametreApplication3 = new ParametreApplication(
                35,                // indexDateDebutDeCampagne
                "20/05/2020:0000"  // date et heure du début de la campagne
            );

            this.scenario.ajouteEntite(parametreApplication3);



            ParametreApplication parametreApplication4 = new ParametreApplication(
                334,                // indexDateOuvertureCompleteInternats
                "01/07/2020:0000"   // date et heure de l'ouverture complète des internats
            );

            this.scenario.ajouteEntite(parametreApplication4);

            
            // Candidats

            HashMap<String, Object> parametrageCandidat1 = new HashMap<>();
            
            parametrageCandidat1.put(
                Candidat.CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE,
                1    // 1 = a activé son répondeur automatique
            );
            
            Candidat candidat1 = new Candidat(
                17_644,                             // identifiant numérique du candidat
                100,                               // 100 = candidat inscrit
                parametrageCandidat1
            );

            this.scenario.ajouteEntite(candidat1);


            HashMap<String, Object> parametrageCandidat2 = new HashMap<>();
            
            parametrageCandidat2.put(
                Candidat.CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE,
                0    // 0 = n'a pas activé son répondeur automatique
            );
            
            Candidat candidat2 = new Candidat(
                23_455,                             // identifiant numérique du candidat
                100,                               // 100 = candidat inscrit
                parametrageCandidat2
            );

            this.scenario.ajouteEntite(candidat2);

            
            // Filières     

            Filiere filiere1 = new Filiere(
                679                             // identifiant numérique de la filière
            );

            this.scenario.ajouteEntite(filiere1);



            Filiere filiere2 = new Filiere(
                983                             // identifiant numérique de la filière
            );

            this.scenario.ajouteEntite(filiere2);



            // Types de formation

            TypeFormation typeFormation1 = new TypeFormation(
                23                              // identifiant numérique du type de formation
            );

            this.scenario.ajouteEntite(typeFormation1);


            TypeFormation typeFormation2 = new TypeFormation(
                45                              // identifiant numérique du type de formation
            );

            this.scenario.ajouteEntite(typeFormation2);



            // Etablissements

            Etablissement etablissement1 = new Etablissement(
                "e1"                           // identifiant de l'établissement
            );      



            Etablissement etablissement2 = new Etablissement(
                "e2"                          // identifiant de l'établissement
            );


            // Formations d'inscription

            HashMap<String, Object> parametrageFormationInscription1 = new HashMap<>();
            
            parametrageFormationInscription1.put(
                FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
                1    // code 1 = paramétrage de la formation vérifié
            );
            
            parametrageFormationInscription1.put(
                FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
                2    // code 2 = classement du dossier terminé
            ); 
            
            parametrageFormationInscription1.put(
                    FormationInscription.CODE_TYPE_INTERNAT_ASSOCIE,
                1    // code 1 = internat commun à plusieurs formations de l'établissement
            );          
            
            
            FormationInscription formationInscription1 = new FormationInscription(
                329,                               // identifiant numérique de la formation d'inscription
                etablissement1,                    // établissement
                typeFormation1,                    // type de formation
                filiere1,                          // filière
                parametrageFormationInscription1
            );

            this.scenario.ajouteEntite(formationInscription1);


            HashMap<String, Object> parametrageFormationInscription2 = new HashMap<>();
            
            parametrageFormationInscription2.put(
                FormationInscription.CODE_PARAMETRAGE_EFFECTUE,
                1    // code 1 = paramétrage de la formation vérifié
            );
            
            parametrageFormationInscription2.put(
                FormationInscription.CODE_ETAT_CLASSEMENT_DOSSIERS,
                2    // code 2 = classement du dossier terminé
            ); 
            
            parametrageFormationInscription2.put(
                    FormationInscription.CODE_TYPE_INTERNAT_ASSOCIE,
                0    // code 0 = l'internat associé est uniquement associé à cette formation
            );  
            

            FormationInscription formationInscription2 = new FormationInscription(
                567,                               // identifiant numérique de la formation d'inscription
                etablissement2,                    // établissement
                typeFormation2,                    // type de formation
                filiere2,                          // filière
                parametrageFormationInscription2
            );

            this.scenario.ajouteEntite(formationInscription2);


            // Jurys pédagogiques   

            JuryPedagogique juryPedagogique1 = new JuryPedagogique(
                17,                                // identifiant numérique du jury
                formationInscription1              // formation d'inscription
            );

            this.scenario.ajouteEntite(juryPedagogique1);



            JuryPedagogique juryPedagogique2 = new JuryPedagogique(
                23,                                // identifiant numérique du jury
                formationInscription2              // formation d'inscription
            );

            this.scenario.ajouteEntite(juryPedagogique2);



            // Formations d'affectation

            FormationAffectation formationAffectation1 = new FormationAffectation(
                560                                // identifiant numérique de la formation d'affectation

            );

            this.scenario.ajouteEntite(formationAffectation1);



            FormationAffectation formationAffectation2 = new FormationAffectation(
                899                                // identifiant numérique de la formation d'affectation
            );

            this.scenario.ajouteEntite(formationAffectation2);



            // Formations

            Formation formation1 = new Formation(
                formationInscription1,             // formation d'inscription
                formationAffectation1              // formation d'affectation

            );

            this.scenario.ajouteEntite(formation1);



            Formation formation2 = new Formation(
                formationInscription2,             // formation d'inscription
                formationAffectation2              // formation d'affectation

            );

            this.scenario.ajouteEntite(formation2);



            // Groupes d'affectation

            HashMap<String, Object> parametrageGroupeAffectationFormation1 = new HashMap<>();
            
            parametrageGroupeAffectationFormation1.put(
                GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
                5
            );

            parametrageGroupeAffectationFormation1.put(
                GroupeAffectationFormation.RANG_LIMITE_APPEL,
                5
            );
            
            GroupeAffectationFormation groupeAffectationFormation1 = new GroupeAffectationFormation(
                2_391,                              // identifiant numérique du groupe d'affectation / formation
                formation1,                        // formation
                parametrageGroupeAffectationFormation1
            );

            this.scenario.ajouteEntite(groupeAffectationFormation1);


            HashMap<String, Object> parametrageGroupeAffectationFormation2 = new HashMap<>();
            
            parametrageGroupeAffectationFormation2.put(
                GroupeAffectationFormation.NOMBRE_RECRUTEMENTS_SOUHAITE,
                5
            );

            parametrageGroupeAffectationFormation2.put(
                GroupeAffectationFormation.RANG_LIMITE_APPEL,
                5
            );

            GroupeAffectationFormation groupeAffectationFormation2 = new GroupeAffectationFormation(
                5_687,                             // identifiant numérique du groupe d'affectation / formation
                formation2,                       // formation
                parametrageGroupeAffectationFormation2
            );

            this.scenario.ajouteEntite(groupeAffectationFormation2);



            // Groupes de classement pédagogiques

            HashMap<String, Object> parametrageGroupeClassementPedagogique1 = new HashMap<>();
            
            parametrageGroupeClassementPedagogique1.put(
                GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
                2    // 2 = classement finalisé
            );
            
            parametrageGroupeClassementPedagogique1.put(
                GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
                0    // code 0 = formation établissant un classement
            );
            
            
            GroupeClassementPedagogique groupeClassementPedagogique1 = new GroupeClassementPedagogique(
                groupeAffectationFormation1,     // groupe d'affectation / formation
                parametrageGroupeClassementPedagogique1
            );

            this.scenario.ajouteEntite(groupeClassementPedagogique1);


            HashMap<String, Object> parametrageGroupeClassementPedagogique2 = new HashMap<>();
            
            parametrageGroupeClassementPedagogique2.put(
                GroupeClassementPedagogique.CODE_ETAT_CLASSEMENT_DOSSIERS,
                2    // 2 = classement finalisé
            );
            
            parametrageGroupeClassementPedagogique2.put(
                GroupeClassementPedagogique.CODE_FORMATION_SANS_CLASSEMENT,
                0    // code 0 = formation établissant un classement
            );

            GroupeClassementPedagogique groupeClassementPedagogique2 = new GroupeClassementPedagogique(
                groupeAffectationFormation2,      // groupe d'affectation / formation
                parametrageGroupeClassementPedagogique2
            );

            this.scenario.ajouteEntite(groupeClassementPedagogique2);


            // Groupes d'affectation en internat

            HashMap<String, Object> parametrageGroupeAffectationInternat1 = new HashMap<>();
            
            parametrageGroupeAffectationInternat1.put(
                "formation",
                formation1
            );
            
            parametrageGroupeAffectationInternat1.put(
                GroupeAffectationInternat.NOMBRE_RECRUTEMENTS_SOUHAITE,
                30
            );       
            
            GroupeAffectationInternat groupeAffectationInternat1 = new GroupeAffectationInternat(
                5_674,                              // identifiant numérique du groupe d'affectation en internat
                parametrageGroupeAffectationInternat1
            );

            this.scenario.ajouteEntite(groupeAffectationInternat1);

            
            HashMap<String, Object> parametrageGroupeAffectationInternat2 = new HashMap<>();

            parametrageGroupeAffectationInternat2.put(
                    "formation",
                    formation2
                );
                
                parametrageGroupeAffectationInternat2.put(
                    GroupeAffectationInternat.NOMBRE_RECRUTEMENTS_SOUHAITE,
                    40
                );  

            GroupeAffectationInternat groupeAffectationInternat2 = new GroupeAffectationInternat(
                8_196,                              // identifiant numérique du groupe d'affectation en internat
                parametrageGroupeAffectationInternat2
            );

            this.scenario.ajouteEntite(groupeAffectationInternat2);


            // Groupes de classement en internat

            GroupeClassementInternat groupeClassementInternat1 = new GroupeClassementInternat(
                groupeAffectationInternat1         // groupe d'affectation / internat
            );

            
            GroupeClassementInternat groupeClassementInternat2 = new GroupeClassementInternat(
                groupeAffectationInternat2         // groupe d'affectation / internat
            );



            // Candidatures

            Candidature candidature1Formation1 = new Candidature(
                candidat1,                         // candidat
                formationInscription1              // formation d'inscription
            );

            this.scenario.ajouteEntite(candidature1Formation1);



            Candidature candidature1Formation2 = new Candidature(
                candidat1,                         // candidat
                formationInscription2              // formation d'inscription
            );

            this.scenario.ajouteEntite(candidature1Formation2);



            Candidature candidature2Formation1 = new Candidature(
                candidat2,                         // candidat
                formationInscription1              // formation d'inscription
            );

            this.scenario.ajouteEntite(candidature2Formation1);



            Candidature candidature2Formation2 = new Candidature(
                candidat2,                         // candidat
                formationInscription2              // formation d'inscription
            );

            this.scenario.ajouteEntite(candidature2Formation2);



            // Positions des candidats dans les groupes de classement pédagogiques

            HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementPedagogique1 = new HashMap<>();
            
            parametragePositionCandidat1DansGroupeClassementPedagogique1.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );              
            
            parametragePositionCandidat1DansGroupeClassementPedagogique1.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
                1
            );
            
            PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique1 = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique1,      // groupe de classement pédagogique
                candidat1,                         // candidat
                parametragePositionCandidat1DansGroupeClassementPedagogique1
            );

            this.scenario.ajouteEntite(positionCandidat1DansGroupeClassementPedagogique1);


            HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementPedagogique1 = new HashMap<>();
            
            parametragePositionCandidat2DansGroupeClassementPedagogique1.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );              
            
            parametragePositionCandidat2DansGroupeClassementPedagogique1.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
                2
            );

            PositionCandidatDansGroupeClassementPedagogique positionCandidat2DansGroupeClassementPedagogique1 = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique1,     // groupe de classement pédagogique
                candidat2,                        // candidat
                parametragePositionCandidat2DansGroupeClassementPedagogique1
            );

            this.scenario.ajouteEntite(positionCandidat2DansGroupeClassementPedagogique1);


            HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementPedagogique2 = new HashMap<>();
            
            parametragePositionCandidat1DansGroupeClassementPedagogique2.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );              
            
            parametragePositionCandidat1DansGroupeClassementPedagogique2.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
                1
            );

            PositionCandidatDansGroupeClassementPedagogique positionCandidat1DansGroupeClassementPedagogique2 = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique2,     // groupe de classement pédagogique
                candidat1,                        // candidat
                parametragePositionCandidat1DansGroupeClassementPedagogique2
            );

            this.scenario.ajouteEntite(positionCandidat1DansGroupeClassementPedagogique2);


            HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementPedagogique2 = new HashMap<>();
            
            parametragePositionCandidat2DansGroupeClassementPedagogique2.put(
                PositionCandidatDansGroupeClassementPedagogique.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );              
            
            parametragePositionCandidat2DansGroupeClassementPedagogique2.put(
                PositionCandidatDansGroupeClassementPedagogique.RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL,
                2
            );
            
            
            PositionCandidatDansGroupeClassementPedagogique positionCandidat2DansGroupeClassementPedagogique2 = new PositionCandidatDansGroupeClassementPedagogique(
                groupeClassementPedagogique2,     // groupe de classement pédagogique
                candidat2,                        // candidat
                parametragePositionCandidat2DansGroupeClassementPedagogique2
            );

            this.scenario.ajouteEntite(positionCandidat2DansGroupeClassementPedagogique2);



            // Régimes d'hébergement possibles

            RegimeHebergement regimeHebergementSansInternat  = new RegimeHebergement(
                0                           // 0 = pas d'hébergement en internat
            );


            RegimeHebergement regimeHebergementAvecInternat  = new RegimeHebergement(
                1                           // 0 = hébergement en internat
            );


            // Positions des candidats dans les groupes de classement internats

            HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementInternat1 = new HashMap<>();
            
            parametragePositionCandidat1DansGroupeClassementInternat1.put(
                PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
            
            parametragePositionCandidat1DansGroupeClassementInternat1.put(
                PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
                1
            );
            
            PositionCandidatDansGroupeClassementInternat positionCandidat1DansGroupeClassementInternat1 = new PositionCandidatDansGroupeClassementInternat(
                groupeClassementInternat1,            // groupe de classement / internat
                candidat1,                            // candidat
                parametragePositionCandidat1DansGroupeClassementInternat1
            );

            this.scenario.ajouteEntite(positionCandidat1DansGroupeClassementInternat1);


            HashMap<String, Object> parametragePositionCandidat1DansGroupeClassementInternat2 = new HashMap<>();
            
            parametragePositionCandidat1DansGroupeClassementInternat2.put(
                PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
            
            parametragePositionCandidat1DansGroupeClassementInternat2.put(
                PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
                2
            );

            PositionCandidatDansGroupeClassementInternat positionCandidat1DansGroupeClassementInternat2 = new PositionCandidatDansGroupeClassementInternat(
                groupeClassementInternat2,            // groupe de classement / internat
                candidat1,                            // candidat
                parametragePositionCandidat1DansGroupeClassementInternat2
            );

            this.scenario.ajouteEntite(positionCandidat1DansGroupeClassementInternat2);


            HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementInternat1 = new HashMap<>();
            
            parametragePositionCandidat2DansGroupeClassementInternat1.put(
                PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
            
            parametragePositionCandidat2DansGroupeClassementInternat1.put(
                PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
                2
            );
                
            PositionCandidatDansGroupeClassementInternat positionCandidat2DansGroupeClassementInternat1 = new PositionCandidatDansGroupeClassementInternat(
                groupeClassementInternat1,            // groupe de classement / internat
                candidat2,                            // candidat
                parametragePositionCandidat2DansGroupeClassementInternat1
            );

            this.scenario.ajouteEntite(positionCandidat2DansGroupeClassementInternat1);

            
            HashMap<String, Object> parametragePositionCandidat2DansGroupeClassementInternat2 = new HashMap<>();
            
            parametragePositionCandidat2DansGroupeClassementInternat2.put(
                PositionCandidatDansGroupeClassementInternat.CODE_ETAT_AVANCEMENT_DOSSIER,
                5    // 5 = candidat classé
            );  
            
            parametragePositionCandidat2DansGroupeClassementInternat2.put(
                PositionCandidatDansGroupeClassementInternat.RANG_CANDIDAT,
                1
            );
            
            PositionCandidatDansGroupeClassementInternat positionCandidat2DansGroupeClassementInternat2 = new PositionCandidatDansGroupeClassementInternat(
                groupeClassementInternat2,            // groupe de classement / internat
                candidat2,                            // candidat
                parametragePositionCandidat2DansGroupeClassementInternat2
            );

            this.scenario.ajouteEntite(positionCandidat2DansGroupeClassementInternat2);


            // Situations possibles des voeux

            SituationVoeu situationVoeuEnAttente = new SituationVoeu(
                1,                                 // identifiant numérique
                1,                                 // code 1 = voeu en attente 
                0,                                 // code 0 = voeu non affecte 
                0                                  // code 0 = voeu non clôturé 
            );

            this.scenario.ajouteEntite(situationVoeuEnAttente);


            SituationVoeu situationVoeuAffecte = new SituationVoeu(
                2,                                // identifiant numérique
                0,                                // code 0 = voeu en attente 
                1,                                // code 1 = voeu affecte 
                0                                 // code 0 = voeu non clôturé 
            );

            this.scenario.ajouteEntite(situationVoeuAffecte);


            // Voeux formulés par les candidats

            HashMap<String, Object> parametrageVoeuCandidat1Formation1AvecInternat = new HashMap<>();
            
            parametrageVoeuCandidat1Formation1AvecInternat.put(
                Voeu.RANG_VOEU_REPONDEUR,
                1
            );
            
            Voeu voeuCandidat1Formation1AvecInternat = new Voeu(
                candidat1,                        // candidat
                groupeAffectationFormation1,      // groupe d'affectation/ formation
                regimeHebergementAvecInternat,    // régime d'hébergement demandé
                situationVoeuEnAttente,           // situation du voeu
                parametrageVoeuCandidat1Formation1AvecInternat
            );

            this.scenario.ajouteEntite(voeuCandidat1Formation1AvecInternat);


            HashMap<String, Object> parametrageVoeuCandidat1Formation2AvecInternat = new HashMap<>();
            
            parametrageVoeuCandidat1Formation2AvecInternat.put(
                Voeu.RANG_VOEU_REPONDEUR,
                2
            );
            
            Voeu voeuCandidat1Formation2AvecInternat = new Voeu(
                candidat1,                         // candidat
                groupeAffectationFormation2,       // groupe d'affectation/ formation
                regimeHebergementAvecInternat,     /// régime d'hébergement demandé
                situationVoeuAffecte,              // situation du voeu         
                parametrageVoeuCandidat1Formation2AvecInternat
            );

            this.scenario.ajouteEntite(voeuCandidat1Formation2AvecInternat);



            Voeu voeuCandidat2Formation1AvecInternat = new Voeu(
                candidat2,                         // candidat
                groupeAffectationFormation1,       // groupe d'affectation/ formation
                regimeHebergementAvecInternat,     // régime d'hébergement demandé
                situationVoeuEnAttente             // situation du voeu
            );



            this.scenario.ajouteEntite(voeuCandidat2Formation1AvecInternat);

            Voeu voeuCandidat2Formation2AvecInternat = new Voeu(
                candidat2,                         // candidat
                groupeAffectationFormation2,       // groupe d'affectation/ formation
                regimeHebergementAvecInternat,     // régime d'hébergement demandé
                situationVoeuEnAttente             // situation du voeu
            );

            this.scenario.ajouteEntite(voeuCandidat2Formation2AvecInternat);



            Voeu voeuCandidat2Formation2SansInternat = new Voeu(
                candidat2,                        // candidat
                groupeAffectationFormation2,      // groupe d'affectation/ formation
                regimeHebergementSansInternat,    // régime d'hébergement demandé
                situationVoeuAffecte              // situation du voeu

            );

            this.scenario.ajouteEntite(voeuCandidat2Formation2SansInternat);

        }

    }


    public String getFlatXml() {        
        
        return this.scenario.getFlatXml();

    }


}

// Notes sur le scénario développé ici :
//
// - deux groupes d'affectation en formation sont définis
//
// - deux groupes d'affectation en internat sont définis
//
// - cinq voeux sont enregistrés :
//   - quatre voeux avec demande d'internat dont un affecté et trois en attente
//   - un voeu affecté sans demande d'internat
