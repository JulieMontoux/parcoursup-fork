package fr.parcoursup.algos.prod;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.javalite.activejdbc.DB;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;


public class PreparationBddOraclePropositions {

    public static void main(String[] args) throws Exception {

        Map<String, String> env = System.getenv();

        String param_files = env.get("PARAMS_FILE");

        if((param_files== null) || (param_files.isEmpty())) {
            throw new NullPointerException("Variable d'environnement PARAMS_FILE non définie");
        }

        Path p = Paths.get(param_files);

        if (!p.isAbsolute()) {
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            param_files = Paths.get(currentPath.toString(), param_files).toString();
        }

        File file = new File(param_files);

        if (!file.exists()) {
            throw new FileNotFoundException("Le fichier de configuration " + param_files + " est introuvable");
        }

        ExecutionParams params = ExecutionParams.fromFile(param_files);

        DB db = new DB();

        db.open(
            "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:thin:@"+params.tnsAlias,
            params.user,
            params.password
        );

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

        ParametreApplication parametreApplication1 = new ParametreApplication(
            31,                 // indexFlagInterruptionDonneesEntrantes
            "1"                 // 0 = flag off
            );

        parametreApplication1.insert();


        ParametreApplication parametreApplication2 = new ParametreApplication(
            35,                // indexDateDebutDeCampagne
            "20/05/2020:0000"  // date et heure du début de la campagne
        );

        parametreApplication2.insert();



        ParametreApplication parametreApplication3 = new ParametreApplication(
            334,                // indexDateOuvertureCompleteInternats
            "01/07/2020:0000"   // date et heure de l'ouverture complète des internats
        );

        parametreApplication3.insert();

        db.close();

        System.out.println("BDD prête.");

        System.exit(0);

    }

}
