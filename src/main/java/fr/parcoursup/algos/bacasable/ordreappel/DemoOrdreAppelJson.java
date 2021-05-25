package fr.parcoursup.algos.bacasable.ordreappel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppel;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelEntree;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelSortie;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class DemoOrdreAppelJson {
    public static void main(String[] args) throws VerificationException, IOException {

        String entreeFilepath = args[0];
        String sortieFilepath = args[1];

        try(Reader reader = new FileReader(entreeFilepath)) {
            AlgoOrdreAppelEntree entree = new Gson().fromJson(reader, AlgoOrdreAppelEntree.class);
            AlgoOrdreAppelSortie sortie = AlgoOrdreAppel.calculerOrdresAppels(entree);
            try (FileWriter writer = new FileWriter(sortieFilepath)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(sortie, writer);
            }
        }
        
    }
}
