package fr.parcoursup.algos.prod;

import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

import org.javalite.activejdbc.DB;

import fr.parcoursup.algos.bacasable.peuplementbdd.*;

public class PreparationBddOracleOrdreAppel {

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

        db.close();

        System.out.println("BDD prête.");

        System.exit(0);

    }

}
