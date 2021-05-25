package fr.parcoursup.algos.bacasable;

import fr.parcoursup.algos.bacasable.propositions.DemoPropositionsJson;
import org.junit.Test;

public class TestDemoPropositionsJson {

    @Test
    public void test_methode_sexecute() throws Exception {
        String[] args = {"src/test/java/fr/parcoursup/algos/bacasable/algoPropositionsEntree.json", "/tmp/sortie.json"};
        DemoPropositionsJson.main(args);
    }
    
}
