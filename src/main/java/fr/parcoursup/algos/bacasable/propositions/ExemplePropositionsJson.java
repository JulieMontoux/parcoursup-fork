package fr.parcoursup.algos.bacasable.propositions;

import com.google.gson.GsonBuilder;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.parcoursup.algos.propositions.algo.Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION;

public class ExemplePropositionsJson {

    private static final Logger LOGGER = Logger.getLogger(ExemplePropositionsJson.class.getSimpleName());

    public static void main(String[] args) throws VerificationException, IOException {

        if(args.length <= 1 ) {
            LOGGER.log(Level.INFO, "Usage: {0} [output_file]", ExemplePropositionsJson.class.getSimpleName());
            return;
        }

        String entreeFilepath = args[0];

        //crée un exemple et le sérialize en json
        Parametres parametres = new Parametres(1, 60);

        AlgoPropositionsEntree entree
                = new AlgoPropositionsEntree(parametres);
        GroupeAffectationUID guid = new GroupeAffectationUID(11, 12, 13);
        GroupeAffectation groupe = new GroupeAffectation(2, guid, 3, 0, parametres);
        entree.groupesAffectations.put(guid, groupe);

        GroupeInternatUID iuid = new GroupeInternatUID(14, 13);
        GroupeInternat internat = new GroupeInternat(iuid, 2);
        entree.internats.put(iuid, internat);

        Voeu voeu = new Voeu(
                10,
                true,
                guid,
                1,
                1,
                1,
                EN_ATTENTE_DE_PROPOSITION,
                false);

        entree.voeux.add(voeu);

        try (FileWriter writer = new FileWriter(entreeFilepath)) {
            GsonBuilder builder = new GsonBuilder();
            builder.enableComplexMapKeySerialization();
            builder.setPrettyPrinting().create().toJson(entree, writer);
        }

    }

}
