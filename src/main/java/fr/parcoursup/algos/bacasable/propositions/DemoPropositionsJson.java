package fr.parcoursup.algos.bacasable.propositions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.AlgoPropositions;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoPropositionsJson {

    private static final Logger LOGGER = Logger.getLogger(DemoPropositionsJson.class.getSimpleName());

    public static void main(String[] args) throws VerificationException, IOException {

        if(args.length <= 2 ) {
            LOGGER.log(Level.INFO, "Usage: {0}} [input_file] [output_file]", DemoPropositionsJson.class.getSimpleName());
            return;
        }

        String entreeFilepath = args[0];
        String sortieFilepath = args[1];

        try(Reader reader = new FileReader(entreeFilepath)) {
            AlgoPropositionsEntree entree = new Gson().fromJson(reader, AlgoPropositionsEntree.class);
            entree.injecterGroupesEtInternatsDansVoeux();

            AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);
            try (FileWriter writer = new FileWriter(sortieFilepath)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(sortie, writer);
            }
        }
        
    }

}
